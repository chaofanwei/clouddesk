package cn.myroute.clouddesk;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MIME;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

public class HttpUtil {

	public static List<String> executeGet(String url) throws Exception {
		BufferedReader in = null;
		List<String> lines = new LinkedList<String>();
		try {
			// 定义HttpClient
			HttpClient client = new DefaultHttpClient();
			// 实例化HTTP方法
			HttpGet request = new HttpGet();
			request.setURI(new URI(url));
			HttpResponse response = client.execute(request);

			in = new BufferedReader(new InputStreamReader(response.getEntity()
					.getContent()));
			String line = null;

			while ((line = in.readLine()) != null) {
				lines.add(line);
			}
			in.close();
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}finally {
			if (in != null) {
				try {
					in.close();// 最后要关闭BufferedReader
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		lines = parse(lines);
		if(lines.size() <= 0 ) throw new RuntimeException("get list size 0");
		return lines;
	}
	
	static List<String> parse(List<String> pre){
		List<String> result = new LinkedList<String>();
		for(String line:pre){
			String[] ll = line.split("<br/>");
			for(String l:ll){
				if(l.contains("/desktop/")){
					result.add(l.trim().replace("/desktop", ""));
				}
			}
		}
		
		return result;
	}

	public static boolean delRemote( String remotePath) {
		CloseableHttpClient httpClient = null;
		CloseableHttpResponse response = null;
		try {
			httpClient = HttpClients.createDefault();
			if (remotePath == null || remotePath.isEmpty() || remotePath.equals("/")) {
				remotePath = "desktop";
			}else{
				remotePath = "desktop" + remotePath;
			}
//			System.out.println("remote:"+remotePath);
//			System.out.println("remoteurl :"+ConfigUtil.delUrl + URLEncoder.encode(remotePath));
			
			HttpPost httpPost = new HttpPost(ConfigUtil.delUrl + URLEncoder.encode(remotePath));
			response = httpClient.execute(httpPost);
			HttpEntity resEntity = response.getEntity();
			if(null != resEntity){
				String res = IOUtils.toString(resEntity.getContent());
				//System.out.println(res);
				if(res.startsWith("Error:")){
					System.out.println(res);
					return false;
				}
				
			}
			// 销毁
			EntityUtils.consume(resEntity);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (response != null) {
					response.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			try {
				if (httpClient != null) {
					httpClient.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	public static boolean upload(String url, File localFile, String remotePath) {
		CloseableHttpClient httpClient = null;
		CloseableHttpResponse response = null;
		try {
			httpClient = HttpClients.createDefault();

			HttpPost httpPost = new HttpPost(url);

			ContentType contentType= ContentType.create(HTTP.OCTET_STREAM_TYPE, HTTP.UTF_8);
			
			// 把文件转换成流对象FileBody
			FileBody bin = new FileBody(localFile,contentType,URLEncoder.encode(localFile.getName()));
			
			
			if (remotePath == null || remotePath.isEmpty() || remotePath.equals("/")) {
				remotePath = "desktop";
			}else{
				remotePath = "desktop" + remotePath;
			}
		//	System.out.println(remotePath);
			StringBody p = new StringBody(remotePath, ContentType.create(
					"text/plain", Consts.UTF_8));

			
			
			HttpEntity reqEntity = MultipartEntityBuilder.create()
					.setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
				//	.setCharset(Charset.forName("utf-8"))
					.addPart("file", bin)
					.addPart("p", p)
					.build();

			httpPost.setEntity(reqEntity);

			response = httpClient.execute(httpPost);

			// 获取响应对象
			HttpEntity resEntity = response.getEntity();

			if(null != resEntity){
				String res = IOUtils.toString(resEntity.getContent());
				if(res.startsWith("Error:")){
					System.out.println(res);
					return false;
				}
				
			}
			// 销毁
			EntityUtils.consume(resEntity);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (response != null) {
					response.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			try {
				if (httpClient != null) {
					httpClient.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public static boolean download(String url,String localPath){
		InputStream in = null;
		FileOutputStream out = null;
		boolean suc = false;
		try {
			//System.out.println("begin download :" + url);
			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet();
			request.setURI(new URI(url));
			HttpResponse response = client.execute(request);

			in = response.getEntity().getContent();
			String line = null;
			byte[] buffer = new byte[1024];
			int len = -1;
			File f = new File(localPath);
			if(!f.getParentFile().exists()){
				f.getParentFile().mkdirs();
			}
			out = new FileOutputStream(f);
			while (-1 != (len = in.read(buffer))) {
				out.write(buffer, 0, len);
			}
			suc = true;
		}catch(Exception e){
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return suc;
	}
}
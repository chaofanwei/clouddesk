package cn.myroute.clouddesk;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class HttpUtil {

	public static List<String> executeGet(String url) {
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
		return lines;
	}
	
	static List<String> parse(List<String> pre){
		List<String> result = new LinkedList<String>();
		for(String line:pre){
			String[] ll = line.split("<br/>");
			for(String l:ll){
				if(l.contains("/desktop")){
					result.add(l.trim().replace("/desktop", ""));
				}
			}
		}
		
		return result;
	}

	public static boolean upload(String url, File localFile, String remotePath) {
		CloseableHttpClient httpClient = null;
		CloseableHttpResponse response = null;
		try {
			httpClient = HttpClients.createDefault();

			HttpPost httpPost = new HttpPost(url);

			// 把文件转换成流对象FileBody
			FileBody bin = new FileBody(localFile);
			if (remotePath == null || remotePath.isEmpty()) {
				remotePath = "desktop";
			}else{
				remotePath = "desktop" + remotePath;
			}
			StringBody p = new StringBody(remotePath, ContentType.create(
					"text/plain", Consts.UTF_8));

			HttpEntity reqEntity = MultipartEntityBuilder.create()
					.addPart("file", bin).addPart("p", p).build();

			httpPost.setEntity(reqEntity);

			response = httpClient.execute(httpPost);

			// 获取响应对象
			HttpEntity resEntity = response.getEntity();

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
			System.out.println("begin download :" + url);
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
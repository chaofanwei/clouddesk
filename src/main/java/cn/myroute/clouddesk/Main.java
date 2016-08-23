package cn.myroute.clouddesk;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {

	public static void main(String[] args) {
		try {
			args = new String[]{"down"};
			if(args.length > 0 ){
				if("up".equals(args[0])){
					ConfigUtil.init();
					upload();
				}else if ("down".equals(args[0])) {
					ConfigUtil.init();
					download();
				}else{
					System.out.println("up or down");
				}
			}else {
				System.out.println("up or down");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	static void  download() throws Exception{
		System.out.println("begin download");
		List<String> lines = HttpUtil.executeGet(ConfigUtil.listUrl);
		for(String line:lines){
			String[] temp = line.split(" ");
			if(temp.length == 2){
				String remoteFile = temp[0];
				String remoteFileMd5= temp[1];
				String localPath = getLoalPath(remoteFile);
				//System.out.println(remoteFile + " ->" + localPath);
				File f = new File(localPath);
				if(f.exists()){
					String localMd5 = MD5Util.getFileMD5String(f);
					if(localMd5.equalsIgnoreCase(remoteFileMd5)) continue;
				}
				boolean res = HttpUtil.download(ConfigUtil.downUrl + remoteFile, localPath);
				if(!res){
					System.err.println("download failed ! " + remoteFile + " ->" + localPath);
					System.exit(-1);
				}
			}
		}
		System.out.println("end download");
	}
	static String getLoalPath(String remoteFile){
		String preKey  = "";
		String preValue = "";
		for(Map.Entry<String, String> entry:ConfigUtil.map.entrySet()){
			if(remoteFile.startsWith(entry.getKey()) && entry.getKey().length() > preKey.length()){
				preKey = entry.getKey();
				preValue = entry.getValue();
			}
		}
		remoteFile = remoteFile.replaceFirst(preKey, preValue);
		
		return remoteFile;
	}
	static Map<String, String> listMap = new HashMap<String, String>();
	static void upload() throws Exception{
		System.out.println("begin upload");
		List<String> lines = HttpUtil.executeGet(ConfigUtil.listUrl);
		for(String line:lines){
			String[] temp = line.split(" ");
			if(temp.length == 2){
				String remoteFile = temp[0];
				String remoteFileMd5= temp[1];
				listMap.put(remoteFile, remoteFileMd5);
			}
			
		}
		for(Map.Entry<String, String> entry:ConfigUtil.map.entrySet()){
			String remote = entry.getKey();
			String local = entry.getValue();
			File localFile = new File(local);
			uploadFile(localFile, remote, local);
		}
		System.out.println("begin upload");
	}
	
	static void uploadFile(File localFile,String key,String value) throws Exception{
		if(localFile.isDirectory()){
			for(File f:localFile.listFiles()){
				uploadFile(f,key,value);
			}
		}else{
			String remotePath = localFile.getAbsolutePath().replace("\\", "/").replace(value, key);
			String fullPath = remotePath;
			int index = remotePath.lastIndexOf("/");
			if(index == 0){
				remotePath = "/";
			}else{
				remotePath = remotePath.substring(0,index);
			}			
			checkUp(localFile,remotePath,fullPath);
		}
	}
	
	static void checkUp(File localFile,String remotePath,String fullPath) throws Exception{
		String localMd5 = MD5Util.getFileMD5String(localFile);
		if(!localMd5.equals(listMap.get(fullPath))){
			System.out.println("upload  " + localFile.getAbsolutePath() + "->" + remotePath + "  -> " + fullPath);
			boolean res = HttpUtil.upload(ConfigUtil.uploadUrl, localFile, remotePath);
			if(!res){
				System.err.println("uploadFile failed ! ");
				System.exit(-1);
			}
		}
	}
}



	

	


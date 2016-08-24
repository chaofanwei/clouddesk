package cn.myroute.clouddesk;

import java.io.File;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {

	public static void main(String[] args) {
		try {
			boolean check = false;
			//args = new String[]{"down"};
			if(args.length > 0 ){
				if("up".equals(args[0])){
					ConfigUtil.init();					
					if(args.length > 1 && "check".equals(args[1]))check = true;
					upload(check);
				}else if ("down".equals(args[0])) {
					ConfigUtil.init();
					if(args.length > 1 && "check".equals(args[1]))check = true;
					download(check);
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
	
	static void  download(boolean onlyCheck) throws Exception{
		System.out.println("begin download");
		List<String> lines = HttpUtil.executeGet(ConfigUtil.listUrl);
		for(String line:lines){
			String[] temp = line.split("\\|");
			if(temp.length == 3){
				String remoteFile = temp[0];
				String remoteFileMd5= temp[1];
				String localPath = getLoalPath(remoteFile);
				File f = new File(localPath);
				if(f.exists()){
					String localMd5 = MD5Util.getFileMD5String(f);
					if(localMd5.equalsIgnoreCase(remoteFileMd5)) continue;
				}
				System.out.println("need download : " +  remoteFile + " ->" + localPath);
				if(!onlyCheck){
					System.out.println("begin down");
					boolean res = HttpUtil.download(ConfigUtil.downUrl + URLEncoder.encode(remoteFile), localPath);
					System.out.println("end down");
					if(!res){
						System.err.println("download failed ! " + remoteFile + " ->" + localPath);
						System.exit(-1);
					}
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
	static void upload(boolean onlyCheck) throws Exception{
		System.out.println("begin upload");
		List<String> lines = HttpUtil.executeGet(ConfigUtil.listUrl);
		for(String line:lines){
			String[] temp = line.split("\\|");
			if(temp.length == 3){
				String remoteFile = temp[0];
				String remoteFileMd5= temp[1];
				listMap.put(remoteFile, remoteFileMd5);
			}
			
		}
		for(Map.Entry<String, String> entry:ConfigUtil.map.entrySet()){
			String remote = entry.getKey();
			String local = entry.getValue();
			File localFile = new File(local);
			uploadFile(localFile, remote, local,onlyCheck);
		}
		System.out.println("end upload");
	}
	
	static void uploadFile(File localFile,String key,String value,boolean onlyCheck) throws Exception{
		if(localFile.isDirectory()){
			for(File f:localFile.listFiles()){
				uploadFile(f,key,value,onlyCheck);
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
			checkUp(localFile,remotePath,fullPath,onlyCheck);
		}
	}
	
	static void checkUp(File localFile,String remotePath,String fullPath,boolean onlyCheck) throws Exception{
		String localMd5 = MD5Util.getFileMD5String(localFile);
		if(!localMd5.equals(listMap.get(fullPath))){
			System.out.println("need upload  " + localFile.getAbsolutePath() + "->" + remotePath + "  -> " + fullPath);
			if(!onlyCheck){
				System.out.println("begin upload");
				boolean res = HttpUtil.upload(ConfigUtil.uploadUrl, localFile, remotePath);
				System.out.println("end upload");
				if(!res){
					System.err.println("uploadFile failed ! ");
					System.exit(-1);
				}
			}
		}
	}
}



	

	


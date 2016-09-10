package cn.myroute.clouddesk;

import java.io.File;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {

	public static void main(String[] args) {
		try {
			boolean check = false;
			//args = new String[]{"cleanRemote"};
			if(args.length > 0 ){
				if("up".equals(args[0])){
					ConfigUtil.init();					
					if(args.length > 1 && "check".equals(args[1]))check = true;
					upload(check);
				}else if ("down".equals(args[0])) {
					ConfigUtil.init();
					if(args.length > 1 && "check".equals(args[1]))check = true;
					download(check);
				}else if ("cleanLocal".equals(args[0])) {//以远程为准，删除本地多余的文件和文件夹
					ConfigUtil.init();
					cleanLocal();
				}else if ("cleanRemote".equals(args[0])) {//以本地为准，删除远程多余的文件好文件夹
					ConfigUtil.init();
					cleanRemote();
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
	
	static void cleanRemote() throws Exception{
		System.out.println("begin cleanRemote");
		
		List<String> localFiles = new LinkedList<String>();
		
		for(Map.Entry<String, String> entry:ConfigUtil.map.entrySet()){
			String remote = entry.getKey();
			String local = entry.getValue();
			File localFile = new File(local);
			for(File f:localFile.listFiles()){
				collectLocalFile(f,remote,local,localFiles);
			}
		}
		
		List<String> lines = HttpUtil.executeGet(ConfigUtil.listUrl);
		List<String> deleteFile= new LinkedList<String>();
		for(String line:lines){
			String[] temp = line.split("\\|");
			String remoteFile = temp[0];
			//remoteFiles.add(remoteFile);
			if(!localFiles.contains(remoteFile)){
				deleteFile.add(remoteFile);
			}
		}
		
		if(deleteFile.size() > 0){
			for(String del :deleteFile){
				System.out.println("need delete remote file:"+del);
			}
			System.out.println("are you sure to delete these remote files:[y/n]?");
			Scanner sc = new Scanner(System.in); 
			if("y".equals(sc.next())){
				List<String> deletedFile = new LinkedList<String>();
				out:for(String del :deleteFile){
						for(String temp:deletedFile){
							if(del.startsWith(temp+"/")){
								continue out;
							}
						}
						System.out.println("delete remote file:"+ del +" " + (HttpUtil.delRemote(del)?"success!":"fail !!!!!"));
						deletedFile.add(del);
				}
			}
			sc.close();
		}
		
		System.out.println("end cleanRemote");
	}
	
	static void collectLocalFile(File localFile,final String key,final String value,List<String> localFiles) throws Exception{
		String remotePath = localFile.getAbsolutePath().replace("\\", "/").replace(value, key);
		localFiles.add(remotePath);
		
		if(localFile.isDirectory()){
			for(File f:localFile.listFiles()){
				collectLocalFile(f,key,value,localFiles);
			}
		}
	}
	
	static void cleanLocal() throws Exception{
		System.out.println("begin cleanLocal");
		
		List<String> lines = HttpUtil.executeGet(ConfigUtil.listUrl);
		List<String> remoteFiles= new LinkedList<String>();
		for(String line:lines){
			String[] temp = line.split("\\|");
			String remoteFile = temp[0];
			remoteFiles.add(remoteFile);
			
		}
		List<File> deleteFile= new LinkedList<File>();
		for(Map.Entry<String, String> entry:ConfigUtil.map.entrySet()){
			String remote = entry.getKey();
			String local = entry.getValue();
			File localFile = new File(local);
			for(File f:localFile.listFiles()){
				cleanLocalFile(f,remote,local,remoteFiles,deleteFile);
			}
		}
		
		if(deleteFile.size() > 0){
			for(File del :deleteFile){
				System.out.println("need delete local file:"+del.getAbsolutePath());
			}
			System.out.println("are you sure to delete these files:[y/n]?");
			Scanner sc = new Scanner(System.in); 
			if("y".equals(sc.next())){
				for(File del :deleteFile){
					System.out.println("delete local file:"+del.getAbsolutePath() + " " +(del.delete()? "success!":"fail !!!!!!!!!!!!!"));
				}
			}
			sc.close();
		}
		
		System.out.println("end cleanLocal");
	}
	
	static void cleanLocalFile(File localFile,final String key,final String value,final List<String> remoteFiles,List<File> deleteFile) throws Exception{
		String remotePath = localFile.getAbsolutePath().replace("\\", "/").replace(value, key);
		if(localFile.isDirectory()){
			for(File f:localFile.listFiles()){
				cleanLocalFile(f,key,value,remoteFiles,deleteFile);
			}
		}
		
		if(!remoteFiles.contains(remotePath)){
			//System.out.println("need delete local file:"+localFile.getAbsolutePath());
			deleteFile.add(localFile);
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



	

	


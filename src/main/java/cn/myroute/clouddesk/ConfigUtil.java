package cn.myroute.clouddesk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ConfigUtil {

	static Properties properties = new Properties();
	static String uploadUrl;
	static String listUrl;
	static String downUrl;
	static String delUrl;
	static String[] urls;
	static Map<String, String> map = new HashMap<String, String>();
	
	public static void init(){
		try {
			properties.load(ConfigUtil.class.getClassLoader().getResourceAsStream("global.properties"));
			listUrl = properties.getProperty("listUrl");
			uploadUrl = properties.getProperty("uploadUrl");
			downUrl = properties.getProperty("downUrl");
			delUrl = properties.getProperty("delUrl");
			initMap();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	public static void initMap(){
		map.clear();
		BufferedReader br = null;
		try {
			br = new BufferedReader( new InputStreamReader(ConfigUtil.class.getClassLoader().getResourceAsStream("url.conf")));
			String line = null;
			while(null != (line=br.readLine())){
				line=line.trim();
				String[] vv = line.split(" ");
				if(vv.length == 2){
					map.put(vv[0].trim(), vv[1].trim());
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
}

package com.easygo.config;

import java.net.InetAddress;
import java.net.UnknownHostException;



public class ConstApp {
	
	
	public static String getFilePath() {
		String os=System.getProperty("os.name").toLowerCase();
		if(os.contains("windows")){
//			return System.getProperty("java.io.tmpdir");
			return "F:/easygo_media/";
			
		}
		else if(os.contains("linux")){
			
			System.out.println("This is linux");
			return "/opt/ubuntu/easygo_media/";
		}
		else 
			return "not found";
		
	}

	public static int getFilelength() {
		String os=System.getProperty("os.name").toLowerCase();
		if(os.contains("windows")){
//			return System.getProperty("java.io.tmpdir");
			return 16;
			
		}
		else if(os.contains("linux")){
			
			System.out.println("This is linux");
			return 25;
		}
		else 
			return 0;
		
	}
	
	public static String getFilePathDB() {
		String os=System.getProperty("os.name").toLowerCase();
//		System.getenv();
		if(os.contains("windows")){
			
			System.out.println("This is windows");
			String hostAddress="localhost";
			try {
				hostAddress=InetAddress.getLocalHost().getHostAddress();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String url="http://"+hostAddress+":8181";
//			System.out.println(url);
			return url;
		}
		else if(os.contains("linux")){
			
			System.out.println("This is linux");
			return "https://api.iamlisted.in";
//			return "http://ftechiz.com/ongojiimage/";
		}
		else 
			return "not found";
		
	}

}

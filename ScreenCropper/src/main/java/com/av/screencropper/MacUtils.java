package com.av.screencropper;

public class MacUtils {

	
	public static String getDesktopPath(){
		
		String username =   System.getProperty("user.name");
		return "/users/"+username+"/Desktop/";	
	}	
}

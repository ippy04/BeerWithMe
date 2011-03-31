package com.BeerMessenger;

public class PictureInfo {

	public String picturePath;
	public String message;
	public String url;
	public String contactName;
	public String contactNumber;
	public String sendersName;
	
	public PictureInfo(String _picturePath, 
			String _message, 
			String _url, 
			String _contactName, 
			String _contactNumber, 
			String _sendersName){
		picturePath = _picturePath;
		message = _message;
		url = _url;
		contactName = _contactName;
		contactNumber = _contactNumber;
		sendersName = _sendersName;		
	}
}

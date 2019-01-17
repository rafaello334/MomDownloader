package com.momdownloader.tools;

public class URLParser {

	public static String urlParse(String url, String orderID)
	{
		return url.replace("ORDER_ID_VALUE", orderID);	
	}
}

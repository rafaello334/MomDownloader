package com.momdownloader.tools;

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.chrome.ChromeDriver;

public class ChromeDriverManager {
		
	public ChromeDriverManager() {
		System.setProperty("webdriver.chrome.driver", "C://chromedriver.exe");
		
	}
	
	public ChromeDriver createChromeDriver()
	{
		ChromeDriver chromeDriver = new ChromeDriver();
		chromeDriver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
		return chromeDriver;
	}
}

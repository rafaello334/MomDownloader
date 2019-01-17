package com.momdownloader.configuration;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.momdownloader.model.OrderFile;
import com.momdownloader.tools.ChromeDriverManager;
import com.momdownloader.tools.FileManager;

@Configuration
@ComponentScan("com.momdownloader")
public class SpringConfig {

	@Bean
	public ChromeDriverManager chromeDriverManager() {
		return new ChromeDriverManager();
	}

	@Bean
	public FileManager fileManager() {
		return new FileManager();
	}

	@Bean
	public Map<OrderFile, String> downloadedFilesMap() {
		return new HashMap<>();
	}
}

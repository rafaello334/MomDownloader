package com.momdownloader.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.momdownloader.model.OrderFile;
import com.momdownloader.tools.FileManager;
import com.momdownloader.tools.WebNavigator;

@Controller
public class IndexController {
	private static final String APPLICATION_ZIP = "application/zip";
	private List<String> urlsList;
	
	@Autowired
	private Map<OrderFile,String> downloadedFilesMap;
	
	@Autowired
	private WebNavigator webNavigator;
	
	@RequestMapping("/")
	public String index() {
		return "index";
	}
	
	@RequestMapping("/clearFileMap")
	public ModelAndView clearFileList() {
		ModelAndView model = new ModelAndView("index");
		downloadedFilesMap.clear();
		model.addObject("clear", true);
		return model;
	}
	@RequestMapping("/download")
	public ModelAndView download(@RequestParam("orderID") String orderID) {
		ModelAndView model = new ModelAndView("result");
		int numberOfFiles = downloadedFilesMap.size();
		try {
			
			webNavigator.prepareChromeDriver();
			webNavigator.loginToMOM();
			webNavigator.openOrder(orderID);
			urlsList = webNavigator.getListOfUrlsToDownload();
			if(urlsList != null)
				webNavigator.downloadXMLFromUrls(urlsList, orderID);
			if(downloadedFilesMap.size() == numberOfFiles)
			{
				model.addObject("emptyOrder", true);
				return model;
			}
		} catch (Exception e) {
			model.addObject("error", true);
			model.addObject("message", e.getMessage());
		} 
		finally
		{
			model.addObject("downloadedFilesMap", downloadedFilesMap);
			model.addObject("orderID", orderID);
			webNavigator.closeWebDriver();		
		}
		return model;
	}
	
	@RequestMapping("/fileDetails")
	public ModelAndView fileDetails(@RequestParam("fileName") String fileName) {
		ModelAndView model = new ModelAndView("fileDetails");
		model.addObject("fileName", fileName);
		for(OrderFile orderFile : downloadedFilesMap.keySet())
		{
			if(orderFile.getFileName().equals(fileName))
				model.addObject("content", downloadedFilesMap.get(orderFile));
		}
		
		return model;
	}
	
	@RequestMapping("/showResults")
	public ModelAndView showResults() {
		ModelAndView model = new ModelAndView("result");
		model.addObject("downloadedFilesMap", downloadedFilesMap);
		
		return model;
	}
	
    @RequestMapping("/saveFiles")
    public @ResponseBody void downloadA(HttpServletRequest request, HttpServletResponse response) throws IOException {
    	String path = request.getSession().getServletContext().getRealPath("/");
    	File file = FileManager.createZipFile(path, downloadedFilesMap);
        InputStream in = new FileInputStream(file);

        response.setContentType(APPLICATION_ZIP);
        response.setHeader("Content-Disposition", "attachment; filename=" + file.getName());
        response.setHeader("Content-Length", String.valueOf(file.length()));
        FileCopyUtils.copy(in, response.getOutputStream());
    }
}

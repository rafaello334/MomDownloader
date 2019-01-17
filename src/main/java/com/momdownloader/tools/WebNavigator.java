package com.momdownloader.tools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.momdownloader.model.OrderFile;

@Component
public class WebNavigator {

	private static final String URL_LOGIN = "http://wp28.test.pl:7700/mom/console/index.jsp?action=login&authorizedPage=%2Fmom%2FMomConsole%2Findex.jsp&login=test&password=test";
	private static final String ORDER_URL_TEMPLATE = "http://wp28.test.pl:7700/mom/console/index.jsp?destinationCodeFilter=ioe&destinationCode=WS.SCC.ESB.INT.IOE.GIS&attributeValueOne=ORDER_ID&attributeSearchOption=AUTO&filter=Filter&msgParentChild=ParentAndChild&itemsOnPage=100";
	private static final String LINK_SEARCH_TEMPLATE = "//*[@id=\"auditTable(REPLACE)1\"]/tbody/tr/td[11]/a";
	private static final String BLUE_PLUS_SEARCH_TEMPLATE = "//*[@id=\"msgsContainer(REPLACE)\"]/td[2]/table/thead/tr/th[1]/div";
	private static final String XPATH_SELECTION_LIST = "//*[@id=\"selectionList\"]";
	private static final String XPATH_PLUS = "//*[@id=\"selectionList\"]/table/thead/tr/th[1]/div";
	private static final String XPATH_TO_DIV_GENE = "//*[@id=\"smxconsole-932904391\"]/div/div[2]/div/div[3]/div/div/div[3]/div/div/div/div[2]/div/div/div[1]/div/div/div[2]/div/div/div[3]/div//div[2]/div[1]/table/tbody/tr[5]";
	private static final String XPATH_TO_XML = "//*[@id=\"smxconsole-932904391\"]/div/div[2]/div/div[3]/div/div/div[3]/div/div/div/div[2]/div/div/div[3]/div/div/div/div[2]/div/div[2]/code/code";
	private static final String XPATH_TO_ERROR_LOG = "//*[@id=\"smxconsole-932904391-overlays\"]/div[3]/div";
	private static final String XPATH_TO_CLOSE_BUTTON = "v-window-closebox";
	
	//private HtmlUnitDriver htmlDriver;
	private ChromeDriver chromeDriver;
	private WebDriverWait wait;
	
	@Autowired
	private Map<OrderFile, String> downloadedFilesMap;

	@Autowired
	private ChromeDriverManager chromeDriverManager;

	/*
	 * @Autowired public WebNavigator(HtmlUnitDriver htmlDriver) throws
	 * InterruptedException { this.htmlDriver = htmlDriver;
	 * htmlDriver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS); }	
	public HtmlUnitDriver getHtmlDriver() {
		return htmlDriver;
	}
	 */
	
	public void prepareChromeDriver() {
		chromeDriver = chromeDriverManager.createChromeDriver();
		wait = new WebDriverWait(chromeDriver, 10);
	}

	public void loginToMOM() {

		chromeDriver.get(URL_LOGIN);
	}

	public void openOrder(String orderID) {

		chromeDriver.get(ORDER_URL_TEMPLATE.replace("ORDER_ID", orderID));
	}

	public List<String> getListOfUrlsToDownload() throws InterruptedException {
		WebElement selectionList = wait
				.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(XPATH_SELECTION_LIST)));
		String temp;
		WebElement tableProducts = selectionList.findElement(By.tagName("table"));
		if (tableProducts.getText().contains("Cannot find any message."))
			return null;
		List<WebElement> trList = wait
				.until(ExpectedConditions.visibilityOfNestedElementsLocatedBy(tableProducts, By.tagName("tr")));
		List<String> searchList = new ArrayList<>();
		List<String> bluePlusXPathList = new ArrayList<>();
		List<String> urlsList = new ArrayList<>();

		for (WebElement el : trList) {
			temp = el.getAttribute("id");
			if (temp.contains("auditContainer")) {
				searchList.add(LINK_SEARCH_TEMPLATE.replace("(REPLACE)",
						temp.substring(temp.indexOf("r") + 2, temp.length() - 1)));
			} else if (temp.contains("msgsContainer")) {
				bluePlusXPathList
						.add(BLUE_PLUS_SEARCH_TEMPLATE.replace("(REPLACE)", temp.substring(temp.indexOf("r") + 1)));
			}
		}

		WebElement plus = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(XPATH_PLUS)));
		plus.click();

		for (String xPath : bluePlusXPathList) {
			plus = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xPath)));
			plus.click();
			Thread.sleep(200);
		}

		for (String s : searchList) {
			WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(s)));

			if (element.getAttribute("href").contains("SCC_INT_GIS_IOE"))
				urlsList.add(element.getAttribute("href"));
		}

		return urlsList;
	}

	public void downloadXMLFromUrls(List<String> urlsList, String orderID) throws InterruptedException, IOException {
		WebElement webElement;
		String fileName;
		for (String url : urlsList) {
			chromeDriver.get(url);
			waitForPageLoaded();
			if (chromeDriver.findElements(By.xpath(XPATH_TO_DIV_GENE)).size() != 0) {
				webElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(XPATH_TO_DIV_GENE)));
				webElement.click();
				Thread.sleep(200);
				if(chromeDriver.findElements(By.xpath(XPATH_TO_ERROR_LOG)).size() != 0)
				{
					webElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className(XPATH_TO_CLOSE_BUTTON)));
					webElement.click();
					continue;
				}
				if(chromeDriver.findElements(By.xpath(XPATH_TO_XML)).size() != 0)
				{
					webElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(XPATH_TO_XML)));
					fileName = url.substring(url.indexOf("=") + 1, url.indexOf(":", url.indexOf("=") + 1)) + ".xml";
					OrderFile orderFile = new OrderFile(fileName, orderID);
					downloadedFilesMap.put(orderFile, webElement.getText());
				}
			}
		}
	}

	public void closeWebDriver() {
		chromeDriver.close();
	}

	public void waitForPageLoaded() throws InterruptedException {
		ExpectedCondition<Boolean> expectation = new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver driver) {
				return ((JavascriptExecutor) driver).executeScript("return document.readyState").toString()
						.equals("complete");
			}
		};

		Thread.sleep(500);
		wait.until(expectation);

	}

}

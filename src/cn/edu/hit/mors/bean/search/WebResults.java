package cn.edu.hit.mors.bean.search;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class WebResults
{
	private File xmlFile;
	
	public WebResults(String path)
	{
		this(new File(path));
	}
	
	public WebResults(File file)
	{
		this.xmlFile = file;
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<WebResult> getUrls()
	{
		List<Element> webResults = null;
		Map<String, String> ns = new HashMap<String, String>();
		ns.put("web", "http://schemas.microsoft.com/LiveSearch/2008/04/XML/web");
		ArrayList<WebResult> results = new ArrayList<WebResult>();
		
		try
		{
			SAXReader reader = new SAXReader();
			reader.getDocumentFactory().setXPathNamespaceURIs(ns);
			//reader.setEncoding("utf-8");
			
			if (!xmlFile.exists())
			{
				System.err.println("Error! " + xmlFile.getPath() + "/" + xmlFile.getName() + " not exisit!");
				return results;
			}
			else if (xmlFile.isFile() && xmlFile.length() == 0)
			{
				System.err.println("Error! " + xmlFile.getPath() + "/" + xmlFile.getName() + " is empty!");
				return results;
			}
			
			Document document = reader.read(xmlFile);
			webResults = document.selectNodes("//web:WebResult");
			
			for (Element e : webResults)
			{
				String title = e.elementText("Title");
				String description = e.elementText("Description");
				String url = e.elementText("Url");
				String cacheUrl = e.elementText("CacheUrl");
				String displayUrl = e.elementText("DisplayUrl");
				String dateTime = e.elementText("DateTime");
				results.add(new WebResult(title, description, url, cacheUrl, displayUrl, dateTime));
			}
		}
		catch (Exception e)
		{
			//e.printStackTrace();
			System.err.println("Error! " + xmlFile.getPath() + "/" + xmlFile.getName() + " is not a valid XML File!");
		}
		
		return results;
	}
	
	/**
	 * just for test
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	{
		String path = "web_search_result/sample.xml";
		WebResults webResults = new WebResults(path);
		ArrayList<WebResult> results = webResults.getUrls();
		for (WebResult result : results)
		{
			System.out.println(result.getUrl());
		}
	}
}

package cn.edu.hit.mors.bean.thread;

import java.io.File;

import cn.edu.hit.mors.bean.search.WebSearcher;

public class SearchThread implements Runnable
{
	
	private String keyword;
	private File xmlFile;
	private WebSearcher webSearcher;
	
	public SearchThread(String keyword, File xmlFile, WebSearcher webSearcher)
	{
		this.keyword = keyword;
		this.xmlFile = xmlFile;
		this.webSearcher = webSearcher;
	}
	
	public void run()
	{
		webSearcher.search(xmlFile, keyword, 10, 0);
	}
}

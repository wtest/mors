package cn.edu.hit.mors.bean.search;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class WebSearcher
{
	
	private final static int defaultCount = 20;
	private final static int defaultOffset = 0;
	
	private String AppID;
	
	public WebSearcher(String AppID)
	{
		this.AppID = AppID;
	}
	
	public String search(String keyword, int count, int offset)
	{
		String request = null;
		String result = "";
		
		try
		{
			request = "http://api.search.live.net/xml.aspx?Appid=" + this.AppID;
			request += "&sources=web&web.count=" + count + "&web.offset=" + offset;
			request += "&query=" + URLEncoder.encode(keyword, "utf-8");
			
			URL query = new URL(request);
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(query.openStream(), "utf-8"));
			
			String line = null;
			
			while ((line = reader.readLine()) != null)
			{
				result += line;
			}
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
		catch (MalformedURLException e)
		{
			System.err.println("A Exception happend, Maybe you use a wrong AppID");
		}
		catch (IOException e)
		{
			System.err.println("Can't execute query, Check you Network connect");
		}
		return result;
	}
	
	public String search(String keyword, int count)
	{
		return this.search(keyword, count, defaultOffset);
	}
	
	public String search(String keyword)
	{
		return this.search(keyword, defaultCount, defaultOffset);
	}
	
	public void search(File destFile, String keyword, int count, int offset)
	{
		try
		{
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(destFile), "utf-8"));
			writer.write(this.search(keyword, count, offset));
			writer.close();
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void search(File destFile, String keyword, int count)
	{
		this.search(destFile, keyword, count, defaultOffset);
	}
	
	public void search(File destFile, String keyword)
	{
		this.search(destFile, keyword, defaultCount, defaultOffset);
	}
	
	//Only for Debug
	
	public static void main(String[] args)
	{
		String myAppID = "F3780F7DB4132283CCB215A729257D703B1BB700";
		WebSearcher ws = new WebSearcher(myAppID);
		File file = new File("web_search_result/sample.xml");
		String keyword = "";
		ws.search(file, keyword, 10, 0);
	}
}

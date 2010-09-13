package cn.edu.hit.mors.bean.search;

public class WebResult
{
	private String title;
	private String description;
	private String url;
	private String cacheUrl;
	private String displayUrl;
	private String dateTime;
	
	public WebResult(String title, String description, String url, String cacheUrl, String displayUrl, String dateTime)
	{
		this.title = title;
		this.description = description;
		this.url = url;
		this.cacheUrl = cacheUrl;
		this.displayUrl = displayUrl;
		this.dateTime = dateTime;
	}
	
	public WebResult()
	{
		// TODO Auto-generated constructor stub
	}
	
	public String getTitle()
	{
		return title;
	}
	
	public void setTitle(String title)
	{
		this.title = title;
	}
	
	public String getDescription()
	{
		return description;
	}
	
	public void setDescription(String description)
	{
		this.description = description;
	}
	
	public String getUrl()
	{
		return url;
	}
	
	public void setUrl(String url)
	{
		this.url = url;
	}
	
	public String getCacheUrl()
	{
		return cacheUrl;
	}
	
	public void setCacheUrl(String cacheUrl)
	{
		this.cacheUrl = cacheUrl;
	}
	
	public String getDisplayUrl()
	{
		return displayUrl;
	}
	
	public void setDisplayUrl(String displayUrl)
	{
		this.displayUrl = displayUrl;
	}
	
	public String getDateTime()
	{
		return dateTime;
	}
	
	public void setDateTime(String dateTime)
	{
		this.dateTime = dateTime;
	}
	
}

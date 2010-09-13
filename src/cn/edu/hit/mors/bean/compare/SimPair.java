package cn.edu.hit.mors.bean.compare;

import java.util.LinkedHashSet;

public class SimPair
{
	private volatile int hashCode;
	private float degree;
	private String first;
	private String second;
	private String htmlSource; // for web detection
	private int sid = 0;
	
	public SimPair(Float degree, String pair)
	{
		this.degree = degree;
		String[] docs = pair.split("<-->");
		
		if (docs.length != 2)
		{
			System.err.println("Format error!");
			System.exit(1);
		}
		
		first = (docs[0].compareTo(docs[1]) >= 0) ? docs[0] : docs[1];
		second = (docs[0].compareTo(docs[1]) >= 0) ? docs[1] : docs[0];
	}
	
	public void setSid(int sid)
	{
		this.sid = sid;
	}
	
	public int getSid()
	{
		return this.sid;
	}
	
	public float degree()
	{
		return this.degree;
	}
	
	public String first()
	{
		return this.first;
	}
	
	public void setFirst(String first)
	{
		this.first = first;
	}
	
	public String second()
	{
		return this.second;
	}
	
	public void setSecond(String second)
	{
		this.second = second;
	}
	
	public String getHtmlSource()
	{
		return this.htmlSource;
	}
	
	public void setHtmlSource(String html)
	{
		this.htmlSource = html;
	}
	
	@Override
	public boolean equals(Object pair)
	{
		if (!(pair instanceof SimPair)) return false;
		
		return (((SimPair) pair).first.equals(first)) && (((SimPair) pair).second.equals(second));
	}
	
	@Override
	public int hashCode()
	{
		int result = hashCode;
		if (result == 0)
		{
			result = 17;
			result = 31 * result + first.hashCode();
			result = 31 * result + second.hashCode();
			hashCode = result;
		}
		return result;
	}
	
	public static void main(String[] args)
	{
		SimPair fir = new SimPair(3.1f, "abc<-->dev");
		SimPair sec = new SimPair(3.2f, "dev<-->abc");
		
		if (fir.equals(sec))
		{
			System.out.println("hello");
		}
		
		LinkedHashSet<SimPair> set = new LinkedHashSet<SimPair>();
		set.add(fir);
		set.add(sec);
		
		System.out.println(set.size());
	}
}

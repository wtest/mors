package cn.edu.hit.mors.bean.task;

import java.util.Date;

public class Task
{
	
	private int wid;
	private String wname;
	private Date wtime;
	private String state;
	private int filenum;
	private int uid;
	private double threshold;
	
	public Task(int wid, String wname, Date wtime, String state, int filenum, int uid, double threshold)
	{
		this.wid = wid;
		this.wname = wname;
		this.wtime = wtime;
		this.state = state;
		this.filenum = filenum;
		this.uid = uid;
		this.threshold = threshold;
	}
	
	public void setState(String newState)
	{
		this.state = newState;
	}
	
	public int getWid()
	{
		return this.wid;
	}
	
	public String getWname()
	{
		return this.wname;
	}
	
	public Date getWtime()
	{
		return this.wtime;
	}
	
	public String getState()
	{
		return this.state;
	}
	
	public int getFilenum()
	{
		return this.filenum;
	}
	
	public int getUid()
	{
		return this.uid;
	}
	
	public double getThreshold()
	{
		return this.threshold;
	}
}

package cn.edu.hit.mors.bean.thread;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;

import cn.edu.hit.mors.log.Logger;

public class LoadThread implements Runnable
{
	
	private String url;
	private File destFile;
	private int taskAmount;
	private Logger logger;
	private static int curse = 0;
	private static int priorDegree;
	
	public LoadThread()
	{
	}
	
	public LoadThread(String url, File destFile, int taskAmount, Logger logger)
	{
		this.url = url;
		this.destFile = destFile;
		this.taskAmount = taskAmount;
		this.logger = logger;
	}
	
	public void run()
	{
		try
		{
			
			URL u = new URL(url);
			URLConnection uc = u.openConnection();
			uc.setConnectTimeout(3000);
			uc.setReadTimeout(6000);
			InputStream in = uc.getInputStream();
			in = new BufferedInputStream(in);
			FileOutputStream fos = new FileOutputStream(destFile);
			int c;
			
			while ((c = in.read()) != -1)
			{
				fos.write(c);
			}
			
			System.out.print(getInfo());
			logger.log("Hit: " + url);
			
			fos.close();
			in.close();
			
		}
		catch (IOException ie)
		{
			System.out.print(getInfo());
			logger.log("Ignore: " + url);
			System.err.println("Not able to open: " + url);
		}
	}
	
	public synchronized String getInfo()
	{
		int degree = getDegree();
		
		if (degree == priorDegree)
		{
			return "";
		}
		
		else
		{
			priorDegree = degree;
			return getCurrentTime() + "   Downloading web pages：[ " + degree + "% ]\n";
		}
	}
	
	private synchronized String getCurrentTime()
	{
		
		SimpleDateFormat tempDate = new SimpleDateFormat("yyyy-MM-dd" + " " + "hh:mm:ss");
		String dateTime = tempDate.format(new Date());
		
		return dateTime;
	}
	
	public synchronized int getDegree()
	{
		return (++curse) * 100 / taskAmount;
	}
	
	public static void resetCurse()
	{
		curse = 0;
	}
}
package cn.edu.hit.mors.log;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class Logger
{
	
	private BufferedWriter logWriter = null;
	
	public Logger()
	{
	}
	
	public Logger(String logPath)
	{
		try
		{
			logWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logPath), "utf-8"));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void log(String message)
	{
		if (this.logWriter != null)
		{
			try
			{
				this.logWriter.write(message);
				this.logWriter.newLine();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public void close()
	{
		try
		{
			this.logWriter.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void setLogger(BufferedWriter logWriter)
	{
		this.logWriter = logWriter;
	}
	
	public BufferedWriter getLogWriter()
	{
		return this.logWriter;
	}
}

package cn.edu.hit.mors.view;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import cn.edu.hit.mors.bean.compare.SimPair;
import cn.edu.hit.mors.bean.compare.TextComparer;
import cn.edu.hit.mors.bean.task.Task;
import cn.edu.hit.mors.duplication.local.LocalDuplication;
import cn.edu.hit.mors.duplication.web.WebDuplication;

public class MorsService
{
	
	private Queue<Task> taskQueue;
	private Connection connection;
	private Statement statement;
	private String driver = "com.mysql.jdbc.Driver";
	private String url = "jdbc:mysql://localhost/mors";
	private String username = "root";
	private String passwd = "";
	
	private final String docRoot = "C:/JiangFeng-PC/Solaris/uncompress/";
	private final String resultRoot = "C:/JiangFeng-PC/Solaris/Result/";
	
	public MorsService()
	{
		taskQueue = new LinkedList<Task>();
		setDriver();
		setConnection(url, username, passwd);
	}
	
	public boolean setDriver()
	{
		try
		{
			Class.forName(driver);
			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean setConnection(String url, String username, String passwd)
	{
		try
		{
			connection = DriverManager.getConnection(url, username, passwd);
			statement = connection.createStatement();
			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}
	
	public void addTask(Task newTask)
	{
		this.taskQueue.offer(newTask);
	}
	
	public void addAllTask(List<Task> newTasks)
	{
		this.taskQueue.addAll(newTasks);
		try
		{
			for (Task task : newTasks)
			{
				String stateUpdate = "update works set state='q' where wid=" + task.getWid();
				statement.addBatch(stateUpdate);
			}
			statement.executeBatch();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	public List<Task> queryNewTask()
	{
		List<Task> newTasks = new ArrayList<Task>();
		String queryForNewTask = "select * from works where state='w'";//need thinking...
		ResultSet rs = sendQuery(queryForNewTask);
		try
		{
			if (rs != null)
			{
				while (rs.next())
				{
					int wid = rs.getInt("wid");
					String wname = rs.getString("wname");
					java.util.Date wtime = rs.getTimestamp("wtime");
					String state = rs.getString("state");
					int filenum = rs.getInt("filenum");
					int uid = rs.getInt("uid");
					double threshold = rs.getDouble("threshold");
					Task task = new Task(wid, wname, wtime, state, filenum, uid, threshold);
					newTasks.add(task);
				}
				return newTasks;
			}
			return null;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public String queryMethod(int taskId)
	{
		String method;
		String methodQuery = "select method from works where wid=" + taskId;
		ResultSet rs = sendQuery(methodQuery);
		try
		{
			if (rs != null)
			{
				rs.next();
				method = rs.getString("method");
				return method;
			}
			return null;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public ResultSet sendQuery(String query)
	{
		try
		{
			System.out.println("query = " + query);
			ResultSet rs = statement.executeQuery(query);
			return rs;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public int sendUpdate(String update)
	{
		try
		{
			return statement.executeUpdate(update);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return -1;
		}
	}
	
	public int insertSim(SimPair simPair, Task task)
	{
		String sname1 = simPair.first().toString();
		sname1 = sname1.replaceAll("\\\\", "/");
		String sname2 = simPair.second().toString();
		sname2 = sname2.replaceAll("\\\\", "/");
		Float simValue = simPair.degree(); //mors.sql needs to change
		int wid = task.getWid();
		
		try
		{
			/*
			 * PrintStream console = System.out; System.setOut(new PrintStream("/export/home/jiang/Result/log.txt"));
			 */
			String simInsert = "insert sim(sname1, sname2, wid, simvalue, simresult) " + "values ('" + sname1 + "', '" + sname2 + "', " + wid + ", " + simValue + ", '" + "Y')";
			System.out.println("Insert = " + simInsert);
			/*
			 * System.out.close(); System.setOut(console);
			 */
			return statement.executeUpdate(simInsert);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return -1;
		}
	}
	
	public int insertWebSim(SimPair simPair, Task task)
	{
		String weburl = simPair.first().toString();
		weburl = weburl.replaceAll("\\\\", "/");
		String localfile = simPair.second().toString();
		localfile = localfile.replaceAll("\\\\", "/");
		Float simValue = simPair.degree(); //mors.sql needs to change
		int wid = task.getWid();
		
		try
		{
			/*
			 * PrintStream console = System.out; System.setOut(new PrintStream("/export/home/jiang/Result/log.txt"));
			 */
			String simInsert = "insert websim(localfile, weburl, wid, simvalue, simresult) " + "values ('" + localfile + "', '" + weburl + "', " + wid + ", " + simValue + ", '"
					+ "Y')";
			System.out.println("Insert = " + simInsert);
			/*
			 * System.out.close(); System.setOut(console);
			 */
			return statement.executeUpdate(simInsert);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return -1;
		}
	}
	
	public void localDetect(Task task)
	{
		LocalDuplication localDup = new LocalDuplication();
		localDup.setThreshold(task.getThreshold());
		Set<SimPair> localResult = new LinkedHashSet<SimPair>();
		try
		{
			String destDir = Integer.toString(task.getWid());
			String xmlDir = destDir + "/local";
			System.out.println("Dest dir = " + destDir);
			new File(resultRoot + destDir).mkdirs();
			new File(resultRoot + xmlDir).mkdirs();
			
			localDup.execute(docRoot + destDir, resultRoot + xmlDir + "/local.txt");
			localResult = localDup.getResult();
			System.out.println(localResult.size() + "pair of local plagiarized documents detected.");
			for (SimPair simPair : localResult)
			{
				if (insertSim(simPair, task) == -1) System.out.println("update sim failed");
			}
			
			String queryForSid = "select sid from sim where wid=" + task.getWid();
			ResultSet sidSet = statement.executeQuery(queryForSid);
			Iterator<SimPair> itForSimPair = localResult.iterator();
			while (sidSet.next() && itForSimPair.hasNext())
			{
				itForSimPair.next().setSid(sidSet.getInt("sid"));
				System.out.println("sid = " + sidSet.getInt("sid"));
			}
			
			TextComparer comparer = new TextComparer(10, resultRoot + xmlDir);
			comparer.compare(localResult);
		}
		catch (Exception e)
		{
			//e.printStackTrace();
			return;
		}
	}
	
	public void webDetect(Task task)
	{
		WebDuplication webDup = new WebDuplication();
		webDup.setThreshold(task.getThreshold());
		Set<SimPair> webResult = new LinkedHashSet<SimPair>();
		try
		{
			String destDir = Integer.toString(task.getWid());
			String xmlDir = destDir + "/web";
			System.out.println("Dest dir = " + destDir);
			new File(resultRoot + destDir).mkdirs();
			new File(resultRoot + xmlDir).mkdirs();
			
			webDup.execute(docRoot + destDir, resultRoot + xmlDir + "/web.txt");
			webResult = webDup.getResult();
			System.out.println(webResult.size() + "pair of local plagiarized documents detected.");
			for (SimPair simPair : webResult)
			{
				if (insertWebSim(simPair, task) == -1) System.out.println("update sim failed");
				simPair.setFirst(simPair.getHtmlSource());
			}
			
			String queryForSid = "select wsid from websim where wid=" + task.getWid();
			ResultSet sidSet = statement.executeQuery(queryForSid);
			Iterator<SimPair> itForSimPair = webResult.iterator();
			while (sidSet.next() && itForSimPair.hasNext())
			{
				itForSimPair.next().setSid(sidSet.getInt("wsid"));
				System.out.println("wsid = " + sidSet.getInt("wsid"));
			}
			
			TextComparer comparer = new TextComparer(10, resultRoot + xmlDir);
			comparer.compare(webResult);
			webDup.postProcess();
		}
		catch (Exception e)
		{
			//e.printStackTrace();
			return;
		}
	}
	
	public static void main(String[] args)
	{
		MorsService morsService = new MorsService();
		
		while (true)
		{
			List<Task> newTasks = morsService.queryNewTask();
			if (newTasks != null)
			{
				morsService.addAllTask(newTasks);
			}
			
			if (morsService.taskQueue.size() > 0)
			{
				Task task = morsService.taskQueue.poll();
				String stateUpdate = "update works set state='s' where wid=" + task.getWid();
				if (morsService.sendUpdate(stateUpdate) == -1)
				{
					System.err.println("Failed to update the state of task: " + task.getWid());
				}
				
				String method = morsService.queryMethod(task.getWid());
				if (method != null)
				{
					if (method.equalsIgnoreCase("l"))
					{
						morsService.localDetect(task);
					}
					else if (method.equalsIgnoreCase("w"))
					{
						morsService.webDetect(task);
					}
					else
					{
						morsService.localDetect(task);
						morsService.webDetect(task);
					}
				}
				else
				{
					System.err.println("Failed to get the method of task: " + task.getWid());
				}
				stateUpdate = "update works set state='e' where wid=" + task.getWid();
				if (morsService.sendUpdate(stateUpdate) == -1)
				{
					System.err.println("Failed to update the state of task: " + task.getWid());
				}
			}
			else
			{
				try
				{
					Thread.sleep(10000);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}
}

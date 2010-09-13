package cn.edu.hit.mors.view;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import cn.edu.hit.mors.bean.compare.SimPair;
import cn.edu.hit.mors.duplication.local.LocalDuplication;
import cn.edu.hit.mors.duplication.web.WebDuplication;

public class Mors
{
	
	private static final String USAGE = "java -jar Mors.jar [options [value]]... " + "<document_directory>";
	
	public Mors()
	{
	}
	
	public static void main(String[] args)
	{
		Options options = new Options();
		options.addOption("l", true, "run duplication at local system");
		options.addOption("w", true, "run duplication on Internet");
		options.addOption("t", true, "to specify the amount of threads to run mors(web)");
		Option threOption = new Option("T", true, "to specify a threshold, [required]");
		threOption.setRequired(true);
		options.addOption(threOption);
		
		CommandLineParser parser = new BasicParser();
		CommandLine cmd = null;
		try
		{
			cmd = parser.parse(options, args);
		}
		catch (ParseException e)
		{
			usage(options);
			System.exit(1);
		}
		
		if (cmd.hasOption("l"))
		{
			String[] params = cmd.getArgs();
			LocalDuplication localDup = new LocalDuplication();
			double threshold = Double.parseDouble(cmd.getOptionValue("T"));
			localDup.setThreshold(threshold);
			Set<SimPair> localResult = new LinkedHashSet<SimPair>();
			try
			{
				localDup.execute(params[0], cmd.getOptionValue("l"));
				localResult = localDup.getResult();
				System.out.println(localResult.size() + " duplication detected(local)...");
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		if (cmd.hasOption("w"))
		{
			String[] params = cmd.getArgs();
			WebDuplication webDup = new WebDuplication();
			if (cmd.hasOption("t"))
			{
				int threadQut = Integer.parseInt(cmd.getOptionValue("t"));
				webDup.setThreadQut(threadQut);
			}
			double threshold = Double.parseDouble(cmd.getOptionValue("T"));
			webDup.setThreshold(threshold);
			Set<SimPair> webResult = new LinkedHashSet<SimPair>();
			try
			{
				webDup.execute(params[0], cmd.getOptionValue("w"));
				webResult = webDup.getResult();
				System.out.println(webResult.size() + " duplication detected(web)...");
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public static void usage(Options options)
	{
		System.err.println("Usage: " + USAGE);
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("Mors", options);
	}
}

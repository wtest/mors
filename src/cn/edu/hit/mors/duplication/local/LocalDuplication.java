package cn.edu.hit.mors.duplication.local;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Searcher;

import java.io.*;
import java.util.*;

import jeasy.analysis.MMAnalyzer;
import cn.edu.hit.mors.duplication.Duplication;
import cn.edu.hit.mors.log.*;

public class LocalDuplication extends Duplication
{
	private final File indexDir = new File("index");
	private final File logDir = new File("log/local");
	
	public LocalDuplication()
	{
		
		super();
		this.logDir.mkdirs();
		this.indexDir.mkdirs();
		
		try
		{
			this.err = new PrintStream(new BufferedOutputStream(new FileOutputStream(logDir + "/error.log")));
			System.setErr(err);
			this.logger = new Logger(logDir + "/mors.log");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param docDirectory
	 * @param resultFile
	 */
	public void execute(String docDirectory, String resultFile) throws Exception
	{
		
		Date startTime = new Date();
		
		final File docDir = new File(docDirectory);
		if (!docDir.exists() || !docDir.canRead())
		{
			System.err.append("Document directory '" + docDir.getAbsolutePath() + "' does not exist or is not readable, please check the path");
			return;
		}
		
		try
		{
			IndexReader reader = IndexReader.open(indexDir);
			deleteDocs(reader, docDir);
			reader.close();
		}
		catch (FileNotFoundException fe)
		{
			System.err.println("no index files built yet...");
		}
		
		IndexWriter writer;
		boolean create = true;
		writer = new IndexWriter(indexDir, new MMAnalyzer(), create, IndexWriter.MaxFieldLength.LIMITED);
		docAmount = listAllFiles(docDir).size();
		indexDocAmount = docAmount;
		console.println(getCurrentTime() + "   Indexing...");
		indexDocs(writer, docDir);
		writer.optimize();
		writer.close();
		
		IndexReader reader = IndexReader.open(indexDir);
		Searcher searcher = new IndexSearcher(reader);
		Analyzer analyzer = new MMAnalyzer();
		QueryParser parser = new QueryParser("contents", analyzer);
		parser.setPhraseSlop(1);
		
		HashMap<String, Float> results = new HashMap<String, Float>();
		console.println(getCurrentTime() + "   Plagiarism detecting...");
		searchDocs(searcher, parser, docDir, results);
		reader.close();
		sortResults(results, resultFile);
		
		postProcess();
		console.println(getCurrentTime() + "   Result file stored as：" + resultFile);
		
		Date endTime = new Date();
		console.println("Total run time： " + getRunTime(startTime, endTime) + " seconds");
	}
	
	/**
	 * delete all the temporary directories
	 */
	@Override
	public void postProcess()
	{
		console.println(getCurrentTime() + "   Deleting temporary file...");
		ArrayList<File> dirToDel = new ArrayList<File>();
		dirToDel.add(indexDir);
		
		for (File dir : dirToDel)
			deleteFolder(dir);
		
		this.logger.close();
		this.err.close();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		
		LocalDuplication localDuplication = new LocalDuplication();
		try
		{
			localDuplication.execute("reports/os2", "local.txt");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
}

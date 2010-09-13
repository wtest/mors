package cn.edu.hit.mors.duplication.web;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import jeasy.analysis.MMAnalyzer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TopDocs;

import cn.edu.hit.mors.bean.compare.SimPair;
import cn.edu.hit.mors.bean.extract.FeatureCoder;
import cn.edu.hit.mors.bean.extract.FeatureExtractor;
import cn.edu.hit.mors.bean.search.WebResult;
import cn.edu.hit.mors.bean.search.WebResults;
import cn.edu.hit.mors.bean.search.WebSearcher;
import cn.edu.hit.mors.bean.thread.LoadThread;
import cn.edu.hit.mors.bean.thread.SearchThread;
import cn.edu.hit.mors.duplication.Duplication;
import cn.edu.hit.mors.log.Logger;

public class WebDuplication extends Duplication
{
	private String myAppID = "3F51CB8D52FDCDE72973FB5931528A756E567AD9";
	private final File indexDir = new File("web_index");
	private final File logDir = new File("log/web");
	private final File webSearchResult = new File("web_search_xml");
	private final File webPages = new File("web_pages");
	private HashMap<String, String> fileToUrl;
	private int threadQut = 10;
	
	public WebDuplication()
	{
		
		super();
		this.logDir.mkdirs();
		this.indexDir.mkdirs();
		this.webSearchResult.mkdirs();
		this.webPages.mkdirs();
		
		try
		{
			this.err = new PrintStream(new BufferedOutputStream(new FileOutputStream(logDir + "/error.log")));
			//System.setErr(err);
			this.logger = new Logger(logDir + "/mors.log");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void setThreadQut(int threadQut)
	{
		this.threadQut = threadQut;
	}
	
	public void setAppID(String appID)
	{
		this.myAppID = appID;
	}
	
	/**
	 * @param docDirectory
	 * @param resultFile
	 * @throws java.io.IOException
	 */
	public void execute(String docDirectory, String resultFile) throws Exception
	{
		Date startTime = new Date();
		
		final File docDir = new File(docDirectory);
		if (!docDir.exists() || !docDir.canRead())
		{
			System.err.println("Document directory '" + docDir.getAbsolutePath() + "' does not exist or is not readable, please check the path");
			return;
		}
		
		HashSet<String> urlCache = new HashSet<String>();
		ArrayList<String> urls = new ArrayList<String>();
		ArrayList<File> docs = listAllFiles(docDir);
		docAmount = docs.size();
		WebSearcher ws = new WebSearcher(myAppID);
		
		console.println(getCurrentTime() + "   Feature extracting and web retrieval...");
		for (int m = 0; m < docAmount; m++)
		{
			File doc = docs.get(m);
			if (doc.isHidden())
			{
				continue;
			}
			
			console.println(getCurrentTime() + "   [ " + (m + 1) + "/" + docAmount + " ]\t" + "Processing " + doc);
			FeatureExtractor coder = new FeatureExtractor(10, 14);
			ArrayList<String> codes = coder.getFeatureCodes(doc);
			codes = coder.tidyWordBoundry(codes);
			
			ArrayList<File> xmlFiles = new ArrayList<File>();
			for (int x = 0; x < codes.size(); x++)
			{
				File xmlFile = new File(webSearchResult + "/" + doc.getName() + "_" + x + ".xml");
				xmlFiles.add(xmlFile);
			}
			String keyword = "";
			ExecutorService exec = Executors.newFixedThreadPool(10);
			for (int i = 0; i < codes.size(); i++)
			{
				keyword = "\"" + codes.get(i) + "\" ";
				exec.execute(new SearchThread(keyword, xmlFiles.get(i), ws));
			}
			exec.shutdown();
			exec.awaitTermination(1, TimeUnit.DAYS);
			
			for (File xmlFile : xmlFiles)
			{
				String path = xmlFile.getPath();
				WebResults webResults = new WebResults(path);
				ArrayList<WebResult> results = webResults.getUrls();
				
				for (WebResult result : results)
				{
					String url = "";
					if (result.getCacheUrl() != null) url = result.getUrl().toString();
					else
						continue;
					if (!urlCache.contains(url))
					{
						urls.add(url);
						urlCache.add(url);
					}
				}
			}
		}
		
		console.println(getCurrentTime() + "   Downloading web pages...");
		loadPages(urls, webPages, threadQut);
		
		try
		{
			IndexReader reader = IndexReader.open(indexDir);
			deleteDocs(reader, webPages);
			reader.close();
		}
		catch (FileNotFoundException fe)
		{
			System.err.println("no index files built yet...");
		}
		
		/* index */
		boolean create = true;
		console.println(getCurrentTime() + "   Indexing...");
		IndexWriter writer = new IndexWriter(indexDir, new MMAnalyzer(), create, IndexWriter.MaxFieldLength.LIMITED);
		indexDocAmount = listAllFiles(webPages).size();
		indexDocs(writer, webPages);
		writer.optimize();
		writer.close();
		
		/* search */
		IndexReader reader = IndexReader.open(indexDir);
		Searcher searcher = new IndexSearcher(reader);
		Analyzer analyzer = new MMAnalyzer();
		QueryParser parser = new QueryParser("contents", analyzer);
		parser.setPhraseSlop(1);
		console.println(getCurrentTime() + "   Plagiarism detecting...");
		
		HashMap<String, Float> searchResults = new HashMap<String, Float>();
		System.out.println("docDir = " + docDir);
		searchDocs(searcher, parser, docDir, searchResults);
		reader.close();
		System.out.println("resultFile = " + resultFile);
		sortResults(searchResults, resultFile);
		
		//postProcess();
		console.println(getCurrentTime() + "   Result file stored as：" + resultFile);
		
		Date endTime = new Date();
		console.println("Total time： " + getRunTime(startTime, endTime) + " seconds");
	}
	
	/**
	 * search document with feature codes
	 * 
	 * @param searcher
	 * @param parser
	 * @param file
	 * @param results
	 * @throws IOException
	 */
	public void searchDocs(Searcher searcher, QueryParser parser, File file, HashMap<String, Float> results) throws IOException
	{
		if (file.canRead() && !file.isHidden())
		{
			if (file.isDirectory())
			{
				String[] files = file.list();
				if (files != null)
				{
					for (int i = 0; i < files.length; i++)
					{
						searchDocs(searcher, parser, new File(file, files[i]), results);
					}
				}
			}
			else
			{
				System.out.println("Searching ... " + file.toString());
				FeatureCoder coder = new FeatureCoder();
				ArrayList<String> codes = new ArrayList<String>();
				coder.getFeatureCodes(file, codes);
				
				String queryStr = "";
				for (int i = 0; i < codes.size(); i++)
				{
					queryStr += "\"" + codes.get(i) + "\" ";
				}
				
				if (!queryStr.equals(""))
				{
					try
					{
						Query query = parser.parse(queryStr);
						
						TopDocs topDocs = searcher.search(query, null, 5);
						ScoreDoc[] hits = topDocs.scoreDocs;
						for (int j = 0; j < hits.length; j++)
						{
							Document doc = searcher.doc(hits[j].doc);
							float score = hits[j].score;
							String path = doc.get("path");
							if (path != null)
							{
								path = path.replaceAll("\\\\", "/");
								String fileName = file.getPath().replaceAll("\\\\", "/");
								if (!path.equals(fileName))
								{
									results.put(fileName + "<-->" + path, score);
								}
							}
							else
							{
								System.out.println("No path for this document");
							}
						}
					}
					catch (ParseException e)
					{
						System.err.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
					}
					catch (IOException e)
					{
						System.err.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
					}
				}
			}
		}
	}
	
	/**
	 * sort the results and save the results into file
	 * 
	 * @param results
	 * @param resultsfile
	 * @throws IOException
	 */
	protected void sortResults(HashMap<String, Float> results, String resultsfile) throws IOException
	{
		List<Map.Entry<String, Float>> list = new ArrayList<Map.Entry<String, Float>>(results.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<String, Float>>()
		{
			
			public int compare(Map.Entry<String, Float> o1, Map.Entry<String, Float> o2)
			{
				return o2.getValue().compareTo(o1.getValue());
			}
		});
		
		FileWriter fwriter = new FileWriter(resultsfile);
		BufferedWriter writer = new BufferedWriter(fwriter);
		
		for (Iterator<Map.Entry<String, Float>> iter = list.iterator(); iter.hasNext();)
		{
			Map.Entry<String, Float> entry = iter.next();
			float score = normalize(entry.getValue());
			
			if (score >= this.threshold)
			{
				SimPair simPair = new SimPair(score, entry.getKey());
				String[] items = entry.getKey().split("<-->");
				simPair.setFirst(fileToUrl.get(items[1]));
				simPair.setSecond(items[0]);
				simPair.setHtmlSource(items[1]);
				if (!this.result.contains(simPair))
				{
					this.result.add(simPair);
					writer.write(score + " " + items[0] + "<-->" + simPair.first());
					writer.newLine();
				}
			}
		}
		writer.close();
	}
	
	/**
	 * delete all temporary directories
	 */
	public void postProcess()
	{
		console.println(getCurrentTime() + "   deleting temporary file...");
		ArrayList<File> dirToDel = new ArrayList<File>();
		dirToDel.add(indexDir);
		dirToDel.add(webSearchResult);
		dirToDel.add(webPages);
		
		for (File dir : dirToDel)
			deleteFolder(dir);
		
		this.logger.close();
		this.err.close();
	}
	
	/**
	 * normalize the score using sigmoid function
	 * 
	 * @param score
	 * @return
	 */
	public float normalize(float score)
	{
		return (float) (1 / (1 + 4 * Math.exp((-5) * score)));
	}
	
	/**
	 * load web pages
	 * 
	 * @param url
	 * @param destDir
	 */
	private void loadPages(ArrayList<String> url, File destDir, int threadQut) throws InterruptedException
	{
		if (destDir.isDirectory())
		{
			File[] files = destDir.listFiles();
			if (files != null)
			{
				for (File f : files)
				{
					f.delete();
				}
			}
		}
		ArrayList<String> validUrls = filterList(url);
		fileToUrl = new HashMap<String, String>();
		ExecutorService exec = Executors.newFixedThreadPool(threadQut);
		
		int taskAmount = validUrls.size();
		for (int i = 0; i < taskAmount; i++)
		{
			File destFile = new File(destDir + "/" + i + ".html");
			fileToUrl.put(destDir + "/" + i + ".html", validUrls.get(i));
			exec.execute(new LoadThread(validUrls.get(i), destFile, taskAmount, logger));
		}
		
		exec.shutdown();
		exec.awaitTermination(1, TimeUnit.DAYS);
		LoadThread.resetCurse();
	}
	
	/**
	 * filter web pages by suffix
	 * 
	 * @param url
	 * @return
	 */
	private static boolean filter(String url)
	{
		if (url.endsWith(".doc") || url.endsWith(".pdf") || url.endsWith(".ppt") || url.endsWith(".docx") || url.endsWith(".DOC") || url.endsWith(".PDF") || url.endsWith(".PPT")
				|| url.endsWith("DOCX"))
		{
			return true;
		}
		
		return false;
	}
	
	private static ArrayList<String> filterList(ArrayList<String> urls)
	{
		ArrayList<String> newUrls = new ArrayList<String>();
		
		for (String url : urls)
		{
			if (filter(url) == false) newUrls.add(url);
		}
		
		return newUrls;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		WebDuplication webDuplication = new WebDuplication();
		webDuplication.setThreadQut(30);
		try
		{
			webDuplication.execute("test_data", "web_result.txt");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
}

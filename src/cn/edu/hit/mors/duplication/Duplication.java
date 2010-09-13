package cn.edu.hit.mors.duplication;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TopDocs;
import cn.edu.hit.mors.bean.extract.DocExtractor;
import cn.edu.hit.mors.bean.extract.FeatureCoder;
import cn.edu.hit.mors.log.*;
import cn.edu.hit.mors.bean.compare.SimPair;

public class Duplication
{
	
	protected PrintStream out = null;
	protected PrintStream console = System.out;
	protected int docAmount;
	protected int indexDocAmount;
	protected Logger logger = null;
	protected PrintStream err = null;
	protected double threshold = 0.3;
	protected Set<SimPair> result;
	
	public Duplication()
	{
		this.result = new LinkedHashSet<SimPair>();
	}
	
	protected void execute()
	{
	}
	
	protected void postProcess()
	{
	}
	
	public void setThreshold(double threshold)
	{
		this.threshold = threshold;
	}
	
	public double getThreshold()
	{
		return this.threshold;
	}
	
	public Set<SimPair> getResult()
	{
		return this.result;
	}
	
	/**
	 * to list all the files under a directory recursively
	 * 
	 * @param directory
	 * @return
	 */
	public ArrayList<File> listAllFiles(File directory)
	{
		ArrayList<File> dirFiles = new ArrayList<File>();
		File[] files = directory.listFiles();
		for (File f : files)
		{
			if (!f.isHidden())
			{
				if (!f.isDirectory())
				{
					dirFiles.add(f);
				}
				else if (f.isDirectory())
				{
					dirFiles.addAll(listAllFiles(f));
				}
			}
		}
		return dirFiles;
	}
	
	/**
	 * to delete a folder recursevely
	 * 
	 * @param dir
	 */
	public void deleteFolder(File dir)
	{
		if (dir.isDirectory() && dir.canRead())
		{
			File[] files = dir.listFiles();
			for (File f : files)
			{
				if (f.isDirectory() && f.canRead()) deleteFolder(f);
				else
					f.delete();
			}
		}
		dir.delete();
	}
	
	/**
	 * get the current time
	 * 
	 * @return String
	 */
	protected String getCurrentTime()
	{
		
		SimpleDateFormat tempDate = new SimpleDateFormat("yyyy-MM-dd" + " " + "hh:mm:ss");
		String dateTime = tempDate.format(new Date());
		
		return dateTime;
	}
	
	protected String getRunTime(Date startTime, Date endTime)
	{
		long runTime = (endTime.getTime() - startTime.getTime()) / 1000;
		
		String information = "Total run time: " + (runTime / 60) + " minutes " + (runTime % 60) + " seconds";
		if (runTime <= 60) information = "Total run time:  " + runTime + " seconds";
		
		return information;
	}
	
	/**
	 * At first, delete the existing files
	 * 
	 * @param reader
	 * @param file
	 */
	protected void deleteDocs(IndexReader reader, File file)
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
						deleteDocs(reader, new File(file, files[i]));
					}
				}
			}
			else
			{
				try
				{
					Term term = new Term("path", file.getPath());
					reader.deleteDocuments(term);
				}
				catch (Exception e)
				{
					System.err.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
				}
			}
		}
	}
	
	private int curse = 0;
	
	/**
	 * index documents
	 * 
	 * @param writer
	 * @param file
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	protected void indexDocs(IndexWriter writer, File file) throws FileNotFoundException, IOException
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
						indexDocs(writer, new File(file, files[i]));
					}
				}
			}
			else
			{
				String information = getCurrentTime() + "   [ " + (++curse) + "/" + indexDocAmount + " ]\t" + "   indexing: " + file;
				console.println(information);
				Document doc = new Document();
				doc.add(new Field("path", file.getPath(), Field.Store.YES, Field.Index.NOT_ANALYZED));
				
				DocExtractor extractor = new DocExtractor(file);
				String text = extractor.getContent();
				if (text != null)
				{
					doc.add(new Field("contents", text, Field.Store.YES, Field.Index.ANALYZED));
					writer.addDocument(doc);
				}
			}
		}
	}
	
	protected void searchDocs(Searcher searcher, QueryParser parser, File file, HashMap<String, Float> results) throws IOException
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
				//System.out.println("searching " + file.toString());
				FeatureCoder coder = new FeatureCoder();
				ArrayList<String> codes = new ArrayList<String>();
				try
				{
					coder.getFeatureCodes(file, codes);
				}
				catch (Exception e)
				{
					return;
				}
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
						
						TopDocs topDocs = searcher.search(query, null, 20);
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
								System.err.println("No path for this document");
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
				if (!this.result.contains(simPair))
				{
					this.result.add(simPair);
					writer.write(score + " " + entry.getKey());
					writer.newLine();
				}
			}
		}
		writer.close();
	}
	
	protected float normalize(float score)
	{
		//return score; for local;
		return (float) (1 / (1 + 12 * Math.exp((-5) * score)));
	}
	
}

package cn.edu.hit.mors.bean.compare;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import cn.edu.hit.mors.bean.extract.*;

public class TextComparer
{
	private int threshold; //控制LCS的最小长度。如果大于域值，则认为是一处雷同；否则，忽略
	private String path; //the path to store xml results
	
	public TextComparer()
	{
		this(10, "result"); //默认的域值为10
	}
	
	public TextComparer(int threshold, String path)
	{
		this.threshold = threshold;
		this.path = path;
	}
	
	public int getThreshold()
	{
		return threshold;
	}
	
	public void setThreshold(int threshold)
	{
		this.threshold = threshold;
	}
	
	/**
	 * 
	 * 找出两个文本中的雷同字符串，并将其起始、终止位置保存起来
	 * 
	 * @param path1
	 * @param path2
	 */
	public ArrayList<TextSim> findSim(String s1, String s2)
	{
		ArrayList<TextSim> list = new ArrayList<TextSim>();
		
		int max = 0;
		
		while (true)
		{
			TextSim lcs = new CommonSubString().find(s1, s2);
			max = lcs.getLength();
			if (max < threshold) break;
			
			list.add(lcs);
			
			//用随机字符串替换掉原字符串中的最长公共子串，为下一轮寻找新的子串做准备
			s1 = randomReplace(s1, lcs.source);
			s2 = randomReplace(s2, lcs.dest);
		}
		
		return list;
	}
	
	public void compare(SimPair pair)
	{
		System.out.println("=====Comparing file " + pair.first() + " and " + pair.second() + "=====");
		
		String source = new DocExtractor(pair.first()).getContent();
		String dest = new DocExtractor(pair.second()).getContent();
		ArrayList<TextSim> list = this.findSim(source, dest);
		//String fileName = pair.first().getPath().replaceAll("[/\\\\]", "[sep]") + "--" + pair.second().getPath().replaceAll("[/\\\\]", "[sep]") + ".xml";
		String fileName = "" + pair.getSid() + ".xml";
		
		Document doc = DocumentHelper.createDocument();
		doc.setXMLEncoding("UTF-8");
		
		Element root = doc.addElement("mors");
		
		//source text
		Element firstReport = DocumentHelper.createElement("report");
		firstReport.addAttribute("id", "first");
		firstReport.addAttribute("originalPath", pair.first());
		firstReport.addAttribute("txtPath", "");
		firstReport.addCDATA(source);
		
		//dest text
		Element secondReport = DocumentHelper.createElement("report");
		secondReport.addAttribute("id", "second");
		secondReport.addAttribute("originalPath", pair.second());
		secondReport.addAttribute("txtPath", "");
		secondReport.addCDATA(dest);
		
		Element files = root.addElement("reports");
		files.add(firstReport);
		files.add(secondReport);
		
		Element sims = root.addElement("sims");
		sims.addAttribute("degree", Float.toString(pair.degree()));
		
		if (list != null)
		{
			for (int i = 0; i < list.size(); i++)
			{
				TextSim sim = list.get(i);
				
				Element e = DocumentHelper.createElement("sim");
				e.addAttribute("id", Integer.toString(i + 1));
				e.addAttribute("firstStart", Integer.toString(sim.source.getStart()));
				e.addAttribute("firstEnd", Integer.toString(sim.source.getEnd()));
				e.addAttribute("secondStart", Integer.toString(sim.dest.getStart()));
				e.addAttribute("secondEnd", Integer.toString(sim.dest.getEnd()));
				e.addAttribute("color", this.generateColor());
				
				System.out.printf("\n=====Sim %d, length %d=====\n", i + 1, sim.getLength());
				System.out.printf("first: [%d, %d]\n", sim.source.getStart(), sim.source.getEnd());
				System.out.println(source.substring(sim.source.getStart(), sim.source.getEnd()));
				System.out.printf("second: [%d, %d]\n", sim.dest.getStart(), sim.dest.getEnd());
				System.out.println(dest.substring(sim.dest.getStart(), sim.dest.getEnd()));
				
				sims.add(e);
			}
		}
		
		//System.out.println(doc.asXML());
		
		//writing file
		File file = new File(path);
		file.mkdirs();
		
		OutputFormat format = OutputFormat.createPrettyPrint();
		
		System.out.println("Writing result to file " + fileName + "\n\n\n");
		try
		{
			XMLWriter writer = new XMLWriter(new OutputStreamWriter(new FileOutputStream(path + File.separator + fileName), "UTF-8"), format);
			writer.write(doc);
			writer.close();
		}
		catch (UnsupportedEncodingException ex)
		{
			Logger.getLogger(TextComparer.class.getName()).log(Level.SEVERE, null, ex);
			return;
		}
		catch (IOException ex)
		{
			Logger.getLogger(TextComparer.class.getName()).log(Level.SEVERE, null, ex);
			return;
		}
	}
	
	public void compare(Set<SimPair> set)
	{
		for (SimPair pair : set)
		{
			this.compare(pair);
		}
	}
	
	private String generateColor()
	{
		//构造可选的颜色集
		ArrayList<String> colorSet = new ArrayList<String>();
		colorSet.add("#ff0000");
		colorSet.add("#000066");
		colorSet.add("#0066ff");
		colorSet.add("#009933");
		colorSet.add("#00ff00");
		colorSet.add("#990099");
		colorSet.add("#990000");
		colorSet.add("#ccff00");
		colorSet.add("#996666");
		colorSet.add("#ffff00");
		
		return colorSet.get(new Random().nextInt(colorSet.size()));
	}
	
	/**
	 * 产生用于替换的长度为length的随机字符串
	 * 
	 * @param length
	 * @return
	 */
	private String randomString(int length)
	{
		//32 - 126 are printable ASCII characters
		Random rand = new Random();
		StringBuffer sb = new StringBuffer();
		while (length-- > 0)
		{
			int n = rand.nextInt(95) + 32;
			sb.append((char) n);
		}
		
		return sb.toString();
	}
	
	/**
	 * 用随机字符串替换掉找到的LCS
	 * 
	 * @param s
	 * @param pair
	 * @return
	 */
	private String randomReplace(String s, Pair pair)
	{
		return s.substring(0, pair.getStart()) + randomString(pair.getLength()) + s.substring(pair.getEnd());
	}
	
	public static void main(String[] args)
	{
		SimPair pair1 = new SimPair(3.1f, "reports/report1/mfc.doc<-->reports/report1/09SG37001_王朝_实验1.doc");
		//SimPair pair2 = new SimPair(3.2f, "test_data/3.doc<-->test_data/4.doc");
		
		LinkedHashSet<SimPair> set = new LinkedHashSet<SimPair>();
		set.add(pair1);
		//set.add(pair2);
		
		TextComparer comparer = new TextComparer(10, "xml");
		comparer.compare(set);
	}
}

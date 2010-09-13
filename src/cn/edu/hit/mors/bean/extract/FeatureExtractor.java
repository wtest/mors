package cn.edu.hit.mors.bean.extract;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import jeasy.analysis.MMAnalyzer;

public class FeatureExtractor
{
	
	private int codeNum;
	private int codeLength;
	private final String punctuations = "！？：，。；!?:,.;";
	
	/**
	 * 
	 * @param codeNum
	 *            要找的特征码个数
	 * @param codeLen
	 *            在标点两边各取codeLen个unicode字符即为一个特征码
	 */
	public FeatureExtractor(int codeNum, int codeLen)
	{
		this.codeNum = codeNum;
		this.codeLength = codeLen;
	}
	
	public FeatureExtractor()
	{
		codeNum = 10;
		codeLength = 5;
	}
	
	/**
	 * Extracts feature codes from a file whose path is path
	 * 
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public ArrayList<String> getFeatureCodes(String path) throws IOException
	{
		return getFeatureCodes(new File(path));
	}
	
	/**
	 * Extracts feature codes from a file.
	 * 
	 * @param file
	 * @param codes
	 * @throws IOException
	 */
	public ArrayList<String> getFeatureCodes(File file) throws IOException
	{
		ArrayList<String> codes = new ArrayList<String>();
		DocExtractor extractor = new DocExtractor(file);
		String text = extractor.getContent();
		
		if (text != null)
		{
			// 获取所有标点符号出现的位置
			ArrayList<Integer> punctPos = getPunctuationPos(text);
			if (punctPos == null || punctPos.size() == 0)
			{
				return codes;
			}
			
			int randPos;
			Random rand = new Random();
			Set<Integer> set = new HashSet<Integer>();
			
			// 找到的特征码个数最多为标点符号的个数
			if (punctPos.size() < codeNum)
			{
				codeNum = punctPos.size();
			}
			
			while (codes.size() < codeNum)
			{
				randPos = Math.abs(rand.nextInt()) % punctPos.size();
				
				if (set.contains(randPos))
				{
					continue;
				}
				set.add(randPos);
				
				int begin = punctPos.get(randPos) - codeLength;
				int end = punctPos.get(randPos) + codeLength + 1;
				
				begin = begin < 0 ? 0 : begin;
				end = end > text.length() ? text.length() : end;
				
				codes.add(text.substring(begin, end));
			}
		}
		
		return codes;
	}
	
	/**
	 * 获取字符串中所有标点符号的位置
	 * 
	 * @param text
	 * @return
	 */
	private ArrayList<Integer> getPunctuationPos(String text)
	{
		ArrayList<Integer> pos = new ArrayList<Integer>();
		
		for (int i = 0; i < punctuations.length(); i++)
		{
			pos.addAll(getPunctuationPos(text, punctuations.charAt(i)));
		}
		
		return pos;
	}
	
	/**
	 * 获取text中标点符号punct出现的所有位置
	 * 
	 * @param text
	 * @param punct
	 * @return
	 */
	private ArrayList<Integer> getPunctuationPos(String text, char punct)
	{
		ArrayList<Integer> pos = new ArrayList<Integer>();
		
		int prePos = -1;
		while ((prePos = text.indexOf(punct, prePos + 1)) != -1)
		{
			pos.add(prePos);
		}
		
		return pos;
	}
	
	/**
	 * 为字符串找出分词边界，比如 参数为 "电下乡社会实践调"， 返回 "下乡社会实践"
	 * 
	 * @param s
	 * @throws IOException
	 */
	public String tidyWordBoundry(String text) throws IOException
	{
		MMAnalyzer analyzer = new MMAnalyzer();
		String separator = "@#$%%%%@#@$"; //分词的分隔符
		
		String textNew = analyzer.segment(text, separator);
		int beg = textNew.indexOf(separator);
		int end1 = textNew.lastIndexOf(separator);
		int end2 = textNew.lastIndexOf(separator, end1 - 1);
		
		//        System.out.println("text:" + text);
		//        System.out.println("newText" + textNew);
		//        System.out.println("beg:" + beg + "\tend1:" + end1 + "\tend12:" + end2);
		
		if (end1 < 0)
		{
			return text;
		}
		else if (beg == end1)
		{
			return text.substring(0, end1);
		}
		
		String textTidy = text.substring(beg, text.length() + separator.length() - end1 + end2);
		
		return textTidy;
	}
	
	public ArrayList<String> tidyWordBoundry(ArrayList<String> codes) throws IOException
	{
		ArrayList<String> codesNew = new ArrayList<String>();
		
		for (String code : codes)
		{
			codesNew.add(tidyWordBoundry(code));
		}
		
		return codesNew;
	}
	
	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException
	{
		//File file = new File("test_data/5.doc");
		
		// 找10个特征码，每个特征码的长度为5+1+5=11
		FeatureExtractor coder = new FeatureExtractor(10, 5);
		//        ArrayList<String> codes = coder.getFeatureCodes(file);
		//        coder.tidyWordBoundry(codes);
		String str = coder.tidyWordBoundry("about");
		System.out.println(str);
		
		//        for (int i = 0; i < codes.size(); i++)
		//        {
		//            System.out.println("特征码" + i + ":" + codes.get(i));
		//        }
	}
}

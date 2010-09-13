package cn.edu.hit.mors.bean.extract;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import jeasy.analysis.MMAnalyzer;

public class FeatureCoder
{
	private int maxnumCodes;
	private int codeLength;
	private int maxFindNum;
	
	public FeatureCoder(int maxnumCodes, int codeLength, int maxFindNum)
	{
		this.maxnumCodes = maxnumCodes;
		this.codeLength = codeLength;
		this.maxFindNum = maxFindNum;
	}
	
	public FeatureCoder()
	{
		maxnumCodes = 10;
		codeLength = 5;
		maxFindNum = 10;
	}
	
	/**
	 * Extracts feature codes from a file.
	 * 
	 * @param file
	 * @param codes
	 * @throws IOException
	 */
	public String getFeatureCodes(File file, ArrayList<String> codes) throws IOException
	{
		//System.out.println("before");
		DocExtractor extractor = new DocExtractor(file);
		String text = extractor.getContent();
		
		//System.out.println(text);
		/*
		 * String fileName = file.getName()+".txt"; extractor.writeToFile("temp/"+fileName);
		 */
		codes.clear();
		
		if (text != null)
		{
			String[] tokens = getTokens(text);
			
			if (tokens.length > 0)
			{
				getFeatureCodes(tokens, codes);
			}
		}
		
		return text;
	}
	
	private void getFeatureCodes(String[] tokens, ArrayList<String> codes)
	{
		getFeatureCodes(tokens, codes, "！");
		getFeatureCodes(tokens, codes, "？");
		getFeatureCodes(tokens, codes, "：");
		getFeatureCodes(tokens, codes, "，");
		getFeatureCodes(tokens, codes, "。");
		getFeatureCodes(tokens, codes, "；");
		getFeatureCodes(tokens, codes, "!");
		getFeatureCodes(tokens, codes, "?");
		getFeatureCodes(tokens, codes, ":");
		getFeatureCodes(tokens, codes, ",");
		getFeatureCodes(tokens, codes, ".");
		getFeatureCodes(tokens, codes, ";");
	}
	
	/**
	 * get tokens of a text.
	 * 
	 * @param text
	 * @return
	 * @throws IOException
	 */
	private String[] getTokens(String text) throws IOException
	{
		MMAnalyzer analyzer = new MMAnalyzer();
		String[] sentences = text.split("[！？：，。；!?:,.;]");
		ArrayList<String> tokens = new ArrayList<String>();
		String[] result = new String[10];
		
		for (String s : sentences)
		{
			String[] t = analyzer.segment(s, " ").split(" ");
			for (String tmp : t)
			{
				tokens.add(tmp);
			}
			
			text = text.substring(s.length());
			if (text == null || text.length() < 1) break;
			tokens.add("" + text.charAt(0));
			text = text.substring(1);
		}
		
		//if (tokens == null) return null;
		return tokens.toArray(result);
	}
	
	/**
	 * Extracts feature codes from a tokens according to a punctuation randomly.
	 * 
	 * @param text
	 * @param codes
	 */
	private void getFeatureCodes(String[] tokens, ArrayList<String> codes, String punct)
	{
		Random rand = new Random();
		int randPos;
		int randPunct;
		int findNum = 0;
		Set<Integer> puncPosSet = new HashSet<Integer>();
		
		while (codes.size() < maxnumCodes && findNum++ < maxFindNum)
		{
			randPos = Math.abs(rand.nextInt()) % tokens.length;
			randPunct = findPunct(tokens, randPos, punct);
			
			if (randPunct < 0 || puncPosSet.contains(randPunct))
			{
				continue;
			}
			puncPosSet.add(randPunct);
			int begin = randPunct - codeLength > 0 ? randPunct - codeLength : 0;
			int end = randPunct + codeLength < tokens.length ? randPunct + codeLength : tokens.length;
			
			String code = "";
			for (int i = begin; i < end; i++)
			{
				code += tokens[i] + " ";
			}
			if (!code.equals(""))
			{
				codes.add(code);
			}
		}
	}
	
	/**
	 * find the position of a punctuation from a random position
	 * 
	 * @param tokens
	 * @param randPos
	 * @param punct
	 * @return
	 */
	private int findPunct(String[] tokens, int randPos, String punct)
	{
		for (int i = randPos; i < tokens.length; i++)
		{
			if (tokens[i].equals(punct))
			{
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException
	{
		File file = new File("D:/workplace/Eclipse/Mors/test_data1/论文.rtf");
		FeatureCoder coder = new FeatureCoder();
		ArrayList<String> codes = new ArrayList<String>();
		coder.getFeatureCodes(file, codes);
		
		for (int i = 0; i < codes.size(); i++)
		{
			System.out.println(codes.get(i));
		}
		
	}
}

package cn.edu.hit.mors.bean.compare;

import cn.edu.hit.mors.bean.extract.*;

/**
 * 
 * @author icycandy
 */
public class CommonSubString
{
	public TextSim find(String s1, String s2)
	{
		int max = 0;
		int indexm = 0, indexn = 0;
		
		int m = s1.length();
		int n = s2.length();
		//System.out.println("m: " + m + " n: " + n);
		int[][] ans = new int[m][n];
		
		//下面开始初始化
		for (int i = 0; i < m; i++)
		{
			if (s1.charAt(i) == s2.charAt(0))
			{
				ans[i][0] = 1;
			}
		}
		
		for (int j = 0; j < n; j++)
		{
			if (s1.charAt(0) == s2.charAt(j))
			{
				ans[0][j] = 1;
			}
		}
		
		//下面开始寻找最长公共子串
		max = 0;
		for (int i = 1; i < m; i++)
		{
			for (int j = 1; j < n; j++)
			{
				if (s1.charAt(i) == s2.charAt(j))
				{
					ans[i][j] = ans[i - 1][j - 1] + 1;
					if (ans[i][j] > max)
					{
						max = ans[i][j];
						indexm = i + 1;
						indexn = j + 1;
					}
				}
				else
				{
					ans[i][j] = 0;
				}
			}
		}
		
		//Date t2 = new Date();
		//System.out.println((t2.getTime()-t1.getTime())/1);
		
		//output, just for test
		//       String commonSequence = s1.substring(indexm - max, indexm);
		//       System.out.println("begin:" + (indexm - max) + "\tend:" + indexm);
		//       System.out.println("length: " + max + "\tstring:" + commonSequence);
		
		return new TextSim(new Pair(indexm - max, indexm), new Pair(indexn - max, indexn));
	}
	
	public static void main(String[] args)
	{
		String path1 = "test_data/1.doc";
		String path2 = "test_data/2.doc";
		String source = new DocExtractor(path1).getContent();
		String dest = new DocExtractor(path2).getContent();
		new CommonSubString().find(source, dest);
	}
}

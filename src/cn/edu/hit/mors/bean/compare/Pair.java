package cn.edu.hit.mors.bean.compare;

import java.awt.Point;

public class Pair
{
	public Point point;
	
	public Pair()
	{
		point = new Point(0, 0);
	}
	
	public Pair(int start, int end)
	{
		if (start < 0 || end < 0)
		{
			start = 0;
			end = 0;
		}
		
		if (start > end)
		{
			int tmp = start;
			start = end;
			end = tmp;
		}
		
		point = new Point(start, end);
	}
	
	public int getStart()
	{
		return point.x;
	}
	
	public int getEnd()
	{
		return point.y;
	}
	
	public void setStart(int start)
	{
		point.x = start;
	}
	
	public void setEnd(int end)
	{
		point.y = end;
	}
	
	public int getLength()
	{
		return this.getEnd() - this.getStart();
	}
}

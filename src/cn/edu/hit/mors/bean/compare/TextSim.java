package cn.edu.hit.mors.bean.compare;

public class TextSim
{
	public Pair source, dest;
	public int id;
	public String color;
	
	public TextSim(int id, Pair source, Pair dest)
	{
		this.id = id;
		this.source = source;
		this.dest = dest;
	}
	
	public TextSim(Pair source, Pair dest)
	{
		this.source = source;
		this.dest = dest;
	}
	
	public int getLength()
	{
		return Math.max(source.getLength(), dest.getLength());
	}
	
	public String getColor()
	{
		return color;
	}
	
	public void setColor(String color)
	{
		this.color = color;
	}
	
	public int getId()
	{
		return id;
	}
	
	public void setId(int id)
	{
		this.id = id;
	}
}

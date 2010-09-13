package cn.edu.hit.mors.bean.extract;

import java.io.File;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.apache.poi.POITextExtractor;
import org.apache.poi.extractor.ExtractorFactory;

import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.util.PDFTextStripper;

import net.htmlparser.jericho.Source;

public class DocExtractor
{
	private String content = "";
	private String line = "";
	private File file = null;
	private String type = null;
	
	public DocExtractor(File file, String type)
	{
		this.file = file;
		this.type = type;
	}
	
	public DocExtractor(File file)
	{
		this.file = file;
		this.type = getType(file);
	}
	
	public DocExtractor(String path, String type)
	{
		this.file = new File(path);
		this.type = type;
	}
	
	public DocExtractor(String path)
	{
		this.file = new File(path);
		this.type = getType(this.file);
	}
	
	public String getContent()
	{
		content = "";
		try
		{
			if (type.equals("TXT"))
			{
				BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"));
				while ((line = reader.readLine()) != null)
				{
					content += line + "\n";
				}
			}
			else if (type.equals("DOC"))
			{
				POITextExtractor extractor = ExtractorFactory.createExtractor(file);
				content = extractor.getText();
				//System.out.println("doc found");
			}
			else if (type.equals("PDF"))
			{
				PDDocument document = PDDocument.load(file);
				PDFTextStripper stripper = new PDFTextStripper();
				StringWriter writer = new StringWriter();
				stripper.writeText(document, writer);
				content = writer.toString();
				document.close();
			}
			else if (type.equals("HTML"))
			{
				Source htmlSource = new Source(new FileInputStream(file));
				content = htmlSource.getRenderer().toString();
			}
			else
			{
				return "";//
			}
		}
		catch (FileNotFoundException e)
		{
			System.err.println("File \"" + file.getPath() + "\" not found");
		}
		catch (IOException e)
		{
			System.err.println("File \"" + file.getPath() + "\" read error");
		}
		catch (Exception e)
		{
			System.err.println(this.file.toString());
			//e.printStackTrace();
			System.err.println(e.toString());
			return "";
		}
		
		return this.filter(content);
	}
	
	/**
	 * filter invalid xml characters invalid chracters are 0x00 - 0x08 0x0b - 0x0c 0x0e - 0x1f
	 * 
	 * @param s
	 * @return
	 */
	private String filter(String s)
	{
		s = s.replaceAll("[\\x00-\\x08\\x0b-\\x0c\\x0e-\\x1f]", "");
		s = s.replaceAll("\\x0d\\x0a", "\n");
		int end = (s.length() >= 3000) ? 3000 : s.length();
		s = s.substring(0, end);
		
		return s;
	}
	
	public void writeToFile(File destFile)
	{
		try
		{
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(destFile), "utf8"));
			writer.write(this.getContent());
			writer.close();
		}
		catch (IOException e)
		{
			System.err.println("File \"" + destFile.getPath() + "\" write error");
		}
	}
	
	public void writeToFile(String destPath)
	{
		this.writeToFile(new File(destPath));
	}
	
	private String getType(File file)
	{
		String fileName = file.getName();
		if (fileName.toLowerCase().endsWith(".txt"))
		{
			return "TXT";
		}
		else if (fileName.toLowerCase().endsWith(".doc"))
		{
			return "DOC";
		}
		else if (fileName.toLowerCase().endsWith(".docx"))
		{
			return "DOC";
		}
		else if (fileName.toLowerCase().endsWith(".pdf"))
		{
			return "PDF";
		}
		else if (fileName.toLowerCase().endsWith(".html"))
		{
			return "HTML";
		}
		else if (fileName.toLowerCase().endsWith(".php"))
		{
			return "HTML";
		}
		else if (fileName.toLowerCase().endsWith(".asp"))
		{
			return "HTML";
		}
		else
		{
			return "OTHER";
		}
	}
	
	//Only for debug
	public static void main(String[] args) throws Exception
	{
		String path = "papers/1053710819.ppt";
		DocExtractor myDoc = new DocExtractor(path);
		//myDoc.getContent();
		System.out.println(myDoc.getContent());
		myDoc.writeToFile(new File("out2.txt"));
	}
}

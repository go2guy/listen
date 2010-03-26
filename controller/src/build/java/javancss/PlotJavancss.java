import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class PlotJavancss
{
	Document dom;
	long classes = 0;
	long functions = 0;
	long ncss =	0;
	double average_ccn = 0.0;
	long ccn = 0;
	String dir = null;

	public PlotJavancss(String dir)
	{
		this.dir = dir + "/";
	}

	private	void parseXmlFile()
	{
		//get the factory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		try
		{
			//Using	factory	get	an instance	of document	builder
			DocumentBuilder	db = dbf.newDocumentBuilder();

			//parse	using builder to get DOM representation	of the XML file
			dom	= db.parse(dir + "javancss-output.xml");
		}
		catch(Exception	e)
		{
			e.printStackTrace();
		}
	}

	private	void parseDocument()
	{
		//get the root elememt
		Element	docEle = dom.getDocumentElement();

		//get the total
		NodeList nl;
		nl = docEle.getElementsByTagName("total");

		if (nl != null && nl.getLength() > 0)
		{
			Element	el = (Element)nl.item(0);
			classes	= Long.parseLong(getValue(el, "classes"));
			functions =	Long.parseLong(getValue(el,	"functions"));
			ncss = Long.parseLong(getValue(el, "ncss"));
		}

		//get the function averages
		nl = docEle.getElementsByTagName("function_averages");

		if (nl != null && nl.getLength() > 0)
		{
			Element	el = (Element)nl.item(0);
			average_ccn	= Double.parseDouble(getValue(el, "ccn"));
			ccn	= java.lang.Math.round(functions * average_ccn);
		}
	}

	private	String getValue(Element	el,	String tagName)
	{
		NodeList nl	= el.getElementsByTagName(tagName);

		if (nl != null && nl.getLength() > 0)
		{
			Element	el2	= (Element)nl.item(0);
			return el2.getFirstChild().getNodeValue();
		}

		return null;
	}

	private	void write()
	{
		try
		{
			BufferedWriter out;
			out	= new BufferedWriter(new FileWriter(dir	+ "javancss-classes.properties"));
			out.write("YVALUE="	+ classes);
			out.close();

			out	= new BufferedWriter(new FileWriter(dir	+ "javancss-functions.properties"));
			out.write("YVALUE="	+ functions);
			out.close();

			out	= new BufferedWriter(new FileWriter(dir	+ "javancss-ncss.properties"));
			out.write("YVALUE="	+ ncss);
			out.close();

			out	= new BufferedWriter(new FileWriter(dir	+ "javancss-ccn.properties"));
			out.write("YVALUE="	+ ccn);
			out.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void main(String[] args)
	{
		try
		{
			PlotJavancss plot =	new	PlotJavancss(args[0]);
			plot.parseXmlFile();
			plot.parseDocument();
			plot.write();
		}
		catch(Exception	e)
		{
			e.printStackTrace();
		}
	}
}

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXParseException;

public class PlotReviews implements Runnable
{
    private static final String UNSET = "item.label.unset";
    private static final String TYPE_CLARITY = "item.type.label.clarity";
    private static final String TYPE_CODING_STANDARDS = "item.type.label.codingStandards";
    private static final String TYPE_IRRELEVANT = "item.type.label.irrelevant";
    private static final String TYPE_MISSING = "item.type.label.missing";
    private static final String TYPE_OPTIMIZATION = "item.type.label.optimization";
    private static final String TYPE_OTHER = "item.type.label.other";
    private static final String TYPE_PROGRAM_LOGIC = "item.type.label.programLogic";
    private static final String TYPE_SUGGESTION = "item.type.label.suggestion";
    private static final String TYPE_USABILITY = "item.type.label.usability";

    private static final String SEV_TRIVIAL = "item.severity.label.trivial";
    private static final String SEV_MINOR = "item.severity.label.minor";
    private static final String SEV_NORMAL = "item.severity.label.normal";
    private static final String SEV_MAJOR = "item.severity.label.major";
    private static final String SEV_CRITICAL = "item.severity.label.critical";
    
    private static final String STATUS_OPEN = "item.status.label.open";
    private static final String STATUS_REOPENED = "item.status.label.reopened";

    private static final String RES_DUPLICATE = "item.resolution.label.validDuplicate";
    private static final String RES_INVALID = "item.resolution.label.invalidWontFix";

	private static AtomicInteger totalTasks = new AtomicInteger();
	private static AtomicInteger openTasks = new AtomicInteger();
	private static AtomicInteger closedTasks = new AtomicInteger();
	private static HashMap<String, Float> taskTypes = new HashMap<String, Float>();
	private static HashMap<String, Float> severity = new HashMap<String, Float>();

	static
	{
	    taskTypes.put(TYPE_CLARITY, (float)0);
	    taskTypes.put(TYPE_CODING_STANDARDS, (float)0);
	    taskTypes.put(TYPE_IRRELEVANT, (float)0);
	    taskTypes.put(TYPE_MISSING, (float)0);
	    taskTypes.put(TYPE_OPTIMIZATION, (float)0);
	    taskTypes.put(TYPE_OTHER, (float)0);
	    taskTypes.put(TYPE_PROGRAM_LOGIC, (float)0);
	    taskTypes.put(TYPE_SUGGESTION, (float)0);
	    taskTypes.put(TYPE_USABILITY, (float)0);
	    taskTypes.put(UNSET, (float)0);

	    severity.put(SEV_TRIVIAL, (float)0);
	    severity.put(SEV_MINOR, (float)0);
	    severity.put(SEV_NORMAL, (float)0);
	    severity.put(SEV_MAJOR, (float)0);
	    severity.put(SEV_CRITICAL, (float)0);
	    severity.put(UNSET, (float)0);
	}

    private int clarity = 0;
    private int standards = 0;
    private int irrelevant = 0;
    private int missing = 0;
    private int optimization = 0;
    private int other = 0;
    private int logic = 0;
    private int suggestion = 0;
    private int typeUnset = 0;
    private int usability = 0;

    private int trivial = 0;
    private int minor = 0;
    private int normal = 0;
    private int major = 0;
    private int critical = 0;
    private int sevUnset = 0;

    private Document dom;
    private File reviewFile;
	public PlotReviews(File reviewFile)
	{
        this.reviewFile = reviewFile; 
	}

	private	void parseXmlFile() throws Exception
	{
		//get the factory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		//Using	factory	get	an instance	of document	builder
		DocumentBuilder	db = dbf.newDocumentBuilder();

		//parse	using builder to get DOM representation	of the XML file
		dom	= db.parse(reviewFile);
	}

	private	void parseDocument()
	{
		//get the root element
		Element	docEle = dom.getDocumentElement();

		//get the total
		NodeList nl;
		nl = docEle.getElementsByTagName("ReviewIssue");

		if (nl != null && nl.getLength() > 0)
		{
		    totalTasks.addAndGet(nl.getLength());

		    for(int i = 0; i < nl.getLength(); i++)
		    {
    			Element	el = (Element)nl.item(i);
    		    String resolution = getValue(el, "Resolution");

    		    // Do not include invalid or duplicate tasks in our counting
    		    if(resolution.equals(RES_INVALID) || resolution.equals(RES_DUPLICATE))
    		    {
    		        totalTasks.decrementAndGet();
    		        continue;
    		    }

    		    String status = getValue(el, "Status");
    		    if(status.equals(STATUS_OPEN) || status.equals(STATUS_REOPENED))
    		    {
    		        openTasks.getAndIncrement();
    		    }
    		    else
    		    {
    		        closedTasks.getAndIncrement();
    		    }

    		    String severity = getValue(el, "Severity");
    		    if(severity.equals(SEV_TRIVIAL))
    		    {
    		        trivial++;
    		    }
    		    else if(severity.equals(SEV_MINOR))
    		    {
    		        minor++;
    		    }
    		    else if(severity.equals(SEV_NORMAL))
    		    {
    		        normal++;
    		    }
    		    else if(severity.equals(SEV_MAJOR))
    		    {
    		        major++;
    		    }
    		    else if(severity.equals(SEV_CRITICAL))
    		    {
    		        critical++;
    		    }
    		    else if(severity.equals(UNSET))
    		    {
    		        sevUnset++;
    		    }

    		    String taskType = getValue(el, "Type");
                if(taskType.equals(TYPE_CLARITY))
                {
                    clarity++;
                }
                else if(taskType.equals(TYPE_CODING_STANDARDS))
                {
                    standards++;
                }
                else if(taskType.equals(TYPE_IRRELEVANT))
                {
                    irrelevant++;
                }
                else if(taskType.equals(TYPE_MISSING))
                {
                    missing++;
                }
                else if(taskType.equals(TYPE_OPTIMIZATION))
                {
                    optimization++;
                }
                else if(taskType.equals(TYPE_OTHER))
                {
                    other++;
                }
                else if(taskType.equals(TYPE_PROGRAM_LOGIC))
                {
                    logic++;
                }
                else if(taskType.equals(TYPE_SUGGESTION))
                {
                    suggestion++;
                }
                else if(taskType.equals(UNSET))
                {
                    typeUnset++;
                }
                else if(taskType.equals(TYPE_USABILITY))
                {
                    usability++;
                }
		    }
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

	private void saveResults()
	{
        synchronized(taskTypes)
        {
            taskTypes.put(TYPE_CLARITY, taskTypes.get(TYPE_CLARITY) + clarity);
            taskTypes.put(TYPE_CODING_STANDARDS, taskTypes.get(TYPE_CODING_STANDARDS) + standards);
            taskTypes.put(TYPE_IRRELEVANT, taskTypes.get(TYPE_IRRELEVANT) + irrelevant);
            taskTypes.put(TYPE_MISSING, taskTypes.get(TYPE_MISSING) + missing);
            taskTypes.put(TYPE_OPTIMIZATION, taskTypes.get(TYPE_OPTIMIZATION) + optimization);
            taskTypes.put(TYPE_OTHER, taskTypes.get(TYPE_OTHER) + other);
            taskTypes.put(TYPE_PROGRAM_LOGIC, taskTypes.get(TYPE_PROGRAM_LOGIC) + logic);
            taskTypes.put(TYPE_SUGGESTION, taskTypes.get(TYPE_SUGGESTION) + suggestion);
            taskTypes.put(UNSET, taskTypes.get(UNSET) + typeUnset);
            taskTypes.put(TYPE_USABILITY, taskTypes.get(TYPE_USABILITY) + usability);
        }

        synchronized(severity)
        {
            severity.put(SEV_TRIVIAL, severity.get(SEV_TRIVIAL) + trivial);
            severity.put(SEV_MINOR, severity.get(SEV_MINOR) + minor);
            severity.put(SEV_NORMAL, severity.get(SEV_NORMAL) + normal);
            severity.put(SEV_MAJOR, severity.get(SEV_MAJOR) + major);
            severity.put(SEV_CRITICAL, severity.get(SEV_CRITICAL) + critical);
            severity.put(UNSET, severity.get(UNSET) + sevUnset);
        }
	}

	public void run()
	{
	    try
	    {
            parseXmlFile();
	    }
	    catch(SAXParseException sax)
	    {
	        return;
	    }
	    catch(Exception e)
	    {
	        System.out.println("Error occurred with file " + reviewFile.getAbsolutePath() + " : " + e);
	        return;
	    }
        parseDocument();
        saveResults();
	}

	public static void main(String[] args)
	{
	    File reviewDir = new File(args[0] + "/review/");
	    ArrayList<File> reviewFiles = getReviewFiles(reviewDir);

	    int totalThreads = reviewFiles.size();
	    ThreadPoolExecutor pool = new ThreadPoolExecutor(totalThreads, totalThreads, 30, TimeUnit.SECONDS,
	                                                     new ArrayBlockingQueue<Runnable>(totalThreads));
	    for(File reviewFile : reviewFiles)
	    {
	        PlotReviews plot = new PlotReviews(reviewFile);
    		try
    		{
    			pool.execute(plot);
    		}
    		catch(Exception	e)
    		{
    			e.printStackTrace();
    		}
	    }

	    do
	    {
	        if(pool.getCompletedTaskCount() == (long)totalThreads)
	        {
	            break;
	        }

	        try
	        {
    	        Thread.sleep(100);
	        }
	        catch(InterruptedException e)
	        {}
	    } while(true);

	    try
	    {
	        pool.shutdown();
	        pool.awaitTermination(5, TimeUnit.SECONDS);
	    }
	    catch(InterruptedException e)
	    {
	        System.out.println("Thread pool interrupted: " + e); 
	    }

	    for(Map.Entry<String, Float> entry : taskTypes.entrySet())
	    {
	        float percentage = entry.getValue() / totalTasks.floatValue() * 100;
	        taskTypes.put(entry.getKey(), percentage);
	    }

	    for(Map.Entry<String, Float> entry : severity.entrySet())
	    {
	        float percentage = entry.getValue() / totalTasks.floatValue() * 100;
	        severity.put(entry.getKey(), percentage);
	    }
        writeToDisk(args[0]);
	}

	private static void writeToDisk(String rootDir)
	{
	    // Write task type percentages chart
        DecimalFormat percentFormat = new DecimalFormat();
        BufferedWriter out;
        try
        {
            out = new BufferedWriter(new FileWriter(rootDir + "/build/codereview/codereview-tasks-clarity.properties"));
            out.write("YVALUE=");
            out.write(percentFormat.format(taskTypes.get(TYPE_CLARITY)));
            out.flush();
            out.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            out = new BufferedWriter(new FileWriter(rootDir + "/build/codereview/codereview-tasks-standards.properties"));
            out.write("YVALUE=");
            out.write(percentFormat.format(taskTypes.get(TYPE_CODING_STANDARDS)));
            out.flush();
            out.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            out = new BufferedWriter(new FileWriter(rootDir + "/build/codereview/codereview-tasks-irrelevant.properties"));
            out.write("YVALUE=");
            out.write(percentFormat.format(taskTypes.get(TYPE_IRRELEVANT)));
            out.flush();
            out.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            out = new BufferedWriter(new FileWriter(rootDir + "/build/codereview/codereview-tasks-missing.properties"));
            out.write("YVALUE=");
            out.write(percentFormat.format(taskTypes.get(TYPE_MISSING)));
            out.flush();
            out.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            out = new BufferedWriter(new FileWriter(rootDir + "/build/codereview/codereview-tasks-optimization.properties"));
            out.write("YVALUE=");
            out.write(percentFormat.format(taskTypes.get(TYPE_OPTIMIZATION)));
            out.flush();
            out.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
 
        try
        {
            out = new BufferedWriter(new FileWriter(rootDir + "/build/codereview/codereview-tasks-other.properties"));
            out.write("YVALUE=");
            out.write(percentFormat.format(taskTypes.get(TYPE_OTHER)));
            out.flush();
            out.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            out = new BufferedWriter(new FileWriter(rootDir + "/build/codereview/codereview-tasks-logic.properties"));
            out.write("YVALUE=");
            out.write(percentFormat.format(taskTypes.get(TYPE_PROGRAM_LOGIC)));
            out.flush();
            out.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            out = new BufferedWriter(new FileWriter(rootDir + "/build/codereview/codereview-tasks-suggestion.properties"));
            out.write("YVALUE=");
            out.write(percentFormat.format(taskTypes.get(TYPE_SUGGESTION)));
            out.flush();
            out.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            out = new BufferedWriter(new FileWriter(rootDir + "/build/codereview/codereview-tasks-unset.properties"));
            out.write("YVALUE=");
            out.write(percentFormat.format(taskTypes.get(UNSET)));
            out.flush();
            out.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            out = new BufferedWriter(new FileWriter(rootDir + "/build/codereview/codereview-tasks-usability.properties"));
            out.write("YVALUE=");
            out.write(percentFormat.format(taskTypes.get(TYPE_USABILITY)));
            out.flush();
            out.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        // Write severities chart
        try
        {
            out = new BufferedWriter(new FileWriter(rootDir + "/build/codereview/codereview-severity-trivial.properties"));
            out.write("YVALUE=");
            out.write(percentFormat.format(severity.get(SEV_TRIVIAL)));
            out.flush();
            out.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            out = new BufferedWriter(new FileWriter(rootDir + "/build/codereview/codereview-severity-minor.properties"));
            out.write("YVALUE=");
            out.write(percentFormat.format(severity.get(SEV_MINOR)));
            out.flush();
            out.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            out = new BufferedWriter(new FileWriter(rootDir + "/build/codereview/codereview-severity-normal.properties"));
            out.write("YVALUE=");
            out.write(percentFormat.format(severity.get(SEV_NORMAL)));
            out.flush();
            out.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
 
        try
        {
            out = new BufferedWriter(new FileWriter(rootDir + "/build/codereview/codereview-severity-major.properties"));
            out.write("YVALUE=");
            out.write(percentFormat.format(severity.get(SEV_MAJOR)));
            out.flush();
            out.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            out = new BufferedWriter(new FileWriter(rootDir + "/build/codereview/codereview-severity-critical.properties"));
            out.write("YVALUE=");
            out.write(percentFormat.format(severity.get(SEV_CRITICAL)));
            out.flush();
            out.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            out = new BufferedWriter(new FileWriter(rootDir + "/build/codereview/codereview-severity-unset.properties"));
            out.write("YVALUE=");
            out.write(percentFormat.format(severity.get(UNSET)));
            out.flush();
            out.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        // Plot the task statuses
        try
        {
            out = new BufferedWriter(new FileWriter(rootDir + "/build/codereview/codereview-status-open.properties"));
            out.write("YVALUE=");
            out.write(Integer.valueOf(openTasks.get()).toString());
            out.flush();
            out.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            out = new BufferedWriter(new FileWriter(rootDir + "/build/codereview/codereview-status-closed.properties"));
            out.write("YVALUE=");
            out.write(Integer.valueOf(closedTasks.get()).toString());
            out.flush();
            out.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            out = new BufferedWriter(new FileWriter(rootDir + "/build/codereview/codereview-status-total.properties"));
            out.write("YVALUE=");
            out.write(Integer.valueOf(totalTasks.get()).toString());
            out.flush();
            out.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
	}

	private static ArrayList<File> getReviewFiles(File directory)
	{
	    ArrayList<File> returnFiles = new ArrayList<File>();

	    File[] listFiles = directory.listFiles();
	    for(File file : listFiles)
	    {
	        if(file.isDirectory())
	        {
	            returnFiles.addAll(getReviewFiles(file));
	        }
	        else if(file.getName().endsWith(".review"))
	        {
	            returnFiles.add(file);
	        }
	    }
        return returnFiles;
	}
}

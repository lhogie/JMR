package jmr;

import java.util.List;

import j4u.CommandLine;
import toools.SystemMonitor;
import toools.exceptions.ExceptionUtilities;
import toools.io.Cout;
import toools.io.file.Directory;
import toools.io.file.RegularFile;

public class worker2 extends JMRCmd
{
	public worker2(RegularFile launcher)
	{
		super(launcher);
	}

	
	public static void main(String[] args) throws Throwable
	{
		System.getProperties().store(System.out, "");
		SystemMonitor.defaultMonitor.start();
		new worker2(null).run(args);
	}

	@Override
	public int runScript(CommandLine cmdLine)
	{
		
		List<String> parms = cmdLine.findParameters();

		if (parms.isEmpty())
		{
			printMessage(getHelp(false));
			return 1;
		}
		else
		{
			for (String path : parms)
			{
				FSMapReduce mr = new FSMapReduce<>(new Directory(path));

				while (mr.completionRatio() < 1)
				{
					processOneJob(mr);
				}
			}

			return 0;
		}
	}

	public static void processOneJob(MapReduce mr)
	{
		try
		{
			Job job = mr.extractNextJob();

			if (job != null)
			{
				Cout.progress("worker2 is now processing job " + job);
				mr.process(job);
			}
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			mr.logError(ExceptionUtilities.toString(e));
		}
	}

	@Override
	public String getShortDescription()
	{
		return "process one M/R computation";
	}
}

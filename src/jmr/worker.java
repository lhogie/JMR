package jmr;

import java.util.List;
import java.util.Random;

import j4u.CommandLine;
import toools.SystemMonitor;
import toools.exceptions.ExceptionUtilities;
import toools.io.Cout;
import toools.io.file.Directory;
import toools.io.file.RegularFile;
import toools.math.MathsUtilities;
import toools.thread.Threads;

public class worker extends JMRCmd
{
	public worker(RegularFile launcher)
	{
		super(launcher);
	}

	public static void main(String[] args) throws Throwable
	{
		SystemMonitor.defaultMonitor.start();
		new worker(null).run(args);
	}

	@Override
	public int runScript(CommandLine cmdLine)
	{
		Random prng = new Random();

		while (true)
		{
			if ( ! ActiveComputationFile.registredComputationsFile.exists())
			{
				Cout.debug("file does not exist, waiting 1s");
				Threads.sleepMs(1000);
				continue;
			}

			List<ComputationFileEntry> l = ActiveComputationFile.registredComputationsFile
					.readObject();

			if (l.isEmpty())
			{
				Cout.debug("nothing to do, waiting 1s");
				Threads.sleepMs(1000);
				continue;
			}

			double[] weights = new double[l.size()];

			for (int i = 0; i < weights.length; ++i)
			{
				weights[i] = l.get(i).priority;
			}

			double[] partialSums = MathsUtilities.partialSums(weights);
			
			if (partialSums[partialSums.length - 1] == 0)
			{
				Cout.debug("all computations have priority 0, waiting 1s");
				Threads.sleepMs(1000);
				continue;
			}
			
			int entryIndex = MathsUtilities.pick(partialSums, prng);
			ComputationFileEntry entry = l.get(entryIndex);
			Cout.info("processing " + entry);
			FSMapReduce mr = new FSMapReduce<>(new Directory(entry.path));

			processOneJob(mr);

			if (mr.completionRatio() == 1)
			{
				// set priority to 0 so that it won't be chosen next time
				entry.priority = 0;
				ActiveComputationFile.registredComputationsFile.setObject(l);
			}
		}
	}


	public static void processOneJob(MapReduce mr)
	{
		try
		{
			Job job = mr.extractNextJob();

			if (job != null)
			{
				Cout.progress("Processing job " + job);
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
		return "process all active Map/Reduce computations";
	}
}

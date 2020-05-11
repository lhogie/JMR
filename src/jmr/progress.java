package jmr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import j4u.CommandLine;
import toools.io.Cout;
import toools.io.file.Directory;
import toools.io.file.RegularFile;
import toools.math.MathsUtilities;
import toools.text.TextUtilities;

public class progress extends JMRCmd
{
	public static void main(String[] args) throws Throwable
	{
		new progress(null).run(args);
	}

	public progress(RegularFile launcher)
	{
		super(launcher);
		addOption("--pertencage", "-p", null, null, "print just percentage");
	}

	@Override
	public int runScript(CommandLine cmdLine) throws Throwable
	{
		for (String s : cmdLine.findParameters())
		{
			FSMapReduce mr = new FSMapReduce(new Directory(s));

			System.out.println(s + "\t" + 100 * mr.completionRatio() + "%");

			if (isOptionSpecified(cmdLine, "-m") && mr.getNbResults() > 0)
			{
				Cout.result(mr.getNbUnprocessed() + " jobs unprocessed");
				Cout.result(mr.getNbInprogress() + " jobs in progress");
				Cout.result(mr.getNbResults() + " results");

				System.out.println();
				Cout.result(
						"Duration: " + TextUtilities.seconds2date(mr.durationS(), true));

				Cout.result("Cumulated duration: "
						+ TextUtilities.seconds2date(mr.getCumulatedDurationS(), true));

				Cout.result("Progress ratio: " + 100 * mr.completionRatio() + "%");

				Cout.result("One result each: " + TextUtilities
						.seconds2date((int) (1d / mr.speedInResultPerSecond()), true));

				Cout.result("Expected total duration: "
						+ TextUtilities.seconds2date(mr.getExpectedDurationS(), true));

				Cout.result("Completion expected in: " + TextUtilities
						.seconds2date(mr.getExpectedDurationS() - mr.durationS(), true));

				Map<String, Integer> workers = mr.getWorkers();

				List<String> workerList = new ArrayList<>(workers.keySet());
				Collections.sort(workerList,
						(a, b) -> - Integer.compare(workers.get(a), workers.get(b)));

				Cout.result("nb of workers: " + workers.size());

				for (String w : workerList)
				{
					int n = workers.get(w);
					double p = 100f * n / mr.getNbResults();
					p = MathsUtilities.round(p, 1);
					Cout.result("\t" + w + "\t" + n + "\t" + p + "%");
				}

			}
		}

		return 0;
	}

	@Override
	public String getShortDescription()
	{
		return "prints progress info on map/reduce process";
	}
}

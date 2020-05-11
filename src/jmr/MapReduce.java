package jmr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import toools.SimpleCache;
import toools.io.Cout;
import toools.net.NetUtilities;
import toools.progression.LongProcess;
import toools.thread.Threads;

public abstract class MapReduce<L extends LocalResult, G extends GlobalResult<L>>
{
	public SimpleCache<List<L>> resultCache = new SimpleCache<List<L>>(null,
			() -> collectResults());

	private List<L> collectResults()
	{
		LongProcess lp = new LongProcess("scanning results", "result", getNbResults());
		List<L> l = new ArrayList<>();
		resultIterator().forEachRemaining(r -> {
			l.add(r);
			lp.sensor.progressStatus++;
		});
		lp.end();
		return l;
	}

	public long durationMs()
	{
		long start = Long.MAX_VALUE;
		long end = Long.MIN_VALUE;

		for (L l : resultCache.get())
		{
			if (l.computationStartDateMs < start)
				start = l.computationStartDateMs;

			if (l.computationEndDateMs > end)
				end = l.computationEndDateMs;
		}

		return end - start;
	}

	public long getCumulatedDurationS()
	{
		long s = 0;

		for (L r : resultCache.get())
		{
			s += r.durationMS() / 1000;
		}

		return s;
	}

	public long durationS()
	{
		return durationMs() / 1000;
	}

	public long getExpectedDurationS()
	{
		return (long) (durationS() / completionRatio());
	}

	public double completionRatio()
	{
		double target = getNbUnprocessed() + getNbInprogress() + getNbResults();
		return getNbResults() / target;
	}

	public double speedInResultPerSecond()
	{
		long dateOfFirstResult = Long.MAX_VALUE;
		long dateOfLastResult = Long.MIN_VALUE;
		int count = 1;

		for (L r : resultCache.get())
		{
			++count;
			long d = r.computationEndDateMs;

			if (d < dateOfFirstResult)
			{
				dateOfFirstResult = d;
			}

			if (d > dateOfLastResult)
			{
				dateOfLastResult = d;
			}
		}

		return (dateOfLastResult - dateOfFirstResult) / (double) count;
	}

	public void waitUntilCompletion()
	{
		double target = getNbUnprocessed() + getNbInprogress() + getNbResults();

		LongProcess p = new LongProcess("waiting for results", " job", target);

		while (getNbUnprocessed() + getNbInprogress() > 0)
		{
			p.sensor.progressStatus = getNbResults();
			Threads.sleepMs(1000);
		}

		p.end();
	}

	public abstract void addJob(Job<L> r);

	public abstract Job<L> extractNextJob();

	public abstract void deliver(Job<L> job, L localResult);

	public abstract boolean allRequestsSolved();

	public abstract long getNbUnprocessed();

	public abstract long getNbInprogress();

	public abstract long getNbResults();

	public abstract Iterator<L> resultIterator();

	public abstract long getDateOfFirstRequest();

	public abstract long getDateOfMoreRecentResult();

	public abstract GlobalResult<L> createEmptyGlobalOutput();

	public void process(Job<L> j) throws Throwable
	{
		final long startDate = System.currentTimeMillis();
		Cout.debugSuperVisible(j);
		L localResult = j.call();
		localResult.computationStartDateMs = startDate;
		localResult.computationEndDateMs = System.currentTimeMillis();
		localResult.workerName = NetUtilities.determineLocalHostName();
		localResult.oarJobID = System.getenv("OAR_JOB_ID");

		deliver(j, localResult);
		Cout.info("Completed");
	}

	protected abstract void logError(String string);

	public Map<String, Integer> getWorkers()
	{
		Map<String, Integer> m = new HashMap<>();

		for (L l : resultCache.get())
		{
			if (m.containsKey(l.workerName))
			{
				m.put(l.workerName, m.get(l.workerName) + 1);
			}
			else
			{
				m.put(l.workerName, 1);
			}

		}

		return m;
	}
}

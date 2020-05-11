package jmr;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import toools.math.Distribution;
import toools.text.TextUtilities;

public class DeliveryAgenda implements Serializable
{
	private final List<Delivery> list = new ArrayList<>();

	public static class Delivery implements Comparable<Delivery>, Serializable
	{
		final long date;
		final String workerName;

		public Delivery(long computationEndDate, String workerName)
		{
			this.date = computationEndDate;
			this.workerName = workerName;
		}

		@Override
		public int compareTo(Delivery o)
		{
			return Long.compare(date, o.date);
		}

		@Override
		public String toString()
		{
			return date + "\t " + workerName;
		}

	}

	public Distribution<String> getWorkerDistribution()
	{
		Distribution<String> workerDistribution = new Distribution<>();

		for (Delivery e : list)
			workerDistribution.addOccurence(e.workerName);

		return workerDistribution;
	}

	public void add(Delivery d)
	{
		int pos = Collections.binarySearch(list, d);
		list.add(pos < 0 ? - pos - 1 : pos, d);
	}

	@Override
	public String toString()
	{
		return toGant();
	}

	public String toList()
	{
		StringBuilder b = new StringBuilder();

		for (Delivery d : list)
		{
			b.append(d);
			b.append('\n');
		}

		return b.toString();
	}

	public List<String> getWorkers()
	{
		List<String> l = new ArrayList<>();

		for (Delivery d : list)
		{
			if ( ! l.contains(d.workerName))
			{
				l.add(d.workerName);
			}
		}

		return l;
	}

	public static String workerIDS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789@#&()*$€?+=%£";

	public String toGant()
	{
		StringBuilder b = new StringBuilder();
		List<String> workers = new ArrayList<>(getWorkers());

		for (int i = 0; i < workers.size(); ++i)
		{
			char workerID = workerIDS.charAt(i);

			b.append(workerID + " => " + list.get(i).workerName + "\n");
		}

		b.append("\n");
		int i = 0;
		long now = System.currentTimeMillis();

		for (Delivery d : list)
		{
			int workerIndex = workers.indexOf(d.workerName);
			char workerID = workerIDS.charAt(workerIndex);
			String longAgo = TextUtilities.seconds2date((now - d.date) / 1000, true);

			b.append(TextUtilities.flushLeft(longAgo, 20, ' '));
			b.append(TextUtilities.flushRight(i++ + " ", 10, ' '));
			b.append("[");
			b.append(TextUtilities.repeat(' ', workerIndex));
			b.append(workerID);
			b.append(TextUtilities.repeat(' ', workers.size() - workerIndex - 1));
			b.append("]\n");
		}

		return b.toString();
	}

	public DeliveryAgenda getAgenda(String worker)
	{
		DeliveryAgenda a = new DeliveryAgenda();
		a.list.addAll(list);
		a.list.removeIf(e -> ! e.workerName.equals(worker));
		return a;
	}

	public boolean isStillActive(String worker)
	{
		long avgDlvIntervalMs = averageDeliveryIntervalMs();
		DeliveryAgenda agenda = getAgenda(worker);

		if (agenda.getNbEntries() == 0)
			throw new IllegalStateException("agenda is empty");

		Delivery lastEntry = agenda.getEntry(agenda.getNbEntries() - 1);

		return lastEntry.date + 2 * avgDlvIntervalMs < System.currentTimeMillis();
	}

	public Delivery getEntry(int i)
	{
		return list.get(i);
	}

	public int getNbEntries()
	{
		return list.size();
	}

	public Set<String> findActiveWorkers()
	{
		Set<String> r = new HashSet<>();

		for (String w : getWorkers())
		{
			if (isStillActive(w))
			{
				r.add(w);
			}
		}

		return r;
	}

	public long averageDeliveryIntervalMs()
	{
		List<String> workers = getWorkers();

		if (workers.isEmpty())
		{
			throw new IllegalStateException("empty agenda");
		}
		else if (workers.size() == 1)
		{
			long sum = 0;

			for (int i = 1; i < list.size(); ++i)
			{
				sum += list.get(i).date - list.get(i - 1).date;
			}

			return sum / (list.size() - 1);
		}
		else
		{
			long r = 0;

			for (String w : workers)
			{
				r += getAgenda(w).averageDeliveryIntervalMs();
			}

			return r / workers.size();
		}

	}
}

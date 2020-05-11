package jmr;

import java.util.Iterator;

import jmr.DeliveryAgenda.Delivery;
import toools.io.Cout;
import toools.text.TextUtilities;
import toools.text.json.JSONMap;

public abstract class GlobalResult<L extends LocalResult> extends Result
{
	final static long serialVersionUID = 76543;

	public DeliveryAgenda agenda = new DeliveryAgenda();
	public long cumulatedComputationTimeMs = 0;
	public double completionRatio;

	public void merge(L r)
	{
		if (computationStartDateMs == - 1)
		{
			computationStartDateMs = r.computationStartDateMs;
		}
		else if (r.computationStartDateMs < computationStartDateMs)
		{
			computationStartDateMs = r.computationStartDateMs;
		}

		if (computationEndDateMs == - 1)
		{
			computationEndDateMs = r.computationEndDateMs;
		}
		else if (r.computationEndDateMs > computationEndDateMs)
		{
			computationEndDateMs = r.computationEndDateMs;
		}

		computationEndDateMs = Math.max(computationEndDateMs, r.computationEndDateMs);
		cumulatedComputationTimeMs += r.durationMS();
		agenda.add(new Delivery(r.computationEndDateMs, r.workerName));
	}

	public long getCumulatedCompuationTimeMS()
	{
		return cumulatedComputationTimeMs;
	}

	@Override
	public String toString()
	{
		if (completionRatio == 0)
		{
			Cout.result("Nothing done yet!");
			return null;
		}
		else
		{
			String s = "";
			s += "Started " + (TextUtilities.seconds2date(
					(System.currentTimeMillis() - computationStartDateMs) / 1000, true))
					+ " ago";
			s += "Progress ratio: " + 100 * completionRatio + "%";
			s += "\n" + agenda.getNbEntries() + " result merged";
			s += "\nDuration: " + TextUtilities.seconds2date(durationMS() / 1000, true);
			s += "\nCumulated computation time: "
					+ TextUtilities.seconds2date(cumulatedComputationTimeMs / 1000, true);
			return s;
		}
	}

	@Override
	public JSONMap toJSONElement()
	{
		JSONMap m = super.toJSONElement();
		m.add("cumulatedComputationTimeMs", cumulatedComputationTimeMs);
		m.add("completionRatio", completionRatio);
		return m;
	}

	public void merge(Iterator<L> i)
	{
		while (i.hasNext())
		{
			merge(i.next());
		}
	}
}

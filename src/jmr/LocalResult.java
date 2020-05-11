package jmr;

import toools.text.TextUtilities;
import toools.text.json.JSONMap;

public abstract class LocalResult extends Result
{
	final static long serialVersionUID = 76543;

	public String workerName;
	public String oarJobID;
	protected final transient Job job;

	public LocalResult(Job j)
	{
		this.job = j;
	}

	@Override
	public JSONMap toJSONElement()
	{
		JSONMap m = super.toJSONElement();
		m.add("workerName", workerName);

		if (oarJobID != null)
			m.add("oarJobID", oarJobID);

		return m;
	}

	@Override
	public String toString()
	{
		String s = "";
		s += "computed from " + TextUtilities.seconds2date(computationStartDateMs, true)
				+ " to " + TextUtilities.seconds2date(computationStartDateMs, true)
				+ "s by worker " + workerName;
		return s;
	}
}

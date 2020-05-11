package jmr;

import java.io.Serializable;

import toools.io.file.Directory;
import toools.io.file.ObjectFile;
import toools.io.file.RegularFile;
import toools.text.json.JSONMap;
import toools.text.json.JSONable;

public abstract class Result implements Serializable, JSONable
{
	final static long serialVersionUID = 76543;

	public long computationStartDateMs = - 1;
	public long computationEndDateMs = - 1;

	public long durationMS()
	{
		if (computationStartDateMs == - 1)
			throw new IllegalStateException();

		if (computationEndDateMs == - 1)
			throw new IllegalStateException();

		return computationEndDateMs - computationStartDateMs;
	}

	public void saveTo(Directory d)
	{
		new ObjectFile<>(d, "result.ser").setObject(this);

		new RegularFile(d, "result.json")
				.setContent(toJSONElement().toString().getBytes());
	}

	protected abstract boolean isAlreadySaved(Directory d);

	@Override
	public JSONMap toJSONElement()
	{
		JSONMap m = new JSONMap();
		m.add("computationStartDateMs", computationStartDateMs);
		m.add("computationEndDateMs", computationEndDateMs);
		return m;
	}

	@Override
	public String toString()
	{
		return toJSONElement().toString();
	}

}

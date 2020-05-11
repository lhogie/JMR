package jmr;

import java.io.Serializable;

public class ComputationFileEntry implements Serializable
{
	private static final long serialVersionUID = 954407486321942059L;

	String path;
	double priority = 1;

	@Override
	public String toString()
	{
		return path + " (priority: " + priority + ")";
	}

	@Override
	public int hashCode()
	{
		return path.hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof ComputationFileEntry && equals((ComputationFileEntry) obj);
	}

	public boolean equals(ComputationFileEntry e)
	{
		return e.path.equals(path);
	}
}
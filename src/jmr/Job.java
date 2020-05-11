package jmr;

import java.io.Serializable;

import toools.io.file.ObjectFile;

public abstract class Job<L extends LocalResult> implements Serializable
{
	static final long serialVersionUID = 5939282;

	protected transient ObjectFile<Job<L>> requestFile;
	// protected transient FSMapReduce<L> mapReduce;

	public  String name;

	// required for deserialization
	public Job()
	{
	}
	
	public Job(String name)
	{
		this.name = name;
	}

	public Job(ObjectFile<Job<L>> requestFile)
	{
		this.requestFile = requestFile;
		this.name = requestFile.getName();
	}

	protected abstract L call() throws Exception;

	@Override
	public String toString()
	{
		return name;
	}

}

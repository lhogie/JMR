package jmr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class RAMMapReduce<L extends LocalResult, G extends GlobalResult<L>>
		extends MapReduce<L, G>
{
	List<Job<L>> jobs = new ArrayList<>();
	Map<Job<L>, L> resuls = new HashMap<>();
	
	@Override
	public void addJob(Job<L> r)
	{
		jobs.add(r);
	}

	@Override
	public Job<L> extractNextJob()
	{
		return jobs.remove(0);
	}

	@Override
	public void deliver(Job<L> job, L localResult)
	{
		resuls.put(job, localResult);
	}

	@Override
	public boolean allRequestsSolved()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public long getNbUnprocessed()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getNbInprogress()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getNbResults()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Iterator<L> resultIterator()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getDateOfFirstRequest()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getDateOfMoreRecentResult()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public GlobalResult<L> createEmptyGlobalOutput()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void logError(String string)
	{
		// TODO Auto-generated method stub
		
	}

}

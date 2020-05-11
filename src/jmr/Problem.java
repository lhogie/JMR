package jmr;

import java.util.ArrayList;
import java.util.List;

public abstract class Problem<L extends LocalResult, G extends GlobalResult<L>>
{
	public abstract int size();

	public abstract Class<G> getResultClass();

	public abstract Job<L> createJob(int start, int end);

	public List<Job<L>> createSubProblems(int nbJobs, MapReduce<L, G> mr)
	{
		int sz = size();

		if (nbJobs > sz)
			throw new IllegalArgumentException("nbJobs > problem size");

		List<Job<L>> r = new ArrayList<>();
		int jobLength = sz / nbJobs;

		for (int i = 0; i < nbJobs; ++i)
		{
			int start = jobLength * i;
			int end = i < nbJobs - 1 ? jobLength * (i + 1) : sz;
			Job<L> j = createJob(start, end);
			r.add(j);
		}

		return r;
	}
}

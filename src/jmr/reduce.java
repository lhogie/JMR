package jmr;

import toools.io.Cout;
import toools.io.file.Directory;

public class reduce
{
	public static <L extends LocalResult, G extends GlobalResult<L>> void main(
			String[] args)
	{
		FSMapReduce<L, G> mapReduce = new FSMapReduce<>(new Directory(args[0]));

		if (mapReduce.getNbResults() == 0)
		{
			Cout.result("Nothing done yet!");
		}
		else
		{
			G gr = mapReduce.createEmptyGlobalOutput();

			for (L r : mapReduce.resultCache.get())
			{
				gr.merge(r);
			}

			Cout.result(gr);
		}
	}
}

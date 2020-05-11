package jmr;

import jmr.DeliveryAgenda.Delivery;
import toools.io.Cout;
import toools.io.file.Directory;

public class gant
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
			DeliveryAgenda agenda = new DeliveryAgenda();

			for (L r : mapReduce.resultCache.get())
			{
				agenda.add(new Delivery(r.computationEndDateMs, r.workerName));
			}

			Cout.result(agenda.toGant());
		}
	}
}

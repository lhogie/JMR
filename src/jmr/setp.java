package jmr;

import java.util.List;

import j4u.CommandLine;
import toools.io.file.RegularFile;

public class setp extends JMRCmd
{
	public static void main(String[] args) throws Throwable
	{
		new setp(null).run(args);
	}

	public setp(RegularFile launcher)
	{
		super(launcher);
	}

	@Override
	public int runScript(CommandLine cmdLine) throws Throwable
	{
		List<String> parms = cmdLine.findParameters();
		double p = Double.valueOf(parms.get(parms.size() -1));
		
		for (int i = 0; i < parms.size() - 1; ++i)
		{
			int index = Integer.valueOf(parms.get(i));
			ActiveComputationFile.registredComputationsFile.setPriority(index, p);
		}

		return 0;
	}

	@Override
	public String getShortDescription()
	{
		return "add a computution to the list of registered ones";
	}

}

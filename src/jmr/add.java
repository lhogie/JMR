package jmr;

import java.util.List;

import j4u.CommandLine;
import toools.io.file.RegularFile;

public class add extends JMRCmd
{
	public static void main(String[] args) throws Throwable
	{
		new add(null).run(args);
	}

	public add(RegularFile launcher)
	{
		super(launcher);
	}

	@Override
	public int runScript(CommandLine cmdLine)
	{
		List<String> parms = cmdLine.findParameters();

		for (String n : parms)
		{
			ActiveComputationFile.registredComputationsFile.add(n, 1);
		}

		return 0;
	}

	@Override
	public String getShortDescription()
	{
		return "registers a new computation";
	}

}

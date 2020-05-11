package jmr;

import java.util.List;

import j4u.CommandLine;
import toools.io.file.RegularFile;

public class del extends JMRCmd
{
	public static void main(String[] args) throws Throwable
	{
		new del(null).run(args);
	}

	public del(RegularFile launcher)
	{
		super(launcher);
		addOption("--all", "-a", null, null, "removes all entries");
	}

	@Override
	public int runScript(CommandLine cmdLine) throws Throwable
	{
		if (isOptionSpecified(cmdLine, "--all"))
		{
			ActiveComputationFile.registredComputationsFile.clear();
		}
		else
		{
			List<String> parms = cmdLine.findParameters();
			
			for (String s : parms)
			{
				int index = Integer.valueOf(s);
				ActiveComputationFile.registredComputationsFile.removeEntry(index);
			}
		}

		return 0;
	}

	@Override
	public String getShortDescription()
	{
		return "adds a computution to the list of registered ones";
	}

}

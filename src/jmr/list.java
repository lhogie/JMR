package jmr;

import java.util.List;

import j4u.CommandLine;
import toools.io.file.Directory;
import toools.io.file.RegularFile;
import toools.spreadsheet.SpreadSheet;
import toools.text.TextUtilities.HORIZONTAL_ALIGNMENT;

public class list extends JMRCmd
{
	public static void main(String[] args) throws Throwable
	{
		new list(null).run(args);
	}

	public list(RegularFile launcher)
	{
		super(launcher);
	}

	@Override
	public int runScript(CommandLine cmdLine) throws Throwable
	{
		if ( ! ActiveComputationFile.registredComputationsFile.exists())
		{
			printMessage("No map/reduce computation known");
		}
		else
		{
			List<ComputationFileEntry> entries = ActiveComputationFile.registredComputationsFile
					.readObject();

			if (entries.isEmpty())
			{
				printMessage("No computation in progress.");
			}
			else
			{
				SpreadSheet ss = new SpreadSheet(entries.size(), 4);
				ss.getColumnProperty(0).setTitle("index");
				ss.getColumnProperty(1).setTitle("priority");
				ss.getColumnProperty(2).setTitle("completion");
				ss.getColumnProperty(3).setA(HORIZONTAL_ALIGNMENT.CENTER);
				ss.getColumnProperty(3).setTitle("path");
				ss.getColumnProperty(3).setA(HORIZONTAL_ALIGNMENT.LEFT);

				for (int i = 0; i < entries.size(); ++i)
				{
					ComputationFileEntry entry = entries.get(i);

					FSMapReduce mr = new FSMapReduce(new Directory(entry.path));

					if (mr.processedDir.exists())
					{
						ss.set(i, 0, i);
						ss.set(i, 1, entry.priority);
						ss.set(i, 2, (100 * mr.completionRatio()) + "%");
						ss.set(i, 3, entry.path);
					}
					else
					{
						ss.set(i, 0, i);
						ss.set(i, 2, "(not found)");
						ss.set(i, 3, entry.path);
					}
				}

				System.out.println(ss.toString(true));
			}
		}

		return 0;
	}

	@Override
	public String getShortDescription()
	{
		return "list active compututions";
	}

}

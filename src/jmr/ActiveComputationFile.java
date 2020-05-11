package jmr;

import java.util.ArrayList;
import java.util.List;

import toools.io.file.Directory;
import toools.io.file.ObjectFile;

public class ActiveComputationFile extends ObjectFile<List<ComputationFileEntry>>
{
	public final static ActiveComputationFile registredComputationsFile = new ActiveComputationFile(
			new Directory(Directory.getHomeDirectory() + "/.jmr"),
			"running_computations.lst");

	public ActiveComputationFile(Directory d, String name)
	{
		super(d, name);

		if ( ! exists())
		{
			if ( ! getParent().exists())
				getParent().mkdirs();

			setObject(new ArrayList<ComputationFileEntry>());
		}
	}

	public void add(String path, double priority)
	{
		ComputationFileEntry entry = new ComputationFileEntry();
		entry.path = new Directory(path).getPath();
		entry.priority = priority;
		add(entry);
	}

	public int contains(String path)
	{
		int i = 0;

		for (ComputationFileEntry e : readObject())
		{
			if (e.path.equals(path))
				return i;

			++i;
		}

		return - 1;
	}

	public void add(ComputationFileEntry e)
	{
		List<ComputationFileEntry> list = exists() ? readObject() : new ArrayList<>();

		if (list.contains(e))
			throw new IllegalStateException(this + " is already registered");

		list.add(e);

		if ( ! getParent().exists())
			getParent().mkdirs();

		setObject(list);
	}

	public void removeEntry(int index)
	{
		if ( ! getParent().exists())
			throw new IllegalStateException(this + " does not exist");

		List<ComputationFileEntry> list = readObject();
		list.remove(index);
		setObject(list);
	}

	public void clear()
	{
		setObject(new ArrayList<>());
	}

	private ComputationFileEntry read(int i)
	{
		if ( ! getParent().exists())
			throw new IllegalStateException(this + " does not exist");

		return readObject().get(i);
	}

	public String get(int i)
	{
		return read(i).path;
	}

	public static int find(List<ComputationFileEntry> list, String name)
	{
		for (int i = 0; i < list.size(); ++i)
		{
			ComputationFileEntry e = list.get(i);

			if (e.path.equals(name))
			{
				return i;
			}
		}

		return - 1;
	}

	public void setPriority(int index, double p)
	{
		List<ComputationFileEntry> list = readObject();
		ComputationFileEntry e = list.get(index);
		e.priority = p;
		setObject(list);
	}

}

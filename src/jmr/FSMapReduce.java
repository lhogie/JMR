package jmr;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Random;

import toools.io.Cout;
import toools.io.IORuntimeException;
import toools.io.file.Directory;
import toools.io.file.ObjectFile;
import toools.io.file.RegularFile;
import toools.io.ser.JavaSerializer;
import toools.progression.LongProcess;
import toools.reflect.Clazz;
import toools.thread.Generator;
import toools.thread.Threads;

public class FSMapReduce<L extends LocalResult, G extends GlobalResult<L>>
		extends MapReduce<L, G>
{
	public final Directory baseDirectory, unprocessedDir, inProgressDir, resultDir,
			processedDir;
	public final RegularFile globalResultClassFile;

	public FSMapReduce(Directory d)
	{
		baseDirectory = d;

		unprocessedDir = new Directory(baseDirectory, "requests/unprocessed");
		inProgressDir = new Directory(baseDirectory, "requests/in_progress");
		processedDir = new Directory(baseDirectory, "requests/completed");
		resultDir = new Directory(baseDirectory, "results");
		this.globalResultClassFile = new RegularFile(baseDirectory,
				"global_result_class_name.txt");
	}

	public boolean allRequestsSolved()
	{
		return unprocessedDir.isEmpty() && inProgressDir.isEmpty();
	}

	public long getNbResults()
	{
		return resultDir.getNbFiles();
	}

	@Override
	public long getNbUnprocessed()
	{
		return unprocessedDir.getNbFiles();
	}

	@Override
	public long getNbInprogress()
	{
		return inProgressDir.getNbFiles();
	}

	@Override
	public void addJob(Job<L> job)
	{
		RegularFile f = new RegularFile(unprocessedDir, job.name);

		if (f.exists())
			throw new IllegalStateException("job file already exists: " + f);

		f.setContent(JavaSerializer.getDefaultSerializer().toBytes(job));
	}

	public Iterator<L> resultIterator()
	{
		return new Generator()
		{
			@Override
			public void produce()
			{
				try
				{
					DirectoryStream<Path> s = Files
							.newDirectoryStream(resultDir.javaFile.toPath());
					Iterator<Path> i = s.iterator();

					while (i.hasNext())
					{
						try
						{
							Path path = i.next();
							ObjectFile<L> f = getFile(path);

							if (f != null)
							{
//								Cout.progress("reading " + f);
								L r = f.readObject();
								deliver(r);
							}

							if ( ! i.hasNext())
								s.close();
						}
						catch (IOException e)
						{
							throw new IORuntimeException(e);
						}
					}
				}

				catch (IOException e)
				{
					throw new IORuntimeException(e);
				}
			}

			private ObjectFile<L> getFile(Path path)
			{
				if (Files.isDirectory(path))
				{
					Directory d = new Directory(path.toString());
					return new ObjectFile<>(d, "result.ser");
				}
				else if (path.getFileName().endsWith(".ser"))
				{
					return new ObjectFile<>(path.toString());
				}

				return null;
			}
		}.iterator();

	}

	@Override
	public long getDateOfFirstRequest()
	{
		try
		{
			long oldest = Long.MAX_VALUE;

			for (Path p : Files.newDirectoryStream(processedDir.javaFile.toPath()))
			{
				if (p.toFile().lastModified() < oldest)
				{
					oldest = p.toFile().lastModified();
				}
			}

			return oldest;
		}
		catch (IOException e)
		{
			throw new IORuntimeException(e);
		}
	}

	@Override
	public long getDateOfMoreRecentResult()
	{
		try
		{
			long newest = 0;

			for (Path p : Files.newDirectoryStream(processedDir.javaFile.toPath()))
			{
				if (p.toFile().lastModified() > newest)
				{
					newest = p.toFile().lastModified();
				}
			}

			return newest;
		}
		catch (IOException e)
		{
			throw new IORuntimeException(e);
		}
	}

	public void createDirectoryTree(Class<G> globalResultClass)
	{
		baseDirectory.create();
		unprocessedDir.create();
		inProgressDir.create();
		resultDir.create();
		processedDir.create();
		globalResultClassFile.setContent(globalResultClass.getName().getBytes());
	}

	public G createEmptyGlobalOutput()
	{
		Class<GlobalResult<L>> c = Clazz
				.findClassOrFail(globalResultClassFile.getContentAsText().trim());
		return (G) Clazz.makeInstanceOrFail(c);
	}

	public Job<L> extractNextJob()
	{
		ObjectFile<Job<L>> file = pick(unprocessedDir);

		// there is no request waiting in the unprocessed directory
		if (file == null)
		{
			// and none either in the in_progress directory
			if ((file = pick(inProgressDir)) == null)
			{
				return null;
			}
			else
			{
				Job<L> j = file.readObject();
				// no need to move, it's already in the in_progress dir
				j.requestFile = file;
				return j;
			}
		}
		else
		{
			file.moveTo(inProgressDir, false);
			Job<L> j = file.readObject();
			j.requestFile = new ObjectFile<>(inProgressDir, file.getName());
			return j;
		}
	}

	private <R extends Serializable> ObjectFile<R> pick(Directory d)
	{
		if (d.exists())
		{
			String requestFileName = d.pickOneFileOrNull(new Random());

			if (requestFileName == null)
			{
				return null;
			}
			else
			{
				return new ObjectFile<>(d, requestFileName);
			}
		}
		else
		{
			return null;
		}
	}

	@Override
	public void deliver(Job<L> job, L localResult)
	{
		// another worker has computed it before
		if (localResult.isAlreadySaved(resultDir))
		{
			Cout.info("Done. Skipping writing result to " + resultDir
					+ ". The result file is already here.");
		}
		else
		{
			Cout.info("Done. Writing result to " + resultDir);
			localResult.saveTo(resultDir);
			job.requestFile.moveTo(processedDir, false);
		}
	}

	public G execute(Problem<L, G> p, int nbJobs, boolean waitUntilCompletion,
			boolean processLocally)
	{
		if (nbJobs < 1)
			throw new IllegalArgumentException("invalid number of jobs " + nbJobs);

		createJobs(p, nbJobs);

		if (processLocally)
		{
			new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					while (completionRatio() < 1)
					{
						worker.processOneJob(FSMapReduce.this);
					}
				}
			}).start();
		}
		else
		{
			int i = ActiveComputationFile.registredComputationsFile
					.contains(baseDirectory.getPath());

			if (i >= 0)
			{
				ActiveComputationFile.registredComputationsFile.setPriority(i, 1);
			}
			else
			{
				ActiveComputationFile.registredComputationsFile
						.add(baseDirectory.getPath(), 1);
			}
		}


		if (waitUntilCompletion)
		{
			LongProcess lp = new LongProcess("Map/Reduce", " job", 1000);
			
			while (true)
			{
				double r = completionRatio();
				lp.sensor.progressStatus = r * 1000;

				if (r == 1)
					break;

				Threads.sleepMs(1000);
			}

			lp.end();
		}

		double completionRatio = completionRatio();
		G r = createEmptyGlobalOutput();
		r.merge(resultIterator());
		r.completionRatio = completionRatio;
		r.saveTo(baseDirectory);
		return r;
	}

	private void createJobs(Problem<L, G> p, int nbJobs)
	{
		if ( ! baseDirectory.exists())
		{
			LongProcess lp = new LongProcess(
					"generating " + nbJobs + " job files at " + baseDirectory.getPath(),
					" job", nbJobs);

			createDirectoryTree(p.getResultClass());

			for (Job<L> j : p.createSubProblems(nbJobs, this))
			{
				addJob(j);
				++lp.sensor.progressStatus;
			}

			lp.end();
		}
	}

	@Override
	protected void logError(String s)
	{
		// log the error
		RegularFile errFile = new RegularFile(baseDirectory, "errors.txt");
		PrintStream os = new PrintStream(errFile.createWritingStream(true, 128));
		os.println(s);
		os.close();
	}

}

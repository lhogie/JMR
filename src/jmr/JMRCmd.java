package jmr;

import j4u.CommandLineApplication;
import j4u.License;
import toools.io.file.RegularFile;

public abstract class JMRCmd extends CommandLineApplication
{

	public JMRCmd(RegularFile launcher)
	{
		super(launcher);
	}


	@Override
	public String getApplicationName()
	{
		return "jmr";
	}

	@Override
	public String getAuthor()
	{
		return "Luc Hogie";
	}

	@Override
	public License getLicence()
	{
		return License.ApacheLicenseV2;
	}

	@Override
	public String getYear()
	{
		return "2017-2018";
	}
}

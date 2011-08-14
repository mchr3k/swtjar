package org.swtjar.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class SWTJarTask extends Task
{
  private String targetmainclass = null;
  private String targetjar = null;
  private String swtversion = null;

  public void setTargetmainclass(String targetmainclass)
  {
    this.targetmainclass = targetmainclass;
  }

  public void setTargetjar(String targetjar)
  {
    this.targetjar = targetjar;
  }

  public void setSwtversion(String swtversion)
  {
    this.swtversion = swtversion;
  }

  @Override
  public void execute() throws BuildException
  {
    if (targetmainclass == null)
    {
      throw new BuildException("Must specify targetmainclass");
    }

    if (targetjar == null)
    {
      throw new BuildException("Must specify targetjar");
    }

    if (swtversion == null)
    {
      throw new BuildException("Must specify swtversion");
    }
  }
}

package org.swtjar.ant;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class EmmaCheckTask extends Task
{
  private File coveragefile;
  private File metadatafile;
  private File requiredcoverage;
  private File outputfile;
  private String overallcoverage;

  public void setOverallcoverage(String overallcoverage)
  {
    this.overallcoverage = overallcoverage;    
  }
  
  public void setCoveragefile(File coveragefile)
  {
    this.coveragefile = coveragefile;
  }

  public void setMetadatafile(File metadatafile)
  {
    this.metadatafile = metadatafile;
  }

  public void setRequiredcoverage(File requiredcoverage)
  {
    this.requiredcoverage = requiredcoverage;
  }
  
  public void setOutput(File output)
  {
    this.outputfile = output;
  }

  @Override
  public void execute() throws BuildException
  {
    try
    {
      if (coveragefile == null)
      {
        throw new BuildException("Must specify coveragefile");
      }
      
      if (metadatafile == null)
      {
        throw new BuildException("Must specify metadatafile");
      }
      
      if (outputfile == null)
      {
        throw new BuildException("Must specify outputfile");
      }
      
      Properties coverageProps = new Properties();
      if (requiredcoverage != null)
      {
        coverageProps.load(new FileInputStream(requiredcoverage));
      }
    }
    catch (IOException ex)
    {
      throw new BuildException(ex);
    }
  }
}

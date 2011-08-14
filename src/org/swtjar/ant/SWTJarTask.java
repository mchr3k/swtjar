package org.swtjar.ant;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Jar;
import org.apache.tools.ant.taskdefs.Manifest;
import org.apache.tools.ant.taskdefs.Manifest.Attribute;
import org.apache.tools.ant.taskdefs.ManifestException;
import org.apache.tools.ant.types.ZipFileSet;
import org.swtjar.SWTLoader;

public class SWTJarTask extends Jar
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

    try
    {
      Manifest swtAttrs = new Manifest();
      swtAttrs.getMainSection().addConfiguredAttribute(new Attribute(SWTLoader.SWTJAR_JAR, targetjar));
      swtAttrs.getMainSection().addConfiguredAttribute(new Attribute(SWTLoader.SWTJAR_MAIN_CLASS, targetmainclass));
      swtAttrs.getMainSection().addConfiguredAttribute(new Attribute(SWTLoader.SWTJAR_VERSION, swtversion));
      swtAttrs.getMainSection().addConfiguredAttribute(new Attribute("Main-Class", SWTLoader.class.getName()));
      addConfiguredManifest(swtAttrs);
    }
    catch (ManifestException ex)
    {
      throw new BuildException("Failed to construct SWT manifest: " + ex.getMessage());
    }

    ZipFileSet zset = new ZipFileSet();
    String jarFile = getJarFilePath();
    System.out.println(jarFile);
    zset.setSrc(new File(jarFile));
    zset.setIncludes("**/jarinjarloader/**/*.class,**/SWTLoader*.class");
    addZipfileset(zset);

    super.execute();
  }

  private String getJarFilePath()
  {
    // get name and path
    String name = getClass().getName().replace('.', '/');
    name = getClass().getResource("/" + name + ".class").toString();

    // remove junk
    int jarIndex = name.indexOf(".jar");
    if (jarIndex < 0)
    {
      throw new BuildException("This class must be run from within a JAR file");
    }
    name = name.substring(0, jarIndex + ".jar".length());
    name = name.substring(name.lastIndexOf(':') - 1).replace('%', ' ');

    // remove escape characters
    String s = "";
    for (int k = 0; k < name.length(); k++)
    {
      s += name.charAt(k);
      if (name.charAt(k) == ' ')
        k += 2;
    }
    // replace '/' with system separator char
    return s.replace('/', File.separatorChar);
  }

}

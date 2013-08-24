/*******************************************************************************
* Copyright (c) 2011-2012 mchr3k
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* mchr3k - initial API and implementation
*******************************************************************************/
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
  private String swtversion = null;

  public void setTargetmainclass(String targetmainclass)
  {
    this.targetmainclass = targetmainclass;
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

    if (swtversion == null)
    {
      throw new BuildException("Must specify swtversion");
    }

    try
    {
      Manifest swtAttrs = new Manifest();
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

    // clean up result
    int jarIndex = name.indexOf(".jar");
    if (jarIndex < 0)
    {
      throw new BuildException("This class must be run from within a JAR file");
    }
    name = name.substring(0, jarIndex + ".jar".length());
    name = name.substring(name.lastIndexOf(':') + 1);
    name = name.replace("%20", " ");
    name = name.replace('%', ' ');
    name = name.replace('/', File.separatorChar);
    return name;
  }

}

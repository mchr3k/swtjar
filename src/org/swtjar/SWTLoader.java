package org.swtjar;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.eclipse.jdt.internal.jarinjarloader.RsrcURLStreamHandlerFactory;

public class SWTLoader
{
  public static final String SWTJAR_MAIN_CLASS = "SwtJar-TargetMainClass";
  public static final String SWTJAR_VERSION = "SwtJar-SwtVersion";

  private static String sTargetMainClass = null;
  private static String sSwtVersion = null;

  public static void main(String[] args) throws Throwable
  {
    try
    {
      loadConfig();
      ClassLoader cl = getSWTClassloader();
      Thread.currentThread().setContextClassLoader(cl);
      try
      {
        try
        {
          System.err.println("Launching UI ...");
          Class<?> c = Class.forName(sTargetMainClass, true, cl);
          Method main = c.getMethod("main", new Class[]{args.getClass()});
          main.invoke((Object)null, new Object[]{args});
        }
        catch (InvocationTargetException ex)
        {
          Throwable th = ex.getCause();
          if (th instanceof UnsatisfiedLinkError)
          {
            UnsatisfiedLinkError linkError = (UnsatisfiedLinkError)th;
            String errorMessage = "(UnsatisfiedLinkError: " + linkError.getMessage() + ")";
            String arch = getArch();
            if ("32".equals(arch))
            {
              errorMessage += "\nTry adding '-d64' to your command line arguments";
            }
            else if ("64".equals(arch))
            {
              errorMessage += "\nTry adding '-d32' to your command line arguments";
            }
            throw new SWTLoadFailed(errorMessage);
          }
          else if ((th.getMessage() != null) &&
                    th.getMessage().toLowerCase().contains("invalid thread access"))
          {
            String errorMessage = "(SWTException: Invalid thread access)";
            errorMessage += "\nTry adding '-XstartOnFirstThread' to your command line arguments";
            throw new SWTLoadFailed(errorMessage);
          }
          else
          {
            throw th;
          }
        }
      }
      catch (ClassNotFoundException ex)
      {
        throw new SWTLoadFailed("Failed to find main class: " + sTargetMainClass);
      }
      catch (NoSuchMethodException ex)
      {
        throw new SWTLoadFailed("Failed to find main method");
      }
    }
    catch (SWTLoadFailed ex)
    {
      String reason = ex.getMessage();
      System.err.println("Launch failed: " + reason);
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      JOptionPane.showMessageDialog(null, reason, "Launching UI Failed", JOptionPane.ERROR_MESSAGE);
    }
  }

  private static Manifest getSWTLoaderManifest() throws IOException
  {
    Class<?> clazz = SWTLoader.class;
    String className = clazz.getSimpleName() + ".class";
    String classPath = clazz.getResource(className).toString();
    if (!classPath.startsWith("jar"))
    {
      // Class not from JAR
      return null;
    }
    else
    {
      String manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1) +
                            "/META-INF/MANIFEST.MF";
      return new Manifest(new URL(manifestPath).openStream());
    }
  }

  private static void loadConfig() throws SWTLoadFailed
  {
    try
    {
      Manifest m = getSWTLoaderManifest();
      if (m == null)
      {
        throw new SWTLoadFailed("Failed to find swtjar manifest");
      }

      Attributes mainAttributes = m.getMainAttributes();
      String mainClass = mainAttributes.getValue(SWTJAR_MAIN_CLASS);
      if (mainClass != null)
      {
        sTargetMainClass = mainClass;
      }

      String swtVer = mainAttributes.getValue(SWTJAR_VERSION);
      if (swtVer != null)
      {
        sSwtVersion = swtVer;
      }

      if ((sTargetMainClass == null) ||
          (sSwtVersion == null))
      {
        throw new SWTLoadFailed("Failed to load swtjar config from manifest");
      }
    }
    catch (IOException ex)
    {
      throw new SWTLoadFailed("Error when loading swtjar config: " + ex.getMessage());
    }
  }

  private static String getArch()
  {
    // Detect 32bit vs 64 bit
    String jvmArch = System.getProperty("os.arch").toLowerCase();
    String arch = (jvmArch.contains("64") ? "64" : "32");
    return arch;
  }

  private static String getSwtJarName() throws SWTLoadFailed
  {
    // Detect OS
    String osName = System.getProperty("os.name").toLowerCase();
    String swtFileNameOsPart = osName.contains("win") ? "win" : osName
        .contains("mac") ? "osx" : osName.contains("linux")
        || osName.contains("nix") ? "linux" : "";
    if ("".equals(swtFileNameOsPart))
    {
      throw new SWTLoadFailed("Unknown OS name: " + osName);
    }

    // Detect 32bit vs 64 bit
    String swtFileNameArchPart = getArch();

    // Generate final filename
    String swtFileName = "swt-" +
                         swtFileNameOsPart +
                         swtFileNameArchPart +
                         "-" +
                         sSwtVersion +
                         ".jar";
    return swtFileName;
  }

  private static ClassLoader getSWTClassloader() throws SWTLoadFailed
  {
    String swtFileName = getSwtJarName();
    try
    {
      URLClassLoader cl = (URLClassLoader)SWTLoader.class.getClassLoader();
      URL.setURLStreamHandlerFactory(new RsrcURLStreamHandlerFactory(cl));
      Method addUrlMethod = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
      addUrlMethod.setAccessible(true);

      URL swtFileUrl = new URL("rsrc:" + swtFileName);
      System.err.println("Using SWT Jar: " + swtFileName);
      addUrlMethod.invoke(cl, swtFileUrl);

      return cl;
    }
    catch (Exception exx)
    {
      throw new SWTLoadFailed(exx.getClass().getSimpleName() + ": " + exx.getMessage());
    }
  }

  private static class SWTLoadFailed extends Exception
  {
    private static final long serialVersionUID = 1L;

    private SWTLoadFailed(String xiMessage)
    {
      super(xiMessage);
    }
  }
}

package org.zeroturnaround.process.win;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper for locating <code>WMIC.exe</code> which is usually at <code>C:\Windows\System32\Wbem</code>.
 */
public class WmicUtil {


  private static final Logger log = LoggerFactory.getLogger(WmicUtil.class);

  private static final String EXECUTABLE_NAME = "WMIC.exe";
  private static final String DEFAULT_ROOT = "C:/Windows";
  private static final String DEFAULT_DIR = "System32/Wbem/";
  private static final File PATH = findPath();

  public static File getPath() {
    return PATH;
  }

  private static File findPath() {
    File path = new File(EXECUTABLE_NAME); // As a fallback use a relative path
    if (SystemUtils.IS_OS_WINDOWS) {
      String rootDir = getSystemRoot();
      if (rootDir != null) {
        path = new File(rootDir, DEFAULT_DIR + EXECUTABLE_NAME);
      }
    }
    log.debug("Using {} at {}", EXECUTABLE_NAME, path);
    return path;
  }

  /**
   * Gets the system root environment variable value which on most Windows systems is "C:\Windows".
   *
   * We have seen that on some machines the variable is written in different cases. Therefore we need to do a case insensitive search.
   */
  private static String getSystemRoot() {
    Map<String, String> envMap = System.getenv();
    for (Entry<String, String> envVar : envMap.entrySet()) {
      if ("systemroot".equalsIgnoreCase(envVar.getKey())) {
        return envVar.getValue();
      }
    }
    
    if (new File(DEFAULT_ROOT).exists()) {
      return DEFAULT_ROOT;
    }

    return null;
  }
}

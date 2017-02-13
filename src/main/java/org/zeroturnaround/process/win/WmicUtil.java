package org.zeroturnaround.process.win;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper for locating <code>WMIC.exe</code> which is usually at <code>C:\Windows\System32\Wbem</code>.
 */
public class WmicUtil {

  private static final Logger log = LoggerFactory.getLogger(WmicUtil.class);

  private static final File PATH = findPath();

  public static File getPath() {
    return PATH;
  }

  private static File findPath() {
    String name = "WMIC.exe";
    File result = new File(name); // As a fallback use a relative path
    if (SystemUtils.IS_OS_WINDOWS) {
      String windows = getSystemRoot();
      if (windows != null) {
        result = new File(windows, "System32/Wbem/" + name);
      }
    }
    log.debug("Using {} at {}", name, result);
    return result;
  }

  /**
   * Gets the system root environment variable value which on most Windows systems is "C:\Windows".
   *
   * We have seen that on some machines the variable is written in different cases. Therefor we need to do a case insensitive search.
   */
  private static String getSystemRoot() {
    Map<String, String> envMap = System.getenv();
    for (Entry<String, String> envVar : envMap.entrySet()) {
      if ("systemroot".equalsIgnoreCase(envVar.getKey())) {
        return envVar.getValue();
      }
    }
    return null;
  }
}

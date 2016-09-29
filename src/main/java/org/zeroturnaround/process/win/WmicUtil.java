package org.zeroturnaround.process.win;

import java.io.File;

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
      String windows = System.getenv().get("SYSTEMROOT"); // C:\Windows
      if (windows != null) {
        result = new File(windows, "System32/Wbem/" + name);
      }
    }
    log.debug("Using {} at {}", name, result);
    return result;
  }

}

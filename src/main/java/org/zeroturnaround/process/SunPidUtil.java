package org.zeroturnaround.process;

import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper for finding current PID on some JVMs.
 */
class SunPidUtil {

  private static final Logger log = LoggerFactory.getLogger(SunPidUtil.class);

  static Integer tryGetPid(RuntimeMXBean runtime) {
    log.debug("RuntimeMXBean is an instance of {}", runtime.getClass().getName());
    if (runtime.getClass().getName().equals("sun.management.RuntimeImpl")) {
      try {
        return getPid(runtime);
      }
      catch (Exception e) {
        log.debug("Could not detect my PID:", e);
      }
    }
    return null;
  }

  private static int getPid(RuntimeMXBean runtime) throws Exception {
    return getPidFromVManagement(getJvm(runtime));
  }

  private static Object getJvm(Object runtime) throws Exception {
    // private final VMManagement jvm;
    Field f = runtime.getClass().getDeclaredField("jvm");
    f.setAccessible(true);
    return f.get(runtime);
  }

  private static int getPidFromVManagement(Object vMManagement) throws Exception {
    // private native int getProcessId();
    Method m = vMManagement.getClass().getDeclaredMethod("getProcessId");
    m.setAccessible(true);
    return (Integer) m.invoke(vMManagement);
  }

}

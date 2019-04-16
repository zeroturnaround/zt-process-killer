package org.zeroturnaround.process;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.JavaVersion;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.process.win.Kernel32;
import org.zeroturnaround.process.win.W32API;

import com.sun.jna.Pointer;

/**
 * Helper methods for retrieving process IDs.
 */
public final class PidUtil {

  private static final Logger log = LoggerFactory.getLogger(PidUtil.class);

  /**
   * @return process ID of the current JVM.
   */
  public static int getMyPid() {
    Integer result = MyPidHolder.MY_PID;
    if (result == null) {
      throw new UnsupportedOperationException("Could not detect my process ID.");
    }
    return result;
  }

  /**
   * Helper for lazy initialization.
   */
  private static class MyPidHolder {

    private static final Integer MY_PID = findMyPid();

    private static Integer findMyPid() {
      Integer result = null;
      try {
        if (SystemUtils.IS_JAVA_9) {
          result = getCurrentPIdOnJava9();
        }
        else {
          RuntimeMXBean rtb = ManagementFactory.getRuntimeMXBean();
        /*
         * Avoid finding process name if possible as it does a DNS lookup for the "localhost".
         * In some VPN configurations it might take up to 5 seconds.
         */
          result = SunPidUtil.tryGetPid(rtb);
          if (result == null) {
            String processName = rtb.getName();
            result = getPidFromProcessName(processName);
            log.debug("My process name: {}", processName);
          }
        }
        log.debug("My PID: {}", result);
      }
      catch (Exception e) {
        log.error("Could not detect my PID:", e);
      }
      return result;
    }

  }

  private static int getPidFromProcessName(String processName) {
    /* tested on: */
    /* - windows xp sp 2, java 1.5.0_13 */
    /* - mac os x 10.4.10, java 1.5.0 */
    /* - debian linux, java 1.5.0_13 */
    /* all return pid@host, e.g 2204@antonius */

    Pattern pattern = Pattern.compile("^([0-9]+)@.+$", Pattern.CASE_INSENSITIVE);
    Matcher matcher = pattern.matcher(processName);
    if (matcher.matches())
      return Integer.parseInt(matcher.group(1));
    throw new IllegalArgumentException("Invalid process name " + processName);
  }

  /**
   * Detects PID from given {@link Process} object.
   * An error is thrown if we're unable to detect the PID.
   *
   * @param process process object (not <code>null</code>).
   * @return detected PID (not <code>null</code>).
   */
  public static int getPid(Process process) {
    if (process == null)
      throw new IllegalArgumentException("Process must be provided.");

    int result = doGetPid(process);
    log.debug("Found PID for {}: {}", process, result);
    return result;
  }

  private static int doGetPid(Process process) {
    String type;
    try {
    	if (SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_9)) {
        return getPIdOnJava9(process);
      }
      type = process.getClass().getName();
      if (type.equals("java.lang.UNIXProcess")) {
        return getPidFromUnixProcess(process);
      }
      if (type.equals("java.lang.Win32Process") || type.equals("java.lang.ProcessImpl")) {
        return getPidFromWin32Process(process);
      }
    }
    catch (Exception e) {
      throw new IllegalStateException("Could not detect PID from " + process, e);
    }
    throw new IllegalArgumentException("Unknown process class " + type);
  }

  // UNIX

  /**
   * @return PID of the UNIX process.
   * @throws NoSuchFieldException
   * @throws IllegalAccessException
   * @throws IllegalArgumentException
   */
  private static int getPidFromUnixProcess(Process process) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
    Field f = process.getClass().getDeclaredField("pid");
    f.setAccessible(true);
    return f.getInt(process);
  }

  // Windows

  /**
   * @return PID of the Windows process.
   * @throws IllegalAccessException
   * @throws NoSuchFieldException
   *
   * See http://www.golesny.de/p/code/javagetpid
   */
  private static int getPidFromWin32Process(Process process) throws NoSuchFieldException, IllegalAccessException {
    return getPidFromHandle(getHandle(process));
  }

  private static long getHandle(Process process) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
    Field f = process.getClass().getDeclaredField("handle");
    f.setAccessible(true);
    return f.getLong(process);
  }

  private static int getPidFromHandle(long value) {
    Kernel32 kernel = Kernel32.INSTANCE;
    W32API.HANDLE handle = new W32API.HANDLE();
    handle.setPointer(Pointer.createConstant(value));
    return kernel.GetProcessId(handle);
  }

  // Java 9

  private static class Java9Pid {
    private static final Method PROCESS_PID;
    private static final Method PROCESS_HANDLE_CURRENT;
    private static final Method PROCESS_HANDLE_PID;
    static {
      try {
        PROCESS_PID = Process.class.getMethod("pid");
        Class handle = Class.forName("java.lang.ProcessHandle");
        PROCESS_HANDLE_CURRENT = handle.getMethod("current");
        PROCESS_HANDLE_PID = handle.getMethod("pid");
      }
      catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  private static int getPIdOnJava9(Process process) {
    return toInt((Long) ReflectionUtil.invokeWithoutDeclaredExceptions(Java9Pid.PROCESS_PID, process));
  }

  private static int getCurrentPIdOnJava9() throws Exception {
    Object handle = Java9Pid.PROCESS_HANDLE_CURRENT.invoke(null);
    return toInt((Long) Java9Pid.PROCESS_HANDLE_PID.invoke(handle));
  }

  private static int toInt(long pid) {
    if (pid < Integer.MIN_VALUE || pid > Integer.MAX_VALUE) {
      throw new IllegalStateException("PID is out of range: " + pid);
    }
    return (int) pid;
  }

}

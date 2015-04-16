package org.zeroturnaround.process;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.process.win.Kernel32;
import org.zeroturnaround.process.win.W32API;

import com.sun.jna.Pointer;

/**
 * Helper methods for retrieving process IDs.
 */
public class PidUtil {

  private static final Logger log = LoggerFactory.getLogger(PidUtil.class);

  /**
   * Returns process ID of the current JVM.
   *
   * @return process ID of the current JVM.
   */
  public static int getMyPid() {
    Integer result = MyPidHolder.MY_PID;
    if (result == null)
      throw new UnsupportedOperationException("Could not detect my process ID.");
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
        RuntimeMXBean rtb = ManagementFactory.getRuntimeMXBean();
        String processName = rtb.getName();
        result = getPidFromProcessName(processName);
        log.debug("My process name: {}", processName);
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
    String type = process.getClass().getName();
    try {
      if (type.equals("java.lang.UNIXProcess")) {
        return getPidFromUnixProcess(process);
      }
      if (type.equals("java.lang.Win32Process") || type.equals("java.lang.ProcessImpl")) {
        return getPidfromWin32Process(process);
      }
    }
    catch (Exception e) {
      throw new IllegalStateException("Could not detect PID form " + process);
    }
    throw new IllegalArgumentException("Unknown process class " + type);
  }

  // UNIX

  /**
   * @return PID of the UNIX process.
   */
  private static int getPidFromUnixProcess(Process process) throws Exception {
    Field f = process.getClass().getDeclaredField("pid");
    f.setAccessible(true);
    return f.getInt(process);
  }

  // Windows

  /**
   * @return PID of the Windows process.
   *
   * @see http://www.golesny.de/p/code/javagetpid
   */
  private static int getPidfromWin32Process(Process process) throws Exception {
    return getPidfromHandle(getHandle(process));
  }

  private static long getHandle(Process process) throws Exception {
    Field f = process.getClass().getDeclaredField("handle");
    f.setAccessible(true);
    return f.getLong(process);
  }

  private static int getPidfromHandle(long value) throws Exception {
    Kernel32 kernel = Kernel32.INSTANCE;
    W32API.HANDLE handle = new W32API.HANDLE();
    handle.setPointer(Pointer.createConstant(value));
    return kernel.GetProcessId(handle);
  }

}

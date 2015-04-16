package org.zeroturnaround.process;

import java.util.Arrays;

import org.apache.commons.lang.SystemUtils;


/**
 * Creates {@link SystemProcess} instances.
 */
public class Processes {

  /**
   * Creates instance that represents the given {@link Process} by detecting its PID and
   * using both {@link Process} object and the PID with external tools.
   * Java APIs are tried before using the external tools.
   */
  public static SystemProcess newStandardProcess(Process process) {
    return newStandardProcess(process, PidUtil.getPid(process));
  }

  /**
   * Creates instance that represents the given {@link Process} or the given PID.
   * They are expected to belong to the same system process.
   * Java APIs are tried before using the external tools (PID value).
   */
  public static SystemProcess newStandardProcess(Process process, int pid) {
    return newProcessWithAtlernatives(newJavaProcess(process), newPidProcess(pid));
  }

  /**
   * Creates instance that represents given {@link Process} object.
   */
  public static JavaProcess newJavaProcess(Process process) {
    if (Java8Process.isSupported())
      return new Java8Process(process);
    return new JavaProcess(process);
  }

  /**
   * Creates instance that represents PID value of the given {@link Process} and use external tools for the operations.
   */
  public static PidProcess newPidProcess(Process process) {
    return newPidProcess(PidUtil.getPid(process));
  }

  /**
   * Creates instance that represent the given PID value and use external tools for the operations.
   */
  public static PidProcess newPidProcess(int pid) {
    if (SystemUtils.IS_OS_WINDOWS) {
      return new WindowsProcess(pid);
    }
    if (SystemUtils.IS_OS_SOLARIS || SystemUtils.IS_OS_SUN_OS) {
      return new SolarisProcess(pid);
    }
    return new UnixProcess(pid);
  }

  /**
   * Combines existing {@link SystemProcess} objects as alternative implementations for a single process.
   */
  public static SystemProcess newProcessWithAtlernatives(SystemProcess... processes) {
    return new OrProcess(Arrays.asList(processes));
  }

  /**
   * Combines existing {@link SystemProcess} objects for multiple processes.
   */
  public static SystemProcess newProcessForMultiple(SystemProcess... processes) {
    return new AndProcess(Arrays.asList(processes));
  }

}

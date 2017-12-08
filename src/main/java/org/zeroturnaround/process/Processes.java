package org.zeroturnaround.process;

import java.util.Arrays;

import org.apache.commons.lang3.JavaVersion;
import org.apache.commons.lang3.SystemUtils;

/**
 * Creates {@link SystemProcess} instances.
 */
public class Processes {

  interface Factory {

    JavaProcess newJavaProcess(Process process);

    PidProcess newPidProcess(int pid);

  }

  static class Java6 implements Factory {
    @Override public JavaProcess newJavaProcess(Process process) {
      return new JavaProcess(process);
    }
    @Override public PidProcess newPidProcess(int pid) {
      if (SystemUtils.IS_OS_WINDOWS) {
        return new WindowsProcess(pid);
      }
      if (SystemUtils.IS_OS_SOLARIS || SystemUtils.IS_OS_SUN_OS) {
        return new SolarisProcess(pid);
      }
      return new UnixProcess(pid);
    }
  }

  static class Java8 extends Java6 {
    @Override public JavaProcess newJavaProcess(Process process) {
      return new Java8Process(process);
    }
  }

  static class Java9 implements Factory {
    @Override public JavaProcess newJavaProcess(Process process) {
      return new Java9Process(process);
    }

    @Override public PidProcess newPidProcess(int pid) {
      return HandleProcess.of(pid);
    }
  }

  private static final Factory FACTORY = newFactory();

  private static Factory newFactory() {
    if (SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_9)) {
      return new Java9();
    }
    if (SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_1_8)) {
      return new Java8();
    }
    return new Java6();
  }

  /**
   * Creates an instance that represents the given {@link Process} by detecting its PID
   * using both, {@link Process} object and external tools.
   * Java APIs are tried before using the external tools.
   *
   * @param process instance of an existing process started from JVM.
   * @return system process that represents the given input as described above.
   */
  public static SystemProcess newStandardProcess(Process process) {
    return newStandardProcess(process, PidUtil.getPid(process));
  }

  /**
   * Creates an instance that represents the given {@link Process} or the given PID.
   * They are expected to belong to the same system process.
   * Java APIs are tried before using the external tools (PID value).
   *
   * @param process instance of an existing process started from JVM.
   * @param pid PID of the same process.
   * @return system process that represents the given input as described above.
   */
  public static SystemProcess newStandardProcess(Process process, int pid) {
    return newProcessWithAlternatives(newJavaProcess(process), newPidProcess(pid));
  }

  /**
   * Creates an instance that represents the given {@link Process} object.
   *
   * @param process instance of an existing process started from JVM.
   * @return system process that represents the given input as described above.
   */
  public static JavaProcess newJavaProcess(Process process) {
    return FACTORY.newJavaProcess(process);
  }

  /**
   * Creates an instance that represents the PID value of the given {@link Process}.
   * The instance uses external tools for killing the process.
   *
   * @param process instance of an existing process started from JVM.
   * @return system process that represents the given input as described above.
   */
  public static PidProcess newPidProcess(Process process) {
    return newPidProcess(PidUtil.getPid(process));
  }

  /**
   * Creates an instance that represents the given PID value.
   * The instance uses external tools for killing the process.
   *
   * @param pid PID of an external process (running or not).
   * @return system process that represents the given input as described above.
   */
  public static PidProcess newPidProcess(int pid) {
    return FACTORY.newPidProcess(pid);
  }

  /**
   * Combines existing {@link org.zeroturnaround.process.SystemProcess} objects as alternative implementations for a single process.
   *
   * @param processes alternative process instances that represent a single process.
   * @return system process that represents the given inputs as described above.
   * @deprecated Use {@link #newProcessWithAlternatives(org.zeroturnaround.process.SystemProcess...)} instead.
   */
  public static SystemProcess newProcessWithAtlernatives(org.zeroturnaround.process.SystemProcess... processes) {
    return newProcessWithAlternatives(processes);
  }

  /**
   * Combines existing {@link org.zeroturnaround.process.SystemProcess} objects as alternative implementations for a single process.
   * @param processes alternative process instances that represent a single process.
   * @return system process that represents the given inputs as described above.
   */
  public static SystemProcess newProcessWithAlternatives(org.zeroturnaround.process.SystemProcess... processes) {
    return new OrProcess(Arrays.asList(processes));
  }

  /**
   * Combines existing {@link org.zeroturnaround.process.SystemProcess} objects for multiple processes.
   * @param processes process instances that represent different processes.
   * @return system process that represents the given inputs as described above.
   */
  public static SystemProcess newProcessForMultiple(org.zeroturnaround.process.SystemProcess... processes) {
    return new AndProcess(Arrays.asList(processes));
  }

}

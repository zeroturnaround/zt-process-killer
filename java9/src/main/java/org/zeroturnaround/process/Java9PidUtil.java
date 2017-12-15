package org.zeroturnaround.process;

public class Java9PidUtil {

  public static long getMyPid() {
    return ProcessHandle.current().pid();
  }

  public static long getPid(Process process) {
    return process.toHandle().pid();
  }

}

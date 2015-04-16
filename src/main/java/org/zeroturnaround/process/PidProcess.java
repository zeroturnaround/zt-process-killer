package org.zeroturnaround.process;

/**
 * Base killer implementation for <code>PID</code> (Process ID) values.
 */
public abstract class PidProcess extends PollingProcess {

  /**
   * The process ID.
   */
  protected final int pid;

  public PidProcess(int pid) {
    this.pid = pid;
  }

  /**
   * Returns the process ID.
   *
   * @return the process ID.
   */
  public int getPid() {
    return pid;
  }

  @Override
  public String getDescription() {
    return Integer.toString(pid);
  }

}

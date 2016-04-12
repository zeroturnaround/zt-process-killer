package org.zeroturnaround.process;

/**
 * Base implementation for processes that use <code>PID</code> (Process ID) values.
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

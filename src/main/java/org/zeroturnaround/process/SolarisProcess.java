package org.zeroturnaround.process;

import org.zeroturnaround.exec.InvalidExitValueException;

/**
 * Process implementation for Solaris.
 */
public class SolarisProcess extends UnixProcess {

  private static final int EXIT_CODE_NO_SUCH_PROCESS = 2;

  public SolarisProcess(int pid) {
    super(pid);
  }

  @Override
  protected boolean isNoSuchProcess(InvalidExitValueException e) {
    return e.getExitValue() == EXIT_CODE_NO_SUCH_PROCESS;
  }

}

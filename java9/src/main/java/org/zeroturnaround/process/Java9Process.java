package org.zeroturnaround.process;

public class Java9Process extends Java8Process {

  public Java9Process(Process process) {
    super(process);
  }

  @Override
  protected boolean canDestroy(boolean forceful) {
    // We have destroyForcibly() method
    if (forceful) {
      return true;
    }
    return process.supportsNormalTermination();
  }

}

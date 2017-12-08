package org.zeroturnaround.process;

import java.util.concurrent.TimeUnit;

public class NonExistingProcess extends PidProcess {

  public NonExistingProcess(int pid) {
    super(pid);
  }

  @Override public boolean isAlive() {
    return false;
  }

  @Override public void waitFor() {
    // do nothing
  }

  @Override public boolean waitFor(long timeout, TimeUnit unit) {
    return true;
  }

  @Override public void destroy(boolean forceful) {
    // do nothing
  }

}

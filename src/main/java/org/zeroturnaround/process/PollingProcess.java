package org.zeroturnaround.process;

import java.io.IOException;

/**
 * Base implementation that polls for a process status.
 * <p>
 * <code>waitFor</code> methods poll for {@link #isAlive()} method
 * which should be implemented in the sub classes.
 */
public abstract class PollingProcess extends AbstractProcess {

  private volatile long intervalForCheckingFinished = 1000;

  public long getIntervalForCheckingFinished() {
    return intervalForCheckingFinished;
  }

  public void setIntervalForCheckingFinished(long intervalForCheckingFinished) {
    this.intervalForCheckingFinished = intervalForCheckingFinished;
  }

  @Override
  public void waitFor() throws InterruptedException {
    while (true) {
      try {
        boolean alive = isAlive();
        log.trace("{} is alive: {}", getDescription(), alive);
        if (!alive) {
          return;
        }
      }
      catch (IOException e) {
        String message = "Failed to check if process " + getDescription() + " is alive"; 
        log.debug(message, e);
        throw new RuntimeException(message, e);
      }
      Thread.sleep(intervalForCheckingFinished);
    }
  }

}

package org.zeroturnaround.process;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper for {@link java.lang.Process} since Java 8.
 * <p>
 * Java 8 added following methods:
 * <ul>
 *   <li><code>isAlive()</code></li>
 *   <li><code>destroyForcibly()</code></li>
 *   <li><code>waitFor(long, TimeUnit)</code></li>
 * </ul>
 */
public class Java8Process extends JavaProcess {

  private static final Logger log = LoggerFactory.getLogger(Java8Process.class);

  public Java8Process(Process process) {
    super(process);
  }

  // Killing

  @Override
  protected boolean canDestroy(boolean forceful) {
    // We have destroyForcibly() method
    if (forceful) {
      return true;
    }
    return super.canDestroy(forceful);
  }

  @Override
  protected void invokeDestroy(boolean forceful) {
    // We have destroyForcibly() method
    if (forceful) {
      log.debug("Invoking destroyForcibly() on {}", process);
      invokeDestroyForcibly();
    }
    else {
      super.invokeDestroy(forceful);
    }
  }

  // Process methods in Java 8

  public Process invokeDestroyForcibly() {
    return process.destroyForcibly();
  }

  public boolean isAlive() {
    return process.isAlive();
  }

  public boolean waitFor(long timeout, TimeUnit unit) throws InterruptedException {
    return process.waitFor(timeout, unit);
  }

}

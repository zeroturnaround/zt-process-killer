package org.zeroturnaround.process;

import java.io.IOException;

import org.apache.commons.lang.SystemUtils;

/**
 * Wrapper for {@link java.lang.Process}.
 */
public class JavaProcess extends AbstractProcess {

  /**
   * The wrapped process.
   */
  protected final Process process;

  protected JavaProcess(Process process) {
    this.process = process;
  }

  /**
   * Returns the wrapped process.
   *
   * @return the wrapped process.
   */
  public Process getProcess() {
    return process;
  }

  @Override
  public String getDescription() {
    String result = process.toString();
    // Remove the package name to get a shorter result
    int i = result.lastIndexOf('.');
    // This works even with the root package
    return result.substring(i + 1);
  }

  public boolean isAlive() {
    try {
      process.exitValue();
      // Process is already terminated
      return false;
    }
    catch (IllegalThreadStateException e) {
      // Process is still running
      return true;
    }
  }

  public void waitFor() throws InterruptedException {
    process.waitFor();
  }

  @Override
  public void destroy(boolean forceful) throws IOException {
    if (!canDestroy(forceful)) {
      throw new UnsupportedOperationException();
    }
    invokeDestroy(forceful);
  }

  /**
   * @return <code>true</code> if {@link #invokeDestroy(boolean)} is supported with the given <code>forceful</code> flag on the given system.
   */
  protected boolean canDestroy(boolean forceful) {
    /*
     * On Windows Process.destroy() destroys the process forcefully.
     * On UNIX it destroys the process gracefully.
     */
    return forceful == SystemUtils.IS_OS_WINDOWS;
  }

  protected void invokeDestroy(boolean forceful) {
    log.debug("Invoking destroy() on {}", process);
    // We ignore forceful flag as this method is only invoked in case of single value - see canDestroy().
    process.destroy();
  }

}

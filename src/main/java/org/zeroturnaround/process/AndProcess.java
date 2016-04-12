package org.zeroturnaround.process;

import java.io.IOException;
import java.util.List;

/**
 * Represents multiple processes.
 * <p>
 * It tries to kill all child processes.
 * If it fails to destroy any process it still tries to destroy other processes before actually throwing the initial error.
 * {@link #isAlive()} returns <code>true</code> if at least one of the processes is still alive.
 * {@link #isAllAlive()} returns <code>true</code> only if all processes are still alive.
 * <p>
 * If it has only one child it acts the same as invoking the same method directly on the child.
 * </p>
 */
public class AndProcess extends CompositeProcess {

  public AndProcess(List<? extends SystemProcess> children) {
    super(children);
  }

  public boolean isAlive() throws IOException, InterruptedException {
    for (SystemProcess child : children) {
      if (child.isAlive()) {
        return true;
      }
    }
    return false;
  }

  public boolean isAllAlive() throws IOException, InterruptedException {
    for (SystemProcess child : children) {
      if (!child.isAlive()) {
        return false;
      }
    }
    return true;
  }

  @Override
  public void waitFor() throws InterruptedException {
    for (SystemProcess child : children) {
      child.waitFor();
    }
  }

  @Override
  public void destroy(boolean forceful) throws IOException, InterruptedException {
    Exception firstException = null;
    for (SystemProcess child : children) {
      try {
        invokeDestroy(child, forceful);
      }
      catch (InterruptedException e) {
        throw e;
      }
      catch (Exception e) {
        log.error("Failed to destroy {}", child, e);
        if (firstException == null) {
          firstException = e;
        }
      }
    }
    if (firstException != null) {
      try {
        throw firstException;
      }
      catch (IOException e) {
        throw e;
      }
      catch (InterruptedException e) {
        throw e;
      }
      catch (RuntimeException e) {
        throw e;
      }
      catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

}

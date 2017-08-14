package org.zeroturnaround.process;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base implementation of {@link SystemProcess}.
 */
public abstract class AbstractProcess implements SystemProcess {

  protected final Logger log = LoggerFactory.getLogger(getClass());

  @Override
  public String toString() {
    return getClass().getSimpleName() + "(" + getDescription() + ")";
  }

  /**
   * @return the description of the system process.
   */
  protected abstract String getDescription();

  /**
   * Causes the current thread to wait, if necessary, until the process handled by this killer has terminated, or the specified timeout is reached.
   * <p>
   * If the process has already terminated then this method returns immediately with the value <code>true</code>.
   * If the process has not terminated and the timeout value is less than, or equal to, zero, then this method returns immediately with the value <code>false</code>.
   * </p>
   *
   * @param timeout the maximum time to wait
   * @param unit the time unit of the timeout argument
   * @return <code>true</code> if the process has exited and <code>false</code> if the timeout is reached before the process has exited.
   * @throws InterruptedException if interrupted.
   */
  @Override
  public boolean waitFor(long timeout, TimeUnit unit) throws InterruptedException {
    ExecutorService service = Executors.newSingleThreadScheduledExecutor();
    try {
      Runnable task = new Runnable() {
        @Override
        public void run() {
          try {
            waitFor();
          }
          catch (InterruptedException e) {
            log.debug("Interrupted waiting for {}", getDescription());
          }
        }
      };
      service.submit(task).get(timeout, unit);
    }
    catch (ExecutionException e) {
      throw new IllegalStateException("Error occured while waiting for process to finish:", e.getCause());
    }
    catch (TimeoutException e) {
      log.debug("{} is running too long", getDescription());
      return false;
    }
    finally {
      // Interrupt the task if it's still running and release the ExecutorService's resources
      service.shutdownNow();
    }
    return true;
  }

  /**
   * Terminates this process. The process is gracefully terminated (like <code>kill -TERM</code> does).
   * <p>
   * Note: The process may not terminate at all.
   * i.e. <code>isAlive()</code> may return <code>true</code> for a any period after <code>destroyGracefully()</code> is called.
   * This method may be chained to <code>waitFor()</code> if needed.
   * </p>
   * <p>
   * No error is thrown if the process was already terminated.
   * </p>
   *
   * @return this process object.
   * @throws UnsupportedOperationException if this implementation is unable to gracefully terminate the process.
   * @throws IOException on IO error.
   * @throws InterruptedException if interrupted.
   */
  @Override
  public AbstractProcess destroyGracefully() throws IOException, InterruptedException {
    destroy(false);
    return this;
  }

  /**
   * Kills this process. The process is forcibly terminated (like <code>kill -KILL</code> does).
   * <p>
   * Note: The process may not terminate immediately.
   * i.e. <code>isAlive()</code> may return <code>true</code> for a brief period after <code>destroyForcefully()</code> is called.
   * This method may be chained to <code>waitFor()</code> if needed.
   * </p>
   * <p>
   * No error is thrown if the process was already terminated.
   * </p>
   *
   * @return this process object.
   * @throws UnsupportedOperationException if this implementation is unable to gracefully terminate the process.
   * @throws IOException on IO error.
   * @throws InterruptedException if interrupted.
   */
  @Override
  public AbstractProcess destroyForcefully() throws IOException, InterruptedException {
    destroy(true);
    return this;
  }

  /**
   * Destroys the process either forcefully or gracefully according to the given option.
   * <p>
   * Note: The process may not terminate at all.
   * i.e. <code>isAlive()</code> may return <code>true</code> for a any period after <code>destroy()</code> is called.
   * This method may be chained to <code>waitFor()</code> if needed.
   * </p>
   * <p>
   * No error is thrown if the process was already terminated.
   * </p>
   *
   * @param forceful <code>true</code> if the process must be destroyed forcefully (like <code>kill -KILL</code>),
   *    <code>false</code> if it must be destroyed gracefully (like <code>kill -TERM</code>).
   * @throws UnsupportedOperationException if this implementation is unable to terminate the process with this <code>forceful</code> value.
   * @throws IOException on IO error.
   * @throws InterruptedException if interrupted.
   */
  public abstract void destroy(boolean forceful) throws IOException, InterruptedException;

}

package org.zeroturnaround.process;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Additional helper methods for killing processes and waiting until they finish.
 * <p>
 * Here all methods that use a timeout throw {@link TimeoutException} including the given timeout in the message
 * instead of returning a <code>false</code> like {@link SystemProcess} does.
 * Also all methods log a message in case the operation succeeded including the time it took.
 * </p>
 * <p>
 * Notice that methods that destroy a process do not include destroying operation itself in the timeout period. They start measuring time after sending the destroy signal.
 * Also if the current thread is interrupted it may also interrupt sending the destroy signal itself not just the waiting period after it. So if the current thread gets
 * interrupted there's no guarantee that the target process actually got signaled.
 * </p>
 *
 * @see SystemProcess
 */
public class ProcessUtil {

  private static final Logger log = LoggerFactory.getLogger(ProcessUtil.class);

  /**
   * Waits until the given process finishes or the current thread is interrupted.
   *
   * @param process the target process.
   *
   * @throws InterruptedException if the current thread was interrupted.
   */
  public static void waitFor(SystemProcess process) throws InterruptedException {
    log.info("Waiting for {} to finish.", process);
    waitFor(process, Stopwatch.createStarted(), "{} finished");
  }

  /**
   * Waits until the given process finishes, a timeout is reached or the current thread is interrupted.
   *
   * @param process the target process.
   * @param timeout the maximum time to wait until the process finishes.
   * @param unit the time unit of the timeout argument.
   *
   * @throws InterruptedException if the current thread was interrupted.
   * @throws TimeoutException if timeout was reached before the process finished.
   */
  public static void waitFor(SystemProcess process, long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
    log.info("Waiting for {} to finish.", process);
    waitFor(process, Stopwatch.createStarted(), timeout, unit, "{} finished", "%s did not finish");
  }

  /**
   * Destroys the given process gracefully and waits until it finishes or the current thread is interrupted.
   *
   * @param process the target process.
   *
   * @throws IOException on IO error.
   * @throws InterruptedException if the current thread was interrupted.
   */
  public static void destroyGracefullyAndWait(SystemProcess process) throws IOException, InterruptedException {
    Stopwatch sw = Stopwatch.createStarted();
    process.destroyGracefully();
    waitFor(process, sw, "Destroyed {} gracefully");
  }

  /**
   * Destroys the given process gracefully and waits until it finishes, a timeout occurs or the current thread is interrupted.
   *
   * @param process the target process.
   * @param timeout the maximum time to wait until the process finishes.
   * @param unit the time unit of the timeout argument.
   *
   * @throws IOException on IO error.
   * @throws InterruptedException if the current thread was interrupted.
   * @throws TimeoutException if timeout was reached before the process finished.
   */
  public static void destroyGracefullyAndWait(SystemProcess process, long timeout, TimeUnit unit) throws IOException, InterruptedException, TimeoutException {
    Stopwatch sw = Stopwatch.createStarted();
    process.destroyGracefully();
    waitFor(process, sw, timeout, unit, "Destroyed {} gracefully", "Could not destroy %s gracefully");
  }

  /**
   * Destroys the given process forcefully and waits until it finishes or the current thread is interrupted.
   *
   * @param process the target process.
   *
   * @throws IOException on IO error.
   * @throws InterruptedException if the current thread was interrupted.
   */
  public static void destroyForcefullyAndWait(SystemProcess process) throws IOException, InterruptedException {
    Stopwatch sw = Stopwatch.createStarted();
    process.destroyForcefully();
    waitFor(process, sw, "Destroyed {} forcefully");
  }

  /**
   * Destroys the given process forcefully and waits until it finishes, a timeout occurs or the current thread is interrupted.
   *
   * @param process the target process.
   * @param timeout the maximum time to wait until the process finishes.
   * @param unit the time unit of the timeout argument.
   *
   * @throws IOException on IO error.
   * @throws InterruptedException if the current thread was interrupted.
   * @throws TimeoutException if timeout was reached before the process finished.
   */
  public static void destroyForcefullyAndWait(SystemProcess process, long timeout, TimeUnit unit) throws IOException, InterruptedException, TimeoutException {
    Stopwatch sw = Stopwatch.createStarted();
    process.destroyForcefully();
    waitFor(process, sw, timeout, unit, "Destroyed {} forcefully", "Could not destroy %s forcefully");
  }

  /**
   * Destroys the given process gracefully and waits until it finishes or the current thread is interrupted.
   * If the graceful destroy operation throws an exception (e.g. it's unsupported)
   * it destroys the process forcefully and waits until it finishes or the current thread is interrupted.
   *
   * @param process the target process.
   *
   * @throws IOException on IO error.
   * @throws InterruptedException if the current thread was interrupted.
   */
  public static void destroyGracefullyOrForcefullyAndWait(SystemProcess process) throws IOException, InterruptedException {
    try {
      destroyGracefullyAndWait(process);
      return;
    }
    catch (UnsupportedOperationException e) {
      log.trace("Destroying {} gracefully is unsupported, trying forcefully:", process);
    }
    catch (Exception e) {
      log.error("Could not destroy {} gracefully, trying forcefully:", process, e);
    }
    destroyForcefullyAndWait(process);
  }

  /**
   * Destroys the given process gracefully and waits until it finishes, a timeout occurs or the current thread is interrupted.
   * If the graceful destroy operation throws an exception (e.g. it's unsupported) or a timeout is reached
   * it destroys the process forcefully and waits until it finishes or the current thread is interrupted (no timeout is used in this case).
   *
   * @param process the target process.
   * @param gracefulTimeout the maximum time to wait until the process finishes after the graceful destroy operation.
   * @param gracefulTimeoutUnit the time unit of the timeout argument.
   *
   * @throws IOException on IO error.
   * @throws InterruptedException if the current thread was interrupted.
   */
  public static void destroyGracefullyOrForcefullyAndWait(SystemProcess process, long gracefulTimeout, TimeUnit gracefulTimeoutUnit) throws IOException, InterruptedException {
    if (tryDestroyGracefully(process, gracefulTimeout, gracefulTimeoutUnit)) {
      return;
    }
    destroyForcefullyAndWait(process);
  }

  /**
   * Destroys the given process gracefully and waits until it finishes, first timeout occurs or the current thread is interrupted.
   * If the graceful destroy operation throws an exception (e.g. it's unsupported) or a timeout is reached
   * it destroys the process forcefully and waits until it finishes, second timeout occurs or the current thread is interrupted.
   *
   * @param process the target process.
   * @param gracefulTimeout the maximum time to wait until the process finishes after the graceful destroy operation.
   * @param gracefulTimeoutUnit the time unit of the gracefulTimeout argument.
   * @param forcefulTimeout the maximum time to wait until the process finishes after the forceful destroy operation.
   * @param forcefulTimeoutUnit the time unit of the forcefulTimeout argument.
   *
   * @throws IOException on IO error.
   * @throws InterruptedException if the current thread was interrupted.
   * @throws TimeoutException if timeout was reached before the process finished (after the forceful destroy operation).
   */
  public static void destroyGracefullyOrForcefullyAndWait(SystemProcess process, long gracefulTimeout, TimeUnit gracefulTimeoutUnit, long forcefulTimeout, TimeUnit forcefulTimeoutUnit) throws IOException, InterruptedException, TimeoutException {
    if (tryDestroyGracefully(process, gracefulTimeout, gracefulTimeoutUnit)) {
      return;
    }
    destroyForcefullyAndWait(process, forcefulTimeout, forcefulTimeoutUnit);
  }

  private static boolean tryDestroyGracefully(SystemProcess killer, long gracefulTimeout, TimeUnit gracefulTimeoutUnit) {
    try {
      destroyGracefullyAndWait(killer, gracefulTimeout, gracefulTimeoutUnit);
      return true;
    }
    catch (UnsupportedOperationException e) {
      log.trace("Destroying {} gracefully is unsupported, trying forcefully:", killer);
    }
    catch (TimeoutException e) {
      log.info(e.getMessage() + ", trying forcefully.");
    }
    catch (Exception e) {
      log.error("Could not destroy {} gracefully, trying forcefully.", killer, e);
    }
    return false;
  }

  /**
   * Waits until the given process finishes or the current thread is interrupted.
   *
   * @param process the target process.
   * @param sw stopwatch started before the main operation.
   * @param successFormat format for logging a message after the process has finished, including single <code>{}</code> as the process description placeholder,
   *    e.g. <code>"Process {} finished"</code>.
   *
   * @throws InterruptedException if the current thread was interrupted.
   */
  private static void waitFor(SystemProcess process, Stopwatch sw, String successFormat) throws InterruptedException {
    process.waitFor();
    long duration = sw.stop().elapsed(TimeUnit.MILLISECONDS);
    log.info(successFormat + " in {} ms.", process, duration);
  }

  /**
   * Waits until the given process finishes, a timeout occurs or the current thread is interrupted.
   *
   * @param process the target process.
   * @param sw stopwatch started before the main operation.
   * @param timeout the maximum time to wait until the process finishes.
   * @param unit the time unit of the timeout argument.
   * @param successFormat format for logging a message after the process has finished, including single <code>{}</code> as the process description placeholder,
   *    e.g. <code>"Process {} finished"</code>.
   * @param timeoutFormat format for the error message after the timeout is reached, including single <code>%s</code> as the process description placeholder,
   *    e.g. <code>"Process %s did not finish"</code>
   *
   * @throws InterruptedException if the current thread was interrupted.
   * @throws TimeoutException if the process did not finish on time.
   */
  private static void waitFor(SystemProcess process, Stopwatch sw, long timeout, TimeUnit unit, String successFormat, String timeoutFormat) throws InterruptedException, TimeoutException {
    if (!process.waitFor(timeout, unit)) {
      throw new TimeoutException(String.format(timeoutFormat + " in %d %s",
          process, timeout, unit.toString().toLowerCase()));
    }
    long duration = sw.stop().elapsed(TimeUnit.MILLISECONDS);
    log.info(successFormat + " in {} ms.", process, duration);
  }

}

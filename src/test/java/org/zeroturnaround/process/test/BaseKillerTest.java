package org.zeroturnaround.process.test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.SystemUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.zeroturnaround.process.Java8Process;
import org.zeroturnaround.process.JavaProcess;
import org.zeroturnaround.process.SystemProcess;

public class BaseKillerTest extends Assert {

  private static final ExecutorService executorForTimeouts = Executors.newCachedThreadPool();

  @Rule
  public final TestRule logRule = new TestRunnerLogRule();

  // Support checks

  protected static boolean isDestroyGracefullySupported(SystemProcess process) {
    return !SystemUtils.IS_OS_WINDOWS;
  }

  protected static boolean isDestroyForcefullySupported(SystemProcess process) {
    return SystemUtils.IS_OS_WINDOWS || !(process instanceof JavaProcess) || Java8Process.isSupported();
  }

  /**
   * Run a given action with a timeout.
   * @param timeout timeout to wait for.
   * @param unit units.
   * @param action task that should finish before the timeout is reached.
   * @throws Exception failure.
   */
  protected static void timeout(long timeout, TimeUnit unit, final Action action) throws Exception {
    Adapter adapter = new Adapter(action);
    try {
      executorForTimeouts.submit(adapter).get(timeout, unit);
    }
    catch (TimeoutException e) {
      Exception st = adapter.getStackTrace(timeout, unit);
      if (st != null) {
        e.initCause(st);
      }
      throw e;
    }
  }

  private static class Adapter implements Callable<Void> {

    private final Action target;

    private volatile Thread worker;

    private Adapter(Action target) {
      this.target = target;
    }

    @Override
    public Void call() throws Exception {
      worker = Thread.currentThread();
      try {
        target.run();
      }
      finally {
        worker = null;
      }
      return null;
    }

    private Exception getStackTrace(long timeout, TimeUnit unit) {
      Exception e = null;
      Thread w = worker;
      if (w != null) {
        e = new Exception("Thread dump after timeout of " + timeout + " " + unit + " reached.");
        e.setStackTrace(w.getStackTrace());
      }
      return e;
    }

  }

}

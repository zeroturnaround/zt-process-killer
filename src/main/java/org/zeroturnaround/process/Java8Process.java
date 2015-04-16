package org.zeroturnaround.process;

import java.lang.reflect.Method;
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

  /**
   * <code>public Process destroyForcibly()</code>
   */
  private static final Method METHOD_DESTROY_FORCIBLY = getMethod("destroyForcibly");

  /**
   * <code>public boolean isAlive()</code>
   */
  private static final Method METHOD_IS_ALIVE = getMethod("isAlive");

  /**
   * <code>public boolean waitFor(long timeout, TimeUnit unit) throws InterruptedException</code>
   */
  private static final Method METHOD_WAIT_FOR_TIMEOUT = getMethod("waitFor", long.class, TimeUnit.class);

  public static boolean isSupported() {
    return METHOD_DESTROY_FORCIBLY != null;
  }

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
    return (Process) ReflectionUtil.invokeWithoutDeclaredExceptions(METHOD_DESTROY_FORCIBLY, process);
  }

  public boolean isAlive() {
    return (Boolean) ReflectionUtil.invokeWithoutDeclaredExceptions(METHOD_IS_ALIVE, process);
  }

  public boolean waitFor(long timeout, TimeUnit unit) throws InterruptedException {
    return (Boolean) ReflectionUtil.invokeWithInterruptedException(METHOD_WAIT_FOR_TIMEOUT, process, timeout, unit);
  }

  private static Method getMethod(String name, Class<?>... parameterTypes) {
    return ReflectionUtil.getMethodOrNull(Process.class, name, parameterTypes);
  }

}

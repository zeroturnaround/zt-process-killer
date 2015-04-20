package org.zeroturnaround.process;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Represents a native system process.
 * <p>
 * This interface provides methods for checking aliveness, waiting for the process to complete and destroying (killing) the process.
 * It does not have methods to control streams or check exit status of the process.
 * </p>
 * <p>
 * It is implementation specific whether the represented system process is a sub process of this JVM
 * or some external process referred by a process ID, service name or etc.
 * An instance of this class may also represent more than one process. In this case it is considered alive as long as any of the processes is alive.
 * </p>
 * <p>
 * Some of the operations may be unsupported by throwing {@link UnsupportedOperationException}.
 * </p>
 *
 * @see Processes
 * @see ProcessUtil
 */
public interface SystemProcess {

  /**
   * Tests whether this process is alive.
   * <p>
   * This operation may also take some time to finish.
   * </p>
   *
   * @return <code>true</code> if this process is alive, <code>false</code> if it is finished or not found.
   */
  boolean isAlive() throws IOException, InterruptedException;

  /**
   * Causes the current thread to wait, if necessary, until this process has terminated.
   * <p>
   * This method returns immediately if the process has already terminated.
   * If the process has not yet terminated, the calling thread will be blocked until the process exits.
   * </p>
   */
  void waitFor() throws InterruptedException;

  /**
   * Causes the current thread to wait, if necessary, until the process handled by this killer has terminated, or the specified waiting time elapses.
   * <p>
   * If the process has already terminated then this method returns immediately with the value <code>true</code>.
   * If the process has not terminated and the timeout value is less than, or equal to, zero, then this method returns immediately with the value <code>false</code>.
   * </p>
   * <p>
   * Checking the process status itself may also take time.
   * If the process has exited but timeout is reached before the checking operation finishes <code>false</code> is returned.
   * </p>
   *
   * @param timeout the maximum time to wait
   * @param unit the time unit of the timeout argument
   * @return <code>true</code> if the process has exited and <code>false</code> if the waiting time elapsed before the process has exited.
   */
  boolean waitFor(long timeout, TimeUnit unit) throws InterruptedException;

  /**
   * Terminates this process. The process is gracefully terminated (like <code>kill -TERM</code> does).
   * <p>
   * Note: The process may not terminate at all.
   * i.e. <code>isAlive()</code> may return <code>true</code> for any period after <code>destroyGracefully()</code> is called.
   * This method may be chained to <code>waitFor()</code> if needed.
   * </p>
   * <p>
   * If this process was already finished (or it was not found) this method finishes without throwing any errors.
   * </p>
   *
   * @return this process object.
   * @throws UnsupportedOperationException if this implementation cannot gracefully terminate any process.
   */
  SystemProcess destroyGracefully() throws IOException, InterruptedException;

  /**
   * Kills this process. The process is forcibly terminated (like <code>kill -KILL</code> does).
   * <p>
   * Note: The process may not terminate immediately.
   * i.e. <code>isAlive()</code> may return <code>true</code> for a brief period after <code>destroyForcefully()</code> is called.
   * This method may be chained to <code>waitFor()</code> if needed.
   * </p>
   * <p>
   * If this process was already finished (or it was not found) this method finishes without throwing any errors.
   * </p>
   *
   * @return this process object.
   * @throws UnsupportedOperationException if this implementation cannot forcibly terminate any process.
   */
  SystemProcess destroyForcefully() throws IOException, InterruptedException;

}
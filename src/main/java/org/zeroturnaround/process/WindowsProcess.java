package org.zeroturnaround.process;

import java.io.File;
import java.io.IOException;

import org.zeroturnaround.exec.InvalidExitValueException;
import org.zeroturnaround.exec.MessageLoggers;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;
import org.zeroturnaround.process.win.WmicUtil;

/**
 * Process implementation for Windows PID values.
 * <p>
 * It uses <code>wmic</code> for checking the process status and <code>taskkill</code> command for destroying the process.
 * </p>
 * <p>
 * Although the <code>taskkill</code> command officially supports killing both forcefully or gracefully
 * we expect it to fail killing anything gracefully by default and we throw {@link UnsupportedOperationException}.
 * To enable this operation call {@link #setGracefulDestroyEnabled(boolean)} with <code>true</code> first.
 * </p>
 */
public class WindowsProcess extends PidProcess {

  private static final int EXIT_CODE_COULD_NOT_BE_TERMINATED = 1;

  private static final int EXIT_CODE_NO_SUCH_PROCESS = 128;

  /**
   * Path to the <code>wmic.exe</code> command.
   */
  private File wmicPath;

  /**
   * <code>true</code> if <code>taskkill</code> is used for graceful destroying, <code>false</code> if {@link UnsupportedOperationException} is thrown instead.
   */
  private boolean gracefulDestroyEnabled;

  /**
   * <code>true</code> if <code>/T</code> flag should be used.
   */
  private boolean includeChildren;

  public WindowsProcess(int pid) {
    this(pid, WmicUtil.getPath());
  }

  public WindowsProcess(int pid, File wmicPath) {
    super(pid);
    this.wmicPath = wmicPath;
  }

  public File getWmicPath() {
    return wmicPath;
  }

  public void setWmicPath(File wmicPath) {
    this.wmicPath = wmicPath;
  }

  public boolean isGracefulDestroyEnabled() {
    return gracefulDestroyEnabled;
  }

  public void setGracefulDestroyEnabled(boolean gracefulDestroyEnabled) {
    this.gracefulDestroyEnabled = gracefulDestroyEnabled;
  }

  public boolean isIncludeChildren() {
    return includeChildren;
  }

  public void setIncludeChildren(boolean includeChildren) {
    this.includeChildren = includeChildren;
  }

  public boolean isAlive() throws IOException, InterruptedException {
    String out = new ProcessExecutor()
        .commandSplit(String.format("%s process where ProcessId=%d get ProcessId", wmicPath, pid))
        .readOutput(true)
        .redirectOutput(Slf4jStream.ofCaller().asTrace())
        .setMessageLogger(MessageLoggers.TRACE)
        .exitValueNormal()
        .executeNoTimeout().outputString();
    return out.contains(String.valueOf(pid));
  }

  @Override
  public void destroy(boolean forceful) throws IOException, InterruptedException {
    if (!forceful && !gracefulDestroyEnabled) {
      throw new UnsupportedOperationException();
    }
    taskkill(forceful);
  }

  /**
   * Sends the destroy signal to this process.
   *
   * @param forceful <code>true</code> if this process should be destroyed forcefully.
   * @return <code>true</code> if this process got the signal, <code>false</code> if the process was not found (any more).
   *
   * @throws IOException on IO error.
   * @throws InterruptedException if interrupted.
   */
  public boolean taskkill(boolean forceful) throws IOException, InterruptedException {
    try {
      new ProcessExecutor()
      .commandSplit(String.format("taskkill%s%s /PID %d", includeChildren ? " /T" : "", forceful ? " /F" : "", pid))
      .redirectOutput(Slf4jStream.ofCaller().asDebug()).exitValueNormal().executeNoTimeout();
      return true;
    }
    catch (InvalidExitValueException e) {
      if (e.getExitValue() == EXIT_CODE_COULD_NOT_BE_TERMINATED) {
        // Process could be either alive or not, if it's not alive we don't want to throw an exception
        if (isAlive()) {
          throw e; // process is still alive
        }
        return false; // process is stopped but but not because of us
      }
      if (e.getExitValue() == EXIT_CODE_NO_SUCH_PROCESS) {
        return false;
      }
      throw e;
    }
  }

}

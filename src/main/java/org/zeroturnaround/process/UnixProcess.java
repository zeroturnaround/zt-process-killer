package org.zeroturnaround.process;

import java.io.IOException;

import org.zeroturnaround.exec.InvalidExitValueException;
import org.zeroturnaround.exec.MessageLoggers;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;

/**
 * Process implementation for UNIX PID values.
 * <p>
 * It uses the <code>kill</code> command for both checking the status and destroying the process.
 * </p>
 */
public class UnixProcess extends PidProcess {

  private static final int EXIT_CODE_NO_SUCH_PROCESS = 1;

  public UnixProcess(int pid) {
    super(pid);
  }

  public boolean isAlive() throws IOException, InterruptedException {
    try {
      new ProcessExecutor()
      .commandSplit(String.format("kill -0 %d", pid)).readOutput(true)
      .redirectOutput(Slf4jStream.ofCaller().asTrace())
      .setMessageLogger(MessageLoggers.TRACE)
      .exitValueNormal()
      .executeNoTimeout();
      return true;
    }
    catch (InvalidExitValueException e) {
      if (isNoSuchProcess(e)) {
        return false;
      }
      throw e;
    }
  }

  @Override
  public void destroy(boolean forceful) throws IOException, InterruptedException {
    kill(forceful ? "kill" : "term");
  }

  /**
   * Sends a signal to this process.
   *
   * @param signal name of the signal.
   * @return <code>true</code> if this process received the signal, <code>false</code> if this process was not found (any more).
   */
  public boolean kill(String signal) throws IOException, InterruptedException {
    try {
      new ProcessExecutor()
      .commandSplit(String.format("kill -%s %d", signal, pid))
      .redirectOutput(Slf4jStream.ofCaller().asDebug()).exitValueNormal().executeNoTimeout();
      return true;
    }
    catch (InvalidExitValueException e) {
      if (isNoSuchProcess(e)) {
        return false;
      }
      throw e;
    }
  }

  /**
   * @return <code>true</code> if this exception indicates that the process was not found (any more).
   */
  protected boolean isNoSuchProcess(InvalidExitValueException e) {
    return e.getExitValue() == EXIT_CODE_NO_SUCH_PROCESS;
  }

}

package org.zeroturnaround.process;

import java.io.IOException;

import com.sun.jna.Native;
import org.zeroturnaround.process.unix.LibC;

/**
 * Process implementation for UNIX PID values.
 * <p>
 * It uses the <code>getpgid</code> system call for checking the status and the <code>kill</code> one for
 * destroying the process.
 * </p>
 */
public class UnixProcess extends PidProcess {

  public UnixProcess(int pid) {
    super(pid);
  }

  public boolean isAlive() throws IOException {
    if (LibC.INSTANCE.getpgid(pid) != -1) {
      return true;
    }
    int errno = Native.getLastError();
    if (errno == LibC.ESRCH) {
      return false;
    }
    throw new IOException("Error getting target process group - errno = " + errno);
  }

  @Override
  public void destroy(boolean forceful) throws IOException {
    kill(forceful ? LibC.SIGKILL : LibC.SIGTERM);
  }

  /**
   * Sends a signal to this process.
   *
   * @param signal name of the signal.
   * @return <code>true</code> if this process received the signal, <code>false</code> if this process was not found (any more).
   *
   * @throws IOException on system call error.
   */
  public boolean kill(int signal) throws IOException {
    if (LibC.INSTANCE.kill(pid, signal) != -1) {
      return true;
    }
    int errno = Native.getLastError();
    if (errno == LibC.ESRCH) {
      return false;
    }
    throw new IOException("Error killing target process with signal " + signal + " - errno = " + errno);
  }

}

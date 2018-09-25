package org.zeroturnaround.process.unix;

import com.sun.jna.Library;
import com.sun.jna.Native;

public interface LibC extends Library {

  LibC INSTANCE = (LibC) Native.loadLibrary("c", LibC.class);

  /* errnos */
  int ESRCH = 3; /* No such process */

  /* signals */
  int SIGKILL = 9;
  int SIGTERM = 15;

  int kill(int pid, int signal);
  int getpgid(int pid);

}

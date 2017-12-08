package org.zeroturnaround.process;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class HandleProcess extends PidProcess {

  private final ProcessHandle handle;

  public static PidProcess of(int pid) {
    Optional<ProcessHandle> handle = ProcessHandle.of(pid);
    return handle.isPresent() ? new HandleProcess(handle.get()) : new NonExistingProcess(pid);
  }

  public HandleProcess(ProcessHandle handle) {
    super((int) handle.pid());
    this.handle = handle;
  }

  public ProcessHandle getHandle() {
    return handle;
  }

  @Override public void destroy(boolean forceful) {
    if (forceful) {
      handle.destroyForcibly();
    }
    else if (handle.supportsNormalTermination()) {
      handle.destroy();
    }
    else {
      throw new UnsupportedOperationException();
    }
  }

  @Override public boolean isAlive() {
    return handle.isAlive();
  }

  @Override public void waitFor() throws InterruptedException {
    try {
      handle.onExit().get();
    }
    catch (ExecutionException e) {
      // ignore
    }
  }

  @Override public boolean waitFor(long timeout, TimeUnit unit) throws InterruptedException {
    CompletableFuture<ProcessHandle> exit = handle.onExit();
    try {
      exit.get(timeout, unit);
    }
    catch (TimeoutException e) {
      return false;
    }
    catch (ExecutionException e) {
      // ignore
    }
    return true;
  }

}

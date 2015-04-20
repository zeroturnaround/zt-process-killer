package org.zeroturnaround.process.test.running;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;

abstract class SleepingProcessFactory {

  private static final int DEFAULT_SLEEP_DURATION = 2; // seconds

  public Process createSleepingProcess() throws Exception {
    return createSleepingProcess(DEFAULT_SLEEP_DURATION);
  }

  public Process createSleepingProcess(int seconds) throws Exception {
    return sleep(seconds).start().getProcess();
  }

  public ProcessExecutor sleep(int seconds) {
    List<String> cmd = new ArrayList<String>(Arrays.asList(getCommand()));
    cmd.add(String.valueOf(seconds));
    return new ProcessExecutor(cmd).redirectOutput(Slf4jStream.ofCaller().asDebug());
  }

  protected abstract String[] getCommand();

  protected abstract boolean supportsShutdownFile();

}

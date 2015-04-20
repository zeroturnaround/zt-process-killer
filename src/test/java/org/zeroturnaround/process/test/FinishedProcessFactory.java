package org.zeroturnaround.process.test;

import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.StartedProcess;
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;

public class FinishedProcessFactory {

  public static Process createFinishedProcess() throws Exception {
    StartedProcess sp = exitQuickly().start();
    Process process = sp.getProcess();
    sp.getFuture().get();
    return process;
  }

  public static ProcessExecutor exitQuickly() {
    return new ProcessExecutor().command("java", "-version").redirectOutput(Slf4jStream.ofCaller().asDebug());
  }

}

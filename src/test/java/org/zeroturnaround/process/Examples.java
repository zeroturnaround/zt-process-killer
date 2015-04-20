package org.zeroturnaround.process;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;

public class Examples {

  public static void getMyPid() {
    int pid = PidUtil.getMyPid();
    System.out.println("My PID is " + pid);
  }

  public static void isAlivePid() throws Exception {
    int pid = Integer.parseInt(FileUtils.readFileToString(new File("pidfile")));
    PidProcess process = Processes.newPidProcess(pid);
    boolean isAlive = process.isAlive();
    System.out.println("PID " + pid + " is alive: " + isAlive);
  }

  public static void isAliveProcess() throws Exception {
    Process p = new ProcessBuilder("my-application").start();
    JavaProcess process = Processes.newJavaProcess(p);
    boolean isAlive = process.isAlive();
    System.out.println("Process " + process + " is alive: " + isAlive);
  }

  public static void waitForPid() throws Exception {
    int pid = Integer.parseInt(FileUtils.readFileToString(new File("pidfile")));
    PidProcess process = Processes.newPidProcess(pid);
    boolean finished = process.waitFor(10, TimeUnit.MINUTES);
    System.out.println("PID " + pid + " finished on time: " + finished);
  }

  public static void waitForProcess() throws Exception {
    Process p = new ProcessBuilder("my-application").start();
    JavaProcess process = Processes.newJavaProcess(p);
    boolean finished = process.waitFor(10, TimeUnit.MINUTES);
    System.out.println("Process " + process + " finished on time: " + finished);
  }

  public static void destroyPid() throws Exception {
    int pid = Integer.parseInt(FileUtils.readFileToString(new File("pidfile")));
    PidProcess process = Processes.newPidProcess(pid);
    ProcessUtil.destroyGracefullyOrForcefullyAndWait(process, 30, TimeUnit.SECONDS, 10, TimeUnit.SECONDS);
  }

  public static void destroyProcess() throws Exception {
    Process p = new ProcessBuilder("my-application").start();
    SystemProcess process = Processes.newStandardProcess(p);
    ProcessUtil.destroyGracefullyOrForcefullyAndWait(process, 30, TimeUnit.SECONDS, 10, TimeUnit.SECONDS);
  }

}

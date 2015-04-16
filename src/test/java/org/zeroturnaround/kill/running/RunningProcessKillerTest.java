package org.zeroturnaround.kill.running;
/*
 * Copyright (C) 2013 ZeroTurnaround <support@zeroturnaround.com>
 * Contains fragments of code from Apache Commons Exec, rights owned
 * by Apache Software Foundation (ASF).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.StartedProcess;
import org.zeroturnaround.kill.BaseKillerTest;
import org.zeroturnaround.kill.Sleep;
import org.zeroturnaround.process.ProcessUtil;
import org.zeroturnaround.process.Processes;
import org.zeroturnaround.process.SystemProcess;


@RunWith(Parameterized.class)
public class RunningProcessKillerTest extends BaseKillerTest {

  private static final Logger log = LoggerFactory.getLogger(RunningProcessKillerTest.class);

  private static final SleepingProcessFactory[] SLEEPING_PROCESS_FACTORIES = new SleepingProcessFactory[] {
    new JavaSleepingProcessFactory(),
    new ScriptSleepingProcessFactory()
    };

  private final SleepingProcessFactory sleepingProcessFactory;

  private final ProcessFactory factory;

  private Process javaLangProcess;

  @Parameters
  public static Collection<Object[]> data() {
    Collection<Object[]> result = new ArrayList<Object[]>();
    for (SleepingProcessFactory sleepingProcessFactory : SLEEPING_PROCESS_FACTORIES) {
      for (ProcessFactory factory : PROCESS_FACTORIES) {
        result.add(new Object[] { sleepingProcessFactory, factory });
      }
    }
    return result;
  }

  public RunningProcessKillerTest(SleepingProcessFactory sleepingProcessFactory, ProcessFactory factory) throws Exception {
    this.sleepingProcessFactory = sleepingProcessFactory;
    this.factory = factory;
  }

  @Before
  public void init() throws IOException {
    File file = Sleep.getFile();
    if (file.exists()) {
      FileUtils.forceDelete(file);
    }
  }

  @After
  public void after() throws InterruptedException {
    if (javaLangProcess != null) {
      javaLangProcess.waitFor();
      javaLangProcess = null;
    }
  }

  @Test(timeout=5000)
  public void testIsAliveAndWait() throws Exception {
    javaLangProcess = sleepingProcessFactory.createSleepingProcess();
    SystemProcess process = factory.create(javaLangProcess);
    assertTrue(process.isAlive());
    process.waitFor();
    assertFalse(process.isAlive());
  }

  @Test(timeout=15000)
  public void testWaitWithTimeout() throws Exception {
    javaLangProcess = sleepingProcessFactory.createSleepingProcess();
    SystemProcess process = factory.create(javaLangProcess);
    assertTrue(process.waitFor(5, TimeUnit.SECONDS));
  }

  @Test(timeout=10000)
  public void testWaitWithTimeoutExceeds() throws Exception {
    javaLangProcess = sleepingProcessFactory.createSleepingProcess();
    SystemProcess killer = factory.create(javaLangProcess);
    assertFalse(killer.waitFor(1, TimeUnit.SECONDS));
  }

  @Test(timeout=5000)
  public void testDestroy() throws Exception {
    javaLangProcess = sleepingProcessFactory.createSleepingProcess();
    SystemProcess process = factory.create(javaLangProcess);
    ProcessUtil.destroyGracefullyOrForcefullyAndWait(process);
    assertFalse(process.isAlive());
  }

  @Test(timeout=5000)
  public void testDestroyGracefully() throws Exception {
    StartedProcess sp = sleepingProcessFactory.sleep(6).readOutput(true).start();
    javaLangProcess = sp.getProcess();
    SystemProcess process = factory.create(javaLangProcess);
    try {
      // Let the main method start
      Thread.sleep(1000);

      process.destroyGracefully().waitFor();
      assertFalse(process.isAlive());
      if (sleepingProcessFactory.supportsShutdownFile()) {
        // Shutdown hook must have been started
        assertTrue(Sleep.getFile().exists());
      }
    }
    catch (UnsupportedOperationException e) {
      if (isDestroyGracefullySupported(process)) {
        throw e;
      }
    }
  }

  @Test(timeout=5000)
  public void testDestroyForcefully() throws Exception {
    StartedProcess sp = sleepingProcessFactory.sleep(6).readOutput(true).start();
    javaLangProcess = sp.getProcess();
    SystemProcess process = factory.create(javaLangProcess);
    try {
      // Let the main method start
      Thread.sleep(1000);

      process.destroyForcefully().waitFor();
      assertFalse(process.isAlive());
      if (sleepingProcessFactory.supportsShutdownFile()) {
        // Shutdown hook must not have been started
        assertFalse(Sleep.getFile().exists());
      }
    }
    catch (UnsupportedOperationException e) {
      if (isDestroyForcefullySupported(process)) {
        throw e;
      }
    }
  }

  // Killer Factories

  private static final ProcessFactory[] PROCESS_FACTORIES = new ProcessFactory[] {
    new ProcessKillerFactory(),
    new PidKillerFactory(),
    new StandardKillFactory()
  };

  private static interface ProcessFactory {
    SystemProcess create(Process process);
  }

  private static class ProcessKillerFactory implements ProcessFactory {
    public SystemProcess create(Process process) {
      return Processes.newJavaProcess(process);
    }
  }

  private static class PidKillerFactory implements ProcessFactory {
    public SystemProcess create(Process process) {
      return Processes.newPidProcess(process);
    }
  }

  private static class StandardKillFactory implements ProcessFactory {
    public SystemProcess create(Process process) {
      return Processes.newStandardProcess(process);
    }
  }

}

package org.zeroturnaround.process.test.running;
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
import org.zeroturnaround.process.test.Action;
import org.zeroturnaround.process.test.BaseKillerTest;
import org.zeroturnaround.process.test.Sleep;
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

  @Test
  public void testIsAliveAndWait() throws Exception {
    javaLangProcess = sleepingProcessFactory.createSleepingProcess();
    timeout(15, TimeUnit.SECONDS, new Action() {
      @Override
      public void run() throws Exception {
        SystemProcess process = factory.create(javaLangProcess);
        assertTrue(process.isAlive());
        process.waitFor();
        assertFalse(process.isAlive());
      }
    });
  }

  @Test
  public void testWaitWithTimeout() throws Exception {
    javaLangProcess = sleepingProcessFactory.createSleepingProcess();
    SystemProcess process = factory.create(javaLangProcess);
    assertTrue(process.waitFor(15, TimeUnit.SECONDS));
  }

  @Test
  public void testWaitWithTimeoutExceeds() throws Exception {
    javaLangProcess = sleepingProcessFactory.createSleepingProcess();
    SystemProcess killer = factory.create(javaLangProcess);
    assertFalse("Expected that process does not finish in 1 second.", killer.waitFor(1, TimeUnit.SECONDS));
  }

  @Test
  public void testDestroy() throws Exception {
    javaLangProcess = sleepingProcessFactory.createSleepingProcess();
    timeout(15, TimeUnit.SECONDS, new Action() {
      @Override
      public void run() throws Exception {
        SystemProcess process = factory.create(javaLangProcess);
        ProcessUtil.destroyGracefullyOrForcefullyAndWait(process);
        assertFalse(process.isAlive());
      }
    });
  }

  @Test
  public void testDestroyGracefully() throws Exception {
    final StartedProcess sp = sleepingProcessFactory.sleep(15).readOutput(true).start();
    // Use a bit shorter timeout than the process sleeps
    timeout(14, TimeUnit.SECONDS, new Action() {
      @Override
      public void run() throws Exception {
        javaLangProcess = sp.getProcess();
        SystemProcess process = factory.create(javaLangProcess);
        try {
          // Let the main method start
          Thread.sleep(1000);

          process.destroyGracefully().waitFor();
          assertFalse(process.isAlive());
          if (sleepingProcessFactory.supportsShutdownFile()) {
            // Shutdown hook must have been started
            assertTrue("Expected to find " + Sleep.getFile().getAbsolutePath() + " to indicate that shutdown hooks were started.", Sleep.getFile().exists());
          }
        }
        catch (UnsupportedOperationException e) {
          if (isDestroyGracefullySupported(process)) {
            throw e;
          }
        }
      }
    });
  }

  @Test
  public void testDestroyForcefully() throws Exception {
    final StartedProcess sp = sleepingProcessFactory.sleep(15).readOutput(true).start();
    // Use a bit shorter timeout than the process sleeps
    timeout(14, TimeUnit.SECONDS, new Action() {
      @Override
      public void run() throws Exception {
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
    });
  }

  // Process Factories

  private static final ProcessFactory[] PROCESS_FACTORIES = new ProcessFactory[] {
    new JavaProcessFactory(),
    new PidKFactory(),
    new StandardFactory()
  };

  private interface ProcessFactory {
    SystemProcess create(Process process);
  }

  private static class JavaProcessFactory implements ProcessFactory {
    public SystemProcess create(Process process) {
      return Processes.newJavaProcess(process);
    }
  }

  private static class PidKFactory implements ProcessFactory {
    public SystemProcess create(Process process) {
      return Processes.newPidProcess(process);
    }
  }

  private static class StandardFactory implements ProcessFactory {
    public SystemProcess create(Process process) {
      return Processes.newStandardProcess(process);
    }
  }

}

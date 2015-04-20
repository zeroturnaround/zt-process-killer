package org.zeroturnaround.process.test;
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


import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.process.ProcessUtil;
import org.zeroturnaround.process.Processes;
import org.zeroturnaround.process.SystemProcess;


@RunWith(Parameterized.class)
public class FinishedProcessKillerTest extends BaseKillerTest {

  private static final Logger log = LoggerFactory.getLogger(FinishedProcessKillerTest.class);

  private final SystemProcess process;

  @Parameters
  public static Collection<Object[]> data() throws Exception {
    Process process = FinishedProcessFactory.createFinishedProcess();

    Collection<Object[]> result = new ArrayList<Object[]>();
    result.add(new Object[] { Processes.newJavaProcess(process) });
    result.add(new Object[] { Processes.newPidProcess(process) });
    result.add(new Object[] { Processes.newStandardProcess(process) });
    return result;
  }

  public FinishedProcessKillerTest(SystemProcess process) throws Exception {
    this.process = process;
  }

  @Test
  public void testBasic() throws Exception {
    assertNotNull(process);
    assertNotNull(process.toString());
  }

  @Test
  public void testIsAlive() throws Exception {
    assertFalse(process.isAlive());
  }

  @Test(timeout=5000)
  public void testWait() throws Exception {
    process.waitFor();
  }

  @Test(timeout=5000)
  public void testWaitWithTimeout() throws Exception {
    process.waitFor(1, TimeUnit.SECONDS);
  }

  @Test(timeout=5000)
  public void testDestroy() throws Exception {
    ProcessUtil.destroyGracefullyOrForcefullyAndWait(process);
  }

  @Test(timeout=5000)
  public void testDestroyGracefully() throws Exception {
    try {
      process.destroyGracefully().waitFor();
      assertTrue("UnsupportedOperationException expected", isDestroyGracefullySupported(process));
    }
    catch (UnsupportedOperationException e) {
      if (isDestroyGracefullySupported(process)) {
        throw e;
      }
    }
  }

  @Test(timeout=5000)
  public void testDestroyForcefully() throws Exception {
    try {
      process.destroyForcefully().waitFor();
      assertTrue("UnsupportedOperationException expected", isDestroyForcefullySupported(process));
    }
    catch (UnsupportedOperationException e) {
      if (isDestroyForcefullySupported(process)) {
        throw e;
      }
    }
  }

}

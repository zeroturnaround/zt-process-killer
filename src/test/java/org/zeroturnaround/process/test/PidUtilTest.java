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


import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.process.PidUtil;


public class PidUtilTest {

  private static final Logger log = LoggerFactory.getLogger(PidUtilTest.class);

  @Test
  public void testMyPid() {
    // Check that we don't throw an exception here
    PidUtil.getMyPid();
  }

  @Test(expected=IllegalArgumentException.class)
  public void testNull() throws Exception {
    PidUtil.getPid(null);
  }

  @Test
  public void testFinishedProcess() throws Exception {
    Process process = FinishedProcessFactory.createFinishedProcess();
    PidUtil.getPid(process);
  }

}

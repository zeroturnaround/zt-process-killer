package org.zeroturnaround.process.test.running;

import java.io.File;

import org.apache.commons.lang3.SystemUtils;

class ScriptSleepingProcessFactory extends SleepingProcessFactory {

  private static final File DIR = new File("src/test/resources");

  @Override
  protected String[] getCommand() {
    if (SystemUtils.IS_OS_WINDOWS) {
      return new String[] {new File(DIR, "sleep.cmd").toString()};
    }
    return new String[] {"bash", new File(DIR, "sleep.sh").toString()};
  }

  @Override
  protected boolean supportsShutdownFile() {
    return false;
  }

}

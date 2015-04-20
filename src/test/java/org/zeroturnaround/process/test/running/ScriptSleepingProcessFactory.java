package org.zeroturnaround.process.test.running;

import org.apache.commons.lang.SystemUtils;

class ScriptSleepingProcessFactory extends SleepingProcessFactory {

  @Override
  protected String[] getCommand() {
    if (SystemUtils.IS_OS_WINDOWS) {
      return new String[] {"target/test-classes/sleep.cmd"};
    }
    return new String[] {"bash", "target/test-classes/sleep.sh"};
  }

  @Override
  protected boolean supportsShutdownFile() {
    return false;
  }

}

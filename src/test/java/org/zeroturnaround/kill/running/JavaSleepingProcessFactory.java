package org.zeroturnaround.kill.running;

import org.zeroturnaround.kill.Sleep;

class JavaSleepingProcessFactory extends SleepingProcessFactory {

  @Override
  protected String[] getCommand() {
    return new String[] {"java", "-cp", "target/test-classes", Sleep.class.getName()};
  }

  @Override
  protected boolean supportsShutdownFile() {
    return true;
  }

}

package org.zeroturnaround.process.test.running;

import org.zeroturnaround.process.test.Sleep;

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

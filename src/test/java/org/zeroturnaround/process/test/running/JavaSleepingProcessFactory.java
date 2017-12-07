package org.zeroturnaround.process.test.running;

import java.io.File;

import org.zeroturnaround.process.test.Sleep;

class JavaSleepingProcessFactory extends SleepingProcessFactory {

  private static final File MAVEN_DIR = new File("target/test-classes");
  private static final File GRADLE_DIR = new File("build/classes/java/test");
  private static final File DIR = GRADLE_DIR.exists() ? GRADLE_DIR : MAVEN_DIR;

  @Override
  protected String[] getCommand() {
    return new String[] {"java", "-cp", DIR.toString(), Sleep.class.getName()};
  }

  @Override
  protected boolean supportsShutdownFile() {
    return true;
  }

}

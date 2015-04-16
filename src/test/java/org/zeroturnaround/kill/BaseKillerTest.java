package org.zeroturnaround.kill;

import org.apache.commons.lang.SystemUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.zeroturnaround.process.Java8Process;
import org.zeroturnaround.process.JavaProcess;
import org.zeroturnaround.process.SystemProcess;

public class BaseKillerTest extends Assert {

  @Rule
  public final TestRule logRule = new TestRunnerLogRule();

  // Support checks

  protected static boolean isDestroyGracefullySupported(SystemProcess process) {
    return !SystemUtils.IS_OS_WINDOWS;
  }

  protected static boolean isDestroyForcefullySupported(SystemProcess process) {
    return SystemUtils.IS_OS_WINDOWS || !(process instanceof JavaProcess) || Java8Process.isSupported();
  }

}

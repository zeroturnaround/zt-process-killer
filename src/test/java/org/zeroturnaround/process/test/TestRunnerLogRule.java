package org.zeroturnaround.process.test;

import org.junit.internal.AssumptionViolatedException;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logs events about running the tests.
 */
public class TestRunnerLogRule extends TestWatcher {

  private static final Logger log = LoggerFactory.getLogger(TestRunnerLogRule.class);

  @Override
  protected void skipped(AssumptionViolatedException e, Description description) {
    log.debug("{} skipped.", description);
  }

  @Override
  protected void starting(Description description) {
    log.debug("{} started.", description);
  }

  @Override
  protected void succeeded(Description description) {
    log.debug("{} success!", description);
  }

  @Override
  protected void failed(Throwable e, Description description) {
    log.debug("{} failed!", description, e);
  }

}

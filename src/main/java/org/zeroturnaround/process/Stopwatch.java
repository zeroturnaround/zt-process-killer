package org.zeroturnaround.process;

import java.util.concurrent.TimeUnit;

/**
 * Simple stopwatch implementation.
 */
class Stopwatch {

  private final long start;
  private volatile long stop;

  private Stopwatch(long start) {
    this.start = start;
  }

  public static Stopwatch createStarted() {
    return new Stopwatch(System.currentTimeMillis());
  }

  public Stopwatch stop() {
    stop = System.currentTimeMillis();
    return this;
  }

  public long elapsed(TimeUnit desiredUnit) {
    return desiredUnit.convert(stop - start, TimeUnit.MILLISECONDS);
  }

}
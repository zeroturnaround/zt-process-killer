package org.zeroturnaround.process;

import java.io.IOException;
import java.util.List;

/**
 * Represents single system process containing alternative {@link SystemProcess} implementation to control it.
 * Children are expected to throw {@link UnsupportedOperationException} for certain operations.
 * <p>
 * For any operation we try each children until it doesn't throw {@link UnsupportedOperationException}.
 * If all of them throw it we throw it as well.
 * </p>
 * <p>
 * If it has only one child it should act the same as this child alone.
 * </p>
 */
public class OrProcess extends CompositeProcess {

  public OrProcess(List<? extends SystemProcess> children) {
    super(children);
  }

  public boolean isAlive() throws IOException, InterruptedException {
    for (SystemProcess child : children) {
      try {
        return child.isAlive();
      }
      catch (UnsupportedOperationException e) {
        // continue
      }
    }
    throw new UnsupportedOperationException();
  }

  @Override
  public void waitFor() throws InterruptedException {
    for (SystemProcess child : children) {
      try {
        child.waitFor();
        return;
      }
      catch (UnsupportedOperationException e) {
        // continue
      }
    }
    throw new UnsupportedOperationException();
  }

  @Override
  public void destroy(boolean forceful) throws IOException, InterruptedException {
    for (SystemProcess child : children) {
      try {
        invokeDestroy(child, forceful);
        return;
      }
      catch (UnsupportedOperationException e) {
        // continue
      }
    }
    throw new UnsupportedOperationException();
  }

}

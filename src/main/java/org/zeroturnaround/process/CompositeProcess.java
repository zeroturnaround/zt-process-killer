package org.zeroturnaround.process;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains other {@link SystemProcess}es preserving their order.
 */
public abstract class CompositeProcess extends AbstractProcess {

  protected final List<? extends SystemProcess> children;

  public CompositeProcess(List<? extends SystemProcess> children) {
    this.children = children;
  }

  @Override
  public String getDescription() {
    List<String> result = new ArrayList<String>();
    for (SystemProcess child : children) {
      result.add(child.toString());
    }
    return result.toString();
  }

  @Override
  public String toString() {
    List<String> result = new ArrayList<String>();
    for (SystemProcess child : children) {
      result.add(child.toString());
    }
    return getClass().getSimpleName() + result.toString();
  }

  protected static void invokeDestroy(SystemProcess killer, boolean forceful) throws IOException, InterruptedException {
    if (forceful) {
      killer.destroyForcefully();
    }
    else {
      killer.destroyGracefully();
    }
  }

}

/*
 * Copyright (C) 2013 ZeroTurnaround <support@zeroturnaround.com>
 * Contains fragments of code from Apache Commons Exec, rights owned
 * by Apache Software Foundation (ASF).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zeroturnaround.process.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Sleep implements Runnable {

  private static final File SHTUDOWN_HOOK_FILE = new File("shutdown.started");

  public static File getFile() {
    return SHTUDOWN_HOOK_FILE;
  }

  public static void main(String[] args) throws InterruptedException {
    delete(SHTUDOWN_HOOK_FILE);
    Runtime.getRuntime().addShutdownHook(new Thread(new Sleep()));
    int seconds = Integer.parseInt(args[0]);
    System.out.format("Sleeping %d seconds...%n", seconds);
    Thread.sleep(seconds * 1000);
    System.out.println("Finished!");
  }

  public void run() {
    System.out.println("Shutting down...");
    // Create file
    try {
      new FileOutputStream(SHTUDOWN_HOOK_FILE).close();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static void delete(File file) {
    if (file.exists() && !file.delete())
      throw new IllegalStateException("Could not delete " + file);
  }

}

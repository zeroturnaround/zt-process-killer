# System Process Utils

### Continuous Integration 
[![Build Status](https://travis-ci.org/zeroturnaround/zt-process.png)](https://travis-ci.org/zeroturnaround/zt-process)

### Quick Overview

The project was created in [ZeroTurnaround](http://zeroturnaround.com/) to have a stable base functionality of stopping running processes from Java.
It can stop processes started from Java as well as existing system processes based on their process ID (PID).

### Installation

TODO

### Motivation

In Java [Process.destroy()](https://docs.oracle.com/javase/8/docs/api/java/lang/Process.html#destroy--) is ambiguous.
On Windows it terminates processes forcibly. On UNIX it terminates them gracefully.
Sine Java 8 [java.lang.Process](https://docs.oracle.com/javase/8/docs/api/java/lang/Process.html) has additional methods which we would also like to use in earlier Java versions.
As invoking **kill** commands from Java is errorprone it should have an advised API and good test coverage.

We had the following functional requirements:

* check whether a process is alive
* wait until a process has finished
* terminate a process gracefully (by default disabled on Windows as it's unsupported - [WindowsProcess](https://github.com/zeroturnaround/zt-process/blob/master/src/main/java/org/zeroturnaround/process/WindowsProcess.java))
* terminate a process forcibly
* get the process ID (PID) of running JVM

and these non-functional requirements:

* abstraction of processes regardless they are started from Java or not (use process ID) - [SystemProcess](https://github.com/zeroturnaround/zt-process/blob/master/src/main/java/org/zeroturnaround/process/SystemProcess.java)
* have process API similar to [java.lang.Process](https://docs.oracle.com/javase/8/docs/api/java/lang/Process.html) 
* all waiting methods should have one version with timeout and another without it 
* stopping operation should be idempotent - if a process was already finished it is a success 
* separate generic behavior from process implementation - [ProcessUtil](https://github.com/zeroturnaround/zt-process/blob/master/src/main/java/org/zeroturnaround/process/ProcessUtil.java)
* support alternative implementations for stopping same process - [OrProcess](https://github.com/zeroturnaround/zt-process/blob/master/src/main/java/org/zeroturnaround/process/OrProcess.java)
* simple factory for creating process instances - [Processes](https://github.com/zeroturnaround/zt-process/blob/master/src/main/java/org/zeroturnaround/process/Processes.java)
* invoke Java 8 **java.lang.Process** methods if possible - [Java8Process](https://github.com/zeroturnaround/zt-process/blob/master/src/main/java/org/zeroturnaround/process/Java8Process.java)

Limitations:

* As the process abstraction is also used for already running processes it can't access their streams or exit value.
* The scope of this project is stopping single processes. However [WindowsProcess](https://github.com/zeroturnaround/zt-process/blob/master/src/main/java/org/zeroturnaround/process/WindowsProcess.java)
has method **setIncludeChildren** to also stop child processes in case the root process is still running.

### Examples

TODO

package org.zeroturnaround.process;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper methods for Reflection API.
 */
class ReflectionUtil {

  private static final Logger log = LoggerFactory.getLogger(ReflectionUtil.class);

  public static Object invokeWithoutDeclaredExceptions(Method method, Object target, Object... args) {
    try {
      return doInvoke(method, target, args);
    }
    catch (Throwable t) {
      throw uncheck(t);
    }
  }

  public static Object invokeWithInterruptedException(Method method, Object target, Object... args) throws InterruptedException {
    try {
      return doInvoke(method, target, args);
    }
    catch (InterruptedException e) {
      throw e;
    }
    catch (Throwable t) {
      throw uncheck(t);
    }
  }

  private static Object doInvoke(Method method, Object target, Object... args) throws Throwable {
    try {
      return method.invoke(target, args);
    }
    catch (InvocationTargetException e) {
      throw e.getCause();
    }
  }

  private static RuntimeException uncheck(Throwable t) {
    if (t instanceof RuntimeException) {
      throw (RuntimeException) t;
    }
    if (t instanceof Error) {
      throw (Error) t;
    }
    throw new UndeclaredThrowableException(t);
  }

  public static Method getMethodOrNull(Class<?> klass, String name, Class<?>... parameterTypes) {
    try {
      return klass.getMethod(name, parameterTypes);
    }
    catch (Exception e) {
      log.trace("Could not find {} method {}", klass, name);
      return null;
    }
  }

}

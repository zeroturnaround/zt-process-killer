package org.zeroturnaround.process;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;

/**
 * Helper methods for Reflection API.
 */
class ReflectionUtil {

  public static Object invokeWithoutDeclaredExceptions(Method method, Object target, Object... args) {
    try {
      return doInvoke(method, target, args);
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

}

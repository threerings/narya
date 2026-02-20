package com.threerings.presents.server;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.samskivert.util.MethodFinder;

import com.threerings.io.ObjectOutputStream;

import com.threerings.presents.client.InvocationService;
import com.threerings.presents.data.InvocationCodes;
import com.threerings.presents.server.InvocationException;
import com.threerings.presents.server.InvocationProvider;

import static com.threerings.presents.Log.log;

/**
 * Utility methods for getting an InvocationService proxy for a InvocationProvider.
 * This lets you view the methods on your FooProvider on the server through a
 * FooService interface. This is used by multi-noded servers that have peer services
 * to each other when they want to have one code path for making requests whether it's
 * handeled locally or remotely.
 */
public class InvocationProxy
{
  /** If true, create proxies in development mode: validate all arguments are streamable. */
  public static boolean validate =
    Boolean.getBoolean("com.threerings.presents.server.InvocationProxy.validate");

  /**
   * Get an InvocationService method for calling services directly on the provided provider
   * implementation.
   * Note: No checking is done to make sure that the specified provider is in any way related
   * to the supplied service provider. Don't screw up!
   */
  public static <S extends InvocationService<?>> S getProxy (
    Class<S> serviceClass, final InvocationProvider provider)
  {
    return getProxy(serviceClass,
      new ServiceHandler(serviceClass) {
        protected Object invokeMethod (String name, Object[] args)
          throws Throwable
        {
          // make a new arg array including the ClientObject (null)
          Object[] pArgs = new Object[args.length + 1];
          System.arraycopy(args, 0, pArgs, 1, args.length);
          Method pMethod = methodFinder.findMethod(name, pArgs);
          return pMethod.invoke(provider, pArgs);
        }
        final MethodFinder methodFinder = new MethodFinder(provider.getClass());
      });
  }

  /**
   * Get an InvocationService that fails on every request with the specified message.
   */
  public static <S extends InvocationService<?>> S getFailureProxy (
    Class<S> serviceClass, final String failureMessage)
  {
    return getProxy(serviceClass,
      new ServiceHandler(serviceClass) {
        protected Object invokeMethod (String name, Object[] args)
          throws Throwable
        {
          throw new InvocationException(failureMessage);
        }
      });
  }

  /**
   * Convenience method to construct and cast a proxy.
   */
  protected static <S> S getProxy (Class<S> serviceClass, InvocationHandler handler)
  {
    if (validate) {
      final InvocationHandler base = handler;
      handler = (proxy, method, args) -> {
        for (Object o : args) {
          if (!(o instanceof InvocationService.InvocationListener)) {
            ensureStreamable(o);
          }
        }
        return base.invoke(proxy, method, args);
      };
    }

    return serviceClass.cast(Proxy.newProxyInstance(serviceClass.getClassLoader(),
        new Class<?>[] { serviceClass }, handler));
  }

  /**
   * Ensure that the args are all streamable.
   */
  protected static void ensureStreamable (Object anything)
  {
    try {
      new ObjectOutputStream(new ByteArrayOutputStream()).writeObject(anything);

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * A helper class that takes care of finding the InvocationListeners and properly dealing
   * with exceptions.
   */
  protected static abstract class ServiceHandler
    implements InvocationHandler
  {
    public ServiceHandler (Class<? extends InvocationService<?>> clazz)
    {
      _serviceClass = clazz;
    }

    // from InvocationHandler
    public final Object invoke (Object proxy, Method method, Object[] args)
    {
      // first locate the "primary listener"
      InvocationService.InvocationListener listener = null;
      for (Object o : args) {
        if (o instanceof InvocationService.InvocationListener) {
          listener = (InvocationService.InvocationListener)o;
          break;
        }
      }

      try {
        return invokeMethod(method.getName(), args);

      } catch (Throwable t) {
        if (listener == null) {
          log.warning("No listener for uncaught exception in InvocationProxy",
            "serviceClass", _serviceClass, "method", method.getName(), t);
          // nothing to report back to!
          return null;
        }

        // see if we can find an InvocationException in the chain
        for (Throwable cause = t; cause != null; cause = cause.getCause()) {
          if (cause instanceof InvocationException) {
            listener.requestFailed(cause.getMessage());
            return null;
          }
        }

        // unknown error
        log.warning("Throwable in InvocationProxy",
          "serviceClass", _serviceClass, "method", method.getName(), t);
        listener.requestFailed(InvocationCodes.INTERNAL_ERROR);
        return null;
      }
    }

    /**
     * Implement to invoke the service method.
     */
    protected abstract Object invokeMethod (String name, Object[] args) throws Throwable;

    /** The service class. */
    protected final Class<? extends InvocationService<?>> _serviceClass;
  }
}

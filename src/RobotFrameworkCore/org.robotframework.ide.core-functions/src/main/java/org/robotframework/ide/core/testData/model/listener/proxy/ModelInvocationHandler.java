package org.robotframework.ide.core.testData.model.listener.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.listener.IRobotModelEventDispatcher;
import org.robotframework.ide.core.testData.model.listener.proxy.IMethodInvocationConverter.IMethodDescriptor;
import org.robotframework.ide.core.testData.model.listener.proxy.IMethodInvocationConverter.IMethodDescriptor.MatcherType;

import com.google.common.annotations.VisibleForTesting;


public class ModelInvocationHandler<T> implements InvocationHandler {

    private List<Class<?>> declaredListenInterfaces = new LinkedList<>();
    private List<IMethodInvocationConverter<T>> methodEventsMappers = new LinkedList<>();
    private final IRobotModelEventDispatcher dispatcher;
    private final T originalObject;


    private ModelInvocationHandler(final T originalObject,
            final IRobotModelEventDispatcher dispatcher,
            Class<?>... listenInterfaces) {
        this.dispatcher = dispatcher;
        declaredListenInterfaces.addAll(Arrays.asList(listenInterfaces));

        this.originalObject = originalObject;
    }


    @SuppressWarnings("unchecked")
    public static <T> T createListener(final T originalObject,
            final IRobotModelEventDispatcher dispatcher,
            Class<?>... listenInterfaces) {
        List<Class<?>> temp = new LinkedList<>(Arrays.asList(listenInterfaces));
        temp.addAll(Arrays.asList(originalObject.getClass().getInterfaces()));
        Class<?>[] interfaces = new Class<?>[temp.size()];

        return (T) Proxy.newProxyInstance(originalObject.getClass()
                .getClassLoader(), temp.toArray(interfaces),
                new ModelInvocationHandler<T>(originalObject, dispatcher,
                        listenInterfaces));
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
        Object toReturn = method.invoke(originalObject, args);

        Class<?> declaringClass = method.getDeclaringClass();
        if (declaredListenInterfaces.contains(declaringClass)) {
            IMethodInvocationConverter<T> converter = findMapper(method, args);
            if (converter != null) {
                dispatcher.dispatchEvent(converter.map(originalObject, method,
                        args));
            }
        }

        return toReturn;
    }


    @VisibleForTesting
    protected IMethodInvocationConverter<T> findMapper(Method method,
            Object[] args) {
        IMethodInvocationConverter<T> converter = null;
        for (IMethodInvocationConverter<T> conv : methodEventsMappers) {
            IMethodDescriptor descriptor = conv.getDescriptor();
            if (descriptor.isApplicableFor(method)
                    && descriptor.isApplicableFor(args)) {
                if (converter == null) {
                    converter = conv;
                }

                if (descriptor.getType() == MatcherType.EXACTLY) {
                    converter = conv;
                    break;
                }
            }
        }

        return converter;
    }
}

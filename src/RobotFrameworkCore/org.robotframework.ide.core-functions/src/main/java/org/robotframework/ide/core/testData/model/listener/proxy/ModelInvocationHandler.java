package org.robotframework.ide.core.testData.model.listener.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


public class ModelInvocationHandler<T> implements InvocationHandler {

    private List<Class<?>> declaredListenInterfaces = new LinkedList<>();
    private final T originalObject;


    private ModelInvocationHandler(final T originalObject,
            Class<?>... listenInterfaces) {
        declaredListenInterfaces.addAll(Arrays.asList(listenInterfaces));

        this.originalObject = originalObject;
    }


    @SuppressWarnings("unchecked")
    public static <T> T createListener(final T originalObject,
            Class<?>... listenInterfaces) {
        List<Class<?>> temp = new LinkedList<>(Arrays.asList(listenInterfaces));
        temp.addAll(Arrays.asList(originalObject.getClass().getInterfaces()));
        Class<?>[] interfaces = new Class<?>[temp.size()];

        return (T) Proxy
                .newProxyInstance(originalObject.getClass().getClassLoader(),
                        temp.toArray(interfaces),
                        new ModelInvocationHandler<T>(originalObject,
                                listenInterfaces));
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
        Object toReturn = method.invoke(originalObject, args);

        return toReturn;
    }
}

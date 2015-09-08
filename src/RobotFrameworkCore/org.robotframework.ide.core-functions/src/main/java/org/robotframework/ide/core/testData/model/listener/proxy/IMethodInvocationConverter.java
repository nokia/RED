package org.robotframework.ide.core.testData.model.listener.proxy;

import java.lang.reflect.Method;

import org.robotframework.ide.core.testData.model.listener.ARobotModelEvent;


public interface IMethodInvocationConverter<T> {

    ARobotModelEvent<T> map(final T eventSource, final Method invokedMethod,
            final Object[] methodGotArguments);


    IMethodDescriptor getDescriptor();

    public interface IMethodDescriptor {

        MatcherType getType();


        boolean isApplicableFor(final Method m);


        boolean isApplicableFor(final Object[] arguments);

        public enum MatcherType {
            EXACTLY, PATTERN;
        }
    }
}

/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.execution.creation;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.lang.model.element.Modifier;

abstract class ATestFilesCompareStore {

    private final AtomicBoolean wasValidated = new AtomicBoolean(false);

    void validate() throws InvalidTestStoreException {
        final List<String> errors = new ArrayList<>(0);
        errors.addAll(collectMismatchesForNotNullValidation());

        wasValidated.set(true);
        if (!errors.isEmpty()) {
            throw new InvalidTestStoreException(errors);
        }
    }

    boolean wasValidated() {
        return wasValidated.get();
    }

    List<String> collectMismatchesForNotNullValidation() {
        final List<String> errors = new ArrayList<>(0);
        final Class<ValidateNotNull> ano = ValidateNotNull.class;
        final List<Method> publicMethodsAnnotatedWith = getPublicMethodsAnnotatedWith(ano);
        for (final Method method : publicMethodsAnnotatedWith) {
            final ValidateNotNull validError = method.getAnnotation(ano);
            try {
                final Object invoke = method.invoke(this);
                if (invoke == null || ((String) invoke).isEmpty()) {
                    errors.add("Method \'" + method.getName() + "\' should return null for file path "
                            + validError.errorParameterMsg());
                }
            } catch (final Exception e) {
                errors.add("Problem found when \'" + validError.errorParameterMsg() + "\' with message " + e);
            }
        }

        return errors;
    }

    List<Method> getPublicMethodsAnnotatedWith(final Class<? extends Annotation> ano) {
        final List<Method> methodsToCheck = new ArrayList<>(0);

        final Method[] declaredMethods = this.getClass().getDeclaredMethods();
        for (final Method method : declaredMethods) {
            if (Modifier.PUBLIC.ordinal() == method.getModifiers()) {
                if (method.getAnnotation(ano) != null) {
                    methodsToCheck.add(method);
                }
            }
        }

        return methodsToCheck;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.METHOD })
    @interface ValidateNotNull {

        String errorParameterMsg();
    }

    static class InvalidTestStoreException extends Exception {

        private static final long serialVersionUID = 3123604043036477588L;

        public InvalidTestStoreException(final List<String> errors) {
            super(String.join("\nerror: ", errors));
        }
    }

}

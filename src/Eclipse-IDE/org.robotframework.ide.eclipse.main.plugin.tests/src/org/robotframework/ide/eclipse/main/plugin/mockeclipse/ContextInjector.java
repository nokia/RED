/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.mockeclipse;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

/**
 * @author Michal Anglart
 *
 */
public class ContextInjector {

    private final Map<Class<?>, Object> objectsToInject = new HashMap<>();
    
    private ContextInjector() {
        // just hiding
    }
    
    public static ContextInjector prepareContext() {
        return new ContextInjector();
    }

    public ContextInjector with(final Object object) {
        objectsToInject.put(object.getClass(), object);
        return this;
    }

    public <T> T inject(final T object) {
        Class<?> clazz = object.getClass();

        try {
            while (clazz != Object.class) {
                for (final Field field : clazz.getDeclaredFields()) {
                    if (field.getAnnotation(Inject.class) != null
                            && ((field.getModifiers() & Modifier.PUBLIC) != 0)
                            || ((field.getModifiers() & Modifier.PROTECTED) != 0)) {

                        field.setAccessible(true);
                        for (final Entry<Class<?>, Object> binding : objectsToInject.entrySet()) {
                            final Class<?> actualClass = field.getType();
                            if (actualClass.isAssignableFrom(binding.getKey())) {
                                field.set(object, binding.getValue());
                                break;
                            }
                        }
                    }
                }
                clazz = clazz.getSuperclass();
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new IllegalStateException("Unable to bind fields", e);
        }
        return object;
    }
}

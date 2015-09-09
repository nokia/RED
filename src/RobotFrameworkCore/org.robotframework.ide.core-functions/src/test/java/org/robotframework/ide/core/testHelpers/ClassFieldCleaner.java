/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testHelpers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

import org.junit.After;


/**
 * Utility for clean all fields in test class dynamically without any extra code
 * to put in tear down. Only needs is to add following line
 * {@code ClassFieldCleaner.init(this); }
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 */
public class ClassFieldCleaner {

    /**
     * Should be invocated inside {@link After} method block
     * 
     * @param o
     *            test class object
     * @throws IllegalArgumentException
     *             in case set of null value will fail
     * @throws IllegalAccessException
     *             in case setting accessibility will fail
     */
    public static void init(Object o) throws IllegalArgumentException,
            IllegalAccessException {
        if (o != null) {
            Field[] declaredFields = o.getClass().getDeclaredFields();
            if (declaredFields != null) {
                for (Field f : declaredFields) {
                    if (f.isAnnotationPresent(ForClean.class)) {
                        boolean accessible = f.isAccessible();
                        if (!accessible) {
                            f.setAccessible(true);
                        }

                        f.set(o, null);

                        if (!accessible) {
                            f.setAccessible(false);
                        }
                    }
                }
            }
        }
    }

    /**
     * Marker for fields to clean in every
     * {@link ClassFieldCleaner#init(Object)} invocation
     * 
     * @author wypych
     * @since JDK 1.7 update 74
     * @version Robot Framework 2.9 alpha 2
     * 
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface ForClean {

    }
}

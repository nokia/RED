/*
 * Copyright 2020 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.junit.jupiter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.junit.platform.commons.support.ModifierSupport;

class FieldsSupport {

    static void handleFields(final Class<? extends Object> testClass, final boolean staticFields,
            final Class<? extends Annotation> annotationClass, final Consumer<Field> fieldHandler) {
        final Predicate<Field> staticPredicate = staticFields ? ModifierSupport::isStatic
                : ModifierSupport::isNotStatic;
        Stream.of(testClass.getDeclaredFields())
                .filter(ModifierSupport::isNotPrivate)
                .filter(ModifierSupport::isNotFinal)
                .filter(staticPredicate)
                .filter(field -> field.getDeclaredAnnotation(annotationClass) != null)
                .map(FieldsSupport::makeAccessible)
                .forEach(fieldHandler);
    }

    static Field makeAccessible(final Field field) {
        if (!field.isAccessible()) {
            field.setAccessible(true);
        }
        return field;
    }
}

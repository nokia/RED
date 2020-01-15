/*
 * Copyright 2020 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.junit.jupiter;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;


@Retention(RUNTIME)
@Target(FIELD)
public @interface LaunchConfig {

    /**
     * Indicates type identifier of a launch configuration to be created
     * 
     * @return
     */
    String typeId();

    /**
     * Indicates name of a launch configuration to be created
     * 
     * @return
     */
    String name();

}

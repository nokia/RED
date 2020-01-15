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
public @interface Project {

    /**
     * When true the project will have robot nature added. Use wisely since this adds builder
     * to the project, so in some situations project building/validation can start.
     * 
     * @return
     */
    boolean useRobotNature() default false;

    /**
     * When true default red.xml file will be generated.
     * 
     * @return
     */
    boolean createDefaultRedXml() default false;

    /**
     * Defines the name of a project. When empty string is given the class name will be used
     * 
     * @return
     */
    String name() default "";

    /**
     * Defines paths to directories which should be created after project is created
     * 
     * @return
     */
    String[] dirs() default {};

    /**
     * Defines paths to files which should be created (empty ones) after project is created
     * 
     * @return
     */
    String[] files() default {};

    /**
     * Indicates if resources created via {@link StatefulProject} should be cleaned up after each
     * test
     * 
     * @return
     */
    boolean cleanUpAfterEach() default false;

}

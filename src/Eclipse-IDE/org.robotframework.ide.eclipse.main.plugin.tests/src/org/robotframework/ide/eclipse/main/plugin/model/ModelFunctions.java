/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;

import com.google.common.base.Function;

public class ModelFunctions {

    public static Function<RobotElement, String> toNames() {
        return new Function<RobotElement, String>() {

            @Override
            public String apply(final RobotElement element) {
                return element.getName();
            }
        };
    }

}

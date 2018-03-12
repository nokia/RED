/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.model;

import org.assertj.core.api.Condition;

public class RobotCaseConditions {

    public static Condition<RobotCase> properlySetParent() {
        return new Condition<RobotCase>() {

            @Override
            public boolean matches(final RobotCase testCase) {
                return testCase.getParent() != null && testCase.getParent().getChildren().contains(testCase)
                        && testCase.getLinkedElement().getParent() != null
                        && testCase.getLinkedElement().getParent().getTestCases().contains(testCase.getLinkedElement())
                        && testCase.getParent().getLinkedElement() == testCase.getLinkedElement().getParent();
            }
        };
    }
}

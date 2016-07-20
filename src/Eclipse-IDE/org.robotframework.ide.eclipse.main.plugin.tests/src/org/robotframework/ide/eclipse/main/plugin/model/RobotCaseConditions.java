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

package org.robotframework.ide.eclipse.main.plugin.model;

import org.assertj.core.api.Condition;

public class RobotVariableConditions {

    public static Condition<RobotVariable> properlySetParent() {
        return new Condition<RobotVariable>() {

            @Override
            public boolean matches(final RobotVariable variable) {
                return variable.getParent() != null && variable.getParent().getChildren().contains(variable)
                        && variable.getLinkedElement().getParent() != null
                        && variable.getLinkedElement().getParent().getVariables().contains(variable.getLinkedElement())
                        && variable.getParent().getLinkedElement() == variable.getLinkedElement().getParent();
            }
        };
    }
}

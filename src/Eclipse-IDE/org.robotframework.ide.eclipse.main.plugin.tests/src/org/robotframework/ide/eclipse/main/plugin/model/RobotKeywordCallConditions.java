package org.robotframework.ide.eclipse.main.plugin.model;

import org.assertj.core.api.Condition;
import org.rf.ide.core.testdata.model.table.IExecutableStepsHolder;

public class RobotKeywordCallConditions {

    public static Condition<RobotKeywordCall> properlySetParent() {
        return new Condition<RobotKeywordCall>() {

            @Override
            public boolean matches(final RobotKeywordCall call) {
                return call.getParent() != null && call.getParent().getChildren().contains(call)
                        && call.getLinkedElement().getParent() != null
                        && (((IExecutableStepsHolder<?>) call.getLinkedElement().getParent()).getExecutionContext()
                                .contains(call.getLinkedElement())
                                || ((IExecutableStepsHolder<?>) call.getLinkedElement().getParent())
                                        .getUnitSettings().contains(call.getLinkedElement()))
                        && call.getParent().getLinkedElement() == call.getLinkedElement().getParent();
            }
        };
    }
}

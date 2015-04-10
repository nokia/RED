package org.robotframework.ide.eclipse.main.plugin.tempmodel.cmd;

import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.RobotVariable;

public class CmdAddVariable {

    private final RobotSuiteFileSection section;
    private final RobotVariable variable;

    public CmdAddVariable(final RobotSuiteFileSection section, final RobotVariable variable) {
        this.section = section;
        this.variable = variable;
    }

    public void execute() {
        // section.addChild(variable);
    }

    public void undo() {
        // section.getChildren().remove(variable);
    }

}

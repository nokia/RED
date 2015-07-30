package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.handler;

import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetVariableCommentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetVariableNameCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetVariableValueCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

import com.google.common.base.Optional;

class VariablesAttributesCommandsProvider {

    Optional<? extends EditorCommand> provide(final RobotElement element, final int index, final String attribute) {
        if (element instanceof RobotVariable) {
            final RobotVariable variable = (RobotVariable) element;
            if (index == 0) {
                return Optional.of(new SetVariableNameCommand(variable, attribute));
            } else if (index == 1) {
                return Optional.of(new SetVariableValueCommand(variable, attribute));
            } else if (index == 2) {
                return Optional.of(new SetVariableCommentCommand(variable, attribute));
            }
        }
        return Optional.absent();
    }

}

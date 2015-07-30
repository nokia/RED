package org.robotframework.ide.eclipse.main.plugin.tableeditor.code.handler;

import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetCaseCommentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetCaseNameCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallArgumentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallCommentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallNameCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordDefinitionArgumentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordDefinitionCommentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordDefinitionNameCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

import com.google.common.base.Optional;

class CodeAttributesCommandsProvider {

    Optional<? extends EditorCommand> provide(final RobotElement element, final int index, final int noOfColumns, final String attribute) {
        if (element instanceof RobotKeywordDefinition) {
            final RobotKeywordDefinition definition = (RobotKeywordDefinition) element;
            if (index == 0) {
                return Optional.of(new SetKeywordDefinitionNameCommand(definition, attribute));
            } else if (index == noOfColumns - 1) {
                return Optional.of(new SetKeywordDefinitionCommentCommand(definition, attribute));
            } else {
                return Optional.of(new SetKeywordDefinitionArgumentCommand(definition, index - 1, attribute));
            }
        } else if (element instanceof RobotCase) {
            final RobotCase testCase = (RobotCase) element;
            if (index == 0) {
                return Optional.of(new SetCaseNameCommand(testCase, attribute));
            } else if (index == noOfColumns - 1) {
                return Optional.of(new SetCaseCommentCommand(testCase, attribute));
            }
        } else if (element instanceof RobotKeywordCall) {
            final RobotKeywordCall call = (RobotKeywordCall) element;
            if (index == 0) {
                return Optional.of(new SetKeywordCallNameCommand(call, attribute));
            } else if (index == noOfColumns - 1) {
                return Optional.of(new SetKeywordCallCommentCommand(call, attribute));
            } else {
                return Optional.of(new SetKeywordCallArgumentCommand(call, index - 1, attribute));
            }
        }
        return Optional.absent();
    }

}

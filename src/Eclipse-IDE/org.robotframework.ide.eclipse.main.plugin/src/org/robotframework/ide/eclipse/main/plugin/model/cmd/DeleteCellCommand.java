package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import java.util.ArrayList;
import java.util.List;

import org.robotframework.ide.eclipse.main.plugin.model.IRobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.settings.SetSettingArgumentCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.KeywordCallsTableValuesChangingCommandsCollector;
import org.robotframework.services.event.RedEventBroker;

public class DeleteCellCommand extends EditorCommand {

    final private RobotKeywordCall call;
    final private int position;
    private EditorCommand executed;

    public DeleteCellCommand(RobotKeywordCall call, int position) {
        this.call = call;
        this.position = position;
    }

    @Override
    public void execute() throws CommandExecutionException {

        final int callIndex = call.getIndex();
        final IRobotCodeHoldingElement parent = call.getParent();
        final List<EditorCommand> commands = new ArrayList<>();
        if (call instanceof RobotSetting) {
            final RobotSetting selectedSetting = (RobotSetting) call;
            if (position > 0) {
                commands.add(new SetSettingArgumentCommand(selectedSetting, position - 1, null));
            }
        } else {
            commands.addAll(new KeywordCallsTableValuesChangingCommandsCollector()
                    .collect(call, null, position));
        }

        if (commands.size() == 1) {
            executed = commands.get(0);
            executed.setEventBroker(eventBroker);
            executed.execute();
        } else if (commands.size() > 1) {
            throw new IllegalStateException("Only single cell should be deleted this way!");
        }

        RedEventBroker.using(eventBroker).additionallyBinding(RobotModelEvents.ADDITIONAL_DATA)
                .to(parent.getChildren().get(callIndex)).send(
                        RobotModelEvents.ROBOT_KEYWORD_CALL_CONVERTED, parent);
    }

    @Override
    public List<EditorCommand> getUndoCommands() {
        return executed.getUndoCommands();
    }

}

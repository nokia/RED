package org.robotframework.ide.eclipse.main.plugin.cmd;

import org.robotframework.ide.eclipse.main.plugin.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.RobotModelEvents;

public class SetSettingKeywordCallArgument extends SetKeywordCallArgument {

    public SetSettingKeywordCallArgument(final RobotKeywordCall keywordCall, final int index, final String value) {
        super(keywordCall, index, value, RobotModelEvents.ROBOT_SETTING_ARGUMENT_CHANGED);
    }
}

package org.robotframework.ide.eclipse.main.plugin.cmd;

import org.robotframework.ide.eclipse.main.plugin.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.RobotModelEvents;

public class SetArgumentOfSettingKeywordCall extends SetKeywordCallArgument {

    public SetArgumentOfSettingKeywordCall(final RobotKeywordCall keywordCall, final int index, final String value) {
        super(keywordCall, index, value, RobotModelEvents.ROBOT_SETTING_ARGUMENT_CHANGED);
    }
}

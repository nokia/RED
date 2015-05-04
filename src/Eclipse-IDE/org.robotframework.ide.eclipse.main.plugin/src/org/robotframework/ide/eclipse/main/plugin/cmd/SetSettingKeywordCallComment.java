package org.robotframework.ide.eclipse.main.plugin.cmd;

import org.robotframework.ide.eclipse.main.plugin.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.RobotModelEvents;

public class SetSettingKeywordCallComment extends SetKeywordCallComment {

    public SetSettingKeywordCallComment(final RobotKeywordCall keywordCall, final String comment) {
        super(keywordCall, comment, RobotModelEvents.ROBOT_SETTING_COMMENT_CHANGED);
    }

}

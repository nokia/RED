package org.robotframework.ide.core.testData.text;

import java.util.List;


public enum Separator {
    PIPE, DOUBLE_SPACE_OR_TAB;

    public Separator separator(int tokenIndex, List<RobotToken> tokens) {
        Separator separator = Separator.DOUBLE_SPACE_OR_TAB;

        if (tokens.size() > tokenIndex + 2) {
            RobotToken rtPipeMaybe = tokens.get(tokenIndex + 1);
            RobotToken rtSpaceOrTab = tokens.get(tokenIndex + 2);

            if (rtPipeMaybe.getType() == RobotTokenType.PIPE) {
                RobotTokenType nextTokenAfterPipeType = rtSpaceOrTab.getType();
                if (nextTokenAfterPipeType == RobotTokenType.SPACE
                        || nextTokenAfterPipeType == RobotTokenType.TAB) {
                    separator = Separator.PIPE;
                }
            }
        }

        return separator;
    }
}

/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.setting.task;

import java.util.List;
import java.util.Stack;

import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.mapping.table.ElementsUtility;
import org.rf.ide.core.testdata.mapping.table.IParsingMapper;
import org.rf.ide.core.testdata.mapping.table.ParsingStateHelper;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.TaskTeardown;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class TaskTeardownKeywordArgumentMapper implements IParsingMapper {

    protected final ElementsUtility utility = new ElementsUtility();

    private final ParsingStateHelper stateHelper = new ParsingStateHelper();

    @Override
    public boolean isApplicableFor(final RobotVersion robotVersion) {
        return robotVersion.isNewerOrEqualTo(new RobotVersion(3, 1));
    }

    @Override
    public boolean checkIfCanBeMapped(final RobotFileOutput robotFileOutput, final RobotLine currentLine,
            final RobotToken rt, final String text, final Stack<ParsingState> processingState) {

        if (stateHelper.getCurrentState(processingState) == ParsingState.SETTING_TASK_TEARDOWN) {
            final List<TaskTeardown> taskTeardowns = robotFileOutput.getFileModel()
                    .getSettingTable()
                    .getTaskTeardowns();
            return utility.checkIfLastHasKeywordNameAlready(taskTeardowns);
        }
        return stateHelper.getCurrentState(processingState) == ParsingState.SETTING_TASK_TEARDOWN_KEYWORD
                || stateHelper.getCurrentState(processingState) == ParsingState.SETTING_TASK_TEARDOWN_KEYWORD_ARGUMENT;
    }

    @Override
    public RobotToken map(final RobotLine currentLine, final Stack<ParsingState> processingState,
            final RobotFileOutput robotFileOutput, final RobotToken rt, final FilePosition fp, final String text) {

        rt.getTypes().add(0, RobotTokenType.SETTING_TASK_TEARDOWN_KEYWORD_ARGUMENT);
        rt.setText(text);

        final SettingTable settings = robotFileOutput.getFileModel().getSettingTable();
        final List<TaskTeardown> teardowns = settings.getTaskTeardowns();
        teardowns.get(teardowns.size() - 1).addArgument(rt);

        processingState.push(ParsingState.SETTING_TASK_TEARDOWN_KEYWORD_ARGUMENT);
        return rt;
    }
}

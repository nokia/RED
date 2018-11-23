/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.setting.task;

import java.util.List;
import java.util.Stack;

import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.mapping.table.IParsingMapper;
import org.rf.ide.core.testdata.mapping.table.ParsingStateHelper;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.TaskTemplate;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class TaskTemplateTrashDataMapper implements IParsingMapper {

    private final ParsingStateHelper stateHelper = new ParsingStateHelper();

    @Override
    public boolean isApplicableFor(final RobotVersion robotVersion) {
        return robotVersion.isNewerOrEqualTo(new RobotVersion(3, 1));
    }

    @Override
    public boolean checkIfCanBeMapped(final RobotFileOutput robotFileOutput, final RobotLine currentLine,
            final RobotToken rt, final String text, final Stack<ParsingState> processingState) {
        
        final ParsingState currentState = stateHelper.getCurrentState(processingState);

        if (currentState == ParsingState.SETTING_TASK_TEMPLATE) {
            final List<TaskTemplate> taskTemplates = robotFileOutput.getFileModel()
                    .getSettingTable()
                    .getTaskTemplates();
            return checkIfHasAlreadyKeywordName(taskTemplates);
        }
        return currentState == ParsingState.SETTING_TASK_TEMPLATE_KEYWORD
                || currentState == ParsingState.SETTING_TASK_TEMPLATE_KEYWORD_UNWANTED_ARGUMENTS;
    }

    protected boolean checkIfHasAlreadyKeywordName(final List<TaskTemplate> taskTemplates) {
        return !taskTemplates.isEmpty() && taskTemplates.get(taskTemplates.size() - 1).getKeywordName() != null;
    }

    @Override
    public RobotToken map(final RobotLine currentLine, final Stack<ParsingState> processingState,
            final RobotFileOutput robotFileOutput, final RobotToken rt, final FilePosition fp, final String text) {

        rt.getTypes().add(0, RobotTokenType.SETTING_TASK_TEMPLATE_KEYWORD_UNWANTED_ARGUMENT);
        rt.setText(text);

        final SettingTable settings = robotFileOutput.getFileModel().getSettingTable();
        final List<TaskTemplate> templates = settings.getTaskTemplates();
        if (!templates.isEmpty()) {
            templates.get(templates.size() - 1).addUnexpectedTrashArgument(rt);
        }

        processingState.push(ParsingState.SETTING_TASK_TEMPLATE_KEYWORD_UNWANTED_ARGUMENTS);
        return rt;
    }
}

/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter.update.tasks;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.update.ExecutablesStepsHolderMorphOperation;
import org.rf.ide.core.testdata.model.table.LocalSetting;
import org.rf.ide.core.testdata.model.table.LocalSettingTokenTypes;
import org.rf.ide.core.testdata.model.table.tasks.Task;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class LocalSettingToTaskSettingMorphOperation extends ExecutablesStepsHolderMorphOperation<Task> {

    @Override
    public boolean isApplicable(final ModelType elementType) {
        return LocalSetting.KEYWORD_SETTING_TYPES.contains(elementType)
                || LocalSetting.TEST_SETTING_TYPES.contains(elementType);
    }

    @Override
    public AModelElement<Task> insert(final Task task, final int index, final AModelElement<?> modelElement) {
        final LocalSetting<?> taskSetting = (LocalSetting<?>) modelElement;
        final RobotTokenType possibleSettingType = RobotTokenType
                .findTypeOfDeclarationForTaskSettingTable(taskSetting.getDeclaration().getText());
        taskSetting.changeModelType(getTargetModelType(possibleSettingType));
        return task.addElement(index, taskSetting);
    }

    private ModelType getTargetModelType(final RobotTokenType possibleSettingType) {
        return LocalSettingTokenTypes.getModelTypeFromDeclarationType(
                possibleSettingType == RobotTokenType.UNKNOWN ? RobotTokenType.TASK_SETTING_UNKNOWN_DECLARATION
                        : possibleSettingType);
    }
}

/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.settings;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.table.RobotElementsComparatorWithPositionChangedPresave;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.TaskTemplate;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.testdata.text.write.DumperHelper;
import org.rf.ide.core.testdata.text.write.tables.ANotExecutableTableElementDumper;

public class TaskTemplateDumper extends ANotExecutableTableElementDumper<SettingTable> {

    public TaskTemplateDumper(final DumperHelper helper) {
        super(helper, ModelType.SUITE_TASK_TEMPLATE);
    }

    @Override
    public RobotElementsComparatorWithPositionChangedPresave getSorter(
            final AModelElement<SettingTable> currentElement) {
        final TaskTemplate taskTemplate = (TaskTemplate) currentElement;

        final RobotElementsComparatorWithPositionChangedPresave sorter = new RobotElementsComparatorWithPositionChangedPresave();
        final List<RobotToken> keys = new ArrayList<>();
        if (taskTemplate.getKeywordName() != null) {
            keys.add(taskTemplate.getKeywordName());
        }
        sorter.addPresaveSequenceForType(RobotTokenType.SETTING_TASK_TEMPLATE_KEYWORD_NAME, 1, keys);
        sorter.addPresaveSequenceForType(RobotTokenType.SETTING_TASK_TEMPLATE_KEYWORD_UNWANTED_ARGUMENT, 2,
                taskTemplate.getUnexpectedArguments());
        sorter.addPresaveSequenceForType(RobotTokenType.START_HASH_COMMENT, 3,
                elemUtility.filter(taskTemplate.getComment(), RobotTokenType.START_HASH_COMMENT));
        sorter.addPresaveSequenceForType(RobotTokenType.COMMENT_CONTINUE, 4,
                elemUtility.filter(taskTemplate.getComment(), RobotTokenType.COMMENT_CONTINUE));

        return sorter;
    }
}

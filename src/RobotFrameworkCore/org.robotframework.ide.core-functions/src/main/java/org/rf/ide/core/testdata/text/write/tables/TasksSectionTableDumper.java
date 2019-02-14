/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.table.LocalSetting;
import org.rf.ide.core.testdata.model.table.TaskTable;
import org.rf.ide.core.testdata.text.write.DumperHelper;
import org.rf.ide.core.testdata.text.write.SectionBuilder.SectionType;

public class TasksSectionTableDumper extends AExecutableTableDumper<TaskTable> {

    public TasksSectionTableDumper(final DumperHelper helper) {
        super(helper, getDumpers(helper));
    }

    private static List<ExecutableTableElementDumper> getDumpers(final DumperHelper helper) {
        final List<ExecutableTableElementDumper> dumpers = new ArrayList<>();

        for (final ModelType settingType : LocalSetting.TASK_SETTING_TYPES) {
            dumpers.add(new LocalSettingDumper(helper, settingType));
        }
        dumpers.add(new ExecutableHolderExecutionRowDumper(helper, ModelType.TASK_EXECUTABLE_ROW));
        dumpers.add(new ExecutableHolderEmptyLineDumper(helper));
        return dumpers;
    }

    @Override
    public SectionType getSectionType() {
        return SectionType.TASKS;
    }
}

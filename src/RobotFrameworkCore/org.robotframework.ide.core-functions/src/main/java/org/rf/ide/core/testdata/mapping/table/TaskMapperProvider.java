/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.table;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.mapping.tasks.TaskDocumentationTextMapper;
import org.rf.ide.core.testdata.mapping.tasks.TaskEmptyLineMapper;
import org.rf.ide.core.testdata.mapping.tasks.TaskNameMapper;
import org.rf.ide.core.testdata.mapping.tasks.TaskSettingDeclarationMapper;
import org.rf.ide.core.testdata.mapping.tasks.TaskSetupKeywordArgumentMapper;
import org.rf.ide.core.testdata.mapping.tasks.TaskSetupKeywordMapper;
import org.rf.ide.core.testdata.mapping.tasks.TaskTagsTagNameMapper;
import org.rf.ide.core.testdata.mapping.tasks.TaskTeardownKeywordArgumentMapper;
import org.rf.ide.core.testdata.mapping.tasks.TaskTeardownKeywordMapper;
import org.rf.ide.core.testdata.mapping.tasks.TaskTemplateKeywordMapper;
import org.rf.ide.core.testdata.mapping.tasks.TaskTemplateKeywordTrashArgumentMapper;
import org.rf.ide.core.testdata.mapping.tasks.TaskTimeoutMessageMapper;
import org.rf.ide.core.testdata.mapping.tasks.TaskTimeoutValueMapper;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;


public class TaskMapperProvider {

    private static final List<IParsingMapper> MAPPERS = new ArrayList<>();
    static {
        MAPPERS.add(new TaskNameMapper());

        MAPPERS.add(new TaskSettingDeclarationMapper(RobotTokenType.TASK_SETTING_SETUP, ParsingState.TASK_SETTING_SETUP,
                ModelType.TASK_SETUP));
        MAPPERS.add(new TaskSetupKeywordMapper());
        MAPPERS.add(new TaskSetupKeywordArgumentMapper());

        MAPPERS.add(new TaskSettingDeclarationMapper(RobotTokenType.TASK_SETTING_TAGS_DECLARATION,
                ParsingState.TASK_SETTING_TAGS, ModelType.TASK_TAGS));
        MAPPERS.add(new TaskTagsTagNameMapper());

        MAPPERS.add(new TaskSettingDeclarationMapper(RobotTokenType.TASK_SETTING_TEARDOWN,
                ParsingState.TASK_SETTING_TEARDOWN, ModelType.TASK_TEARDOWN));
        MAPPERS.add(new TaskTeardownKeywordMapper());
        MAPPERS.add(new TaskTeardownKeywordArgumentMapper());

        MAPPERS.add(new TaskSettingDeclarationMapper(RobotTokenType.TASK_SETTING_TEMPLATE,
                ParsingState.TASK_SETTING_TASK_TEMPLATE, ModelType.TASK_TEMPLATE));
        MAPPERS.add(new TaskTemplateKeywordMapper());
        MAPPERS.add(new TaskTemplateKeywordTrashArgumentMapper());

        MAPPERS.add(new TaskSettingDeclarationMapper(RobotTokenType.TASK_SETTING_TIMEOUT,
                ParsingState.TASK_SETTING_TASK_TIMEOUT, ModelType.TASK_TIMEOUT));
        MAPPERS.add(new TaskTimeoutValueMapper());
        MAPPERS.add(new TaskTimeoutMessageMapper());

        MAPPERS.add(new TaskSettingDeclarationMapper(RobotTokenType.TASK_SETTING_DOCUMENTATION,
                ParsingState.TASK_SETTING_DOCUMENTATION_DECLARATION, ModelType.TASK_DOCUMENTATION));
        MAPPERS.add(new TaskDocumentationTextMapper());

        MAPPERS.add(new TaskEmptyLineMapper());
    }

    public List<IParsingMapper> getMappers(final RobotVersion robotVersion) {
        if (robotVersion.isOlderThan(new RobotVersion(3, 1))) {
            return new ArrayList<>();
        }
        
        final List<IParsingMapper> mappers = new ArrayList<>();
        for (final IParsingMapper mapper : MAPPERS) {
            if (mapper.isApplicableFor(robotVersion)) {
                mappers.add(mapper);
            }
        }
        return mappers;
    }
}

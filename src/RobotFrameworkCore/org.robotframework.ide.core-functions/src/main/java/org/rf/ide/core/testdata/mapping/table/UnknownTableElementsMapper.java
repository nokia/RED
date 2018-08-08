/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.table;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.mapping.keywords.KeywordExecutableRowActionMapper;
import org.rf.ide.core.testdata.mapping.keywords.KeywordExecutableRowArgumentMapper;
import org.rf.ide.core.testdata.mapping.setting.UnknownSettingArgumentMapper;
import org.rf.ide.core.testdata.mapping.setting.UnknownSettingMapper;
import org.rf.ide.core.testdata.mapping.tasks.TaskExecutableRowActionMapper;
import org.rf.ide.core.testdata.mapping.tasks.TaskExecutableRowArgumentMapper;
import org.rf.ide.core.testdata.mapping.testcases.TestCaseExecutableRowActionMapper;
import org.rf.ide.core.testdata.mapping.testcases.TestCaseExecutableRowArgumentMapper;
import org.rf.ide.core.testdata.mapping.variables.UnknownVariableMapper;
import org.rf.ide.core.testdata.mapping.variables.UnknownVariableValueMapper;
import org.rf.ide.core.testdata.model.RobotVersion;

public class UnknownTableElementsMapper {

    private static final List<IParsingMapper> MAPPERS = new ArrayList<>();
    static {
        MAPPERS.add(new UnknownSettingMapper());
        MAPPERS.add(new UnknownSettingArgumentMapper());
        MAPPERS.add(new UnknownVariableMapper());
        MAPPERS.add(new UnknownVariableValueMapper());
        MAPPERS.add(new TestCaseExecutableRowActionMapper());
        MAPPERS.add(new TestCaseExecutableRowArgumentMapper());
        MAPPERS.add(new TaskExecutableRowActionMapper());
        MAPPERS.add(new TaskExecutableRowArgumentMapper());
        MAPPERS.add(new KeywordExecutableRowActionMapper());
        MAPPERS.add(new KeywordExecutableRowArgumentMapper());
    }

    public List<IParsingMapper> getMappers(final RobotVersion robotVersion) {
        final List<IParsingMapper> mappers = new ArrayList<>();
        for (final IParsingMapper mapper : MAPPERS) {
            if (mapper.isApplicableFor(robotVersion)) {
                mappers.add(mapper);
            }
        }
        return mappers;
    }
}

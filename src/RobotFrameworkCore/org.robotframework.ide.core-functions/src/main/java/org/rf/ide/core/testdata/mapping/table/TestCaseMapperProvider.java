/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.table;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.mapping.testcases.TestCaseEmptyLineMapper;
import org.rf.ide.core.testdata.mapping.testcases.TestCaseNameMapper;
import org.rf.ide.core.testdata.mapping.testcases.TestCaseSetupKeywordArgumentMapper;
import org.rf.ide.core.testdata.mapping.testcases.TestCaseSetupKeywordMapper;
import org.rf.ide.core.testdata.mapping.testcases.TestCaseSetupMapper;
import org.rf.ide.core.testdata.mapping.testcases.TestCaseSetupMapperOld;
import org.rf.ide.core.testdata.mapping.testcases.TestCaseTagsMapper;
import org.rf.ide.core.testdata.mapping.testcases.TestCaseTagsMapperOld;
import org.rf.ide.core.testdata.mapping.testcases.TestCaseTagsTagNameMapper;
import org.rf.ide.core.testdata.mapping.testcases.TestCaseTeardownKeywordArgumentMapper;
import org.rf.ide.core.testdata.mapping.testcases.TestCaseTeardownKeywordMapper;
import org.rf.ide.core.testdata.mapping.testcases.TestCaseTeardownMapper;
import org.rf.ide.core.testdata.mapping.testcases.TestCaseTeardownMapperOld;
import org.rf.ide.core.testdata.mapping.testcases.TestCaseTemplateKeywordMapper;
import org.rf.ide.core.testdata.mapping.testcases.TestCaseTemplateKeywordTrashArgumentMapper;
import org.rf.ide.core.testdata.mapping.testcases.TestCaseTemplateMapper;
import org.rf.ide.core.testdata.mapping.testcases.TestCaseTemplateMapperOld;
import org.rf.ide.core.testdata.mapping.testcases.TestCaseTimeoutMapper;
import org.rf.ide.core.testdata.mapping.testcases.TestCaseTimeoutMapperOld;
import org.rf.ide.core.testdata.mapping.testcases.TestCaseTimeoutMessageMapper;
import org.rf.ide.core.testdata.mapping.testcases.TestCaseTimeoutValueMapper;
import org.rf.ide.core.testdata.mapping.testcases.TestCaseDocumentationMapper;
import org.rf.ide.core.testdata.mapping.testcases.TestCaseDocumentationMapperOld;
import org.rf.ide.core.testdata.mapping.testcases.TestCaseDocumentationTextMapper;
import org.rf.ide.core.testdata.model.RobotVersion;


public class TestCaseMapperProvider {

    private static final List<IParsingMapper> MAPPERS = new ArrayList<>();
    static {
        MAPPERS.add(new TestCaseNameMapper());
        MAPPERS.add(new TestCaseSetupMapper());
        MAPPERS.add(new TestCaseSetupMapperOld());
        MAPPERS.add(new TestCaseSetupKeywordMapper());
        MAPPERS.add(new TestCaseSetupKeywordArgumentMapper());
        MAPPERS.add(new TestCaseTagsMapper());
        MAPPERS.add(new TestCaseTagsMapperOld());
        MAPPERS.add(new TestCaseTagsTagNameMapper());
        MAPPERS.add(new TestCaseTeardownMapper());
        MAPPERS.add(new TestCaseTeardownMapperOld());
        MAPPERS.add(new TestCaseTeardownKeywordMapper());
        MAPPERS.add(new TestCaseTeardownKeywordArgumentMapper());
        MAPPERS.add(new TestCaseTemplateMapper());
        MAPPERS.add(new TestCaseTemplateMapperOld());
        MAPPERS.add(new TestCaseTemplateKeywordMapper());
        MAPPERS.add(new TestCaseTemplateKeywordTrashArgumentMapper());
        MAPPERS.add(new TestCaseTimeoutMapper());
        MAPPERS.add(new TestCaseTimeoutMapperOld());
        MAPPERS.add(new TestCaseTimeoutValueMapper());
        MAPPERS.add(new TestCaseTimeoutMessageMapper());
        MAPPERS.add(new TestCaseDocumentationMapper());
        MAPPERS.add(new TestCaseDocumentationMapperOld());
        MAPPERS.add(new TestCaseDocumentationTextMapper());
        MAPPERS.add(new TestCaseEmptyLineMapper());
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

/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.mapping;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.model.table.testCases.mapping.TestCaseNameMapper;
import org.rf.ide.core.testdata.model.table.testCases.mapping.TestCaseSetupKeywordArgumentMapper;
import org.rf.ide.core.testdata.model.table.testCases.mapping.TestCaseSetupKeywordMapper;
import org.rf.ide.core.testdata.model.table.testCases.mapping.TestCaseSetupMapper;
import org.rf.ide.core.testdata.model.table.testCases.mapping.TestCaseTagsMapper;
import org.rf.ide.core.testdata.model.table.testCases.mapping.TestCaseTagsTagNameMapper;
import org.rf.ide.core.testdata.model.table.testCases.mapping.TestCaseTeardownKeywordArgumentMapper;
import org.rf.ide.core.testdata.model.table.testCases.mapping.TestCaseTeardownKeywordMapper;
import org.rf.ide.core.testdata.model.table.testCases.mapping.TestCaseTeardownMapper;
import org.rf.ide.core.testdata.model.table.testCases.mapping.TestCaseTemplateKeywordMapper;
import org.rf.ide.core.testdata.model.table.testCases.mapping.TestCaseTemplateKeywordTrashArgumentMapper;
import org.rf.ide.core.testdata.model.table.testCases.mapping.TestCaseTemplateMapper;
import org.rf.ide.core.testdata.model.table.testCases.mapping.TestCaseTimeoutMapper;
import org.rf.ide.core.testdata.model.table.testCases.mapping.TestCaseTimeoutMessageMapper;
import org.rf.ide.core.testdata.model.table.testCases.mapping.TestCaseTimeoutValueMapper;
import org.rf.ide.core.testdata.model.table.testCases.mapping.TestDocumentationMapper;
import org.rf.ide.core.testdata.model.table.testCases.mapping.TestDocumentationTextMapper;


public class TestCaseMapperProvider {

    private static final List<IParsingMapper> mappers = new ArrayList<>();
    static {
        mappers.add(new TestCaseNameMapper());
        mappers.add(new TestCaseSetupMapper());
        mappers.add(new TestCaseSetupKeywordMapper());
        mappers.add(new TestCaseSetupKeywordArgumentMapper());
        mappers.add(new TestCaseTagsMapper());
        mappers.add(new TestCaseTagsTagNameMapper());
        mappers.add(new TestCaseTeardownMapper());
        mappers.add(new TestCaseTeardownKeywordMapper());
        mappers.add(new TestCaseTeardownKeywordArgumentMapper());
        mappers.add(new TestCaseTemplateMapper());
        mappers.add(new TestCaseTemplateKeywordMapper());
        mappers.add(new TestCaseTemplateKeywordTrashArgumentMapper());
        mappers.add(new TestCaseTimeoutMapper());
        mappers.add(new TestCaseTimeoutValueMapper());
        mappers.add(new TestCaseTimeoutMessageMapper());
        mappers.add(new TestDocumentationMapper());
        mappers.add(new TestDocumentationTextMapper());
    }


    public List<IParsingMapper> getMappers() {
        return mappers;
    }
}

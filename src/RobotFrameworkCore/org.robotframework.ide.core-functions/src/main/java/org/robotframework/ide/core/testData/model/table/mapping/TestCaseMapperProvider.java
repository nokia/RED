/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.mapping;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.table.testCases.mapping.TestCaseNameMapper;
import org.robotframework.ide.core.testData.model.table.testCases.mapping.TestCaseSetupKeywordArgumentMapper;
import org.robotframework.ide.core.testData.model.table.testCases.mapping.TestCaseSetupKeywordMapper;
import org.robotframework.ide.core.testData.model.table.testCases.mapping.TestCaseSetupMapper;
import org.robotframework.ide.core.testData.model.table.testCases.mapping.TestCaseTagsMapper;
import org.robotframework.ide.core.testData.model.table.testCases.mapping.TestCaseTagsTagNameMapper;
import org.robotframework.ide.core.testData.model.table.testCases.mapping.TestCaseTeardownKeywordArgumentMapper;
import org.robotframework.ide.core.testData.model.table.testCases.mapping.TestCaseTeardownKeywordMapper;
import org.robotframework.ide.core.testData.model.table.testCases.mapping.TestCaseTeardownMapper;
import org.robotframework.ide.core.testData.model.table.testCases.mapping.TestCaseTemplateKeywordMapper;
import org.robotframework.ide.core.testData.model.table.testCases.mapping.TestCaseTemplateKeywordTrashArgumentMapper;
import org.robotframework.ide.core.testData.model.table.testCases.mapping.TestCaseTemplateMapper;
import org.robotframework.ide.core.testData.model.table.testCases.mapping.TestCaseTimeoutMapper;
import org.robotframework.ide.core.testData.model.table.testCases.mapping.TestCaseTimeoutMessageMapper;
import org.robotframework.ide.core.testData.model.table.testCases.mapping.TestCaseTimeoutValueMapper;
import org.robotframework.ide.core.testData.model.table.testCases.mapping.TestDocumentationMapper;
import org.robotframework.ide.core.testData.model.table.testCases.mapping.TestDocumentationTextMapper;


public class TestCaseMapperProvider {

    private static final List<IParsingMapper> mappers = new LinkedList<>();
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

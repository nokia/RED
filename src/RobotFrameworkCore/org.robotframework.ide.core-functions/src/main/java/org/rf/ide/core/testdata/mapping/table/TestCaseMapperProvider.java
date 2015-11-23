/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.table;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.mapping.testcases.TestCaseNameMapper;
import org.rf.ide.core.testdata.mapping.testcases.TestCaseSetupKeywordArgumentMapper;
import org.rf.ide.core.testdata.mapping.testcases.TestCaseSetupKeywordMapper;
import org.rf.ide.core.testdata.mapping.testcases.TestCaseSetupMapper;
import org.rf.ide.core.testdata.mapping.testcases.TestCaseTagsMapper;
import org.rf.ide.core.testdata.mapping.testcases.TestCaseTagsTagNameMapper;
import org.rf.ide.core.testdata.mapping.testcases.TestCaseTeardownKeywordArgumentMapper;
import org.rf.ide.core.testdata.mapping.testcases.TestCaseTeardownKeywordMapper;
import org.rf.ide.core.testdata.mapping.testcases.TestCaseTeardownMapper;
import org.rf.ide.core.testdata.mapping.testcases.TestCaseTemplateKeywordMapper;
import org.rf.ide.core.testdata.mapping.testcases.TestCaseTemplateKeywordTrashArgumentMapper;
import org.rf.ide.core.testdata.mapping.testcases.TestCaseTemplateMapper;
import org.rf.ide.core.testdata.mapping.testcases.TestCaseTimeoutMapper;
import org.rf.ide.core.testdata.mapping.testcases.TestCaseTimeoutMessageMapper;
import org.rf.ide.core.testdata.mapping.testcases.TestCaseTimeoutValueMapper;
import org.rf.ide.core.testdata.mapping.testcases.TestDocumentationMapper;
import org.rf.ide.core.testdata.mapping.testcases.TestDocumentationTextMapper;


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

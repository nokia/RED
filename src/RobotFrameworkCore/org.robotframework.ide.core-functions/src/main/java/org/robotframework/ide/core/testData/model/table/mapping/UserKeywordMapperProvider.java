/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.mapping;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.table.userKeywords.mapping.KeywordArgumentsMapper;
import org.robotframework.ide.core.testData.model.table.userKeywords.mapping.KeywordArgumentsValueMapper;
import org.robotframework.ide.core.testData.model.table.userKeywords.mapping.KeywordDocumentationMapper;
import org.robotframework.ide.core.testData.model.table.userKeywords.mapping.KeywordDocumentationTextMapper;
import org.robotframework.ide.core.testData.model.table.userKeywords.mapping.KeywordReturnMapper;
import org.robotframework.ide.core.testData.model.table.userKeywords.mapping.KeywordReturnValueMapper;
import org.robotframework.ide.core.testData.model.table.userKeywords.mapping.KeywordTagsMapper;
import org.robotframework.ide.core.testData.model.table.userKeywords.mapping.KeywordTagsTagNameMapper;
import org.robotframework.ide.core.testData.model.table.userKeywords.mapping.KeywordTeardownArgumentMapper;
import org.robotframework.ide.core.testData.model.table.userKeywords.mapping.KeywordTeardownMapper;
import org.robotframework.ide.core.testData.model.table.userKeywords.mapping.KeywordTeardownNameMapper;
import org.robotframework.ide.core.testData.model.table.userKeywords.mapping.KeywordTimeoutMapper;
import org.robotframework.ide.core.testData.model.table.userKeywords.mapping.KeywordTimeoutMessageMapper;
import org.robotframework.ide.core.testData.model.table.userKeywords.mapping.KeywordTimeoutValueMapper;
import org.robotframework.ide.core.testData.model.table.userKeywords.mapping.UserKeywordNameMapper;


public class UserKeywordMapperProvider {

    private static final List<IParsingMapper> mappers = new LinkedList<>();

    static {
        mappers.add(new UserKeywordNameMapper());
        mappers.add(new KeywordDocumentationMapper());
        mappers.add(new KeywordDocumentationTextMapper());
        mappers.add(new KeywordTagsMapper());
        mappers.add(new KeywordTagsTagNameMapper());
        mappers.add(new KeywordArgumentsMapper());
        mappers.add(new KeywordArgumentsValueMapper());
        mappers.add(new KeywordReturnMapper());
        mappers.add(new KeywordReturnValueMapper());
        mappers.add(new KeywordTeardownMapper());
        mappers.add(new KeywordTeardownNameMapper());
        mappers.add(new KeywordTeardownArgumentMapper());
        mappers.add(new KeywordTimeoutMapper());
        mappers.add(new KeywordTimeoutValueMapper());
        mappers.add(new KeywordTimeoutMessageMapper());
    }


    public List<IParsingMapper> getMappers() {
        return mappers;
    }
}

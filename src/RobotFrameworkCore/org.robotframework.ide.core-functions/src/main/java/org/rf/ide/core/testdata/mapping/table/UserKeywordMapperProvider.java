/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.table;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.mapping.keywords.KeywordArgumentsMapper;
import org.rf.ide.core.testdata.mapping.keywords.KeywordArgumentsValueMapper;
import org.rf.ide.core.testdata.mapping.keywords.KeywordDocumentationMapper;
import org.rf.ide.core.testdata.mapping.keywords.KeywordDocumentationTextMapper;
import org.rf.ide.core.testdata.mapping.keywords.KeywordReturnMapper;
import org.rf.ide.core.testdata.mapping.keywords.KeywordReturnValueMapper;
import org.rf.ide.core.testdata.mapping.keywords.KeywordTagsMapper;
import org.rf.ide.core.testdata.mapping.keywords.KeywordTagsTagNameMapper;
import org.rf.ide.core.testdata.mapping.keywords.KeywordTeardownArgumentMapper;
import org.rf.ide.core.testdata.mapping.keywords.KeywordTeardownMapper;
import org.rf.ide.core.testdata.mapping.keywords.KeywordTeardownNameMapper;
import org.rf.ide.core.testdata.mapping.keywords.KeywordTimeoutMapper;
import org.rf.ide.core.testdata.mapping.keywords.KeywordTimeoutMessageMapper;
import org.rf.ide.core.testdata.mapping.keywords.KeywordTimeoutValueMapper;
import org.rf.ide.core.testdata.mapping.keywords.UserKeywordNameMapper;


public class UserKeywordMapperProvider {

    private static final List<IParsingMapper> mappers = new ArrayList<>();

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

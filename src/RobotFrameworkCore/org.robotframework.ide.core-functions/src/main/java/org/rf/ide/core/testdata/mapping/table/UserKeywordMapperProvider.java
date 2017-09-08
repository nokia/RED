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
import org.rf.ide.core.testdata.mapping.keywords.KeywordEmptyLineMapper;
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

    private static final List<IParsingMapper> MAPPERS = new ArrayList<>();

    static {
        MAPPERS.add(new UserKeywordNameMapper());
        MAPPERS.add(new KeywordDocumentationMapper());
        MAPPERS.add(new KeywordDocumentationTextMapper());
        MAPPERS.add(new KeywordTagsMapper());
        MAPPERS.add(new KeywordTagsTagNameMapper());
        MAPPERS.add(new KeywordArgumentsMapper());
        MAPPERS.add(new KeywordArgumentsValueMapper());
        MAPPERS.add(new KeywordReturnMapper());
        MAPPERS.add(new KeywordReturnValueMapper());
        MAPPERS.add(new KeywordTeardownMapper());
        MAPPERS.add(new KeywordTeardownNameMapper());
        MAPPERS.add(new KeywordTeardownArgumentMapper());
        MAPPERS.add(new KeywordTimeoutMapper());
        MAPPERS.add(new KeywordTimeoutValueMapper());
        MAPPERS.add(new KeywordTimeoutMessageMapper());
        MAPPERS.add(new KeywordEmptyLineMapper());
    }


    public List<IParsingMapper> getMappers() {
        return MAPPERS;
    }
}

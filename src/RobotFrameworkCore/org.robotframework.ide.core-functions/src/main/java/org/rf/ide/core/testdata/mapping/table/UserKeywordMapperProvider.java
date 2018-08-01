/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.table;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.mapping.keywords.KeywordArgumentsMapper;
import org.rf.ide.core.testdata.mapping.keywords.KeywordArgumentsMapperOld;
import org.rf.ide.core.testdata.mapping.keywords.KeywordArgumentsValueMapper;
import org.rf.ide.core.testdata.mapping.keywords.KeywordDocumentationMapper;
import org.rf.ide.core.testdata.mapping.keywords.KeywordDocumentationMapperOld;
import org.rf.ide.core.testdata.mapping.keywords.KeywordDocumentationTextMapper;
import org.rf.ide.core.testdata.mapping.keywords.KeywordEmptyLineMapper;
import org.rf.ide.core.testdata.mapping.keywords.KeywordReturnMapper;
import org.rf.ide.core.testdata.mapping.keywords.KeywordReturnMapperOld;
import org.rf.ide.core.testdata.mapping.keywords.KeywordReturnValueMapper;
import org.rf.ide.core.testdata.mapping.keywords.KeywordTagsMapper;
import org.rf.ide.core.testdata.mapping.keywords.KeywordTagsMapperOld;
import org.rf.ide.core.testdata.mapping.keywords.KeywordTagsTagNameMapper;
import org.rf.ide.core.testdata.mapping.keywords.KeywordTeardownArgumentMapper;
import org.rf.ide.core.testdata.mapping.keywords.KeywordTeardownMapper;
import org.rf.ide.core.testdata.mapping.keywords.KeywordTeardownMapperOld;
import org.rf.ide.core.testdata.mapping.keywords.KeywordTeardownNameMapper;
import org.rf.ide.core.testdata.mapping.keywords.KeywordTimeoutMapper;
import org.rf.ide.core.testdata.mapping.keywords.KeywordTimeoutMapperOld;
import org.rf.ide.core.testdata.mapping.keywords.KeywordTimeoutMessageMapper;
import org.rf.ide.core.testdata.mapping.keywords.KeywordTimeoutValueMapper;
import org.rf.ide.core.testdata.mapping.keywords.UserKeywordNameMapper;
import org.rf.ide.core.testdata.model.RobotVersion;


public class UserKeywordMapperProvider {

    private static final List<IParsingMapper> MAPPERS = new ArrayList<>();

    static {
        MAPPERS.add(new UserKeywordNameMapper());
        MAPPERS.add(new KeywordDocumentationMapper());
        MAPPERS.add(new KeywordDocumentationMapperOld());
        MAPPERS.add(new KeywordDocumentationTextMapper());
        MAPPERS.add(new KeywordTagsMapper());
        MAPPERS.add(new KeywordTagsMapperOld());
        MAPPERS.add(new KeywordTagsTagNameMapper());
        MAPPERS.add(new KeywordArgumentsMapper());
        MAPPERS.add(new KeywordArgumentsMapperOld());
        MAPPERS.add(new KeywordArgumentsValueMapper());
        MAPPERS.add(new KeywordReturnMapper());
        MAPPERS.add(new KeywordReturnMapperOld());
        MAPPERS.add(new KeywordReturnValueMapper());
        MAPPERS.add(new KeywordTeardownMapper());
        MAPPERS.add(new KeywordTeardownMapperOld());
        MAPPERS.add(new KeywordTeardownNameMapper());
        MAPPERS.add(new KeywordTeardownArgumentMapper());
        MAPPERS.add(new KeywordTimeoutMapper());
        MAPPERS.add(new KeywordTimeoutMapperOld());
        MAPPERS.add(new KeywordTimeoutValueMapper());
        MAPPERS.add(new KeywordTimeoutMessageMapper());
        MAPPERS.add(new KeywordEmptyLineMapper());
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

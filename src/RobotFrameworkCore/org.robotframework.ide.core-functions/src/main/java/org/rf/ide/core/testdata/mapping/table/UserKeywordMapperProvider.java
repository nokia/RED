/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.table;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.mapping.keywords.KeywordArgumentsValueMapper;
import org.rf.ide.core.testdata.mapping.keywords.KeywordDocumentationTextMapper;
import org.rf.ide.core.testdata.mapping.keywords.KeywordNameMapper;
import org.rf.ide.core.testdata.mapping.keywords.KeywordReturnValueMapper;
import org.rf.ide.core.testdata.mapping.keywords.KeywordSettingDeclarationMapper;
import org.rf.ide.core.testdata.mapping.keywords.KeywordTagsTagNameMapper;
import org.rf.ide.core.testdata.mapping.keywords.KeywordTeardownArgumentMapper;
import org.rf.ide.core.testdata.mapping.keywords.KeywordTeardownNameMapper;
import org.rf.ide.core.testdata.mapping.keywords.KeywordTimeoutMessageMapper;
import org.rf.ide.core.testdata.mapping.keywords.KeywordTimeoutValueMapper;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;


public class UserKeywordMapperProvider {

    private static final List<IParsingMapper> MAPPERS = new ArrayList<>();

    static {
        MAPPERS.add(new KeywordNameMapper());

        MAPPERS.add(new KeywordSettingDeclarationMapper(RobotTokenType.KEYWORD_SETTING_DOCUMENTATION,
                ParsingState.KEYWORD_SETTING_DOCUMENTATION_DECLARATION, ModelType.USER_KEYWORD_DOCUMENTATION));
        MAPPERS.add(new KeywordDocumentationTextMapper());

        MAPPERS.add(new KeywordSettingDeclarationMapper(RobotTokenType.KEYWORD_SETTING_TAGS,
                ParsingState.KEYWORD_SETTING_TAGS, ModelType.USER_KEYWORD_TAGS));
        MAPPERS.add(new KeywordTagsTagNameMapper());

        MAPPERS.add(new KeywordSettingDeclarationMapper(RobotTokenType.KEYWORD_SETTING_ARGUMENTS,
                ParsingState.KEYWORD_SETTING_ARGUMENTS, ModelType.USER_KEYWORD_ARGUMENTS));
        MAPPERS.add(new KeywordArgumentsValueMapper());

        MAPPERS.add(new KeywordSettingDeclarationMapper(RobotTokenType.KEYWORD_SETTING_RETURN,
                ParsingState.KEYWORD_SETTING_RETURN, ModelType.USER_KEYWORD_RETURN));
        MAPPERS.add(new KeywordReturnValueMapper());

        MAPPERS.add(new KeywordSettingDeclarationMapper(RobotTokenType.KEYWORD_SETTING_TEARDOWN,
                ParsingState.KEYWORD_SETTING_TEARDOWN, ModelType.USER_KEYWORD_TEARDOWN));
        MAPPERS.add(new KeywordTeardownNameMapper());
        MAPPERS.add(new KeywordTeardownArgumentMapper());

        MAPPERS.add(new KeywordSettingDeclarationMapper(RobotTokenType.KEYWORD_SETTING_TIMEOUT,
                ParsingState.KEYWORD_SETTING_TIMEOUT, ModelType.USER_KEYWORD_TIMEOUT));
        MAPPERS.add(new KeywordTimeoutValueMapper());
        MAPPERS.add(new KeywordTimeoutMessageMapper());
    }

    public List<IParsingMapper> getMappers() {
        return MAPPERS;
    }
}

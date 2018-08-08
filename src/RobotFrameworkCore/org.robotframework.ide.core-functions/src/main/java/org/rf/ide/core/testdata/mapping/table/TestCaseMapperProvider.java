/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.table;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.mapping.testcases.TestCaseDocumentationTextMapper;
import org.rf.ide.core.testdata.mapping.testcases.TestCaseEmptyLineMapper;
import org.rf.ide.core.testdata.mapping.testcases.TestCaseNameMapper;
import org.rf.ide.core.testdata.mapping.testcases.TestCaseSettingDeclarationMapper;
import org.rf.ide.core.testdata.mapping.testcases.TestCaseSettingDeclarationMapperOld;
import org.rf.ide.core.testdata.mapping.testcases.TestCaseSetupKeywordArgumentMapper;
import org.rf.ide.core.testdata.mapping.testcases.TestCaseSetupKeywordMapper;
import org.rf.ide.core.testdata.mapping.testcases.TestCaseTagsTagNameMapper;
import org.rf.ide.core.testdata.mapping.testcases.TestCaseTeardownKeywordArgumentMapper;
import org.rf.ide.core.testdata.mapping.testcases.TestCaseTeardownKeywordMapper;
import org.rf.ide.core.testdata.mapping.testcases.TestCaseTemplateKeywordMapper;
import org.rf.ide.core.testdata.mapping.testcases.TestCaseTemplateKeywordTrashArgumentMapper;
import org.rf.ide.core.testdata.mapping.testcases.TestCaseTimeoutMessageMapper;
import org.rf.ide.core.testdata.mapping.testcases.TestCaseTimeoutValueMapper;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;


public class TestCaseMapperProvider {

    private static final List<IParsingMapper> MAPPERS = new ArrayList<>();
    static {
        MAPPERS.add(new TestCaseNameMapper());

        MAPPERS.add(new TestCaseSettingDeclarationMapper(RobotTokenType.TEST_CASE_SETTING_SETUP,
                ParsingState.TEST_CASE_SETTING_SETUP, ModelType.TEST_CASE_SETUP));
        MAPPERS.add(new TestCaseSettingDeclarationMapperOld(RobotTokenType.TEST_CASE_SETTING_SETUP,
                ParsingState.TEST_CASE_SETTING_SETUP, ModelType.TEST_CASE_SETUP,
                testCase -> testCase.getSetups().isEmpty()));
        MAPPERS.add(new TestCaseSetupKeywordMapper());
        MAPPERS.add(new TestCaseSetupKeywordArgumentMapper());

        MAPPERS.add(new TestCaseSettingDeclarationMapper(RobotTokenType.TEST_CASE_SETTING_TAGS_DECLARATION,
                ParsingState.TEST_CASE_SETTING_TAGS, ModelType.TEST_CASE_TAGS));
        MAPPERS.add(new TestCaseSettingDeclarationMapperOld(RobotTokenType.TEST_CASE_SETTING_TAGS_DECLARATION,
                ParsingState.TEST_CASE_SETTING_TAGS, ModelType.TEST_CASE_TAGS, test -> test.getTags().isEmpty()));
        MAPPERS.add(new TestCaseTagsTagNameMapper());

        MAPPERS.add(new TestCaseSettingDeclarationMapper(RobotTokenType.TEST_CASE_SETTING_TEARDOWN,
                ParsingState.TEST_CASE_SETTING_TEARDOWN, ModelType.TEST_CASE_TEARDOWN));
        MAPPERS.add(new TestCaseSettingDeclarationMapperOld(RobotTokenType.TEST_CASE_SETTING_TEARDOWN,
                ParsingState.TEST_CASE_SETTING_TEARDOWN, ModelType.TEST_CASE_TEARDOWN,
                testCase -> testCase.getTeardowns().isEmpty()));
        MAPPERS.add(new TestCaseTeardownKeywordMapper());
        MAPPERS.add(new TestCaseTeardownKeywordArgumentMapper());

        MAPPERS.add(new TestCaseSettingDeclarationMapper(RobotTokenType.TEST_CASE_SETTING_TEMPLATE,
                ParsingState.TEST_CASE_SETTING_TEST_TEMPLATE, ModelType.TEST_CASE_TEMPLATE));
        MAPPERS.add(new TestCaseSettingDeclarationMapperOld(RobotTokenType.TEST_CASE_SETTING_TEMPLATE,
                ParsingState.TEST_CASE_SETTING_TEST_TEMPLATE, ModelType.TEST_CASE_TEMPLATE,
                testCase -> testCase.getTemplates().isEmpty()));
        MAPPERS.add(new TestCaseTemplateKeywordMapper());
        MAPPERS.add(new TestCaseTemplateKeywordTrashArgumentMapper());

        MAPPERS.add(new TestCaseSettingDeclarationMapper(RobotTokenType.TEST_CASE_SETTING_TIMEOUT,
                ParsingState.TEST_CASE_SETTING_TEST_TIMEOUT, ModelType.TEST_CASE_TIMEOUT));
        MAPPERS.add(new TestCaseSettingDeclarationMapperOld(RobotTokenType.TEST_CASE_SETTING_TIMEOUT,
                ParsingState.TEST_CASE_SETTING_TEST_TIMEOUT, ModelType.TEST_CASE_TIMEOUT,
                testCase -> testCase.getTimeouts().isEmpty()));
        MAPPERS.add(new TestCaseTimeoutValueMapper());
        MAPPERS.add(new TestCaseTimeoutMessageMapper());

        MAPPERS.add(new TestCaseSettingDeclarationMapper(RobotTokenType.TEST_CASE_SETTING_DOCUMENTATION,
                ParsingState.TEST_CASE_SETTING_DOCUMENTATION_DECLARATION, ModelType.TEST_CASE_DOCUMENTATION));
        MAPPERS.add(new TestCaseSettingDeclarationMapperOld(RobotTokenType.TEST_CASE_SETTING_DOCUMENTATION,
                ParsingState.TEST_CASE_SETTING_DOCUMENTATION_DECLARATION,
                ModelType.TEST_CASE_DOCUMENTATION, testCase -> testCase.getDocumentation().isEmpty()));
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

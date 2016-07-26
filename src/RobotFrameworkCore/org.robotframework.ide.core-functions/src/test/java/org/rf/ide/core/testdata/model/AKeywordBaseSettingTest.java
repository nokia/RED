/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

/**
 * @author wypych
 */
public class AKeywordBaseSettingTest {

    private RobotToken decToken;

    private ADummyKeywordBaseSettingForKeywords testable;

    @Before
    public void setUp() {
        this.decToken = new RobotToken();
        this.testable = new ADummyKeywordBaseSettingForKeywords(decToken);
    }

    @Test
    public void test_ifKeywordNameWillBeTheSame_whenNoArgumentsExists() {
        // prepare
        final RobotToken keyToken = new RobotToken();
        testable.setKeywordName(keyToken);

        // execute
        final List<RobotToken> elementTokens = testable.getElementTokens();

        // verify
        assertThat(elementTokens).hasSize(2);
        assertThat(elementTokens.get(0)).isSameAs(decToken);
        assertThat(elementTokens.get(0).getTypes()).contains(testable.getDeclarationType());
        assertThat(elementTokens.get(1)).isSameAs(keyToken);
        assertThat(elementTokens.get(1).getTypes()).contains(testable.getKeywordNameType());
    }

    @Test
    public void test_ifKeywordNameWillBeNotCreated_whenNoArgumentsExists() {
        // execute
        final List<RobotToken> elementTokens = testable.getElementTokens();

        // verify
        assertThat(elementTokens).hasSize(1);
        assertThat(elementTokens.get(0)).isSameAs(decToken);
        assertThat(elementTokens.get(0).getTypes()).contains(testable.getDeclarationType());
    }

    @Test
    public void test_ifKeywordNameWillBeTheSame_whenArgumentsExists() {
        // prepare
        final RobotToken keyToken = new RobotToken();
        final RobotToken argToken = new RobotToken();
        testable.setKeywordName(keyToken);
        testable.addArgument(argToken);

        // execute
        final List<RobotToken> elementTokens = testable.getElementTokens();

        // verify
        assertThat(elementTokens).hasSize(3);
        assertThat(elementTokens.get(0)).isSameAs(decToken);
        assertThat(elementTokens.get(0).getTypes()).contains(testable.getDeclarationType());
        assertThat(elementTokens.get(1)).isSameAs(keyToken);
        assertThat(elementTokens.get(1).getTypes()).contains(testable.getKeywordNameType());
        assertThat(elementTokens.get(2)).isSameAs(argToken);
        assertThat(elementTokens.get(2).getTypes()).contains(testable.getArgumentType());
    }

    @Test
    public void test_ifKeywordNameWillBeCreatedArtifactal_whenArgumentsExists() {
        // prepare
        final RobotToken argToken = new RobotToken();
        testable.addArgument(argToken);

        // execute
        final List<RobotToken> elementTokens = testable.getElementTokens();

        // verify
        assertThat(elementTokens).hasSize(3);
        assertThat(elementTokens.get(0)).isSameAs(decToken);
        assertThat(elementTokens.get(0).getTypes()).contains(testable.getDeclarationType());
        assertThat(elementTokens.get(1).getTypes()).contains(testable.getKeywordNameType());
        assertThat(elementTokens.get(2)).isSameAs(argToken);
        assertThat(elementTokens.get(2).getTypes()).contains(testable.getArgumentType());
    }

    private class ADummyKeywordBaseSettingForKeywords extends AKeywordBaseSetting<Object> {

        private static final long serialVersionUID = 4570317373970770339L;

        public ADummyKeywordBaseSettingForKeywords(RobotToken declaration) {
            super(declaration);
        }

        @Override
        protected RobotTokenType getDeclarationType() {
            return RobotTokenType.SETTING_SUITE_SETUP_DECLARATION;
        }

        @Override
        protected List<AKeywordBaseSetting<Object>> getAllThisKindSettings() {
            return null;
        }

        @Override
        public IRobotTokenType getKeywordNameType() {
            return RobotTokenType.SETTING_SUITE_SETUP_KEYWORD_NAME;
        }

        @Override
        public IRobotTokenType getArgumentType() {
            return RobotTokenType.SETTING_SUITE_SETUP_KEYWORD_ARGUMENT;
        }

        @Override
        public ModelType getModelType() {
            return null;
        }
    }
}

/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class AModelElementTest {

    @Test
    public void test_shouldUpdateTypeForNewElement() {
        // prepare
        final RobotToken tok1 = token("c1");

        // execute
        final AModelElementFake fake = new AModelElementFake();
        final RobotToken returned = fake.updateOrCreate(null, tok1, RobotTokenType.ASSIGNMENT);

        // verify
        assertThat(returned).isSameAs(tok1);
        assertThat(tok1.getTypes().get(0)).isEqualTo(RobotTokenType.ASSIGNMENT);
    }

    @Test
    public void test_updateOrCreatTokenInside_withExpectedTypeToSet_threeElementListAndTheSecondToUpdate() {
        // prepare
        final RobotToken tok1 = token("tok1");
        final RobotToken tok2 = token("tok2");
        final RobotToken tok3 = token("tok3");

        final List<RobotToken> toks = new ArrayList<>(Arrays.asList(tok1, tok2, tok3));
        final RobotToken toSet = token("tok2_update");

        // execute
        final AModelElementFake fake = new AModelElementFake();
        fake.updateOrCreateTokenInside(toks, 1, toSet, null);

        // verify
        assertThat(toks).hasSize(3);
        assertThat(toks.get(0)).isEqualTo(tok1);
        assertThat(toks.get(0).getText()).isEqualTo("tok1");
        assertThat(toks.get(1)).isEqualTo(tok2);
        assertThat(toks.get(1).getText()).isEqualTo("tok2_update");
        assertThat(toks.get(2)).isEqualTo(tok3);
        assertThat(toks.get(2).getText()).isEqualTo("tok3");
    }

    @Test
    public void test_updateOrCreatTokenInside_withExpectedTypeToSet_oneElementListAndSetElementIndex2_shouldFillLack() {
        // prepare
        final List<RobotToken> toks = new ArrayList<>(Arrays.asList(token("tok1")));
        final RobotToken toSet = token("tok3");

        // execute
        final AModelElementFake fake = new AModelElementFake();
        fake.updateOrCreateTokenInside(toks, 2, toSet, null);

        // verify
        assertThat(toks).hasSize(3);
        assertThat(toks.get(0)).isInstanceOf(RobotToken.class);
        assertThat(toks.get(1)).isInstanceOf(RobotToken.class);
        assertThat(toks.get(2)).isEqualTo(toSet);
    }

    @Test
    public void test_updateOrCreatTokenInside_withExpectedTypeToSet_emptyListAndSetElementIndex2_shouldFillLack() {
        // prepare
        final List<RobotToken> toks = new ArrayList<>();
        final RobotToken toSet = token("tok3");

        // execute
        final AModelElementFake fake = new AModelElementFake();
        fake.updateOrCreateTokenInside(toks, 2, toSet, null);

        // verify
        assertThat(toks).hasSize(3);
        assertThat(toks.get(0)).isInstanceOf(RobotToken.class);
        assertThat(toks.get(1)).isInstanceOf(RobotToken.class);
        assertThat(toks.get(2)).isEqualTo(toSet);
    }

    private static class AModelElementFake extends AModelElement<Object> {

        @Override
        public boolean isPresent() {
            return false;
        }

        @Override
        public ModelType getModelType() {
            return null;
        }

        @Override
        public FilePosition getBeginPosition() {
            return null;
        }

        @Override
        public List<RobotToken> getElementTokens() {
            return null;
        }

        @Override
        public RobotToken getDeclaration() {
            return null;
        }

        @Override
        public boolean removeElementToken(int index) {
            // TODO Auto-generated method stub
            return false;
        }
    }

    private RobotToken token(final String text) {
        final RobotToken tok = new RobotToken();
        tok.setText(text);

        return tok;
    }
}

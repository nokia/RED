/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.rf.ide.core.test.helpers.ClassFieldCleaner;
import org.rf.ide.core.test.helpers.ClassFieldCleaner.ForClean;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

import com.google.common.base.Optional;
import com.google.common.collect.Multimap;

public class RobotElementsComperatorWithPositionChangedPresaveTest {

    @ForClean
    private RobotElementsComparatorWithPositionChangedPresave robotElemsCmp;

    @Test
    public void test_getTokensInElement_forOneTypeOfElements() {
        // prepare
        final List<RobotToken> startToks = new ArrayList<>(Arrays.asList(new RobotToken(), new RobotToken()));
        robotElemsCmp.addPresaveSequenceForType(RobotTokenType.START_HASH_COMMENT, 1, startToks);

        // execute
        final List<RobotToken> toks = robotElemsCmp.getTokensInElement();

        // verify
        assertThat(toks).containsExactlyElementsOf(startToks);
    }

    @Test
    public void test_getTokensInElement_forTwoTypesOfElements() {
        // prepare
        final List<RobotToken> startToks = new ArrayList<>(Arrays.asList(new RobotToken(), new RobotToken()));
        final List<RobotToken> contToks = new ArrayList<>(Arrays.asList(new RobotToken(), new RobotToken()));
        robotElemsCmp.addPresaveSequenceForType(RobotTokenType.START_HASH_COMMENT, 1, startToks);
        robotElemsCmp.addPresaveSequenceForType(RobotTokenType.COMMENT_CONTINUE, 2, contToks);

        // execute
        final List<RobotToken> toks = robotElemsCmp.getTokensInElement();

        // verify
        final List<RobotToken> joined = new ArrayList<>(startToks);
        joined.addAll(contToks);
        assertThat(toks).containsOnlyElementsOf(joined);
    }

    @Test
    public void test_findType_typeNotExists() {
        // prepare
        RobotToken tok = new RobotToken();
        tok.setType(RobotTokenType.COMMENT_CONTINUE);

        // execute
        final Optional<IRobotTokenType> res = robotElemsCmp.findType(tok);

        // verify
        assertThat(res.isPresent()).isFalse();
    }

    @Test
    public void test_findType_typeExists() {
        // prepare
        RobotToken tok = new RobotToken();
        tok.setType(RobotTokenType.COMMENT_CONTINUE);
        robotElemsCmp.addPresaveSequenceForType(RobotTokenType.ASSIGNMENT, 0, new ArrayList<>(Arrays.asList(tok)));

        // execute
        final Optional<IRobotTokenType> res = robotElemsCmp.findType(tok);

        // verify
        assertThat(res.isPresent()).isTrue();
        assertThat(res.get()).isEqualTo(RobotTokenType.ASSIGNMENT);
    }

    @Test
    public void test_indexesOf_elementWhichNotExists() {
        // prepare
        final RobotToken tok = new RobotToken();
        final RobotToken tok2 = new RobotToken();

        // execute
        final Multimap<IRobotLineElement, Integer> indexes = robotElemsCmp
                .indexesOf(new ArrayList<IRobotLineElement>(Arrays.asList(tok)), tok2);

        // verify
        assertThat(indexes.isEmpty()).isTrue();
    }

    @Test
    public void test_indexesOf_elementWhichExists() {
        // prepare
        final RobotToken tok = new RobotToken();

        // execute
        final Multimap<IRobotLineElement, Integer> indexes = robotElemsCmp
                .indexesOf(new ArrayList<IRobotLineElement>(Arrays.asList(tok)), tok);

        // verify
        assertThat(indexes.size()).isEqualTo(1);
        assertThat(indexes.get(tok)).containsOnly(0);
    }

    @Test
    public void test_indexesOf_twoElementsWhichExists() {
        // prepare
        final RobotToken tok = new RobotToken();
        final RobotToken tok2 = new RobotToken();

        // execute
        final Multimap<IRobotLineElement, Integer> indexes = robotElemsCmp
                .indexesOf(new ArrayList<IRobotLineElement>(Arrays.asList(tok, tok2)), tok, tok2);

        // verify
        assertThat(indexes.size()).isEqualTo(2);
        assertThat(indexes.get(tok)).containsOnly(0);
        assertThat(indexes.get(tok2)).containsOnly(1);
    }

    @Test
    public void test_indexesOf_oneElementsWhichExists_andOneNotExists() {
        // prepare
        final RobotToken tok = new RobotToken();
        final RobotToken tok2 = new RobotToken();

        // execute
        final Multimap<IRobotLineElement, Integer> indexes = robotElemsCmp
                .indexesOf(new ArrayList<IRobotLineElement>(Arrays.asList(tok)), tok, tok2);

        // verify
        assertThat(indexes.size()).isEqualTo(1);
        assertThat(indexes.get(tok)).containsOnly(0);
    }

    @Test
    public void test_compareFor_startComment_and_commentContinue() {
        // prepare
        RobotToken startComment = new RobotToken();
        startComment.setType(RobotTokenType.START_HASH_COMMENT);
        robotElemsCmp.addPresaveSequenceForType(RobotTokenType.START_HASH_COMMENT, 1,
                new ArrayList<IRobotLineElement>(Arrays.asList(startComment)));

        RobotToken continueComment = new RobotToken();
        continueComment.setType(RobotTokenType.COMMENT_CONTINUE);
        robotElemsCmp.addPresaveSequenceForType(RobotTokenType.COMMENT_CONTINUE, 2,
                new ArrayList<IRobotLineElement>(Arrays.asList(continueComment)));

        // execute
        final int cmpResult = robotElemsCmp.compare(startComment, continueComment);
        final List<RobotToken> toks = robotElemsCmp.getTokensInElement();
        Collections.sort(toks, robotElemsCmp);

        // verify
        assertThat(cmpResult).isEqualTo(ECompareResult.LESS_THAN.getValue());
        assertThat(toks).containsExactly(startComment, continueComment);
    }

    @Test
    public void test_compareFor_commentComment_and_commentContinue() {
        // prepare
        RobotToken continueComment1 = new RobotToken();
        continueComment1.setType(RobotTokenType.COMMENT_CONTINUE);
        robotElemsCmp.addPresaveSequenceForType(RobotTokenType.COMMENT_CONTINUE, 2,
                new ArrayList<IRobotLineElement>(Arrays.asList(continueComment1)));

        RobotToken continueComment2 = new RobotToken();
        continueComment2.setType(RobotTokenType.COMMENT_CONTINUE);
        robotElemsCmp.addPresaveSequenceForType(RobotTokenType.COMMENT_CONTINUE, 2,
                new ArrayList<IRobotLineElement>(Arrays.asList(continueComment2)));

        // execute
        final int cmpResult = robotElemsCmp.compare(continueComment1, continueComment2);
        final List<RobotToken> toks = robotElemsCmp.getTokensInElement();
        Collections.sort(toks, robotElemsCmp);

        // verify
        assertThat(cmpResult).isEqualTo(ECompareResult.LESS_THAN.getValue());
        assertThat(toks).containsExactly(continueComment1, continueComment2);
    }

    @Test
    public void test_compareFor_notExistingTypes_bothOnTheSamePosition() {
        // prepare
        RobotToken tok1 = new RobotToken();
        RobotToken tok2 = new RobotToken();

        // execute
        final int compareResult = robotElemsCmp.compare(tok1, tok2);

        // verify
        assertThat(compareResult).isEqualTo(ECompareResult.EQUAL_TO.getValue());
    }

    @Test
    public void test_compareFor_notExistingTypes_onDifferentPosition_theFirstElementIsTheFirst() {
        // prepare
        RobotToken tok1 = new RobotToken();
        tok1.setStartColumn(0);
        tok1.setStartOffset(0);
        tok1.setLineNumber(0);

        RobotToken tok2 = new RobotToken();

        // execute
        final int compareResult = robotElemsCmp.compare(tok1, tok2);

        // verify
        assertThat(compareResult).isEqualTo(ECompareResult.LESS_THAN.getValue());
    }

    @Test
    public void test_compareFor_notExistingTypes_onDifferentPosition_theFirstElementIsTheSecond() {
        // prepare
        RobotToken tok1 = new RobotToken();

        RobotToken tok2 = new RobotToken();
        tok2.setStartColumn(0);
        tok2.setStartOffset(0);
        tok2.setLineNumber(0);

        // execute
        final int compareResult = robotElemsCmp.compare(tok1, tok2);

        // verify
        assertThat(compareResult).isEqualTo(ECompareResult.GREATER_THAN.getValue());
    }

    @Test
    public void test_compareFor_existingTypes_differentType() {
        // prepare
        RobotToken tok1 = new RobotToken();
        tok1.setType(RobotTokenType.START_HASH_COMMENT);
        RobotToken tok2 = new RobotToken();
        tok2.setType(RobotTokenType.COMMENT_CONTINUE);
        robotElemsCmp.addPresaveSequenceForType(RobotTokenType.START_HASH_COMMENT, 1,
                new ArrayList<RobotToken>(Arrays.asList(tok1)));
        robotElemsCmp.addPresaveSequenceForType(RobotTokenType.COMMENT_CONTINUE, 2,
                new ArrayList<RobotToken>(Arrays.asList(tok2)));

        // execute
        final int compareResult = robotElemsCmp.compare(tok1, tok2);
        final List<RobotToken> toks = robotElemsCmp.getTokensInElement();
        Collections.sort(toks, robotElemsCmp);

        // verify
        assertThat(compareResult).isEqualTo(ECompareResult.LESS_THAN.getValue());
        assertThat(toks).containsExactly(tok1, tok2);
    }

    @Test
    public void test_compareFor_existingTypes_theSameType() {
        // prepare
        RobotToken tok1 = new RobotToken();
        tok1.setType(RobotTokenType.START_HASH_COMMENT);
        RobotToken tok2 = new RobotToken();
        tok2.setType(RobotTokenType.START_HASH_COMMENT);
        robotElemsCmp.addPresaveSequenceForType(RobotTokenType.START_HASH_COMMENT, 1,
                new ArrayList<RobotToken>(Arrays.asList(tok1, tok2)));

        // execute
        final int compareResult = robotElemsCmp.compare(tok1, tok2);
        final List<RobotToken> toks = robotElemsCmp.getTokensInElement();
        Collections.sort(toks, robotElemsCmp);

        // verify
        assertThat(compareResult).isEqualTo(ECompareResult.LESS_THAN.getValue());
        assertThat(toks).containsExactly(tok1, tok2);
    }

    @Test
    public void test_compareFor_modifiedSequenceOfTokens() {
        // prepare
        RobotToken tok1 = new RobotToken();
        tok1.setType(RobotTokenType.START_HASH_COMMENT);
        RobotToken tok2 = new RobotToken();
        tok2.setLineNumber(0);
        tok2.setStartColumn(0);
        tok2.setStartOffset(0);
        tok2.setType(RobotTokenType.COMMENT_CONTINUE);
        RobotToken tok3 = new RobotToken();
        tok3.setLineNumber(0);
        tok3.setStartColumn(1);
        tok3.setStartOffset(1);
        tok3.setType(RobotTokenType.COMMENT_CONTINUE);

        robotElemsCmp.addPresaveSequenceForType(RobotTokenType.START_HASH_COMMENT, 1,
                new ArrayList<RobotToken>(Arrays.asList(tok1)));
        robotElemsCmp.addPresaveSequenceForType(RobotTokenType.COMMENT_CONTINUE, 2,
                new ArrayList<RobotToken>(Arrays.asList(tok3, tok2)));

        // execute
        final int compareResult = robotElemsCmp.compare(tok3, tok2);
        final List<RobotToken> toks = new ArrayList<>(Arrays.asList(tok3, tok2, tok1));
        Collections.sort(toks, robotElemsCmp);

        // verify
        assertThat(compareResult).isEqualTo(ECompareResult.LESS_THAN.getValue());
        assertThat(toks).containsExactly(tok1, tok3, tok2);
    }

    @Before
    public void setUp() {
        this.robotElemsCmp = new RobotElementsComparatorWithPositionChangedPresave();
    }

    @After
    public void tearDown() throws Exception {
        ClassFieldCleaner.init(this);
    }
}

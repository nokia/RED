/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.List;

import org.junit.Test;
import org.mockito.InOrder;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.testdata.RobotParser;
import org.rf.ide.core.testdata.RobotParser.RobotParserConfig;
import org.rf.ide.core.testdata.mapping.TwoModelReferencesLinker.DifferentOutputFile;
import org.rf.ide.core.testdata.mapping.collect.RobotTokensCollector;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.RobotProjectHolder;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

/**
 * @author wypych
 */
public class FileOutputsMatchUpdaterTest {

    @Test
    public void test_replaceNewReferenceByCorrespondingOld_checkReferenceUpdate() {
        // prepare
        final String fContentOld = "*** Settings ***\nLibrary\tnowy\tok\t#bad";
        final String fContentNew = "*** Settings ***\nLibrary\tnowy2\tok2\t#bad2";

        final RobotProjectHolder holder = new RobotProjectHolder(new RobotRuntimeEnvironment(null, null, "3.0.0"));
        final RobotParser parser = RobotParser.create(holder, RobotParserConfig.allImportsLazy(new RobotVersion(3, 0)));
        final RobotFileOutput oldContent = parser.parseEditorContent(fContentOld, new File("fake.txt"));
        final RobotFileOutput newContent = parser.parseEditorContent(fContentNew, new File("fake.txt"));

        final RobotTokensCollector robotTokensCollector = new RobotTokensCollector();
        final ListMultimap<RobotTokenType, RobotToken> oldViewAboutTokens = robotTokensCollector
                .extractRobotTokens(oldContent);
        final ListMultimap<RobotTokenType, RobotToken> newViewAboutTokens = robotTokensCollector
                .extractRobotTokens(newContent);

        // execute
        new TwoModelReferencesLinker().replaceNewReferenceByCorrespondingOld(oldContent, oldViewAboutTokens, newContent,
                newViewAboutTokens);

        // verify
        assertOutputsUpdated(oldContent, oldViewAboutTokens, newContent);
        assertLinesContainsOnlyExpectedTokens(oldContent, oldViewAboutTokens, newContent);
    }

    private void assertOutputsUpdated(final RobotFileOutput oldContent,
            final ListMultimap<RobotTokenType, RobotToken> oldViewAboutTokensPrev, final RobotFileOutput newContent) {

        final RobotTokensCollector robotTokensCollector = new RobotTokensCollector();
        final ListMultimap<RobotTokenType, RobotToken> oldViewAboutTokens = robotTokensCollector
                .extractRobotTokens(oldContent);
        final ListMultimap<RobotTokenType, RobotToken> newViewAboutTokens = robotTokensCollector
                .extractRobotTokens(newContent);

        assertThat(oldViewAboutTokens.keySet()).containsOnlyElementsOf(newViewAboutTokens.keySet());
        for (final RobotTokenType type : oldViewAboutTokens.keySet()) {
            final List<RobotToken> oldOutputForType = oldViewAboutTokens.get(type);
            final List<RobotToken> newOutputForType = newViewAboutTokens.get(type);

            assertThat(oldOutputForType.size()).isEqualTo(newOutputForType.size());
            for (int index = 0; index < oldOutputForType.size(); index++) {
                assertThat(oldOutputForType.get(index)).isSameAs(oldViewAboutTokensPrev.get(type).get(index));
                assertThat(oldOutputForType.get(index).getText()).isEqualTo(newOutputForType.get(index).getText());
                assertThat(oldOutputForType.get(index)
                        .getFilePosition()
                        .isSamePlace(newOutputForType.get(index).getFilePosition())).isTrue();

            }
        }
    }

    private void assertLinesContainsOnlyExpectedTokens(final RobotFileOutput oldContent,
            final ListMultimap<RobotTokenType, RobotToken> oldViewAboutTokens, final RobotFileOutput newContent) {
        final List<RobotLine> oldContentRL = oldContent.getFileModel().getFileContent();
        final List<RobotLine> newContentRL = newContent.getFileModel().getFileContent();
        assertThat(oldContentRL.size()).isEqualTo(newContentRL.size());

        for (final RobotLine rl : oldContentRL) {
            final List<IRobotLineElement> lineElements = rl.getLineElements();
            final RobotLine newRobotLine = newContentRL.get(rl.getLineNumber() - 1);
            final List<IRobotLineElement> newLineElements = newRobotLine.getLineElements();
            assertThat(newRobotLine).isSameAs(rl);

            final int size = lineElements.size();
            for (int i = 0; i < size; i++) {
                final IRobotLineElement elem = lineElements.get(i);
                if (elem instanceof RobotToken) {
                    assertThat(findIfExistsToken(oldViewAboutTokens, (RobotToken) elem)).isNotNull();
                } else {
                    assertThat(newLineElements.get(i)).isSameAs(elem);
                }
            }
        }
    }

    private RobotToken findIfExistsToken(final ListMultimap<RobotTokenType, RobotToken> oldViewAboutTokens,
            final RobotToken tok) {
        RobotToken found = null;

        final List<RobotToken> list = oldViewAboutTokens.get((RobotTokenType) tok.getTypes().get(0));
        for (final RobotToken robotToken : list) {
            if (robotToken == tok) {
                found = robotToken;
            }
        }

        return found;
    }

    @Test(expected = DifferentOutputFile.class)
    public void test_validateThatTheSameTokensInView_areNotTheSame_oneType_differentSize_butLastIsNotEmptyInOldAndMissingInNew() {
        // prepare
        final ListMultimap<RobotTokenType, RobotToken> oldViewAboutTokens = ArrayListMultimap.create();
        final RobotTokenType typeOne = RobotTokenType.KEYWORD_ACTION_ARGUMENT;
        oldViewAboutTokens.put(typeOne, RobotToken.create("ok"));
        oldViewAboutTokens.put(typeOne, RobotToken.create("ok2"));
        oldViewAboutTokens.put(typeOne, RobotToken.create("c"));

        final ListMultimap<RobotTokenType, RobotToken> newViewAboutTokens = ArrayListMultimap.create();
        newViewAboutTokens.put(typeOne, RobotToken.create("ok"));
        newViewAboutTokens.put(typeOne, RobotToken.create("ok2"));

        // execute & verify
        new TwoModelReferencesLinker().validateThatTheSameTokensInView(oldViewAboutTokens, newViewAboutTokens);
    }

    @Test
    public void test_validateThatTheSameTokensInView_areTheSame_oneType_differentSize_butWithEmptyTextInTheEndOfOld() {
        // prepare
        final ListMultimap<RobotTokenType, RobotToken> oldViewAboutTokens = ArrayListMultimap.create();
        final RobotTokenType typeOne = RobotTokenType.KEYWORD_ACTION_ARGUMENT;
        oldViewAboutTokens.put(typeOne, RobotToken.create("ok"));
        oldViewAboutTokens.put(typeOne, RobotToken.create("ok2"));
        //// empties
        oldViewAboutTokens.put(typeOne, RobotToken.create(""));
        oldViewAboutTokens.put(typeOne, RobotToken.create(""));
        oldViewAboutTokens.put(typeOne, RobotToken.create(""));

        final ListMultimap<RobotTokenType, RobotToken> newViewAboutTokens = ArrayListMultimap.create();
        newViewAboutTokens.put(typeOne, RobotToken.create("ok"));
        newViewAboutTokens.put(typeOne, RobotToken.create("ok2"));

        // execute & verify
        new TwoModelReferencesLinker().validateThatTheSameTokensInView(oldViewAboutTokens, newViewAboutTokens);
    }

    @Test
    public void test_validateThatTheSameTokensInView_areTheSame_oneType_sameSize_butWithEmptyText() {
        // prepare
        final ListMultimap<RobotTokenType, RobotToken> oldViewAboutTokens = ArrayListMultimap.create();
        final RobotTokenType typeOne = RobotTokenType.KEYWORD_ACTION_ARGUMENT;
        oldViewAboutTokens.put(typeOne, RobotToken.create("ok"));
        oldViewAboutTokens.put(typeOne, RobotToken.create("ok2"));
        oldViewAboutTokens.put(typeOne, RobotToken.create(""));
        oldViewAboutTokens.put(typeOne, RobotToken.create("d"));

        final ListMultimap<RobotTokenType, RobotToken> newViewAboutTokens = ArrayListMultimap.create();
        newViewAboutTokens.put(typeOne, RobotToken.create("ok"));
        newViewAboutTokens.put(typeOne, RobotToken.create("ok2"));
        newViewAboutTokens.put(typeOne, RobotToken.create("\\"));
        newViewAboutTokens.put(typeOne, RobotToken.create("d"));

        // execute & verify
        new TwoModelReferencesLinker().validateThatTheSameTokensInView(oldViewAboutTokens, newViewAboutTokens);
    }

    @Test
    public void test_validateThatTheSameTokensInView_areTheSame_oneType_sameSize_andText() {
        // prepare
        final ListMultimap<RobotTokenType, RobotToken> oldViewAboutTokens = ArrayListMultimap.create();
        final RobotTokenType typeOne = RobotTokenType.KEYWORD_ACTION_ARGUMENT;
        oldViewAboutTokens.put(typeOne, RobotToken.create("ok"));
        oldViewAboutTokens.put(typeOne, RobotToken.create("ok2"));
        oldViewAboutTokens.put(typeOne, RobotToken.create("ok3"));

        final ListMultimap<RobotTokenType, RobotToken> newViewAboutTokens = ArrayListMultimap.create();
        newViewAboutTokens.put(typeOne, RobotToken.create("ok"));
        newViewAboutTokens.put(typeOne, RobotToken.create("ok2"));
        newViewAboutTokens.put(typeOne, RobotToken.create("ok3"));

        // execute & verify
        new TwoModelReferencesLinker().validateThatTheSameTokensInView(oldViewAboutTokens, newViewAboutTokens);
    }

    @Test
    public void test_validateBasicThatOutputFromSameFile_areTheSameFiles() {
        // prepare
        final RobotFileOutput oldModifiedOutput = mock(RobotFileOutput.class);
        final File fOldModified = new File("old.txt");
        when(oldModifiedOutput.getProcessedFile()).thenReturn(fOldModified);

        final RobotFileOutput newOutput = mock(RobotFileOutput.class);
        final File fNew = new File("old.txt");
        when(newOutput.getProcessedFile()).thenReturn(fNew);

        // execute && verify
        new TwoModelReferencesLinker().validateBasicThatOutputFromSameFile(oldModifiedOutput, newOutput);
    }

    @Test(expected = DifferentOutputFile.class)
    public void test_validateBasicThatOutputFromSameFile_notTheSameFiles() {
        // prepare
        final RobotFileOutput oldModifiedOutput = mock(RobotFileOutput.class);
        final File fOldModified = new File("old.txt");
        when(oldModifiedOutput.getProcessedFile()).thenReturn(fOldModified);

        final RobotFileOutput newOutput = mock(RobotFileOutput.class);
        final File fNew = new File("old2.txt");
        when(newOutput.getProcessedFile()).thenReturn(fNew);

        // execute & verify
        new TwoModelReferencesLinker().validateBasicThatOutputFromSameFile(oldModifiedOutput, newOutput);
    }

    @Test
    public void test_update_fallbackAllowedMethod_logicCheck() {
        assertExecutionLogic(true);
    }

    @Test
    public void test_update_fallbackNotAllowedMethod_logicCheck() {
        assertExecutionLogic(false);
    }

    private void assertExecutionLogic(final boolean fallbackAllowed) {
        // prepare
        final RobotFileOutput oldModifiedOutput = mock(RobotFileOutput.class);
        final RobotFileOutput alreadyDumpedContent = mock(RobotFileOutput.class);
        final RobotTokensCollector collector = mock(RobotTokensCollector.class);
        final ListMultimap<RobotTokenType, RobotToken> oldView = ArrayListMultimap.create();
        when(collector.extractRobotTokens(oldModifiedOutput)).thenReturn(oldView);
        final ListMultimap<RobotTokenType, RobotToken> newView = ArrayListMultimap.create();
        when(collector.extractRobotTokens(alreadyDumpedContent)).thenReturn(newView);

        final TwoModelReferencesLinker tested = spy(new TwoModelReferencesLinker(collector));

        doNothing().when(tested).validateBasicThatOutputFromSameFile(oldModifiedOutput, alreadyDumpedContent);
        doNothing().when(tested).replaceNewReferenceByCorrespondingOld(oldModifiedOutput, oldView, alreadyDumpedContent,
                newView);

        // execute
        if (fallbackAllowed) {
            tested.update(oldModifiedOutput, alreadyDumpedContent);
        } else {
            tested.update(oldModifiedOutput, alreadyDumpedContent, false);
        }

        // verify
        final InOrder order = inOrder(tested, collector);
        if (fallbackAllowed) {
            order.verify(tested, times(1)).update(oldModifiedOutput, alreadyDumpedContent, true);
        }
        order.verify(tested, times(1)).validateBasicThatOutputFromSameFile(oldModifiedOutput, alreadyDumpedContent);
        order.verify(collector, times(1)).extractRobotTokens(oldModifiedOutput);
        order.verify(collector, times(1)).extractRobotTokens(alreadyDumpedContent);
        if (fallbackAllowed) {
            order.verify(tested, times(1)).validateThatTheSameTokensInView(oldView, newView);
        }
        order.verify(tested, times(1)).replaceNewReferenceByCorrespondingOld(oldModifiedOutput, oldView,
                alreadyDumpedContent, newView);
        order.verifyNoMoreInteractions();
    }
}

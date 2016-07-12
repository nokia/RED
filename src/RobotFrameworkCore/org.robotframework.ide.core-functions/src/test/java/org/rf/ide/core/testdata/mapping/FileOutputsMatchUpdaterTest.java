/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.io.File;

import org.junit.Test;
import org.mockito.InOrder;
import org.rf.ide.core.testdata.mapping.collect.RobotTokensCollector;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

/**
 * @author wypych
 */
public class FileOutputsMatchUpdaterTest {

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
        new FileOutputsMatchUpdater().validateBasicThatOutputFromSameFile(oldModifiedOutput, newOutput);
    }

    @Test
    public void test_validateBasicThatOutputFromSameFile_notTheSameFiles() {
        // prepare
        final RobotFileOutput oldModifiedOutput = mock(RobotFileOutput.class);
        final File fOldModified = new File("old.txt");
        when(oldModifiedOutput.getProcessedFile()).thenReturn(fOldModified);

        final RobotFileOutput newOutput = mock(RobotFileOutput.class);
        final File fNew = new File("old2.txt");
        when(newOutput.getProcessedFile()).thenReturn(fNew);

        // execute & verify
        final Exception exc = null;
        try {
            new FileOutputsMatchUpdater().validateBasicThatOutputFromSameFile(oldModifiedOutput, newOutput);
            fail("expected exception");
        } catch (Exception e) {
            e = exc;
        }

    }

    @Test
    public void test_validateBasicThatOutputFromSameFile_ioExceptionOccured() {
        // prepare
        // execute
        // verify
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
        ListMultimap<RobotTokenType, RobotToken> oldView = ArrayListMultimap.create();
        when(collector.extractRobotTokens(oldModifiedOutput)).thenReturn(oldView);
        ListMultimap<RobotTokenType, RobotToken> newView = ArrayListMultimap.create();
        when(collector.extractRobotTokens(alreadyDumpedContent)).thenReturn(newView);

        final FileOutputsMatchUpdater tested = spy(new FileOutputsMatchUpdater(collector));

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
        InOrder order = inOrder(tested, collector);
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

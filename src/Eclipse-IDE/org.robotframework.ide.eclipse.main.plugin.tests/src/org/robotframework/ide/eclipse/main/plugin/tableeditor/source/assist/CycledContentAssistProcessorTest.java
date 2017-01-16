/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.widgets.Display;
import org.junit.Ignore;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.mockdocument.Document;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.CycledContentAssistProcessor.AssitantCallbacks;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.SuiteSourceAssistantContext.AssistPreferences;
import org.robotframework.red.swt.SwtThread;

public class CycledContentAssistProcessorTest {

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void proposalsAreTakenFromNestedProcessorsOneByOneInCycle() throws Exception {
        final ICompletionProposal p1 = mock(ICompletionProposal.class), p2 = mock(ICompletionProposal.class),
                p3 = mock(ICompletionProposal.class), q1 = mock(ICompletionProposal.class),
                q2 = mock(ICompletionProposal.class), r1 = mock(ICompletionProposal.class),
                r2 = mock(ICompletionProposal.class), r3 = mock(ICompletionProposal.class);

        final AssistPreferences assistPreferences = new AssistPreferences(
                new MockRedPreferences(true, "  ", new char[] { 'a', 'b', 'c' }));
        final SuiteSourceAssistantContext assistContext = new SuiteSourceAssistantContext(null, assistPreferences);
        final AssitantCallbacks callback = mock(AssitantCallbacks.class);
        
        final IDocument document = spy(new Document());
        when(document.getContentType(0)).thenReturn("__ct");
        
        final ITextViewer viewer = mock(ITextViewer.class);
        when(viewer.getDocument()).thenReturn(document);

        final RedContentAssistProcessor processor1 = mock(RedContentAssistProcessor.class);
        when(processor1.getApplicableContentTypes()).thenReturn(newArrayList("__ct"));
        when(processor1.computeProposals(viewer, 0)).thenReturn((List) newArrayList(p1, p2, p3));

        final RedContentAssistProcessor processor2 = mock(RedContentAssistProcessor.class);
        when(processor2.getApplicableContentTypes()).thenReturn(newArrayList("__ct"));
        when(processor2.computeProposals(viewer, 0)).thenReturn((List) newArrayList(q1, q2));

        final RedContentAssistProcessor processor3 = mock(RedContentAssistProcessor.class);
        when(processor3.getApplicableContentTypes()).thenReturn(newArrayList("__ct"));
        when(processor3.computeProposals(viewer, 0)).thenReturn((List) newArrayList(r1, r2, r3));

        final CycledContentAssistProcessor cycledProcessor = new CycledContentAssistProcessor(assistContext, callback);
        cycledProcessor.addProcessor(processor1);
        cycledProcessor.addProcessor(processor2);
        cycledProcessor.addProcessor(processor3);

        assertThat(cycledProcessor.computeCompletionProposals(viewer, 0)).containsExactly(p1, p2, p3);
        assertThat(cycledProcessor.computeCompletionProposals(viewer, 0)).containsExactly(q1, q2);
        assertThat(cycledProcessor.computeCompletionProposals(viewer, 0)).containsExactly(r1, r2, r3);
        assertThat(cycledProcessor.computeCompletionProposals(viewer, 0)).containsExactly(p1, p2, p3);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void proposalsAreTakenFromNestedProcessors_butOnlyFromThoseWhichAreApplicableForCurrentContentType()
            throws Exception {
        final ICompletionProposal p1 = mock(ICompletionProposal.class), p2 = mock(ICompletionProposal.class),
                p3 = mock(ICompletionProposal.class), q1 = mock(ICompletionProposal.class),
                q2 = mock(ICompletionProposal.class), r1 = mock(ICompletionProposal.class),
                r2 = mock(ICompletionProposal.class), r3 = mock(ICompletionProposal.class);

        final AssistPreferences assistPreferences = new AssistPreferences(
                new MockRedPreferences(true, "  ", new char[] { 'a', 'b', 'c' }));
        final SuiteSourceAssistantContext assistContext = new SuiteSourceAssistantContext(null, assistPreferences);
        final AssitantCallbacks callback = mock(AssitantCallbacks.class);

        final IDocument document = spy(new Document());
        when(document.getContentType(0)).thenReturn("__ct");

        final ITextViewer viewer = mock(ITextViewer.class);
        when(viewer.getDocument()).thenReturn(document);

        final RedContentAssistProcessor processor1 = mock(RedContentAssistProcessor.class);
        when(processor1.getApplicableContentTypes()).thenReturn(newArrayList("__ct"));
        when(processor1.computeProposals(viewer, 0)).thenReturn((List) newArrayList(p1, p2, p3));

        final RedContentAssistProcessor processor2 = mock(RedContentAssistProcessor.class);
        when(processor2.getApplicableContentTypes()).thenReturn(newArrayList("__ct_different"));
        when(processor2.computeProposals(viewer, 0)).thenReturn((List) newArrayList(q1, q2));

        final RedContentAssistProcessor processor3 = mock(RedContentAssistProcessor.class);
        when(processor3.getApplicableContentTypes()).thenReturn(newArrayList("__ct"));
        when(processor3.computeProposals(viewer, 0)).thenReturn((List) newArrayList(r1, r2, r3));

        final CycledContentAssistProcessor cycledProcessor = new CycledContentAssistProcessor(assistContext, callback);
        cycledProcessor.addProcessor(processor1);
        cycledProcessor.addProcessor(processor2);
        cycledProcessor.addProcessor(processor3);

        assertThat(cycledProcessor.computeCompletionProposals(viewer, 0)).containsExactly(p1, p2, p3);
        assertThat(cycledProcessor.computeCompletionProposals(viewer, 0)).containsExactly(r1, r2, r3);
        assertThat(cycledProcessor.computeCompletionProposals(viewer, 0)).containsExactly(p1, p2, p3);
    }

    @Test
    public void nothingHappens_whenOrdinaryProposalIsApplied() {
        final AssistPreferences assistPreferences = new AssistPreferences(
                new MockRedPreferences(true, "  ", new char[] { 'a', 'b', 'c' }));
        final SuiteSourceAssistantContext assistContext = new SuiteSourceAssistantContext(null, assistPreferences);
        final AssitantCallbacks callback = mock(AssitantCallbacks.class);

        final ICompletionProposal proposal = mock(ICompletionProposal.class);

        final CycledContentAssistProcessor cycledProcessor = new CycledContentAssistProcessor(assistContext, callback);
        cycledProcessor.setCanReopenAssitantProgramatically(true);
        cycledProcessor.applied(proposal);

        verifyZeroInteractions(proposal);
        verifyZeroInteractions(callback);
    }

    @Test
    public void allOperationsOfProposalAreRun_whenProposalIsApplied_1() {
        final AtomicInteger runnedOperations = new AtomicInteger(0);

        final Runnable r1 = new Runnable() {

            @Override
            public void run() {
                runnedOperations.incrementAndGet();
            }
        };
        final Runnable r2 = new Runnable() {

            @Override
            public void run() {
                runnedOperations.incrementAndGet();
            }
        };

        final AssistPreferences assistPreferences = new AssistPreferences(
                new MockRedPreferences(true, "  ", new char[] { 'a', 'b', 'c' }));
        final SuiteSourceAssistantContext assistContext = new SuiteSourceAssistantContext(null, assistPreferences);
        final AssitantCallbacks callback = mock(AssitantCallbacks.class);

        final RedCompletionProposal proposal = mock(RedCompletionProposal.class);
        when(proposal.operationsToPerformAfterAccepting()).thenReturn(newArrayList(r1, r2));

        final CycledContentAssistProcessor cycledProcessor = new CycledContentAssistProcessor(assistContext, callback);
        cycledProcessor.setCanReopenAssitantProgramatically(true);
        cycledProcessor.applied(proposal);

        assertThat(runnedOperations.get()).isEqualTo(2);

        verifyZeroInteractions(callback);
    }

    @Test
    public void allOperationsOfProposalAreRun_whenProposalIsApplied_2() {
        final AtomicInteger runnedOperations = new AtomicInteger(0);

        final Runnable r1 = new Runnable() {

            @Override
            public void run() {
                runnedOperations.incrementAndGet();
            }
        };
        final Runnable r2 = new Runnable() {

            @Override
            public void run() {
                runnedOperations.incrementAndGet();
            }
        };

        final AssistPreferences assistPreferences = new AssistPreferences(
                new MockRedPreferences(true, "  ", new char[] { 'a', 'b', 'c' }));
        final SuiteSourceAssistantContext assistContext = new SuiteSourceAssistantContext(null, assistPreferences);
        final AssitantCallbacks callback = mock(AssitantCallbacks.class);

        final RedCompletionProposalAdapter proposal = mock(RedCompletionProposalAdapter.class);
        when(proposal.operationsToPerformAfterAccepting()).thenReturn(newArrayList(r1, r2));

        final CycledContentAssistProcessor cycledProcessor = new CycledContentAssistProcessor(assistContext, callback);
        cycledProcessor.setCanReopenAssitantProgramatically(true);
        cycledProcessor.applied(proposal);

        assertThat(runnedOperations.get()).isEqualTo(2);

        verifyZeroInteractions(callback);
    }

    @Ignore("this test influences and fails other tests - no idea why yet...")
    @Test
    public void completionProposalAssistantIsOpened_whenAppliedProposalRequiresIt_1() {
        final AssistPreferences assistPreferences = new AssistPreferences(
                new MockRedPreferences(true, "  ", new char[] { 'a', 'b', 'c' }));
        final SuiteSourceAssistantContext assistContext = new SuiteSourceAssistantContext(null, assistPreferences);
        final AssitantCallbacks callback = mock(AssitantCallbacks.class);

        final RedCompletionProposal proposal = mock(RedCompletionProposal.class);
        when(proposal.shouldActivateAssitantAfterAccepting()).thenReturn(true);

        final CycledContentAssistProcessor cycledProcessor = new CycledContentAssistProcessor(assistContext, callback);
        cycledProcessor.setCanReopenAssitantProgramatically(true);
        cycledProcessor.applied(proposal);

        execAllAwaitingMessages();

        verify(callback).openCompletionProposals();
    }

    @Ignore("this test influences and fails other tests - no idea why yet...")
    @Test
    public void completionProposalAssistantIsOpened_whenAppliedProposalRequiresIt_2() {
        final AssistPreferences assistPreferences = new AssistPreferences(
                new MockRedPreferences(true, "  ", new char[] { 'a', 'b', 'c' }));
        final SuiteSourceAssistantContext assistContext = new SuiteSourceAssistantContext(null, assistPreferences);
        final AssitantCallbacks callback = mock(AssitantCallbacks.class);

        final RedCompletionProposalAdapter proposal = mock(RedCompletionProposalAdapter.class);
        when(proposal.shouldActivateAssitantAfterAccepting()).thenReturn(true);

        final CycledContentAssistProcessor cycledProcessor = new CycledContentAssistProcessor(assistContext, callback);
        cycledProcessor.setCanReopenAssitantProgramatically(true);
        cycledProcessor.applied(proposal);

        execAllAwaitingMessages();

        verify(callback).openCompletionProposals();
    }

    @Test
    public void activationCharsAreTakenFromPreferences() {
        final AssistPreferences assistPreferences = new AssistPreferences(
                new MockRedPreferences(true, "  ", new char[] { 'a', 'b', 'c' }));
        final SuiteSourceAssistantContext assistContext = new SuiteSourceAssistantContext(null, assistPreferences);
        final CycledContentAssistProcessor cycledProcessor = new CycledContentAssistProcessor(assistContext, null);

        assertThat(cycledProcessor.getCompletionProposalAutoActivationCharacters()).containsExactly('a', 'b', 'c');
    }

    @Test
    public void nothingHappens_whenSelectionIsChanged() {
        final AssistPreferences assistPreferences = new AssistPreferences(
                new MockRedPreferences(true, "  ", new char[0]));
        final SuiteSourceAssistantContext assistContext = new SuiteSourceAssistantContext(null, assistPreferences);
        final CycledContentAssistProcessor cycledProcessor = spy(new CycledContentAssistProcessor(assistContext, null));

        final ICompletionProposal proposal = mock(ICompletionProposal.class);
        cycledProcessor.selectionChanged(proposal, true);

        verifyZeroInteractions(proposal);
        verify(cycledProcessor).selectionChanged(proposal, true);
        verifyNoMoreInteractions(cycledProcessor);
    }

    private static void execAllAwaitingMessages() {
        while (Display.getDefault().readAndDispatch()) {
            ;
        }

        // injecting empty operation, so that all the events awaiting in queue for SWT thread
        // will be for sure executed
        SwtThread.syncExec(new Runnable() {

            @Override
            public void run() {
                // nothing to do
            }
        });
    }
}

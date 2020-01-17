/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.Proposals.applyToDocument;

import java.util.Collection;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext;
import org.eclipse.jface.text.source.ISourceViewer;
import org.junit.jupiter.api.Test;
import org.robotframework.ide.eclipse.main.plugin.mockdocument.Document;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;

public class LocalAssignQuickAssistProviderTest {

    @Test
    public void noProposalsAreProvided_whenNotInCall() {
        final Document document = new Document(
                "*** Test Cases ***",
                "test",
                "  keyword  1  2  3");
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendDocumentContent(document).build();

        final IQuickAssistInvocationContext invocationContext = new InvocationContext(document, 20);

        final Collection<? extends ICompletionProposal> proposals = new LocalAssignQuickAssistProvider()
                .computeQuickAssistProposals(model, invocationContext);

        assertThat(proposals).isEmpty();
    }

    @Test
    public void noProposalsAreProvided_whenInsideForLoopHeader() {
        final Document document = new Document(
                "*** Test Cases ***",
                "test",
                "  FOR  ${i}  IN RANGE  10",
                "    keyword  1  2  3",
                "  END");
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendDocumentContent(document).build();

        final RobotKeywordCall call = model.findSection(RobotCasesSection.class)
                .get().getChildren().get(0).getChildren().get(0);
        final int offset = call.getLinkedElement().getElementTokens().get(2).getStartOffset();
        final IQuickAssistInvocationContext invocationContext = new InvocationContext(document, offset);

        final Collection<? extends ICompletionProposal> proposals = new LocalAssignQuickAssistProvider()
                .computeQuickAssistProposals(model, invocationContext);

        assertThat(proposals).isEmpty();
    }

    @Test
    public void noProposalsAreProvided_whenInEmptyRow() {
        final Document document = new Document(
                "*** Test Cases ***",
                "test",
                "  \\  # comment");
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendDocumentContent(document).build();

        final RobotKeywordCall call = model.findSection(RobotCasesSection.class)
                .get().getChildren().get(0).getChildren().get(0);
        final int offset = call.getLinkedElement().getElementTokens().get(1).getStartOffset();
        final IQuickAssistInvocationContext invocationContext = new InvocationContext(document, offset);

        final Collection<? extends ICompletionProposal> proposals = new LocalAssignQuickAssistProvider()
                .computeQuickAssistProposals(model, invocationContext);

        assertThat(proposals).isEmpty();
    }

    @Test
    public void noProposalsAreProvided_whenInSetting() {
        final Document document = new Document(
                "*** Test Cases ***",
                "test",
                "  [Setup]  Log  1  2  3");
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendDocumentContent(document).build();

        final RobotKeywordCall call = model.findSection(RobotCasesSection.class)
                .get().getChildren().get(0).getChildren().get(0);
        final int offset = call.getLinkedElement().getElementTokens().get(2).getStartOffset();
        final IQuickAssistInvocationContext invocationContext = new InvocationContext(document, offset);

        final Collection<? extends ICompletionProposal> proposals = new LocalAssignQuickAssistProvider()
                .computeQuickAssistProposals(model, invocationContext);

        assertThat(proposals).isEmpty();
    }

    @Test
    public void proposalsAreProvided_andCanBeAppliedForOrdinaryKeywordCall() {
        final Document document = new Document(
                "*** Test Cases ***",
                "test",
                "  keyword  1  2  3");
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendDocumentContent(document).build();

        final RobotKeywordCall call = model.findSection(RobotCasesSection.class)
                .get().getChildren().get(0).getChildren().get(0);
        final int offset = call.getLinkedElement().getElementTokens().get(1).getStartOffset();
        final IQuickAssistInvocationContext invocationContext = new InvocationContext(document, offset);

        final Collection<? extends ICompletionProposal> proposals = new LocalAssignQuickAssistProvider()
                .computeQuickAssistProposals(model, invocationContext);

        assertThat(proposals).hasSize(3)
                .extracting(ICompletionProposal::getDisplayString)
                .containsExactly("Assign to local scalar", "Assign to local list", "Assign to local dictionary");
        assertThat(proposals).extracting(proposal -> applyToDocument(document, proposal))
                .containsOnly(
                        new Document("*** Test Cases ***", "test", "  ${var}    keyword  1  2  3"),
                        new Document("*** Test Cases ***", "test", "  @{var}    keyword  1  2  3"),
                        new Document("*** Test Cases ***", "test", "  &{var}    keyword  1  2  3"));
    }

    @Test
    public void proposalsAreProvided_andCanBeAppliedForKeywordCallWhichAlreadyAssigns() {
        final Document document = new Document(
                "*** Test Cases ***",
                "test",
                "  ${x}=  keyword  1  2  3");
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendDocumentContent(document).build();

        final RobotKeywordCall call = model.findSection(RobotCasesSection.class)
                .get().getChildren().get(0).getChildren().get(0);
        final int offset = call.getLinkedElement().getElementTokens().get(1).getStartOffset();
        final IQuickAssistInvocationContext invocationContext = new InvocationContext(document, offset);

        final Collection<? extends ICompletionProposal> proposals = new LocalAssignQuickAssistProvider()
                .computeQuickAssistProposals(model, invocationContext);

        assertThat(proposals).hasSize(3)
                .extracting(ICompletionProposal::getDisplayString)
                .containsExactly("Assign to local scalar", "Assign to local list", "Assign to local dictionary");
        assertThat(proposals).extracting(proposal -> applyToDocument(document, proposal))
                .containsOnly(
                        new Document("*** Test Cases ***", "test", "  ${var}    ${x}=  keyword  1  2  3"),
                        new Document("*** Test Cases ***", "test", "  @{var}    ${x}=  keyword  1  2  3"),
                        new Document("*** Test Cases ***", "test", "  &{var}    ${x}=  keyword  1  2  3"));
    }

    @Test
    public void proposalsAreProvided_andCanBeAppliedForKeywordCallInsideOldStyleLoop() {
        final Document document = new Document(
                "*** Test Cases ***",
                "test",
                "  :FOR  ${i}  IN RANGE  10",
                "  \\  keyword  1  2  3");
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendDocumentContent(document).build();

        final RobotKeywordCall call = model.findSection(RobotCasesSection.class)
                .get().getChildren().get(0).getChildren().get(1);
        final int offset = call.getLinkedElement().getElementTokens().get(1).getStartOffset();
        final IQuickAssistInvocationContext invocationContext = new InvocationContext(document, offset);

        final Collection<? extends ICompletionProposal> proposals = new LocalAssignQuickAssistProvider()
                .computeQuickAssistProposals(model, invocationContext);

        assertThat(proposals).hasSize(3)
                .extracting(ICompletionProposal::getDisplayString)
                .containsExactly("Assign to local scalar", "Assign to local list", "Assign to local dictionary");
        assertThat(proposals).extracting(proposal -> applyToDocument(document, proposal))
                .containsOnly(
                        new Document("*** Test Cases ***", "test", "  :FOR  ${i}  IN RANGE  10",
                                "  \\  ${var}    keyword  1  2  3"),
                        new Document("*** Test Cases ***", "test", "  :FOR  ${i}  IN RANGE  10",
                                "  \\  @{var}    keyword  1  2  3"),
                        new Document("*** Test Cases ***", "test", "  :FOR  ${i}  IN RANGE  10",
                                "  \\  &{var}    keyword  1  2  3"));
    }

    @Test
    public void proposalsAreProvided_andCanBeAppliedForKeywordCallInsideLoop() {
        final Document document = new Document(
                "*** Test Cases ***",
                "test",
                "  FOR  ${i}  IN RANGE  10",
                "    keyword  1  2  3",
                "  END");
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendDocumentContent(document).build();

        final RobotKeywordCall call = model.findSection(RobotCasesSection.class)
                .get().getChildren().get(0).getChildren().get(1);
        final int offset = call.getLinkedElement().getElementTokens().get(1).getStartOffset();
        final IQuickAssistInvocationContext invocationContext = new InvocationContext(document, offset);

        final Collection<? extends ICompletionProposal> proposals = new LocalAssignQuickAssistProvider()
                .computeQuickAssistProposals(model, invocationContext);

        assertThat(proposals).hasSize(3)
                .extracting(ICompletionProposal::getDisplayString)
                .containsExactly("Assign to local scalar", "Assign to local list", "Assign to local dictionary");
        assertThat(proposals).extracting(proposal -> applyToDocument(document, proposal))
                .containsOnly(
                        new Document("*** Test Cases ***", "test", "  FOR  ${i}  IN RANGE  10",
                                "    ${var}    keyword  1  2  3", "  END"),
                        new Document("*** Test Cases ***", "test", "  FOR  ${i}  IN RANGE  10",
                                "    @{var}    keyword  1  2  3", "  END"),
                        new Document("*** Test Cases ***", "test", "  FOR  ${i}  IN RANGE  10",
                                "    &{var}    keyword  1  2  3", "  END"));
    }

    private static class InvocationContext implements IQuickAssistInvocationContext {

        private final int offset;

        private final ISourceViewer viewer;

        public InvocationContext(final IDocument document, final int offset) {
            this.offset = offset;
            this.viewer = mock(ISourceViewer.class);
            when(viewer.getDocument()).thenReturn(document);
        }

        @Override
        public int getOffset() {
            return offset;
        }

        @Override
        public int getLength() {
            return 0;
        }

        @Override
        public ISourceViewer getSourceViewer() {
            return viewer;
        }
    }
}

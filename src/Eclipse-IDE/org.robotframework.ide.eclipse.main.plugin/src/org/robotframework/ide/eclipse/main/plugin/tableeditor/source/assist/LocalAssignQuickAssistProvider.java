/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext;
import org.eclipse.jface.text.source.ISourceViewer;
import org.rf.ide.core.testdata.model.FileFormat;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.exec.descs.IExecutableRowDescriptor;
import org.rf.ide.core.testdata.model.table.exec.descs.IExecutableRowDescriptor.RowType;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.testdata.text.read.separators.Separator.SeparatorType;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences.LinkedModeStrategy;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.build.fix.Snippets;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourcePartitionScanner;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.jface.text.link.RedEditorLinkedModeUI;
import org.robotframework.red.swt.SwtThread;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;

public class LocalAssignQuickAssistProvider implements QuickAssistProvider {

    @Override
    public boolean canAssist(final IQuickAssistInvocationContext invocationContext) {
        final ISourceViewer sourceViewer = invocationContext.getSourceViewer();
        try {
            final String cType = sourceViewer.getDocument().getContentType(invocationContext.getOffset());
            return SuiteSourcePartitionScanner.KEYWORDS_SECTION.equals(cType)
                    || SuiteSourcePartitionScanner.TEST_CASES_SECTION.equals(cType)
                    || SuiteSourcePartitionScanner.TASKS_SECTION.equals(cType);

        } catch (final BadLocationException e) {
            return false;
        }
    }

    @Override
    public Collection<? extends ICompletionProposal> computeQuickAssistProposals(final RobotSuiteFile fileModel,
            final IQuickAssistInvocationContext invocationContext) {

        final Optional<? extends RobotElement> element = fileModel.findElement(invocationContext.getOffset());

        if (element.isPresent() && element.get() instanceof RobotKeywordCall) {
            final RobotKeywordCall call = (RobotKeywordCall) element.get();
            if (call.isExecutable()) {
                final IExecutableRowDescriptor<?> description = ((RobotExecutableRow<?>) call.getLinkedElement())
                        .buildLineDescription();
                if (description.getRowType() == RowType.SIMPLE || description.getRowType() == RowType.FOR_CONTINUE) {
                    final int offsetToInsert = computeOffsetToInsert(description);
                    return computeAssignProposals(fileModel, invocationContext.getSourceViewer(), offsetToInsert);
                }
            }
        }
        return new ArrayList<>();
    }

    private int computeOffsetToInsert(final IExecutableRowDescriptor<?> description) {
        final RobotExecutableRow<?> row = (RobotExecutableRow<?>) description.getRow();
        if (description.getRowType() == RowType.FOR_CONTINUE) {
            if (description.getAction().getTypes().contains(RobotTokenType.FOR_CONTINUE_TOKEN)) {
                // old loop style with # continuation
                return row.getArguments().get(0).getStartOffset();
            } else {
                return row.getAction().getStartOffset();
            }
        } else {
            return row.getAction().getStartOffset();
        }
    }

    private Collection<? extends ICompletionProposal> computeAssignProposals(final RobotSuiteFile fileModel,
            final ISourceViewer viewer, final int offsetToInsert) {

        final IRegion regionToChange = new Region(offsetToInsert, 0);
        final String separator = getSeparator(fileModel, offsetToInsert);

        final List<IRegion> linkedModeRegions = newArrayList(new Region(offsetToInsert + 2, 3),
                new Region(offsetToInsert + 6, 0));
        final Collection<Runnable> operations = newArrayList(() -> SwtThread.asyncExec(() -> RedEditorLinkedModeUI
                .enableLinkedMode(viewer, linkedModeRegions, LinkedModeStrategy.EXIT_ON_LAST)));

        final IDocument document = viewer.getDocument();

        final Map<Character, String> names = ImmutableMap.of('$', "scalar", '@', "list", '&', "dictionary");
        final List<ICompletionProposal> proposals = new ArrayList<>();
        for (final char mark : names.keySet()) {
            final String replacement = mark + "{var}" + separator;
            final String info = Snippets.createSnippetInfo(document, regionToChange, replacement);
            final RedCompletionProposal proposal = RedCompletionBuilder.newProposal()
                    .willPut(replacement)
                    .byReplacingRegion(regionToChange)
                    .secondaryPopupShouldBeDisplayedUsingHtml(info)
                    .performAfterAccepting(operations)
                    .thenCursorWillStopAt(offsetToInsert)
                    .displayedLabelShouldBe("Assign to local " + names.get(mark))
                    .proposalsShouldHaveIcon(ImagesManager.getImage(RedImages.getRobotVariableImage()))
                    .create();
            proposals.add(proposal);
        }
        return proposals;
    }

    protected final String getSeparator(final RobotSuiteFile suiteModel, final int offset) {
        final RobotFile fileModel = suiteModel.getLinkedElement();
        final FileFormat fileFormat = fileModel.getParent().getFileFormat();
        if (fileFormat == FileFormat.TSV) {
            return "\t";
        } else {
            return fileModel.getRobotLineIndexBy(offset)
                    .map(l -> fileModel.getFileContent().get(l))
                    .flatMap(RobotLine::getSeparatorForLine)
                    .filter(sep -> sep == SeparatorType.PIPE)
                    .map(sep -> " | ")
                    .orElse(Strings.repeat(" ", 4));
        }
    }
}

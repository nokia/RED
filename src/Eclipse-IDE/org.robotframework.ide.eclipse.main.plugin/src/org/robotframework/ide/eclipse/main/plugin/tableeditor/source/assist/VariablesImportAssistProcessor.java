/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Image;
import org.robotframework.ide.eclipse.main.plugin.PathsConverter;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedVariableFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.DocumentUtilities;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourceEditorContentAssist;
import org.robotframework.ide.eclipse.main.plugin.texteditor.contentAssist.RedCompletionBuilder;
import org.robotframework.ide.eclipse.main.plugin.texteditor.contentAssist.RedCompletionProposal;
import org.robotframework.red.graphics.ImagesManager;

import com.google.common.base.Optional;


/**
 * @author Michal Anglart
 *
 */
public class VariablesImportAssistProcessor extends RedContentAssistProcessor {

    private final AssistPreferences assistPreferences = new AssistPreferences();

    private final SuiteSourceEditorContentAssist assist;

    public VariablesImportAssistProcessor(final SuiteSourceEditorContentAssist assist) {
        this.assist = assist;
    }

    @Override
    protected String getProposalsTitle() {
        return "Variable files";
    }

    @Override
    protected List<? extends ICompletionProposal> computeProposals(final ITextViewer viewer, final int offset) {
        final IDocument document = viewer.getDocument();
        try {
            final String lineContent = DocumentUtilities.lineContentBeforeCurrentPosition(document, offset);
            final boolean shouldShowProposal = shouldShowProposals(lineContent);

            if (shouldShowProposal) {
                final Optional<IRegion> region = DocumentUtilities.findLiveCellRegion(document, offset);
                final String prefix = getPrefix(document, region, offset);
                final String content = region.isPresent()
                        ? document.get(region.get().getOffset(), region.get().getLength()) : "";
                final Image image = ImagesManager.getImage(RedImages.getImageForFileWithExtension(".py"));

                final List<RedCompletionProposal> proposals = newArrayList();
                for (final ReferencedVariableFile varFile : assist.getReferencedVariableFiles()) {
                    final String resourcePath = varFile.getPath();
                    if (resourcePath.toLowerCase().startsWith(prefix.toLowerCase())) {
                        final String resourceRelativePath = createCurrentFileRelativePath(resourcePath);
                        final RedCompletionProposal proposal = RedCompletionBuilder.newProposal()
                                .will(assistPreferences.getAcceptanceMode())
                                .theText(resourceRelativePath)
                                .atOffset(offset - prefix.length())
                                .givenThatCurrentPrefixIs(prefix)
                                .andWholeContentIs(content)
                                .thenCursorWillStopAtTheEndOfInsertion()
                                .displayedLabelShouldBe(resourcePath)
                                .currentPrefixShouldBeDecorated()
                                .proposalsShouldHaveIcon(image)
                                .create();
                        proposals.add(proposal);
                    }
                }
                return proposals;
            }
            return null;
        } catch (final BadLocationException e) {
            return null;
        }
    }

    private String createCurrentFileRelativePath(final String resourcePath) {
        final IPath asPath = new Path(resourcePath);
        final IPath wsRelatedPath = PathsConverter.toWorkspaceRelativeIfPossible(asPath);
        if (wsRelatedPath.isAbsolute()) {
            return asPath.toString();
        }
        return PathsConverter.fromWorkspaceRelativeToResourceRelative(assist.getFile(), wsRelatedPath).toString();
    }

    private boolean shouldShowProposals(final String lineContent) {
        return lineContent.toLowerCase().startsWith("variables")
                && DocumentUtilities.getNumberOfCellSeparators(lineContent) == 1;
    }

    private String getPrefix(final IDocument document, final Optional<IRegion> optional, final int offset) {
        if (!optional.isPresent()) {
            return "";
        }
        try {
            return document.get(optional.get().getOffset(), offset - optional.get().getOffset());
        } catch (final BadLocationException e) {
            return "";
        }
    }

}

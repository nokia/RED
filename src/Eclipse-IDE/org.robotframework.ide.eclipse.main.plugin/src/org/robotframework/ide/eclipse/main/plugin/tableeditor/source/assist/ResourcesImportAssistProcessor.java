/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Image;
import org.robotframework.ide.eclipse.main.plugin.PathsConverter;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.DocumentUtilities;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourcePartitionScanner;
import org.robotframework.red.graphics.ImagesManager;

import com.google.common.base.Optional;


/**
 * @author Michal Anglart
 *
 */
public class ResourcesImportAssistProcessor extends RedContentAssistProcessor {

    private final SuiteSourceAssistantContext assist;

    public ResourcesImportAssistProcessor(final SuiteSourceAssistantContext assist) {
        this.assist = assist;
    }

    @Override
    protected List<String> getApplicableContentTypes() {
        return newArrayList(SuiteSourcePartitionScanner.SETTINGS_SECTION);
    }

    @Override
    protected String getProposalsTitle() {
        return "Resource files";
    }

    @Override
    protected List<? extends ICompletionProposal> computeProposals(final ITextViewer viewer, final int offset) {
        final IDocument document = viewer.getDocument();
        try {
            final String lineContent = DocumentUtilities.lineContentBeforeCurrentPosition(document, offset);
            final boolean shouldShowProposal = shouldShowProposals(lineContent, document, offset);

            if (shouldShowProposal) {
                final boolean isTsv = assist.isTsvFile();
                final Optional<IRegion> region = DocumentUtilities.findLiveCellRegion(document, isTsv, offset);
                final String prefix = DocumentUtilities.getPrefix(document, region, offset);
                final String content = region.isPresent()
                        ? document.get(region.get().getOffset(), region.get().getLength()) : "";
                final Image image = ImagesManager.getImage(RedImages.getImageForFileWithExtension(".robot"));

                final List<RedCompletionProposal> proposals = newArrayList();
                for (final IFile resourceFile : assist.getResourceFiles()) {

                    final String resourcePath = resourceFile.getFullPath().makeRelative().toString();
                    if (resourcePath.toLowerCase().startsWith(prefix.toLowerCase())) {
                        final String resourceRelativePath = createCurrentFileRelativePath(
                                resourceFile.getFullPath().makeRelative());
                        final RedCompletionProposal proposal = RedCompletionBuilder.newProposal()
                                .will(assist.getAcceptanceMode())
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

    private String createCurrentFileRelativePath(final IPath resourcePath) {
        return PathsConverter.fromWorkspaceRelativeToResourceRelative(assist.getFile(), resourcePath).toString();
    }

    private boolean shouldShowProposals(final String lineContent, final IDocument document, final int offset)
            throws BadLocationException {
        return isInApplicableContentType(document, offset) && lineContent.toLowerCase().startsWith("resource")
                && DocumentUtilities.getNumberOfCellSeparators(lineContent, assist.isTsvFile()) == 1;
    }

}

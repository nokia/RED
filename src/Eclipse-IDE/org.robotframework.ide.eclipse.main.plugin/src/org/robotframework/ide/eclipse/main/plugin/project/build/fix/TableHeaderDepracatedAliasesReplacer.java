/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.fix;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.rf.ide.core.testdata.model.table.TableHeader;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;
import org.robotframework.red.graphics.ImagesManager;

import com.google.common.base.Optional;
import com.google.common.collect.Range;

public class TableHeaderDepracatedAliasesReplacer extends RedSuiteMarkerResolution {

    private final String toReplace;

    private final String replacement;

    private final Class<? extends RobotSuiteFileSection> sectionClass;

    private final String label;

    public TableHeaderDepracatedAliasesReplacer(final Class<? extends RobotSuiteFileSection> sectionClass,
            final String toReplace, final String replacement) {
        this.sectionClass = sectionClass;
        this.toReplace = toReplace;
        this.replacement = replacement;
        this.label = "Replace old style " + this.toReplace + " by " + this.replacement;
    }

    @Override
    public String getLabel() {
        return this.label;
    }

    @Override
    public Optional<ICompletionProposal> asContentProposal(final IMarker marker, final IDocument document,
            final RobotSuiteFile suiteModel) {
        Optional<ICompletionProposal> proposal = Optional.absent();

        final Range<Integer> defRange = getRange(marker);
        final Optional<? extends RobotSuiteFileSection> section = suiteModel.findSection(sectionClass);
        if (section.isPresent()) {
            try {
                return createProposal(document, findHeader(section, defRange.lowerEndpoint()));
            } catch (final BadLocationException e) {
            }
        }

        return proposal;
    }

    private TableHeader<? extends ARobotSectionTable> findHeader(
            final Optional<? extends RobotSuiteFileSection> section, final int startOffset) {
        TableHeader<? extends ARobotSectionTable> elem = null;
        final ARobotSectionTable table = section.get().getLinkedElement();
        for (final TableHeader<? extends ARobotSectionTable> th : table.getHeaders()) {
            final RobotToken tableHeader = th.getTableHeader();
            int headerStartOffset = tableHeader.getStartOffset();
            int headerEndOffset = headerStartOffset + th.getTableHeader().getRaw().length();

            if (headerStartOffset <= startOffset && startOffset < headerEndOffset) {
                elem = th;
                break;
            }
        }

        return elem;
    }

    private Optional<ICompletionProposal> createProposal(final IDocument document,
            TableHeader<? extends ARobotSectionTable> header) throws BadLocationException {
        Optional<ICompletionProposal> proposal = Optional.absent();
        if (header != null) {
            final RobotToken declaration = header.getDeclaration();
            final String declarationValue = declaration.getRaw();

            String myReplacement = declarationValue.toLowerCase().replaceFirst(toReplace.toLowerCase(), replacement);

            final String replacementString = myReplacement.toString();
            final int offset = declaration.getStartOffset();
            final ICompletionProposal fix = new CompletionProposal(replacementString, offset, declarationValue.length(),
                    offset + replacementString.length(), ImagesManager.getImage(RedImages.getUserKeywordImage()),
                    getLabel(), null, null);
            proposal = Optional.of(fix);
        }

        return proposal;
    }

    private Range<Integer> getRange(final IMarker marker) {
        try {
            return Range.closed((Integer) marker.getAttribute(IMarker.CHAR_START),
                    (Integer) marker.getAttribute(IMarker.CHAR_END));
        } catch (final CoreException e) {
            throw new IllegalStateException("Given marker should have offsets defined", e);
        }
    }
}

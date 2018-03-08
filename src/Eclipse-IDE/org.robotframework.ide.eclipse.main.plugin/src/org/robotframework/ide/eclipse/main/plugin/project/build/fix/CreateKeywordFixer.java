/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.fix;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.ui.IMarkerResolution;
import org.rf.ide.core.testdata.model.table.keywords.names.GherkinStyleSupport;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.DocumentUtilities;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.RedCompletionBuilder;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.RedCompletionProposal;
import org.robotframework.red.graphics.ImagesManager;

import com.google.common.base.Strings;

/**
 * @author Michal Anglart
 */
public class CreateKeywordFixer extends RedSuiteMarkerResolution {

    public static Collection<? extends IMarkerResolution> createFixers(final String name) {
        final List<IMarkerResolution> fixers = new ArrayList<>();
        if (name == null) {
            return fixers;
        }

        GherkinStyleSupport.forEachPossibleGherkinName(name, gherkinNameVariant -> {
            if (!gherkinNameVariant.isEmpty()) {
                fixers.add(new CreateKeywordFixer(gherkinNameVariant));
            }
        });
        return fixers;
    }

    private final String keywordName;

    public CreateKeywordFixer(final String keywordName) {
        this.keywordName = keywordName;
    }

    @Override
    public String getLabel() {
        return "Create '" + keywordName + "' keyword";
    }

    @Override
    public Optional<ICompletionProposal> asContentProposal(final IMarker marker, final IDocument document,
            final RobotSuiteFile suiteModel) {
        if (keywordName == null) {
            return Optional.empty();
        }

        final String lineDelimiter = DocumentUtilities.getDelimiter(document);
        final String separator = getSeparator(suiteModel);

        final String toInsert;
        final int offset;
        final int cursorShift;

        final Optional<RobotKeywordsSection> section = suiteModel.findSection(RobotKeywordsSection.class);
        if (section.isPresent()) {
            try {
                toInsert = lineDelimiter + keywordName + lineDelimiter + separator + lineDelimiter;

                final int line = section.get().getHeaderLine();
                final IRegion lineInformation = document.getLineInformation(line - 1);
                offset = lineInformation.getOffset() + lineInformation.getLength();
                cursorShift = lineDelimiter.length();
            } catch (final BadLocationException e) {
                return Optional.empty();
            }

        } else {
            toInsert = Strings.repeat(lineDelimiter, 2) + "*** Keywords ***" + lineDelimiter + keywordName
                    + lineDelimiter + separator;
            offset = document.getLength();
            cursorShift = 0;
        }

        final IRegion regionOfChange = new Region(offset, 0);
        final String info = Snippets.createSnippetInfo(document, regionOfChange, toInsert);
        final RedCompletionProposal proposal = RedCompletionBuilder.newProposal()
                .willPut(toInsert)
                .byInsertingAt(regionOfChange.getOffset())
                .secondaryPopupShouldBeDisplayedUsingHtml(info)
                .thenCursorWillStopBeforeEnd(cursorShift)
                .displayedLabelShouldBe(getLabel())
                .proposalsShouldHaveIcon(ImagesManager.getImage(RedImages.getUserKeywordImage()))
                .create();
        return Optional.of(proposal);
    }
}

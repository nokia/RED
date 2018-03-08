/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.fix;

import java.util.Optional;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.DocumentUtilities;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.RedCompletionBuilder;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.RedCompletionProposal;
import org.robotframework.red.graphics.ImagesManager;

import com.google.common.base.Strings;

/**
 * @author Michal Anglart
 */
public class CreateVariableFixer extends RedSuiteMarkerResolution {

    private final String variableName;

    public CreateVariableFixer(final String variableName) {
        this.variableName = variableName;
    }

    @Override
    public String getLabel() {
        return "Define " + variableName + " in Variables table";
    }

    @Override
    public Optional<ICompletionProposal> asContentProposal(final IMarker marker, final IDocument document,
            final RobotSuiteFile suiteModel) {
        if (variableName == null) {
            return Optional.empty();
        }

        final String lineDelimiter = DocumentUtilities.getDelimiter(document);
        final String cellSeparator = getSeparator(suiteModel);

        final Optional<RobotVariablesSection> section = suiteModel.findSection(RobotVariablesSection.class);

        final String toInsert;
        int offsetOfChange;

        final String variableDefinition = lineDelimiter + variableName + cellSeparator;
        if (section.isPresent()) {
            try {
                toInsert = variableDefinition;
                final int line = section.get().getHeaderLine();
                final IRegion lineInformation = document.getLineInformation(line - 1);

                offsetOfChange = lineInformation.getOffset() + lineInformation.getLength();
            } catch (final BadLocationException e) {
                return Optional.empty();
            }
        } else {
            toInsert = Strings.repeat(lineDelimiter, 2) + "*** Variables ***" + variableDefinition;
            offsetOfChange = document.getLength();
        }

        final IRegion regionOfChange = new Region(offsetOfChange, 0);
        final String info = Snippets.createSnippetInfo(document, regionOfChange, toInsert);
        final RedCompletionProposal proposal = RedCompletionBuilder.newProposal()
                .willPut(toInsert)
                .byInsertingAt(regionOfChange.getOffset())
                .secondaryPopupShouldBeDisplayedUsingHtml(info)
                .thenCursorWillStopAtTheEndOfInsertion()
                .displayedLabelShouldBe(getLabel())
                .proposalsShouldHaveIcon(ImagesManager.getImage(RedImages.getRobotVariableImage()))
                .create();
        return Optional.of(proposal);
    }
}

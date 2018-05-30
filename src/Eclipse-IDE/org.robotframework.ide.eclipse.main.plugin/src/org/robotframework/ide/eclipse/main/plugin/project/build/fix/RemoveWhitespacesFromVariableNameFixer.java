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
import org.rf.ide.core.testdata.model.table.variables.AVariable;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.RedCompletionBuilder;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.RedCompletionProposal;
import org.robotframework.red.graphics.ImagesManager;

import com.google.common.collect.Range;

public class RemoveWhitespacesFromVariableNameFixer extends RedSuiteMarkerResolution {

    private final String variableName;

    public RemoveWhitespacesFromVariableNameFixer(final String variableName) {
        this.variableName = variableName;
    }

    @Override
    public String getLabel() {
        return "Remove whitespaces after variable '" + variableName + "' type identificator";
    }

    @Override
    public Optional<ICompletionProposal> asContentProposal(final IMarker marker, final IDocument document,
            final RobotSuiteFile suiteModel) {
        if (variableName == null) {
            return Optional.empty();
        }
        final Optional<RobotVariablesSection> section = suiteModel.findSection(RobotVariablesSection.class);
        if (!section.isPresent()) {
            return Optional.empty();
        }
        for (final RobotVariable variable : section.get().getChildren()) {
            final Range<Integer> defRange = RobotProblem.getRangeOf(marker);
            if (defRange.contains(variable.getDefinitionPosition().getOffset())) {
                try {
                    return createProposal(document, variable);
                } catch (final BadLocationException e) {
                    return Optional.empty();
                }
            }
        }
        return Optional.empty();
    }

    private Optional<ICompletionProposal> createProposal(final IDocument document, final RobotVariable variable)
            throws BadLocationException {

        final AVariable var = variable.getLinkedElement();

        final RobotToken varDeclaration = var.getDeclaration();
        final int offset = varDeclaration.getStartOffset();
        final String varText = varDeclaration.getText();
        final String correctedVariable = varText.charAt(0) + varText.substring(varText.indexOf('{'));

        final IRegion toChange = new Region(offset, varDeclaration.getEndOffset() - offset);

        final String info = Snippets.createSnippetInfo(document, toChange, correctedVariable);
        final RedCompletionProposal proposal = RedCompletionBuilder.newProposal()
                .willPut(correctedVariable)
                .byReplacingRegion(toChange)
                .secondaryPopupShouldBeDisplayedUsingHtml(info)
                .thenCursorWillStopAtTheEndOfInsertion()
                .displayedLabelShouldBe(getLabel())
                .proposalsShouldHaveIcon(ImagesManager.getImage(RedImages.getRobotVariableImage()))
                .create();
        return Optional.of(proposal);
    }
}

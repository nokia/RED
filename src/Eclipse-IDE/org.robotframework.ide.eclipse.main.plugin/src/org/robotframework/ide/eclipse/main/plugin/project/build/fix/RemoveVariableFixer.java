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
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.red.graphics.ImagesManager;

import com.google.common.collect.Range;

/**
 * @author Michal Anglart
 *
 */
public class RemoveVariableFixer extends RedSuiteMarkerResolution {

    private final String variableName;

    public RemoveVariableFixer(final String variableName) {
        this.variableName = variableName;
    }

    @Override
    public String getLabel() {
        return "Remove '" + variableName + "' variable";
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
        final Position position = variable.getPosition();
        final int offset = position.getOffset();
        final int length = position.getLength();

        int shift = 0;

        if (document.getLength() > length + offset) {
            int ch = document.getChar(offset + length + shift);
            while (ch == '\r' || ch == '\n' || ch == ' ') {
                if (ch == -1) {
                    break;
                }
                ch = document.getChar(offset + length + shift);
                shift++;
            }
        }

        return Optional.of(new CompletionProposal("", offset, length + shift - 1, offset,
                ImagesManager.getImage(RedImages.getUserKeywordImage()), getLabel(), null, null));
    }
}

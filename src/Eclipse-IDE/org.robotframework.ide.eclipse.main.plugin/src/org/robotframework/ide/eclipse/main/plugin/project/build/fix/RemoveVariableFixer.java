/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.fix;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.RedMarkerResolution;
import org.robotframework.red.graphics.ImagesManager;

import com.google.common.base.Optional;
import com.google.common.collect.Range;

/**
 * @author Michal Anglart
 *
 */
public class RemoveVariableFixer extends RedMarkerResolution {

    private final String variableName;

    public RemoveVariableFixer(final String variableName) {
        this.variableName = variableName;
    }

    @Override
    public String getLabel() {
        return "Remove '" + variableName + "' variable";
    }

    @Override
    public ICompletionProposal asContentProposal(final IMarker marker, final IDocument document,
            final RobotSuiteFile suiteModel) {
        if (variableName == null) {
            return null;
        }
        final Optional<RobotVariablesSection> section = suiteModel.findSection(RobotVariablesSection.class);
        if (!section.isPresent()) {
            return null;
        }
        for (final RobotVariable variable : section.get().getChildren()) {
            final Range<Integer> defRange = getRange(marker);
            if (defRange.contains(variable.getDefinitionPosition().offset)) {
                try {
                    return createProposal(document, variable);
                } catch (final BadLocationException e) {
                    return null;
                }
            }
        }
        return null;
    }

    private CompletionProposal createProposal(final IDocument document, final RobotVariable variable)
            throws BadLocationException {
        final Position position = variable.getPosition();
        final int offset = position.getOffset();
        final int length = position.getLength();

        int shift = 0;
        int ch = document.getChar(offset + length + shift);
        while (ch == '\r' || ch == '\n' || ch == ' ') {
            if (ch == -1) {
                break;
            }
            ch = document.getChar(offset + length + shift);
            shift++;
        }

        return new CompletionProposal("", offset, length + shift - 1, offset,
                ImagesManager.getImage(RedImages.getUserKeywordImage()), getLabel(), null, null);
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

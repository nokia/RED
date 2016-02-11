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
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;
import org.robotframework.red.graphics.ImagesManager;

import com.google.common.base.Optional;
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
            return Optional.absent();
        }
        final Optional<RobotVariablesSection> section = suiteModel.findSection(RobotVariablesSection.class);
        if (!section.isPresent()) {
            return Optional.absent();
        }
        for (final RobotVariable variable : section.get().getChildren()) {
            final Range<Integer> defRange = getRange(marker);
            if (defRange.contains(variable.getDefinitionPosition().getOffset())) {
                try {
                    return createProposal(document, variable);
                } catch (final BadLocationException e) {
                    return Optional.absent();
                }
            }
        }
        return Optional.absent();
    }

    private Optional<ICompletionProposal> createProposal(final IDocument document, final RobotVariable variable)
            throws BadLocationException {

        final RobotToken varDeclaration = variable.getLinkedElement().getDeclaration();
        final int offset = varDeclaration.getStartOffset();
        final String varText = varDeclaration.getRaw().toString();
        final String correctedVariable = new StringBuilder().append(varText.charAt(0)).append(varText.substring(varText.indexOf('{'))).toString();  
        final ICompletionProposal proposal = new CompletionProposal(correctedVariable, offset, varText.length(), offset + correctedVariable.length(),
                ImagesManager.getImage(RedImages.getUserKeywordImage()), getLabel(), null, null);
        return Optional.of(proposal);
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

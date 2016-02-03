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
import org.rf.ide.core.testdata.model.table.setting.LibraryAlias;
import org.rf.ide.core.testdata.model.table.setting.LibraryImport;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;
import org.robotframework.red.graphics.ImagesManager;

import com.google.common.base.Optional;
import com.google.common.collect.Range;

public class LibraryAliasToUpperCaseReplacer extends RedSuiteMarkerResolution {

    public LibraryAliasToUpperCaseReplacer() {
    }

    @Override
    public String getLabel() {
        return "Library alias declaration will be replace by WITH NAME";
    }

    @Override
    public Optional<ICompletionProposal> asContentProposal(final IMarker marker, final IDocument document,
            final RobotSuiteFile suiteModel) {
        Optional<ICompletionProposal> proposal = Optional.absent();

        final Range<Integer> defRange = getRange(marker);
        final Optional<? extends RobotSuiteFileSection> section = suiteModel.findSection(RobotSettingsSection.class);
        if (section.isPresent()) {
            try {
                return createProposal(document, section.get().findElement(defRange.lowerEndpoint()));
            } catch (final BadLocationException e) {
            }
        }

        return proposal;
    }

    private Optional<ICompletionProposal> createProposal(final IDocument document,
            final Optional<? extends RobotElement> optional) throws BadLocationException {
        Optional<ICompletionProposal> proposal = Optional.absent();
        if (optional.isPresent()) {
            final RobotKeywordCall setting = (RobotKeywordCall) optional.get();
            final LibraryImport libImport = (LibraryImport) setting.getLinkedElement();
            final LibraryAlias alias = libImport.getAlias();
            final RobotToken declaration = alias.getDeclaration();
            final String declarationValue = declaration.getRaw();
            final String replacementString = declarationValue.toUpperCase();
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

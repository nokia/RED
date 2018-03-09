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
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.red.graphics.ImagesManager;

import com.google.common.collect.Range;

/**
 * @author Michal Anglart
 *
 */
public class RemoveKeywordFixer extends RedSuiteMarkerResolution {

    private final String keywordName;

    public RemoveKeywordFixer(final String keywordName) {
        this.keywordName = keywordName;
    }

    @Override
    public String getLabel() {
        return "Remove '" + keywordName + "' keyword";
    }

    @Override
    public Optional<ICompletionProposal> asContentProposal(final IMarker marker, final IDocument document,
            final RobotSuiteFile suiteModel) {
        if (keywordName == null) {
            return Optional.empty();
        }
        final Optional<RobotKeywordsSection> section = suiteModel.findSection(RobotKeywordsSection.class);
        if (!section.isPresent()) {
            return Optional.empty();
        }
        for (final RobotKeywordDefinition keyword : section.get().getChildren()) {
            final Range<Integer> defRange = RobotProblem.getRangeOf(marker);
            if (defRange.contains(keyword.getDefinitionPosition().getOffset())) {
                try {
                    return createProposal(document, keyword);
                } catch (final BadLocationException e) {
                    return Optional.empty();
                }
            }
        }
        return Optional.empty();
    }

    private Optional<ICompletionProposal> createProposal(final IDocument document, final RobotKeywordDefinition keyword)
            throws BadLocationException {
        final Position position = keyword.getPosition();
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
        } else {
            shift = document.getLength() - length - offset + 1;
        }

        return Optional.of(new CompletionProposal("", offset, length + shift - 1, offset,
                ImagesManager.getImage(RedImages.getUserKeywordImage()), getLabel(), null, null));
    }
}

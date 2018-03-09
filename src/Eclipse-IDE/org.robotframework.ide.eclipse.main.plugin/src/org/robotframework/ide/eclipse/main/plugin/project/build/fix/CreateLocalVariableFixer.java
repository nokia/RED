/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.fix;

import java.util.Optional;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Image;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.DocumentUtilities;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.RedCompletionBuilder;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.RedCompletionProposal;
import org.robotframework.red.graphics.ImagesManager;

/**
 * @author Michal Anglart
 */
public class CreateLocalVariableFixer extends RedSuiteMarkerResolution {

    private final String name;

    public CreateLocalVariableFixer(final String name) {
        this.name = name;
    }

    @Override
    public String getLabel() {
        return "Define " + name + " as local variable in previous line";
    }

    @Override
    public Optional<ICompletionProposal> asContentProposal(final IMarker marker, final IDocument document,
            final RobotSuiteFile suiteModel) {

        final String lineDelimiter = DocumentUtilities.getDelimiter(document);
        final String cellSeparator = getSeparator(suiteModel);

        String toInsert = name + cellSeparator;
        final int cursorOffset = toInsert.length();
        toInsert += lineDelimiter;

        final Image image = ImagesManager.getImage(RedImages.getRobotVariableImage());
        try {
            final int problemOffset = (int) marker.getAttribute(IMarker.CHAR_START);
            final IRegion lineRegion = document.getLineInformationOfOffset(problemOffset);

            final int firstCharacterOffset = findFirstCharacter(document, lineRegion.getOffset());
            toInsert += document.get(lineRegion.getOffset(), firstCharacterOffset - lineRegion.getOffset());

            final IRegion regionToChange = new Region(firstCharacterOffset, 0);

            final String info = Snippets.createSnippetInfo(document, regionToChange, toInsert);
            final RedCompletionProposal proposal = RedCompletionBuilder.newProposal()
                    .willPut(toInsert)
                    .byInsertingAt(regionToChange.getOffset())
                    .secondaryPopupShouldBeDisplayedUsingHtml(info)
                    .activateAssistantAfterAccepting(true)
                    .thenCursorWillStopAt(cursorOffset)
                    .displayedLabelShouldBe(getLabel())
                    .proposalsShouldHaveIcon(image)
                    .create();

            return Optional.of(proposal);
        } catch (final CoreException | BadLocationException e) {
            return Optional.empty();
        }
    }

    private int findFirstCharacter(final IDocument document, final int offset) throws BadLocationException {
        for (int i = offset; i < document.getLength(); i++) {
            final char character = document.getChar(i);
            if (character != ' ' && character != '\t') {
                return i;
            }
        }
        throw new BadLocationException();
    }

}

/*
 * Copyright 2020 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.fix;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.RedCompletionBuilder;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.RedCompletionProposal;
import org.robotframework.red.graphics.ImagesManager;

/**
 * @author Aleksandra Polus
 */
public class ConvertDeprecatedForLoopFixer extends RedSuiteMarkerResolution {

    private final ImageDescriptor image;

    private final int length;

    public ConvertDeprecatedForLoopFixer(final int regionLength) {
        this(regionLength, RedImages.getChangeImage());
    }

    public ConvertDeprecatedForLoopFixer(final int regionLength, final ImageDescriptor image) {
        this.image = image;
        this.length = regionLength;
    }

    @Override
    public String getLabel() {
        return "Convert to current FOR loop syntax";
    }

    @Override
    public Optional<ICompletionProposal> asContentProposal(final IMarker marker, final IDocument document,
            final RobotSuiteFile suiteModel) {
        try {
            final IRegion regionToChange = createRegionToChange(document, marker);
            final String replacement = convertForLoopToCurrentSyntax(marker, document, suiteModel, regionToChange);

            final String info = Snippets.createSnippetInfoWithoutContext(document, regionToChange, replacement);
            final RedCompletionProposal proposal = RedCompletionBuilder.newProposal()
                    .willPut(replacement)
                    .byReplacingRegion(regionToChange)
                    .secondaryPopupShouldBeDisplayedUsingHtml(info)
                    .thenCursorWillStopAtTheEndOfInsertion()
                    .displayedLabelShouldBe(getLabel())
                    .proposalsShouldHaveIcon(ImagesManager.getImage(image))
                    .create();
            return Optional.of(proposal);
        } catch (final BadLocationException e) {
            return Optional.empty();
        }
    }

    private IRegion createRegionToChange(final IDocument document, final IMarker marker) throws BadLocationException {
        final int markerOffset = marker.getAttribute(IMarker.CHAR_START, -1);
        final int markerLineNumber = marker.getAttribute(IMarker.LINE_NUMBER, -1) - 1;
        final int lineOffset = document.getLineOffset(markerLineNumber);
        final int forLength = length + markerOffset - lineOffset;
        return new Region(lineOffset, forLength);
    }

    private String convertForLoopToCurrentSyntax(final IMarker marker, final IDocument document,
            final RobotSuiteFile suiteModel, final IRegion regionToChange) throws BadLocationException {
        final int markerLineNumber = marker.getAttribute(IMarker.LINE_NUMBER, -1) - 1;
        final int regionNumberOfLines = document.getNumberOfLines(regionToChange.getOffset(),
                regionToChange.getLength());

        final StringBuilder regionBuilder = new StringBuilder();

        for (int i = markerLineNumber; i < markerLineNumber + regionNumberOfLines; i++) {
            final List<IRobotLineElement> lineModelElements = suiteModel.getLinkedElement()
                    .getFileContent()
                    .get(i)
                    .getLineElements();
            int elementIndex = 0;
            for (final IRobotLineElement element : lineModelElements) {
                if (i == markerLineNumber && atFistPosition(elementIndex, element, lineModelElements, ":FOR")) {
                    regionBuilder.append("FOR");
                } else if (i > markerLineNumber && atFistPosition(elementIndex, element, lineModelElements, "\\")) {
                    regionBuilder.append(" ");
                } else {
                    regionBuilder.append(element.getText());
                }
                elementIndex++;
            }
            if (i != markerLineNumber + regionNumberOfLines - 1) {
                regionBuilder.append("\n");
            }
        }
        if (shouldAddEndStatement(marker, document, suiteModel, regionToChange, markerLineNumber,
                regionNumberOfLines)) {
            final int markerOffset = marker.getAttribute(IMarker.CHAR_START, -1);
            final int markerLineOffset = regionToChange.getOffset();
            final int markerIndentation = markerOffset - markerLineOffset;

            regionBuilder.append("\n");
            regionBuilder.append(document.get(markerLineOffset, markerIndentation));
            regionBuilder.append("END");
        }
        return regionBuilder.toString();
    }

    private boolean shouldAddEndStatement(final IMarker marker, final IDocument document,
            final RobotSuiteFile suiteModel, final IRegion regionToChange, final int markerLineNumber,
            final int regionNumberOfLines) {
        if (document.getNumberOfLines() > markerLineNumber + regionNumberOfLines) {
            final RobotLine potentialEndLine = suiteModel.getLinkedElement()
                    .getFileContent()
                    .get(markerLineNumber + regionNumberOfLines);
            final String joinedPotentialEndLine = potentialEndLine.elementsStream()
                    .map(IRobotLineElement::getText)
                    .collect(Collectors.joining(""));
            if (joinedPotentialEndLine.trim().equals("END")) {
                return false;
            }
        }
        return true;
    }

    private boolean atFistPosition(final int elementIndex, final IRobotLineElement element,
            final List<IRobotLineElement> lineModelElements, final String toVerify) {
        final String joinedToElementLine = lineModelElements.stream()
                .limit(elementIndex + 1)
                .map(IRobotLineElement::getText)
                .collect(Collectors.joining(""));
        if (toVerify.equals(":FOR")) {
            return element.getText().replaceAll("\\s", "").toUpperCase().equals(toVerify)
                    && joinedToElementLine.trim().replaceAll("\\s", "").toUpperCase().equals(toVerify);
        } else {
            return element.getText().equals(toVerify)
                    && joinedToElementLine.trim().replaceAll("\\s", "").toUpperCase().equals(toVerify);
        }
    }
}

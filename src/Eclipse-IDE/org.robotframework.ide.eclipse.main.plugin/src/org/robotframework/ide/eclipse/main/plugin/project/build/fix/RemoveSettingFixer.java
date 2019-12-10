/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.fix;

import java.util.List;
import java.util.Optional;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.RedCompletionBuilder;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.RedCompletionProposal;
import org.robotframework.red.graphics.ImagesManager;

import com.google.common.collect.Range;

public class RemoveSettingFixer extends RedSuiteMarkerResolution {

    // Removes a setting - general or local:

    @Override
    public String getLabel() {
        return "Remove setting";
    }

    @Override
    public Optional<ICompletionProposal> asContentProposal(final IMarker marker, final IDocument document,
            final RobotSuiteFile suiteModel) {
        final Range<Integer> range = RobotProblem.getRangeOf(marker);
        final Optional<? extends RobotElement> element = suiteModel.findElement(range.lowerEndpoint());
        return element.filter(RobotFileInternalElement.class::isInstance)
                .map(RobotFileInternalElement.class::cast)
                .map(RobotFileInternalElement::getLinkedElement)
                .filter(AModelElement.class::isInstance)
                .map(AModelElement.class::cast)
                .filter(elem -> ModelType.getSettings().contains(elem.getModelType())
                        || ModelType.getTestCaseSettings().contains(elem.getModelType())
                        || ModelType.getTaskSettings().contains(elem.getModelType())
                        || ModelType.getKeywordSettings().contains(elem.getModelType()))
                .map(elem -> createProposal(document, elem));
    }

    private RedCompletionProposal createProposal(final IDocument document, final AModelElement<?> linkedSetting) {
        final List<RobotToken> tokens = linkedSetting.getElementTokens();
        final int startLine = tokens.get(0).getLineNumber() - 1;
        final int endLine = tokens.get(tokens.size() - 1).getLineNumber();

        try {
            final int startOffset = document.getLineInformation(startLine).getOffset();
            final int endOffset = document.getLineInformation(endLine).getOffset();

            final IRegion toRemove = new Region(startOffset, endOffset - startOffset);

            final String info = Snippets.createSnippetInfo(document, toRemove, "");
            return RedCompletionBuilder.newProposal()
                    .willRemove()
                    .theRegion(toRemove)
                    .secondaryPopupShouldBeDisplayedUsingHtml(info)
                    .thenCursorWillStopAtTheEndOfInsertion()
                    .displayedLabelShouldBe(getLabel())
                    .proposalsShouldHaveIcon(ImagesManager.getImage(RedImages.getRobotSettingImage()))
                    .create();

        } catch (final BadLocationException e) {
            return null;
        }
    }
}

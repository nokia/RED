/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.fix;

import java.util.List;
import java.util.Optional;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.RedCompletionBuilder;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.RedCompletionProposal;
import org.robotframework.red.graphics.ImagesManager;

import com.google.common.collect.Range;

public class RemoveSettingValuesExceptFirstFixer extends RedSuiteMarkerResolution {

    private final String label;

    // Removes unexpected values from setting:
    // *** Settings ***
    // Test Timeout    2 min    my custom    message
    // ...  which continues    even more    # and have some comment    here
    //
    // ->
    // *** Settings ***
    // Test Timeout    2 min    # and have some comment    here

    public RemoveSettingValuesExceptFirstFixer(final String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
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
                .map(elem -> createProposal(document, suiteModel, elem));
    }

    private RedCompletionProposal createProposal(final IDocument document, final RobotSuiteFile suiteModel,
            final AModelElement<?> linkedSetting) {

        final List<RobotToken> tokens = linkedSetting.getElementTokens();
        final RobotToken declaration = tokens.get(0);
        final RobotToken firstValue = tokens.get(1);

        final int offset = declaration.getStartOffset();
        final IRegion toChange = calculateRegion(tokens, offset);
        final String cellSeparator = getSeparator(suiteModel, offset);

        final String correctedTimeout = declaration.getText() + cellSeparator + firstValue.getText();

        final String info = Snippets.createSnippetInfo(document, toChange, correctedTimeout);
        return RedCompletionBuilder.newProposal()
                .willPut(correctedTimeout)
                .byReplacingRegion(toChange)
                .secondaryPopupShouldBeDisplayedUsingHtml(info)
                .thenCursorWillStopAtTheEndOfInsertion()
                .displayedLabelShouldBe(getLabel())
                .proposalsShouldHaveIcon(ImagesManager.getImage(RedImages.getRobotSettingImage()))
                .create();
    }

    private IRegion calculateRegion(final List<RobotToken> tokens, final int offset) {
        final int lastTokenIndex = tokens.stream()
                .filter(token -> token.getTypes().contains(RobotTokenType.START_HASH_COMMENT))
                .findFirst()
                .map(tokens::indexOf)
                .orElseGet(() -> tokens.size())
                .intValue() - 1;
        return new Region(offset, tokens.get(lastTokenIndex).getEndOffset() - offset);
    }
}

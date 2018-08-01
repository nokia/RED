/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.fix;

import java.util.List;
import java.util.Optional;

import javax.swing.text.BadLocationException;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.table.setting.TestTimeout;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.RedCompletionBuilder;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.RedCompletionProposal;
import org.robotframework.red.graphics.ImagesManager;

import com.google.common.collect.Range;

public class RemoveTimeoutMessageFixer extends RedSuiteMarkerResolution {

    // Removes Test Timeout message:
    // *** Settings ***
    // Test Timeout    2 min    my custom    message
    //
    // ->
    // *** Settings ***
    // Metadata key val

    @Override
    public String getLabel() {
        return "Remove Test Timeout message";
    }

    @Override
    public Optional<ICompletionProposal> asContentProposal(final IMarker marker, final IDocument document,
            final RobotSuiteFile suiteModel) {
        final Optional<RobotSettingsSection> section = suiteModel.findSection(RobotSettingsSection.class);
        if (section.isPresent()) {
            final Range<Integer> range = RobotProblem.getRangeOf(marker);
            final Optional<RobotSetting> setting = section.get()
                    .findElement(range.lowerEndpoint())
                    .filter(RobotSetting.class::isInstance)
                    .map(RobotSetting.class::cast)
                    .filter(s -> s.getLinkedElement().getModelType() == ModelType.SUITE_TEST_TIMEOUT);

            if (setting.isPresent()) {
                try {
                    return createProposal(document, suiteModel, setting.get());
                } catch (final BadLocationException e) {
                    return Optional.empty();
                }
            }
        }
        return Optional.empty();
    }

    private Optional<ICompletionProposal> createProposal(final IDocument document, final RobotSuiteFile suiteModel,
            final RobotSetting timeoutSetting) throws BadLocationException {

        final TestTimeout timeout = (TestTimeout) timeoutSetting.getLinkedElement();
        final RobotToken declaration = timeout.getDeclaration();
        final int offset = declaration.getStartOffset();

        final List<RobotToken> elements = timeout.getMessageArguments();
        final IRegion toChange = new Region(offset, elements.get(elements.size() - 1).getEndOffset() - offset);
        final String cellSeparator = getSeparator(suiteModel, offset);

        final String correctedTimeout = declaration.getText() + cellSeparator + timeout.getTimeout().getText();

        final String info = Snippets.createSnippetInfo(document, toChange, correctedTimeout);
        final RedCompletionProposal proposal = RedCompletionBuilder.newProposal()
                .willPut(correctedTimeout)
                .byReplacingRegion(toChange)
                .secondaryPopupShouldBeDisplayedUsingHtml(info)
                .thenCursorWillStopAtTheEndOfInsertion()
                .displayedLabelShouldBe(getLabel())
                .proposalsShouldHaveIcon(ImagesManager.getImage(RedImages.getRobotSettingImage()))
                .create();
        return Optional.of(proposal);
    }
}

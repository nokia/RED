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
import org.rf.ide.core.testdata.model.table.setting.Metadata;
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

public class MetadataKeyInSameColumnFixer extends RedSuiteMarkerResolution {

    // Splits old syntax:
    // *** Settings ***
    // Meta: key val
    //
    // into :
    // *** Settings ***
    // Metadata key val

    @Override
    public String getLabel() {
        return "Split Metadata key from setting declaration";
    }

    @Override
    public Optional<ICompletionProposal> asContentProposal(final IMarker marker, final IDocument document,
            final RobotSuiteFile suiteModel) {
        final Optional<RobotSettingsSection> section = suiteModel.findSection(RobotSettingsSection.class);
        if (section.isPresent()) {
            final Range<Integer> range = RobotProblem.getRangeOf(marker);
            final List<RobotSetting> metadataSettings = section.get().getMetadataSettings();
            for (final RobotSetting metadataSetting : metadataSettings) {
                if (range.contains(metadataSetting.getDefinitionPosition().getOffset())) {
                    try {
                        return createProposal(document, suiteModel, metadataSetting);
                    } catch (final BadLocationException e) {
                        return Optional.empty();
                    }
                }
            }
        }
        return Optional.empty();
    }

    private Optional<ICompletionProposal> createProposal(final IDocument document, final RobotSuiteFile suiteModel,
            final RobotSetting metadataSetting) throws BadLocationException {

        final Metadata metadata = (Metadata) metadataSetting.getLinkedElement();
        final RobotToken declaration = metadata.getDeclaration();
        final int offset = declaration.getStartOffset();

        final IRegion toChange = new Region(offset, metadata.getKey().getEndOffset() - offset);
        final String cellSeparator = getSeparator(suiteModel, offset);

        final String correctedMetadata = "Metadata" + cellSeparator + metadata.getKey().getText();

        final String info = Snippets.createSnippetInfo(document, toChange, correctedMetadata);
        final RedCompletionProposal proposal = RedCompletionBuilder.newProposal()
                .willPut(correctedMetadata)
                .byReplacingRegion(toChange)
                .secondaryPopupShouldBeDisplayedUsingHtml(info)
                .thenCursorWillStopAtTheEndOfInsertion()
                .displayedLabelShouldBe(getLabel())
                .proposalsShouldHaveIcon(ImagesManager.getImage(RedImages.getRobotSettingImage()))
                .create();
        return Optional.of(proposal);
    }
}

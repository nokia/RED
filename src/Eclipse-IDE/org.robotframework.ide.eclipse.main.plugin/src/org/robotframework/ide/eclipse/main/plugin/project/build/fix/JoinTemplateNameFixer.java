/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.fix;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.swing.text.BadLocationException;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.table.setting.TestTemplate;
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

public class JoinTemplateNameFixer extends RedSuiteMarkerResolution {

    // Joins Test Template keyword written in multiple cells:
    // *** Settings ***
    // Test Template    key    word
    //
    // ->
    // *** Settings ***
    // Test Template    key word

    @Override
    public String getLabel() {
        return "Merge name into single cell";
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
                    .filter(s -> s.getLinkedElement().getModelType() == ModelType.SUITE_TEST_TEMPLATE);

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
            final RobotSetting templateSetting) throws BadLocationException {

        final TestTemplate template = (TestTemplate) templateSetting.getLinkedElement();
        final RobotToken declaration = template.getDeclaration();
        final int offset = declaration.getStartOffset();

        final List<RobotToken> elements = template.getUnexpectedTrashArguments();
        final IRegion toChange = new Region(offset, elements.get(elements.size() - 1).getEndOffset() - offset);
        final String cellSeparator = getSeparator(suiteModel, offset);

        final List<String> nameParts = new ArrayList<>();
        nameParts.add(template.getKeywordName().getText());
        template.getUnexpectedTrashArguments().stream().map(RobotToken::getText).forEach(nameParts::add);
        final String joinedName = String.join(" ", nameParts);

        final String correctedTemplate = declaration.getText() + cellSeparator + joinedName;

        final String info = Snippets.createSnippetInfo(document, toChange, correctedTemplate);
        final RedCompletionProposal proposal = RedCompletionBuilder.newProposal()
                .willPut(correctedTemplate)
                .byReplacingRegion(toChange)
                .secondaryPopupShouldBeDisplayedUsingHtml(info)
                .thenCursorWillStopAtTheEndOfInsertion()
                .displayedLabelShouldBe(getLabel())
                .proposalsShouldHaveIcon(ImagesManager.getImage(RedImages.getRobotSettingImage()))
                .create();
        return Optional.of(proposal);
    }
}

/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.fix;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.RedCompletionBuilder;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.RedCompletionProposal;
import org.robotframework.red.graphics.ImagesManager;

import com.google.common.base.Optional;


/**
 * @author Michal Anglart
 *
 */
public class ChangeToFixer extends RedSuiteMarkerResolution {

    public static Collection<ChangeToFixer> createFixers(final IRegion toChange,
            final Collection<String> replacements) {
        final List<ChangeToFixer> fixers = new ArrayList<>();
        for (final String replacement : replacements) {
            fixers.add(new ChangeToFixer(toChange, replacement));
        }
        return fixers;
    }

    private final IRegion toChange;

    private final String replacement;

    public ChangeToFixer(final IRegion toChange, final String replacement) {
        this.toChange = toChange;
        this.replacement = replacement;
    }

    @Override
    public String getLabel() {
        return "Change to '" + replacement + "'";
    }

    @Override
    public Optional<ICompletionProposal> asContentProposal(final IMarker marker, final IDocument document,
            final RobotSuiteFile suiteModel) {
        final String info = Snippets.createSnippetInfo(document, toChange, replacement);
        final RedCompletionProposal proposal = RedCompletionBuilder.newProposal()
                .willPut(replacement)
                .byReplacingRegion(toChange)
                .secondaryPopupShouldBeDisplayedUsingHtml(info)
                .thenCursorWillStopAtTheEndOfInsertion()
                .displayedLabelShouldBe(getLabel())
                .proposalsShouldHaveIcon(ImagesManager.getImage(RedImages.getChangeImage()))
                .create();
        return Optional.<ICompletionProposal> of(proposal);
    }

}

/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.fix;

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.Optional;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.RedCompletionBuilder;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.RedCompletionProposal;
import org.robotframework.red.graphics.ImagesManager;

/**
 * @author Michal Anglart
 */
public class ChangeToFixer extends RedSuiteMarkerResolution {

    public static Collection<ChangeToFixer> createFixers(final Collection<String> replacements) {
        return replacements.stream().map(ChangeToFixer::new).collect(toList());
    }

    private final IRegion toChange;

    private final String replacement;

    private final ImageDescriptor image;

    public ChangeToFixer(final String replacement) {
        this(null, replacement, RedImages.getChangeImage());
    }

    public ChangeToFixer(final IRegion toChange, final String replacement) {
        this(toChange, replacement, RedImages.getChangeImage());
    }

    public ChangeToFixer(final IRegion toChange, final String replacement, final ImageDescriptor image) {
        this.toChange = toChange;
        this.replacement = replacement;
        this.image = image;
    }

    @Override
    public String getLabel() {
        return "Change to '" + replacement + "'";
    }

    @Override
    public Optional<ICompletionProposal> asContentProposal(final IMarker marker, final IDocument document,
            final RobotSuiteFile suiteModel) {
        final IRegion regionToChange = toChange == null ? RobotProblem.getRegionOf(marker) : toChange;

        final String info = Snippets.createSnippetInfo(document, regionToChange, replacement);
        final RedCompletionProposal proposal = RedCompletionBuilder.newProposal()
                .willPut(replacement)
                .byReplacingRegion(regionToChange)
                .secondaryPopupShouldBeDisplayedUsingHtml(info)
                .thenCursorWillStopAtTheEndOfInsertion()
                .displayedLabelShouldBe(getLabel())
                .proposalsShouldHaveIcon(ImagesManager.getImage(image))
                .create();
        return Optional.of(proposal);
    }
}

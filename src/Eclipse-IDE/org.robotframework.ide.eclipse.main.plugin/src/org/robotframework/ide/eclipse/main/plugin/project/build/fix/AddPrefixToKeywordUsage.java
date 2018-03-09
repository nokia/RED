/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.fix;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.ui.IMarkerResolution;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.red.graphics.ImagesManager;

/**
 * @author Michal Anglart
 */
public class AddPrefixToKeywordUsage extends RedSuiteMarkerResolution {

    public static Collection<? extends IMarkerResolution> createFixers(final String name, final List<String> prefixes) {
        final List<RedSuiteMarkerResolution> fixers = newArrayList();
        for (final String prefix : prefixes) {
            fixers.add(new AddPrefixToKeywordUsage(name, prefix));
        }
        return fixers;
    }

    private final String name;

    private final String prefix;

    public AddPrefixToKeywordUsage(final String name, final String prefix) {
        this.name = name;
        this.prefix = prefix;
    }

    @Override
    public String getLabel() {
        return "Add '" + prefix + "' prefix to keyword call";
    }

    @Override
    public Optional<ICompletionProposal> asContentProposal(final IMarker marker, final IDocument document,
            final RobotSuiteFile suiteModel) {

        final String toInsert = prefix + "." + name;

        try {
            final int charStart = (int) marker.getAttribute(IMarker.CHAR_START);
            final int charEnd = (int) marker.getAttribute(IMarker.CHAR_END);
            final IRegion regionToChange = new Region(charStart, charEnd - charStart);

            return Optional.of(new CompletionProposal(toInsert, charStart, charEnd - charStart, toInsert.length(),
                    ImagesManager.getImage(RedImages.getChangeImage()), getLabel(), null,
                    Snippets.createSnippetInfo(document, regionToChange, toInsert)));

        } catch (final CoreException e) {
            return Optional.empty();
        }
    }
}

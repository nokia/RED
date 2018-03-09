/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.search.participants;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.rf.ide.core.libraries.KeywordSpecification;
import org.rf.ide.core.libraries.LibrarySpecification;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.search.KeywordDocumentationMatch;
import org.robotframework.ide.eclipse.main.plugin.search.LibraryDocumentationMatch;
import org.robotframework.ide.eclipse.main.plugin.search.SearchPattern;
import org.robotframework.ide.eclipse.main.plugin.search.SearchResult;

/**
 * @author Michal Anglart
 *
 */
public class DocumentationSearch extends TargetedSearch {

    public DocumentationSearch(final SearchPattern searchPattern, final RobotModel model, final SearchResult result) {
        super(searchPattern, model, result);
    }

    @Override
    protected void locateMatchesInLibrarySpecification(final IProject project,
            final LibrarySpecification librarySpecification) {
        final Pattern pattern = searchPattern.buildPattern();

        final Matcher matcher = pattern.matcher(librarySpecification.getDocumentation());
        while (matcher.find()) {
            result.addMatch(new LibraryDocumentationMatch(project, librarySpecification, matcher.start(),
                    matcher.end() - matcher.start()));
        }
    }

    @Override
    protected void locateMatchesInKeywordSpecification(final IProject project,
            final LibrarySpecification librarySpecification, final KeywordSpecification keywordSpecification) {
        final Pattern pattern = searchPattern.buildPattern();

        final Matcher matcher = pattern.matcher(keywordSpecification.getDocumentation());
        while (matcher.find()) {
            result.addMatch(new KeywordDocumentationMatch(project, librarySpecification, keywordSpecification,
                    matcher.start(), matcher.end() - matcher.start()));
        }
    }

    @Override
    protected void locateMatchesInRobotFile(final RobotSuiteFile robotSuiteFile) {
        // final Optional<RobotKeywordsSection> kwSection =
        // robotSuiteFile.findSection(RobotKeywordsSection.class);
        // if (kwSection.isPresent()) {
        // for (final RobotKeywordDefinition keywordDefinition : kwSection.get().getChildren()) {
        // final RobotDefinitionSetting docSetting = keywordDefinition.getDocumentationSetting();
        // if (docSetting != null) {
        // result.addMatch(new FileMatch(robotSuiteFile.getFile()));
        // }
        // }
        // }
    }
}

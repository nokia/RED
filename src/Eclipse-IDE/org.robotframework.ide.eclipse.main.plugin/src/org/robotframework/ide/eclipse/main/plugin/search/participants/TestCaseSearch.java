/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.search.participants;

import org.eclipse.core.resources.IProject;
import org.rf.ide.core.libraries.KeywordSpecification;
import org.rf.ide.core.libraries.LibrarySpecification;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.search.SearchPattern;
import org.robotframework.ide.eclipse.main.plugin.search.SearchResult;

/**
 * @author Michal Anglart
 *
 */
public class TestCaseSearch extends TargetedSearch {

    public TestCaseSearch(final SearchPattern searchPattern, final RobotModel model, final SearchResult result) {
        super(searchPattern, model, result);
    }

    @Override
    protected void locateMatchesInLibrarySpecification(final IProject project,
            final LibrarySpecification librarySpecification) {
        // TODO : implement
    }

    @Override
    protected void locateMatchesInKeywordSpecification(final IProject project,
            final LibrarySpecification librarySpecification, final KeywordSpecification keywordSpecification) {
        // TODO : implement
    }

    @Override
    protected void locateMatchesInRobotFile(final RobotSuiteFile robotSuiteFile) {
        // TODO : implement
    }
}

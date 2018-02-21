/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.search.participants;

import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.rf.ide.core.libraries.KeywordSpecification;
import org.rf.ide.core.libraries.LibrarySpecification;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.search.SearchPattern;
import org.robotframework.ide.eclipse.main.plugin.search.SearchResult;

import com.google.common.collect.Multimap;

/**
 * @author Michal Anglart
 *
 */
public abstract class TargetedSearch {

    private final RobotModel model;

    protected final SearchResult result;

    protected final SearchPattern searchPattern;

    TargetedSearch(final SearchPattern searchPattern, final RobotModel model, final SearchResult result) {
        this.searchPattern = searchPattern;
        this.model = model;
        this.result = result;
    }

    public final void run(final IProgressMonitor monitor, final Multimap<IProject, LibrarySpecification> libraries,
            final Set<IFile> files) throws OperationCanceledException {

        monitor.beginTask("Searching for '" + searchPattern.getPattern() + "'",
                libraries.values().size() + files.size());

        for (final IProject project : libraries.keySet()) {
            for (final LibrarySpecification librarySpecification : libraries.get(project)) {
                if (monitor.isCanceled()) {
                    throw new OperationCanceledException();
                }
                monitor.subTask("locating matches in " + librarySpecification.getName() + " library used by '"
                        + project.getName() + "' project");

                locateMatchesInLibrarySpecification(project, librarySpecification);

                for (final KeywordSpecification keywordSpecification : librarySpecification.getKeywords()) {
                    locateMatchesInKeywordSpecification(project, librarySpecification, keywordSpecification);
                }
                monitor.worked(1);
    
            }
        }

        for (final IFile file : files) {
            if (monitor.isCanceled()) {
                throw new OperationCanceledException();
            }
            locateMatchesInRobotFile(model.createSuiteFile(file));
            monitor.worked(1);
        }
    }

    protected abstract void locateMatchesInLibrarySpecification(final IProject project,
            final LibrarySpecification librarySpecification);

    protected abstract void locateMatchesInKeywordSpecification(final IProject project,
            final LibrarySpecification librarySpecification, final KeywordSpecification keywordSpecification);

    protected abstract void locateMatchesInRobotFile(final RobotSuiteFile robotSuiteFile);
}

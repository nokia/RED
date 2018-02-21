/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.search.participants;

import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.rf.ide.core.libraries.KeywordSpecification;
import org.rf.ide.core.libraries.LibrarySpecification;
import org.robotframework.ide.eclipse.main.plugin.mockeclipse.ProgressMonitorMock;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.search.SearchPattern;
import org.robotframework.ide.eclipse.main.plugin.search.SearchResult;
import org.robotframework.red.junit.ProjectProvider;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

public class TargetedSearchTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(TargetedSearchTest.class);

    @BeforeClass
    public static void beforeSuite() throws Exception {
        projectProvider.createFile(new Path("file1.robot"), "line");
        projectProvider.createFile(new Path("file2.robot"), "line");
        projectProvider.createFile(new Path("file3.robot"), "line");
    }

    @Test
    public void whenTargetSearchIsRunWithFiles_properMethodForLocatingMatchesIsCalled() {
        final ProgressMonitorMock monitor = new ProgressMonitorMock();

        final Set<IFile> files = newHashSet(projectProvider.getFile("file1.robot"),
                projectProvider.getFile("file2.robot"), projectProvider.getFile("file3.robot"));

        final TargetedSearch targetedSearch = createTargetedSearch(new SearchPattern("doc"), new SearchResult(null));
        targetedSearch.run(monitor, LinkedHashMultimap.<IProject, LibrarySpecification> create(), files);

        assertThat(monitor.getTotalWorkToBeDone()).isEqualTo(3);
        assertThat(monitor.getWorkDone()).isEqualTo(3);

        verify(targetedSearch, never()).locateMatchesInLibrarySpecification(any(IProject.class),
                any(LibrarySpecification.class));
        verify(targetedSearch, never()).locateMatchesInKeywordSpecification(any(IProject.class),
                any(LibrarySpecification.class), any(KeywordSpecification.class));
        verify(targetedSearch, times(3)).locateMatchesInRobotFile(any(RobotSuiteFile.class));
    }

    @Test(expected = OperationCanceledException.class)
    public void whenTargetSearchWasCancelledWhenRunWithFiles_searchIsNotContinued() {
        final ProgressMonitorMock monitor = new ProgressMonitorMock();
        monitor.performWhenTaskBegins(new Runnable() {
            @Override
            public void run() {
                monitor.setCanceled(true);
            }
        });

        final Set<IFile> files = newHashSet(projectProvider.getFile("file1.robot"),
                projectProvider.getFile("file2.robot"), projectProvider.getFile("file3.robot"));

        final TargetedSearch targetedSearch = createTargetedSearch(new SearchPattern("doc"), new SearchResult(null));
        targetedSearch.run(monitor, LinkedHashMultimap.<IProject, LibrarySpecification> create(), files);
    }

    @Test
    public void whenTargetSearchIsRunWithLibraries_properMethodForLocatingMatchesIsCalled() {
        final SearchPattern pattern = new SearchPattern("doc");
        final SearchResult result = new SearchResult(null);

        final TargetedSearch targetedSearch = createTargetedSearch(pattern, result);

        final ProgressMonitorMock monitor = new ProgressMonitorMock();

        final LibrarySpecification lib1 = new LibrarySpecification();
        final LibrarySpecification lib2 = new LibrarySpecification();
        lib2.getKeywords().add(new KeywordSpecification());
        lib2.getKeywords().add(new KeywordSpecification());
        final Multimap<IProject, LibrarySpecification> libraries = LinkedHashMultimap.create();
        libraries.put(projectProvider.getProject(), lib1);
        libraries.put(projectProvider.getProject(), lib2);
        final Set<IFile> files = new HashSet<>();

        targetedSearch.run(monitor, libraries, files);

        assertThat(monitor.getTotalWorkToBeDone()).isEqualTo(2);
        assertThat(monitor.getWorkDone()).isEqualTo(2);

        verify(targetedSearch, times(2)).locateMatchesInLibrarySpecification(any(IProject.class),
                any(LibrarySpecification.class));
        verify(targetedSearch, times(2)).locateMatchesInKeywordSpecification(any(IProject.class),
                any(LibrarySpecification.class), any(KeywordSpecification.class));
        verify(targetedSearch, never()).locateMatchesInRobotFile(any(RobotSuiteFile.class));
    }

    @Test(expected = OperationCanceledException.class)
    public void whenTargetSearchWasCancelledWhenRunWithLibraries_searchIsNotContinued() {
        final ProgressMonitorMock monitor = new ProgressMonitorMock();
        monitor.performWhenTaskBegins(new Runnable() {
            @Override
            public void run() {
                monitor.setCanceled(true);
            }
        });

        final Multimap<IProject, LibrarySpecification> libraries = LinkedHashMultimap.create();
        libraries.put(projectProvider.getProject(), new LibrarySpecification());

        final TargetedSearch targetedSearch = createTargetedSearch(new SearchPattern("doc"), new SearchResult(null));
        targetedSearch.run(monitor, libraries, new HashSet<IFile>());
    }

    private static TargetedSearch createTargetedSearch(final SearchPattern pattern, final SearchResult result) {
        return spy(new TargetedSearchSpy(pattern, new RobotModel(), result));
    }

    public static class TargetedSearchSpy extends TargetedSearch {

        private TargetedSearchSpy(final SearchPattern searchPattern, final RobotModel model,
                final SearchResult result) {
            super(searchPattern, model, result);
        }

        @Override
        protected void locateMatchesInRobotFile(final RobotSuiteFile robotSuiteFile) {
            // nothing to do
        }

        @Override
        protected void locateMatchesInLibrarySpecification(final IProject project,
                final LibrarySpecification librarySpecification) {
            // nothing to do
        }

        @Override
        protected void locateMatchesInKeywordSpecification(final IProject project,
                final LibrarySpecification librarySpecification, final KeywordSpecification keywordSpecification) {
            // nothing to do
        }
    }
}

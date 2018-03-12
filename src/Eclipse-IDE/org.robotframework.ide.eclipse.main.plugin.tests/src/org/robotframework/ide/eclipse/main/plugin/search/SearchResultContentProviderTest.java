/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.search;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.text.Match;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.rf.ide.core.libraries.KeywordSpecification;
import org.rf.ide.core.libraries.LibraryDescriptor;
import org.rf.ide.core.libraries.LibrarySpecification;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.search.SearchResultContentProvider.KeywordWithParent;
import org.robotframework.ide.eclipse.main.plugin.search.SearchResultContentProvider.LibraryWithParent;
import org.robotframework.ide.eclipse.main.plugin.search.SearchResultContentProvider.Libs;
import org.robotframework.red.junit.ProjectProvider;

public class SearchResultContentProviderTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(SearchResultContentProviderTest.class);

    private static RobotModel model = new RobotModel();

    @BeforeClass
    public static void beforeSuite() throws Exception {
        projectProvider.createDir("a");
        projectProvider.createFile("a/file.txt");
        projectProvider.createDir("a/b");
        projectProvider.createDir("a/c");
        projectProvider.createFile("a/b/file.txt");
        projectProvider.createDir("x");
    }

    @Before
    public void beforeTest() {
        final RobotProject robotProject = model.createRobotProject(projectProvider.getProject());
        robotProject.setStandardLibraries(new HashMap<>());
        robotProject.setReferencedLibraries(new HashMap<>());
    }

    @AfterClass
    public static void afterSuite() {
        model = null;
    }

    @Test
    public void theRootElementsAreProjectsTakenFromSearchResult() {
        final IProject project1 = mock(IProject.class);
        final IProject project2 = mock(IProject.class);
        final IProject project3 = mock(IProject.class);
        final IProject project4 = mock(IProject.class);

        final Match projectMatch = mock(Match.class);
        when(projectMatch.getElement()).thenReturn(project3);

        final IFile file = mock(IFile.class);
        when(file.getProject()).thenReturn(project4);
        final Match fileMatch = mock(Match.class);
        when(fileMatch.getElement()).thenReturn(file);

        final SearchResult result = new SearchResult(null);
        result.addMatch(new KeywordDocumentationMatch(project1, null, null, 100, 5));
        result.addMatch(new LibraryDocumentationMatch(project2, null, 100, 5));
        result.addMatch(projectMatch);
        result.addMatch(fileMatch);

        final SearchResultContentProvider provider = new SearchResultContentProvider();

        assertThat(provider.getElements(result)).containsOnly(project1, project2, project3, project4);
    }

    @Test
    public void thereAreNoRootElements_ifThereAreNoMatches() {
        final SearchQuery query = new SearchQuery(new SearchSettings(), model);
        final ISearchResult result = query.getSearchResult();

        final SearchResultContentProvider provider = new SearchResultContentProvider();
        provider.inputChanged(null, null, result);

        assertThat(provider.getElements(result)).isEmpty();
    }

    @Test
    public void thereIsARootElement_whenThereIsAMatchInFile() {
        final SearchResult result = createResult();
        result.addMatch(matchForFile("a/b/file.txt"));

        final SearchResultContentProvider provider = new SearchResultContentProvider();
        provider.inputChanged(null, null, result);

        assertThat(provider.getElements(result)).containsExactly(projectProvider.getProject());
    }

    @Test
    public void projectChildrenAreOnlyFilesOrDirectoriesContainingMatches() {
        final SearchResult result = createResult();
        result.addMatch(matchForFile("a/b/file.txt"));

        final SearchResultContentProvider provider = new SearchResultContentProvider();
        provider.inputChanged(null, null, result);

        assertThat(provider.hasChildren(projectProvider.getProject())).isTrue();
        assertThat(provider.getChildren(projectProvider.getProject())).containsExactly(projectProvider.getDir("a"));
    }

    @Test
    public void projectChildrenAreLibsElementIfThereIsALibraryDocMatch() {
        final LibrarySpecification libSpec = LibrarySpecification.create("StdLib");

        final IProject project = projectProvider.getProject();
        final RobotProject robotProject = model.createRobotProject(project);
        final Map<LibraryDescriptor, LibrarySpecification> libs = new HashMap<>();
        libs.put(LibraryDescriptor.ofStandardLibrary("StdLib"), libSpec);
        libs.put(LibraryDescriptor.ofStandardLibrary("OtherStdLib"), LibrarySpecification.create("OtherStdLib"));
        robotProject.setStandardLibraries(libs);

        final SearchResult result = createResult();
        result.addMatch(new LibraryDocumentationMatch(project, libSpec, 100, 5));

        final SearchResultContentProvider provider = new SearchResultContentProvider();
        provider.inputChanged(null, null, result);

        assertThat(provider.hasChildren(projectProvider.getProject())).isTrue();
        final Object[] children = provider.getChildren(projectProvider.getProject());
        assertThat(children).hasSize(1);
        assertThat(children[0]).isInstanceOf(Libs.class);
    }

    @Test
    public void projectChildrenAreLibsElementIfThereIsAKeywordDocMatch() {
        final KeywordSpecification kwSpec = KeywordSpecification.create("kw1");
        final LibrarySpecification libSpec = LibrarySpecification.create("StdLib", kwSpec,
                KeywordSpecification.create("kw2"));

        final IProject project = projectProvider.getProject();
        final RobotProject robotProject = model.createRobotProject(project);
        robotProject.setStandardLibraries(stdLibsMap(libSpec));

        final SearchResult result = createResult();
        result.addMatch(new KeywordDocumentationMatch(project, libSpec, kwSpec, 100, 5));

        final SearchResultContentProvider provider = new SearchResultContentProvider();
        provider.inputChanged(null, null, result);

        assertThat(provider.hasChildren(projectProvider.getProject())).isTrue();
        final Object[] children = provider.getChildren(projectProvider.getProject());
        assertThat(children).hasSize(1);
        assertThat(children[0]).isInstanceOf(Libs.class);
    }

    @Test
    public void directoryChildrenAreOnlyFilesOrDirectoriesContainingMatches_1() {
        final SearchResult result = createResult();
        result.addMatch(matchForFile("a/b/file.txt"));

        final SearchResultContentProvider provider = new SearchResultContentProvider();
        provider.inputChanged(null, null, result);

        assertThat(provider.hasChildren(projectProvider.getDir("a"))).isTrue();
        assertThat(provider.getChildren(projectProvider.getDir("a"))).containsExactly(projectProvider.getDir("a/b"));
    }

    @Test
    public void directoryChildrenAreOnlyFilesOrDirectoriesContainingMatches_2() {
        final SearchResult result = createResult();
        result.addMatch(matchForFile("a/b/file.txt"));

        final SearchResultContentProvider provider = new SearchResultContentProvider();
        provider.inputChanged(null, null, result);

        assertThat(provider.hasChildren(projectProvider.getDir("a/b"))).isTrue();
        assertThat(provider.getChildren(projectProvider.getDir("a/b")))
                .containsExactly(projectProvider.getFile("a/b/file.txt"));
    }

    @Test
    public void fileChildrenAreOnlyMatchesForThisFile() {
        final SearchResult result = createResult();
        final Match match1 = matchForFile("a/b/file.txt");
        final Match match2 = matchForFile("a/b/file.txt");
        final Match match3 = matchForFile("a/file.txt");
        result.addMatch(match1);
        result.addMatch(match2);
        result.addMatch(match3);

        final SearchResultContentProvider provider = new SearchResultContentProvider();
        provider.inputChanged(null, null, result);

        assertThat(provider.hasChildren(projectProvider.getFile("a/b/file.txt"))).isTrue();
        assertThat(provider.getChildren(projectProvider.getFile("a/b/file.txt"))).containsExactly(match1, match2);
    }

    @Test
    public void libsElementChildrenAreOnlyLibrariesWithMatches_1() {
        final LibrarySpecification libSpec = LibrarySpecification.create("StdLib");

        final IProject project = projectProvider.getProject();
        final RobotProject robotProject = model.createRobotProject(project);
        robotProject.setStandardLibraries(stdLibsMap(libSpec, LibrarySpecification.create("OtherStdLib")));

        final SearchResult result = createResult();
        result.addMatch(new LibraryDocumentationMatch(project, libSpec, 100, 5));

        final SearchResultContentProvider provider = new SearchResultContentProvider();
        provider.inputChanged(null, null, result);

        final Libs libs = new Libs(project);
        assertThat(provider.hasChildren(libs)).isTrue();
        final Object[] children = provider.getChildren(libs);
        assertThat(children).hasSize(1);
        assertThat(children[0]).isInstanceOf(LibraryWithParent.class);
        assertThat(((LibraryWithParent) children[0]).getName()).isEqualTo("StdLib");
    }

    @Test
    public void libsElementChildrenAreOnlyLibrariesWithMatches_2() {
        final KeywordSpecification kwSpec = KeywordSpecification.create("kw1");
        final LibrarySpecification libSpec = LibrarySpecification.create("StdLib", kwSpec);

        final IProject project = projectProvider.getProject();
        final RobotProject robotProject = model.createRobotProject(project);
        robotProject.setStandardLibraries(stdLibsMap(libSpec, LibrarySpecification.create("OtherStdLib")));

        final SearchResult result = createResult();
        result.addMatch(new KeywordDocumentationMatch(project, libSpec, kwSpec, 100, 5));

        final SearchResultContentProvider provider = new SearchResultContentProvider();
        provider.inputChanged(null, null, result);

        final Libs libs = new Libs(project);
        assertThat(provider.hasChildren(libs)).isTrue();
        final Object[] children = provider.getChildren(libs);
        assertThat(children).hasSize(1);
        assertThat(children[0]).isInstanceOf(LibraryWithParent.class);
        assertThat(((LibraryWithParent) children[0]).getName()).isEqualTo("StdLib");
    }

    @Test
    public void libraryChildrenAreMatchesInItsDocumentation() {
        final KeywordSpecification kwSpec = KeywordSpecification.create("kw1");
        final LibrarySpecification libSpec = LibrarySpecification.create("StdLib", kwSpec);

        final IProject project = projectProvider.getProject();
        final RobotProject robotProject = model.createRobotProject(project);
        robotProject.setStandardLibraries(stdLibsMap(libSpec));

        final SearchResult result = createResult();
        final LibraryDocumentationMatch docMatch1 = new LibraryDocumentationMatch(project, libSpec,  100, 5);
        final LibraryDocumentationMatch docMatch2 = new LibraryDocumentationMatch(project, libSpec,  200, 5);
        result.addMatch(docMatch1);
        result.addMatch(docMatch2);

        final SearchResultContentProvider provider = new SearchResultContentProvider();
        provider.inputChanged(null, null, result);

        final LibraryWithParent libElement = new LibraryWithParent(new Libs(project), libSpec,
                newArrayList(docMatch1, docMatch2));
        assertThat(provider.hasChildren(libElement)).isTrue();
        final Object[] children = provider.getChildren(libElement);
        assertThat(children).containsExactly(docMatch1, docMatch2);
    }

    @Test
    public void libraryChildrenAreKeywordsContainingMatches() {
        final KeywordSpecification kwSpec = KeywordSpecification.create("kw1");
        final LibrarySpecification libSpec = LibrarySpecification.create("StdLib", kwSpec,
                KeywordSpecification.create("kw2"));

        final IProject project = projectProvider.getProject();
        final RobotProject robotProject = model.createRobotProject(project);
        robotProject.setStandardLibraries(stdLibsMap(libSpec));

        final SearchResult result = createResult();
        result.addMatch(new KeywordDocumentationMatch(project, libSpec, kwSpec, 200, 5));

        final SearchResultContentProvider provider = new SearchResultContentProvider();
        provider.inputChanged(null, null, result);

        final LibraryWithParent libElement = new LibraryWithParent(new Libs(project), libSpec, newArrayList());
        assertThat(provider.hasChildren(libElement)).isTrue();
        final Object[] children = provider.getChildren(libElement);
        assertThat(children).hasSize(1);
        assertThat(children[0]).isInstanceOf(KeywordWithParent.class);
        assertThat(((KeywordWithParent) children[0]).getName()).isEqualTo("kw1");
    }

    @Test
    public void keywordChildrenAreMatchesInItsDocumentation() {
        final KeywordSpecification kwSpec = KeywordSpecification.create("kw1");
        final LibrarySpecification libSpec = LibrarySpecification.create("StdLib", kwSpec,
                KeywordSpecification.create("kw2"));

        final IProject project = projectProvider.getProject();
        final RobotProject robotProject = model.createRobotProject(project);
        robotProject.setStandardLibraries(stdLibsMap(libSpec));

        final SearchResult result = createResult();
        final KeywordDocumentationMatch match1 = new KeywordDocumentationMatch(project, libSpec, kwSpec, 200, 5);
        final KeywordDocumentationMatch match2 = new KeywordDocumentationMatch(project, libSpec, kwSpec, 300, 5);
        result.addMatch(match1);
        result.addMatch(match2);

        final SearchResultContentProvider provider = new SearchResultContentProvider();
        provider.inputChanged(null, null, result);

        final LibraryWithParent libElement = new LibraryWithParent(new Libs(project), libSpec, newArrayList());
        final KeywordWithParent kwElement = new KeywordWithParent(libElement, kwSpec, newArrayList(match1, match2));
        assertThat(provider.hasChildren(kwElement)).isTrue();
        final Object[] children = provider.getChildren(kwElement);
        assertThat(children).containsExactly(match1, match2);
    }

    @Test
    public void parentTest() {
        final IProject project = projectProvider.getProject();
        final Libs libs = new Libs(project);
        final LibraryWithParent libElement = new LibraryWithParent(libs, new LibrarySpecification(), newArrayList());
        final KeywordWithParent kwElement = new KeywordWithParent(libElement, new KeywordSpecification(),
                newArrayList());

        final SearchResultContentProvider provider = new SearchResultContentProvider();

        assertThat(provider.getParent(projectProvider.getFile("a/b/file.txt")))
                .isEqualTo(projectProvider.getDir("a/b"));
        assertThat(provider.getParent(projectProvider.getDir("a/b"))).isEqualTo(projectProvider.getDir("a"));
        assertThat(provider.getParent(projectProvider.getDir("a"))).isEqualTo(project);
        assertThat(provider.getParent(libs)).isEqualTo(project);
        assertThat(provider.getParent(libElement)).isSameAs(libs);
        assertThat(provider.getParent(kwElement)).isSameAs(libElement);
        assertThat(provider.getParent(new Object())).isNull();
    }

    private static Map<LibraryDescriptor, LibrarySpecification> stdLibsMap(final LibrarySpecification... libSpec) {
        return Stream.of(libSpec)
                .collect(toMap(spec -> LibraryDescriptor.ofStandardLibrary(spec.getName()), identity()));
    }

    private static SearchResult createResult() {
        final SearchQuery query = new SearchQuery(new SearchSettings(), model);
        return (SearchResult) query.getSearchResult();
    }

    private static Match matchForFile(final String path) {
        final IFile file = projectProvider.getFile(path);

        final Match fileMatch = mock(Match.class);
        when(fileMatch.getElement()).thenReturn(file);
        return fileMatch;
    }
}

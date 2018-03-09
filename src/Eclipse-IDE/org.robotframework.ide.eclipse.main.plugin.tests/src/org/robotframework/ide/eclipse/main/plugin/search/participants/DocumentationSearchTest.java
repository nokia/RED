/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.search.participants;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.search.ui.text.Match;
import org.junit.ClassRule;
import org.junit.Test;
import org.rf.ide.core.libraries.KeywordSpecification;
import org.rf.ide.core.libraries.LibrarySpecification;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.search.KeywordDocumentationMatch;
import org.robotframework.ide.eclipse.main.plugin.search.LibraryDocumentationMatch;
import org.robotframework.ide.eclipse.main.plugin.search.MatchesGroupingElement;
import org.robotframework.ide.eclipse.main.plugin.search.SearchPattern;
import org.robotframework.ide.eclipse.main.plugin.search.SearchResult;
import org.robotframework.red.junit.ProjectProvider;

public class DocumentationSearchTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(DocumentationSearchTest.class);

    @Test
    public void noMatchesAreReported_whenLibraryDocumentationDoesNotHaveGivenPattern() {
        final SearchPattern patern = new SearchPattern("doc*2");
        final SearchResult result = new SearchResult(null);
        final DocumentationSearch documentationSearch = new DocumentationSearch(patern, new RobotModel(), result);

        final LibrarySpecification librarySpecification = new LibrarySpecification();
        librarySpecification.setDocumentation("this is documentation version 1 of some library");

        documentationSearch.locateMatchesInLibrarySpecification(projectProvider.getProject(), librarySpecification);

        assertThat(result.getMatchCount()).isEqualTo(0);
    }

    @Test
    public void libraryDocumentationMatchIsReported() {
        final SearchPattern patern = new SearchPattern("doc*1");
        final SearchResult result = new SearchResult(null);
        final DocumentationSearch documentationSearch = new DocumentationSearch(patern, new RobotModel(), result);

        final LibrarySpecification librarySpecification = new LibrarySpecification();
        librarySpecification.setDocumentation("this is documentation version 1 of some library");

        documentationSearch.locateMatchesInLibrarySpecification(projectProvider.getProject(), librarySpecification);

        assertThat(result.getMatchCount()).isEqualTo(1);

        final Match[] matches = result
                .getMatches(new MatchesGroupingElement(projectProvider.getProject(), librarySpecification));

        assertThat(matches.length).isEqualTo(1);

        assertThat(matches[0]).isInstanceOf(LibraryDocumentationMatch.class);
        assertThat(matches[0].getOffset()).isEqualTo(8);
        assertThat(matches[0].getLength()).isEqualTo(23);
    }

    @Test
    public void multipleLibraryDocumentationMatchesAreReported() {
        final SearchPattern patern = new SearchPattern("is");
        final SearchResult result = new SearchResult(null);
        final DocumentationSearch documentationSearch = new DocumentationSearch(patern, new RobotModel(), result);

        final LibrarySpecification librarySpecification = new LibrarySpecification();
        librarySpecification.setDocumentation("this is documentation version 1 of some library");

        documentationSearch.locateMatchesInLibrarySpecification(projectProvider.getProject(), librarySpecification);

        assertThat(result.getMatchCount()).isEqualTo(2);

        final Match[] matches = result
                .getMatches(new MatchesGroupingElement(projectProvider.getProject(), librarySpecification));

        assertThat(matches.length).isEqualTo(2);

        assertThat(matches[0]).isInstanceOf(LibraryDocumentationMatch.class);
        assertThat(matches[0].getOffset()).isEqualTo(2);
        assertThat(matches[0].getLength()).isEqualTo(2);

        assertThat(matches[1]).isInstanceOf(LibraryDocumentationMatch.class);
        assertThat(matches[1].getOffset()).isEqualTo(5);
        assertThat(matches[1].getLength()).isEqualTo(2);
    }

    @Test
    public void noMatchesAreReported_whenKeywordDocumentationDoesNotHaveGivenPattern() {
        final SearchPattern patern = new SearchPattern("doc*2");
        final SearchResult result = new SearchResult(null);
        final DocumentationSearch documentationSearch = new DocumentationSearch(patern, new RobotModel(), result);

        final KeywordSpecification kwSpec = new KeywordSpecification();
        kwSpec.setDocumentation("this is documentation version 1 of some keyword");
        final LibrarySpecification libSpec = new LibrarySpecification();
        libSpec.getKeywords().add(kwSpec);

        documentationSearch.locateMatchesInKeywordSpecification(projectProvider.getProject(), libSpec, kwSpec);

        assertThat(result.getMatchCount()).isEqualTo(0);
    }

    @Test
    public void keywordDocumentationMatchIsReported() {
        final SearchPattern patern = new SearchPattern("doc*1");
        final SearchResult result = new SearchResult(null);
        final DocumentationSearch documentationSearch = new DocumentationSearch(patern, new RobotModel(), result);

        final KeywordSpecification kwSpec = new KeywordSpecification();
        kwSpec.setDocumentation("this is documentation version 1 of some keyword");
        final LibrarySpecification libSpec = new LibrarySpecification();
        libSpec.getKeywords().add(kwSpec);

        documentationSearch.locateMatchesInKeywordSpecification(projectProvider.getProject(), libSpec, kwSpec);

        assertThat(result.getMatchCount()).isEqualTo(1);

        final Match[] matches = result
                .getMatches(new MatchesGroupingElement(projectProvider.getProject(), libSpec, kwSpec));

        assertThat(matches.length).isEqualTo(1);

        assertThat(matches[0]).isInstanceOf(KeywordDocumentationMatch.class);
        assertThat(matches[0].getOffset()).isEqualTo(8);
        assertThat(matches[0].getLength()).isEqualTo(23);
    }

    @Test
    public void multipleKeywordDocumentationMatchesAreReported() {
        final SearchPattern patern = new SearchPattern("is");
        final SearchResult result = new SearchResult(null);
        final DocumentationSearch documentationSearch = new DocumentationSearch(patern, new RobotModel(), result);

        final KeywordSpecification kwSpec = new KeywordSpecification();
        kwSpec.setDocumentation("this is documentation version 1 of some keyword");
        final LibrarySpecification libSpec = new LibrarySpecification();
        libSpec.getKeywords().add(kwSpec);

        documentationSearch.locateMatchesInKeywordSpecification(projectProvider.getProject(), libSpec, kwSpec);

        assertThat(result.getMatchCount()).isEqualTo(2);

        final Match[] matches = result
                .getMatches(new MatchesGroupingElement(projectProvider.getProject(), libSpec, kwSpec));

        assertThat(matches.length).isEqualTo(2);
        assertThat(matches[0]).isInstanceOf(KeywordDocumentationMatch.class);
        assertThat(matches[0].getOffset()).isEqualTo(2);
        assertThat(matches[0].getLength()).isEqualTo(2);
        assertThat(matches[1]).isInstanceOf(KeywordDocumentationMatch.class);
        assertThat(matches[1].getOffset()).isEqualTo(5);
        assertThat(matches[1].getLength()).isEqualTo(2);
    }

}

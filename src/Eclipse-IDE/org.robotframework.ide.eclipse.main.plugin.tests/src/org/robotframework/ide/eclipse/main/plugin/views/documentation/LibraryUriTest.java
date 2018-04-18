/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.documentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.net.URI;
import java.util.Optional;

import org.junit.ClassRule;
import org.junit.Test;
import org.rf.ide.core.libraries.KeywordSpecification;
import org.rf.ide.core.libraries.LibrarySpecification;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.Libraries;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.LibraryUri.SpecificationsConsumer;
import org.robotframework.red.junit.ProjectProvider;


public class LibraryUriTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(LibraryUriTest.class);

    @Test
    public void isLibraryDocUriTests() {
        assertThat(LibraryUri.isLibraryDocUri(URI.create("http://www.rf.org"))).isFalse();
        assertThat(LibraryUri.isLibraryDocUri(URI.create("file:///file/location"))).isFalse();
        assertThat(LibraryUri.isLibraryDocUri(URI.create("library:/project/library"))).isFalse();
        assertThat(LibraryUri.isLibraryDocUri(URI.create("library:/project/library?show_source=true"))).isFalse();

        assertThat(LibraryUri.isLibraryDocUri(URI.create("library:/project/library?show_doc=true"))).isTrue();
        assertThat(LibraryUri.isLibraryDocUri(URI.create("library:/project/library?show_doc=false"))).isTrue();
        assertThat(LibraryUri.isLibraryDocUri(URI.create("library:/project/library?a=1&show_doc=true"))).isTrue();
    }

    @Test
    public void isLibrarySourceUriTests() {
        assertThat(LibraryUri.isLibrarySourceUri(URI.create("http://www.rf.org"))).isFalse();
        assertThat(LibraryUri.isLibrarySourceUri(URI.create("file:///file/location"))).isFalse();
        assertThat(LibraryUri.isLibrarySourceUri(URI.create("library:/project/library"))).isFalse();
        assertThat(LibraryUri.isLibrarySourceUri(URI.create("library:/project/library?show_doc=true"))).isFalse();

        assertThat(LibraryUri.isLibrarySourceUri(URI.create("library:/project/library?show_source=true"))).isTrue();
        assertThat(LibraryUri.isLibrarySourceUri(URI.create("library:/project/library?show_source=false"))).isTrue();
        assertThat(LibraryUri.isLibrarySourceUri(URI.create("library:/project/library?a=1&show_source=true"))).isTrue();
    }

    @Test
    public void docOfLibraryUriIsProperlyCreatedFromProjectAndLibNames_withAdditionalArguments() throws Exception {
        final URI uri = LibraryUri.createShowLibraryDocUri("project", "lib");
        assertThat(uri.getPath()).isEqualTo("/project/lib");
        assertThat(uri.getQuery()).isEqualTo("show_doc=true");
    }

    @Test
    public void sourceOfLibraryUriIsProperlyCreatedFromProjectAndLibNames_withAdditionalArguments() throws Exception {
        final URI uri = LibraryUri.createShowLibrarySourceUri("project", "lib");
        assertThat(uri.getPath()).isEqualTo("/project/lib");
        assertThat(uri.getQuery()).isEqualTo("show_source=true");
    }

    @Test
    public void docOfKeywordUriIsProperlyCreatedFromProjectLibAndKwNames_withAdditionalArguments() throws Exception {
        final URI uri = LibraryUri.createShowKeywordDocUri("project", "lib", "kw");
        assertThat(uri.getPath()).isEqualTo("/project/lib/kw");
        assertThat(uri.getQuery()).isEqualTo("show_doc=true");
    }

    @Test
    public void sourceOfKeywordUriIsProperlyCreatedFromProjectLibAndKwNames_withAdditionalArguments() throws Exception {
        final URI uri = LibraryUri.createShowKeywordSourceUri("project", "lib", "kw");
        assertThat(uri.getPath()).isEqualTo("/project/lib/kw");
        assertThat(uri.getQuery()).isEqualTo("show_source=true");
    }

    @Test
    public void whenLibraryUriIsBeingOpen_theProjectIsSearchedAndPassedToConsumerWithQueryArguments_1() {
        final RobotModel model = new RobotModel();
        final RobotProject robotProject = model.createRobotProject(projectProvider.getProject());
        robotProject.setStandardLibraries(Libraries.createStdLib("stdLib", "std_kw"));
        robotProject.setReferencedLibraries(Libraries.createRefLib("refLib", "ref kw"));

        final LibrarySpecification libSpec = robotProject.getStandardLibraries().values().iterator().next();
        final KeywordSpecification kwSpec = libSpec.getKeywords().get(0);

        final SpecificationsConsumer specsConsumer = mock(SpecificationsConsumer.class);
        final LibraryUri libUri = new LibraryUri(model, URI.create("library:/LibraryUriTest/stdLib/std_kw"), specsConsumer);
        libUri.open();

        verify(specsConsumer).accept(same(robotProject), eq(Optional.of(libSpec)), eq(Optional.of(kwSpec)));
    }

    @Test
    public void whenLibraryUriIsBeingOpen_theProjectIsSearchedAndPassedToConsumerWithQueryArguments_2() {
        final RobotModel model = new RobotModel();
        final RobotProject robotProject = model.createRobotProject(projectProvider.getProject());
        robotProject.setStandardLibraries(Libraries.createStdLib("stdLib", "std_kw"));
        robotProject.setReferencedLibraries(Libraries.createRefLib("refLib", "ref kw"));

        final LibrarySpecification libSpec = robotProject.getStandardLibraries().values().iterator().next();

        final SpecificationsConsumer specsConsumer = mock(SpecificationsConsumer.class);
        final LibraryUri libUri = new LibraryUri(model, URI.create("library:/LibraryUriTest/stdLib/non_existing_kw"),
                specsConsumer);
        libUri.open();

        verify(specsConsumer).accept(same(robotProject), eq(Optional.of(libSpec)), eq(Optional.empty()));
    }

    @Test
    public void whenLibraryUriIsBeingOpen_theProjectIsSearchedAndPassedToConsumerWithQueryArguments_3() {
        final RobotModel model = new RobotModel();
        final RobotProject robotProject = model.createRobotProject(projectProvider.getProject());
        robotProject.setStandardLibraries(Libraries.createStdLib("stdLib", "std_kw"));
        robotProject.setReferencedLibraries(Libraries.createRefLib("refLib", "ref kw"));

        final LibrarySpecification libSpec = robotProject.getStandardLibraries().values().iterator().next();

        final SpecificationsConsumer specsConsumer = mock(SpecificationsConsumer.class);
        final LibraryUri libUri = new LibraryUri(model, URI.create("library:/LibraryUriTest/stdLib"), specsConsumer);
        libUri.open();

        verify(specsConsumer).accept(same(robotProject), eq(Optional.of(libSpec)), eq(Optional.empty()));
    }

    @Test
    public void whenLibraryUriIsBeingOpen_theProjectIsSearchedAndPassedToConsumerWithQueryArguments_4() {
        final RobotModel model = new RobotModel();
        final RobotProject robotProject = model.createRobotProject(projectProvider.getProject());
        robotProject.setStandardLibraries(Libraries.createStdLib("stdLib", "std_kw"));
        robotProject.setReferencedLibraries(Libraries.createRefLib("refLib", "ref kw"));

        final SpecificationsConsumer specsConsumer = mock(SpecificationsConsumer.class);
        final LibraryUri libUri = new LibraryUri(model, URI.create("library:/LibraryUriTest/nonExistingLib"),
                specsConsumer);
        libUri.open();

        verify(specsConsumer).accept(same(robotProject), eq(Optional.empty()), eq(Optional.empty()));
    }

    @Test
    public void whenLibraryUriIsBeingOpen_theProjectIsSearchedAndPassedToConsumerWithQueryArguments_5() {
        final RobotModel model = new RobotModel();
        final RobotProject robotProject = model.createRobotProject(projectProvider.getProject());
        robotProject.setStandardLibraries(Libraries.createStdLib("stdLib", "std_kw"));
        robotProject.setReferencedLibraries(Libraries.createRefLib("refLib", "ref kw"));

        final SpecificationsConsumer specsConsumer = mock(SpecificationsConsumer.class);
        final LibraryUri libUri = new LibraryUri(model, URI.create("library:/LibraryUriTest"), specsConsumer);
        libUri.open();

        verify(specsConsumer).accept(same(robotProject), eq(Optional.empty()), eq(Optional.empty()));
    }
}

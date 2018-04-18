/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.documentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.WorkspaceFileUri.FileConsumer;
import org.robotframework.red.junit.ProjectProvider;

import com.google.common.collect.ImmutableMap;

public class WorkspaceFileUriTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(WorkspaceFileUriTest.class);

    @BeforeClass
    public static void beforeSuite() throws IOException, CoreException {
        projectProvider.createFile("file.txt");
    }

    @Test
    public void isFileUriTests() {
        assertThat(WorkspaceFileUri.isFileUri(URI.create("http://www.rf.org"))).isFalse();
        assertThat(WorkspaceFileUri.isFileUri(URI.create("http://www.rf.org?query"))).isFalse();
        assertThat(WorkspaceFileUri.isFileUri(URI.create("file:///location/to/file?a=1"))).isFalse();

        assertThat(WorkspaceFileUri.isFileUri(URI.create("file:///location/to/file"))).isTrue();
    }

    @Test
    public void isFileDocTests() {
        assertThat(WorkspaceFileUri.isFileDocUri(URI.create("http://www.rf.org"))).isFalse();
        assertThat(WorkspaceFileUri.isFileDocUri(URI.create("http://www.rf.org?query"))).isFalse();
        assertThat(WorkspaceFileUri.isFileDocUri(URI.create("http://www.rf.org?show_doc=true"))).isFalse();
        assertThat(WorkspaceFileUri.isFileDocUri(URI.create("file:///location/to/file"))).isFalse();
        assertThat(WorkspaceFileUri.isFileDocUri(URI.create("file:///location/to/file?a=1"))).isFalse();

        assertThat(WorkspaceFileUri.isFileDocUri(URI.create("file:///location/to/file?show_doc=true"))).isTrue();
        assertThat(WorkspaceFileUri.isFileDocUri(URI.create("file:///location/to/file?show_doc=false"))).isTrue();
        assertThat(WorkspaceFileUri.isFileDocUri(URI.create("file:///location/to/file?a=1&show_doc=x"))).isTrue();
    }

    @Test
    public void isFileSourceTests() {
        assertThat(WorkspaceFileUri.isFileSrcUri(URI.create("http://www.rf.org"))).isFalse();
        assertThat(WorkspaceFileUri.isFileSrcUri(URI.create("http://www.rf.org?query"))).isFalse();
        assertThat(WorkspaceFileUri.isFileSrcUri(URI.create("http://www.rf.org?show_source=true"))).isFalse();
        assertThat(WorkspaceFileUri.isFileSrcUri(URI.create("file:///location/to/file"))).isFalse();
        assertThat(WorkspaceFileUri.isFileSrcUri(URI.create("file:///location/to/file?a=1"))).isFalse();

        assertThat(WorkspaceFileUri.isFileSrcUri(URI.create("file:///location/to/file?show_source=true"))).isTrue();
        assertThat(WorkspaceFileUri.isFileSrcUri(URI.create("file:///location/to/file?show_source=false"))).isTrue();
        assertThat(WorkspaceFileUri.isFileSrcUri(URI.create("file:///location/to/file?a=1&show_source=x"))).isTrue();
    }

    @Test
    public void fileUriIsCreatedFromFileLocation() throws Exception {
        final IFile file = projectProvider.getFile("file.txt");
        final URI uri = WorkspaceFileUri.createFileUri(file);

        assertThat(uri).isEqualTo(file.getLocationURI());
        assertThat(uri.getQuery()).isNull();
    }

    @Test
    public void docOfSuiteUriIsCreatedFromFileLocation_withAdditionalSuiteArgument() throws Exception {
        final IFile file = projectProvider.getFile("file.txt");
        final URI uri = WorkspaceFileUri.createShowSuiteDocUri(file);

        assertThat(uri.getPath()).isEqualTo(file.getLocationURI().getPath());
        assertThat(uri.getQuery()).isEqualTo("show_doc=true&suite=");
    }

    @Test
    public void sourceOfSuiteUriIsCreatedFromFileLocation_withAdditionalSuiteArgument() throws Exception {
        final IFile file = projectProvider.getFile("file.txt");
        final URI uri = WorkspaceFileUri.createShowSuiteSourceUri(file);

        assertThat(uri.getPath()).isEqualTo(file.getLocationURI().getPath());
        assertThat(uri.getQuery()).isEqualTo("show_source=true&suite=");
    }

    @Test
    public void docOfKeywordUriIsCreatedFromFileLocation_withAdditionalKeywordNameArgument() throws Exception {
        final IFile file = projectProvider.getFile("file.txt");
        final URI uri = WorkspaceFileUri.createShowKeywordDocUri(file, "kw");

        assertThat(uri.getPath()).isEqualTo(file.getLocationURI().getPath());
        assertThat(uri.getQuery()).isEqualTo("show_doc=true&keyword=kw");
    }

    @Test
    public void sourceOfKeywordUriIsCreatedFromFileLocation_withAdditionalKeywordNameArgument() throws Exception {
        final IFile file = projectProvider.getFile("file.txt");
        final URI uri = WorkspaceFileUri.createShowKeywordSourceUri(file, "kw");

        assertThat(uri.getPath()).isEqualTo(file.getLocationURI().getPath());
        assertThat(uri.getQuery()).isEqualTo("show_source=true&keyword=kw");
    }

    @Test
    public void docOfTestCaseUriIsCreatedFromFileLocation_withAdditionalCaseNameArgument() throws Exception {
        final IFile file = projectProvider.getFile("file.txt");
        final URI uri = WorkspaceFileUri.createShowCaseDocUri(file, "case");

        assertThat(uri.getPath()).isEqualTo(file.getLocationURI().getPath());
        assertThat(uri.getQuery()).isEqualTo("show_doc=true&test=case");
    }

    @Test
    public void sourceOfTestCaseUriIsCreatedFromFileLocation_withAdditionalCaseNameArgument() throws Exception {
        final IFile file = projectProvider.getFile("file.txt");
        final URI uri = WorkspaceFileUri.createShowCaseSourceUri(file, "case");

        assertThat(uri.getPath()).isEqualTo(file.getLocationURI().getPath());
        assertThat(uri.getQuery()).isEqualTo("show_source=true&test=case");
    }

    @Test
    public void whenWorkspaceFileUriIsBeingOpen_theFileWillBeSearchedAndPassedToConsumerWithQueryArguments_1() {
        final IFile file = projectProvider.getFile("file.txt");

        final FileConsumer fileConsumer = mock(FileConsumer.class);
        final WorkspaceFileUri wsFileUri = new WorkspaceFileUri(
                URI.create(file.getLocationURI() + "?param1=a&param2=b"), fileConsumer);
        wsFileUri.open();

        verify(fileConsumer).accept(eq(Optional.of(file)), eq(ImmutableMap.of("param1", "a", "param2", "b")));
    }

    @Test
    public void whenWorkspaceFileUriIsBeingOpen_theFileWillBeSearchedAndPassedToConsumerWithQueryArguments_2() {
        final FileConsumer fileConsumer = mock(FileConsumer.class);
        final WorkspaceFileUri wsFileUri = new WorkspaceFileUri(
                URI.create("file:///path/to/non/existing/file.txt?param1=a&param2=b"), fileConsumer);
        wsFileUri.open();

        verify(fileConsumer).accept(eq(Optional.empty()), eq(ImmutableMap.of("param1", "a", "param2", "b")));

    }
}

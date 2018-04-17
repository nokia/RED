/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.libs;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.rf.ide.core.project.RobotProjectConfig.RemoteLocation;
import org.rf.ide.core.testdata.model.table.setting.LibraryImport;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.red.junit.ProjectProvider;

public class RemoteArgumentsResolverTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(RemoteArgumentsResolverTest.class);

    private RobotModel model;

    private RobotProject robotProject;

    @Before
    public void before() throws Exception {
        model = new RobotModel();
        robotProject = model.createRobotProject(projectProvider.getProject());
        projectProvider.configure();
    }

    @After
    public void after() throws Exception {
        model = null;
        robotProject.clearConfiguration();
    }

    @Test
    public void defaultUriIsFound_whenRemoteArgumentsListContainsNoArguments() throws Exception {
        final RobotSuiteFile suite = createSuiteFileForRemoteImport("Library  Remote");
        final LibraryImport libImport = getImport(suite);
        final List<RobotToken> arguments = libImport.getArguments();
        final RemoteArgumentsResolver resolver = new RemoteArgumentsResolver(arguments);

        assertThat(resolver.getUriToken()).isNull();
        assertThat(resolver.getUri().equals(RemoteLocation.DEFAULT_ADDRESS));
    }

    @Test
    public void defaultUriIsFound_whenRemoteArgumentsListContainsOnlyTimeout() throws Exception {
        final RobotSuiteFile suite = createSuiteFileForRemoteImport("Library  Remote  timeout=30");
        final LibraryImport libImport = getImport(suite);
        final List<RobotToken> arguments = libImport.getArguments();
        final RemoteArgumentsResolver resolver = new RemoteArgumentsResolver(arguments);

        assertThat(resolver.getUriToken()).isNull();
        assertThat(resolver.getUri()).isEqualTo(RemoteLocation.DEFAULT_ADDRESS);
    }

    @Test
    public void uriArgumentIsFound_whenRemoteArgumentsListContainsOnlyPositionalUriWithProtocol() throws Exception {
        final RobotSuiteFile suite = createSuiteFileForRemoteImport("Library  Remote  http://127.0.0.1.9000/");
        final LibraryImport libImport = getImport(suite);
        final List<RobotToken> arguments = libImport.getArguments();
        final RemoteArgumentsResolver resolver = new RemoteArgumentsResolver(arguments);

        assertThat(resolver.getUriToken().getText()).isEqualTo("http://127.0.0.1.9000/");
        assertThat(resolver.getUri()).isEqualTo("http://127.0.0.1.9000/");
    }

    @Test
    public void uriArgumentIsFound_whenRemoteArgumentsListContainsOnlyPositionalUriWithoutProtocol()
            throws Exception {
        final RobotSuiteFile suite = createSuiteFileForRemoteImport("Library  Remote  127.0.0.1.9000/");
        final LibraryImport libImport = getImport(suite);
        final List<RobotToken> arguments = libImport.getArguments();
        final RemoteArgumentsResolver resolver = new RemoteArgumentsResolver(arguments);

        assertThat(resolver.getUriToken().getText()).isEqualTo("127.0.0.1.9000/");
        assertThat(resolver.getUri()).isEqualTo("http://127.0.0.1.9000/");
    }

    @Test
    public void uriArgumentIsFound_whenRemoteArgumentsListContainsOnlyNamedUriWithProtocol() throws Exception {
        final RobotSuiteFile suite = createSuiteFileForRemoteImport("Library  Remote  uri=http://127.0.0.1.9000/");
        final LibraryImport libImport = getImport(suite);
        final List<RobotToken> arguments = libImport.getArguments();
        final RemoteArgumentsResolver resolver = new RemoteArgumentsResolver(arguments);

        assertThat(resolver.getUriToken().getText()).isEqualTo("uri=http://127.0.0.1.9000/");
        assertThat(resolver.getUri()).isEqualTo("http://127.0.0.1.9000/");
    }

    @Test
    public void uriArgumentIsFound_whenRemoteArgumentsListContainsOnlyNamedUriWithoutProtocol() throws Exception {
        final RobotSuiteFile suite = createSuiteFileForRemoteImport("Library  Remote  uri=127.0.0.1.9000/");
        final LibraryImport libImport = getImport(suite);
        final List<RobotToken> arguments = libImport.getArguments();
        final RemoteArgumentsResolver resolver = new RemoteArgumentsResolver(arguments);

        assertThat(resolver.getUriToken().getText()).isEqualTo("uri=127.0.0.1.9000/");
        assertThat(resolver.getUri()).isEqualTo("http://127.0.0.1.9000/");
    }

    @Test
    public void uriArgumentIsFound_whenRemoteArgumentsListContainsPositionalTimeoutAndUriWithoutProtocol()
            throws Exception {
        final RobotSuiteFile suite = createSuiteFileForRemoteImport("Library  Remote  127.0.0.1.9000/  30");
        final LibraryImport libImport = getImport(suite);
        final List<RobotToken> arguments = libImport.getArguments();
        final RemoteArgumentsResolver resolver = new RemoteArgumentsResolver(arguments);

        assertThat(resolver.getUriToken().getText()).isEqualTo("127.0.0.1.9000/");
        assertThat(resolver.getUri()).isEqualTo("http://127.0.0.1.9000/");
    }

    @Test
    public void uriArgumentIsFound_whenRemoteArgumentsListContainsPositionalTimeoutAndUriWithProtocol()
            throws Exception {
        final RobotSuiteFile suite = createSuiteFileForRemoteImport("Library  Remote  http://127.0.0.1.9000/  30");
        final LibraryImport libImport = getImport(suite);
        final List<RobotToken> arguments = libImport.getArguments();
        final RemoteArgumentsResolver resolver = new RemoteArgumentsResolver(arguments);

        assertThat(resolver.getUriToken().getText()).isEqualTo("http://127.0.0.1.9000/");
        assertThat(resolver.getUri()).isEqualTo("http://127.0.0.1.9000/");
    }

    @Test
    public void uriArgumentIsFound_whenRemoteArgumentsListContainsNamedTimeoutAndUriWithProtocol() throws Exception {
        final RobotSuiteFile suite = createSuiteFileForRemoteImport(
                "Library  Remote  uri=http://127.0.0.1.9000/  timeout=30");
        final LibraryImport libImport = getImport(suite);
        final List<RobotToken> arguments = libImport.getArguments();
        final RemoteArgumentsResolver resolver = new RemoteArgumentsResolver(arguments);

        assertThat(resolver.getUriToken().getText()).isEqualTo("uri=http://127.0.0.1.9000/");
        assertThat(resolver.getUri()).isEqualTo("http://127.0.0.1.9000/");
    }

    @Test
    public void uriArgumentIsFound_whenRemoteArgumentsListContainsNamedTimeoutAndUriWithoutProtocol() throws Exception {
        final RobotSuiteFile suite = createSuiteFileForRemoteImport("Library  Remote  uri=127.0.0.1.9000/  timeout=30");
        final LibraryImport libImport = getImport(suite);
        final List<RobotToken> arguments = libImport.getArguments();
        final RemoteArgumentsResolver resolver = new RemoteArgumentsResolver(arguments);

        assertThat(resolver.getUriToken().getText()).isEqualTo("uri=127.0.0.1.9000/");
        assertThat(resolver.getUri()).isEqualTo("http://127.0.0.1.9000/");
    }

    @Test
    public void uriArgumentIsFound_whenRemoteArgumentsListContainsInvertedNamedTimeoutAndUriWithProtocol()
            throws Exception {
        final RobotSuiteFile suite = createSuiteFileForRemoteImport(
                "Library  Remote  timeout=30  uri=http://127.0.0.1.9000/");
        final LibraryImport libImport = getImport(suite);
        final List<RobotToken> arguments = libImport.getArguments();
        final RemoteArgumentsResolver resolver = new RemoteArgumentsResolver(arguments);

        assertThat(resolver.getUriToken().getText()).isEqualTo("uri=http://127.0.0.1.9000/");
        assertThat(resolver.getUri()).isEqualTo("http://127.0.0.1.9000/");
    }

    @Test
    public void uriArgumentIsFound_whenRemoteArgumentsListContainsInvertedNamedTimeoutAndUriWithoutProtocol()
            throws Exception {
        final RobotSuiteFile suite = createSuiteFileForRemoteImport("Library  Remote  timeout=30  uri=127.0.0.1.9000/");
        final LibraryImport libImport = getImport(suite);
        final List<RobotToken> arguments = libImport.getArguments();
        final RemoteArgumentsResolver resolver = new RemoteArgumentsResolver(arguments);

        assertThat(resolver.getUriToken().getText()).isEqualTo("uri=127.0.0.1.9000/");
        assertThat(resolver.getUri()).isEqualTo("http://127.0.0.1.9000/");
    }

    @Test
    public void uriArgumentIsNotFound_whenRemoteArgumentsListContainsTooManyNamedArguments() throws Exception {
        final RobotSuiteFile suite = createSuiteFileForRemoteImport(
                "Library  Remote  timeout=30  timeout=30  uri=127.0.0.1.9000/");
        final LibraryImport libImport = getImport(suite);
        final List<RobotToken> arguments = libImport.getArguments();
        final RemoteArgumentsResolver resolver = new RemoteArgumentsResolver(arguments);

        assertThat(resolver.getUriToken()).isNull();
        assertThat(resolver.getUri()).isNull();
    }

    @Test
    public void uriArgumentIsNotFound_whenRemoteArgumentsListContainsTooManyPositionalArguments() throws Exception {
        final RobotSuiteFile suite = createSuiteFileForRemoteImport("Library  Remote  http://127.0.0.1.9000/  30  60");
        final LibraryImport libImport = getImport(suite);
        final List<RobotToken> arguments = libImport.getArguments();
        final RemoteArgumentsResolver resolver = new RemoteArgumentsResolver(arguments);

        assertThat(resolver.getUriToken()).isNull();
        assertThat(resolver.getUri()).isNull();
    }

    @Test
    public void uriArgumentIsNotFound_whenRemoteArgumentsListContainsTwoNamedUris() throws Exception {
        final RobotSuiteFile suite = createSuiteFileForRemoteImport(
                "Library  Remote  uri=http://127.0.0.1.9000/   uri=http://127.0.0.1.10000/");
        final LibraryImport libImport = getImport(suite);
        final List<RobotToken> arguments = libImport.getArguments();
        final RemoteArgumentsResolver resolver = new RemoteArgumentsResolver(arguments);

        assertThat(resolver.getUriToken()).isNull();
        assertThat(resolver.getUri()).isNull();
    }

    @Test
    public void uriArgumentIsNotFound_whenRemoteArgumentsListContainsTwoNamedTokens() throws Exception {
        final RobotSuiteFile suite = createSuiteFileForRemoteImport("Library  Remote  timeout=30   timeout=60");
        final LibraryImport libImport = getImport(suite);
        final List<RobotToken> arguments = libImport.getArguments();
        final RemoteArgumentsResolver resolver = new RemoteArgumentsResolver(arguments);

        assertThat(resolver.getUriToken()).isNull();
        assertThat(resolver.getUri()).isNull();
    }

    @Test
    public void defaultUriIsFound_whenLibrarySettingArgumentsListContainsNoRemoteArguments() throws Exception {
        final List<String> arguments = Arrays.asList("Remote");

        assertThat(RemoteArgumentsResolver.getUriForSettingArgumentsList(arguments))
                .isEqualTo(RemoteLocation.DEFAULT_ADDRESS);
    }

    @Test
    public void defaultUriIsFound_whenLibrarySettingArgumentsListContainsOnlyTimeout() throws Exception {
        final List<String> arguments = Arrays.asList("Remote", "timeout=30");

        assertThat(RemoteArgumentsResolver.getUriForSettingArgumentsList(arguments))
                .isEqualTo(RemoteLocation.DEFAULT_ADDRESS);
    }

    @Test
    public void uriArgumentIsFound_whenLibrarySettingArgumentsListContainsOnlyPositionalUriWithProtocol()
            throws Exception {
        final List<String> arguments = Arrays.asList("Remote", "http://127.0.0.1.9000/");

        assertThat(RemoteArgumentsResolver.getUriForSettingArgumentsList(arguments))
                .isEqualTo("http://127.0.0.1.9000/");
    }

    @Test
    public void uriArgumentIsFound_whenLibrarySettingArgumentsListContainsOnlyPositionalUriWithoutProtocol()
            throws Exception {
        final List<String> arguments = Arrays.asList("Remote", "127.0.0.1.9000");

        assertThat(RemoteArgumentsResolver.getUriForSettingArgumentsList(arguments)).isEqualTo("http://127.0.0.1.9000");
    }

    @Test
    public void uriArgumentIsFound_whenLibrarySettingArgumentsListContainsOnlyNamedUriWithProtocol() throws Exception {
        final List<String> arguments = Arrays.asList("Remote", "uri=http://127.0.0.1.9000");

        assertThat(RemoteArgumentsResolver.getUriForSettingArgumentsList(arguments)).isEqualTo("http://127.0.0.1.9000");
    }

    @Test
    public void uriArgumentIsFound_whenLibrarySettingArgumentsListContainsOnlyNamedUriWithoutProtocol()
            throws Exception {
        final List<String> arguments = Arrays.asList("Remote", "uri=127.0.0.1.9000");

        assertThat(RemoteArgumentsResolver.getUriForSettingArgumentsList(arguments)).isEqualTo("http://127.0.0.1.9000");
    }

    @Test
    public void uriArgumentIsFound_whenLibrarySettingArgumentsListContainsOnlyPositionalTimeoutAndUriWithProtocol()
            throws Exception {
        final List<String> arguments = Arrays.asList("Remote", "http://127.0.0.1.9000/", "30");

        assertThat(RemoteArgumentsResolver.getUriForSettingArgumentsList(arguments))
                .isEqualTo("http://127.0.0.1.9000/");
    }

    @Test
    public void uriArgumentIsFound_whenLibrarySettingArgumentsListContainsOnlyNamedTimeoutAndUriWithoutProtocol()
            throws Exception {
        final List<String> arguments = Arrays.asList("Remote", "uri=127.0.0.1.9000/", "timeout=30");

        assertThat(RemoteArgumentsResolver.getUriForSettingArgumentsList(arguments))
                .isEqualTo("http://127.0.0.1.9000/");
    }

    @Test
    public void uriArgumentIsFound_whenLibrarySettingArgumentsListContainsOnlyInvertedNamedTimeoutAndUri()
            throws Exception {
        final List<String> arguments = Arrays.asList("Remote", "timeout=30", "uri=http://127.0.0.1.9000");

        assertThat(RemoteArgumentsResolver.getUriForSettingArgumentsList(arguments)).isEqualTo("http://127.0.0.1.9000");
    }

    @Test
    public void uriArgumentIsNotFound_whenLibrarySettingArgumentsListContainsTooManyArguments() throws Exception {
        final List<String> arguments = Arrays.asList("Remote", "uri=http://127.0.0.1.9000", "timeout=30", "timeout=60");

        assertThat(RemoteArgumentsResolver.getUriForSettingArgumentsList(arguments)).isNull();
    }

    @Test
    public void uriArgumentIsNotFound_whenLibrarySettingArgumentsListContainsTwoNamedUriArguments() throws Exception {
        final List<String> arguments = Arrays.asList("Remote", "uri=http://127.0.0.1.9000",
                "uri=http://127.0.0.1.9000");

        assertThat(RemoteArgumentsResolver.getUriForSettingArgumentsList(arguments)).isNull();
    }

    @Test
    public void uriArgumentIsNotFound_whenLibrarySettingArgumentsListContainsTwoNamedTimeoutArguments()
            throws Exception {
        final List<String> arguments = Arrays.asList("Remote", "timeout=30", "timeout=60");

        assertThat(RemoteArgumentsResolver.getUriForSettingArgumentsList(arguments)).isNull();
    }

    private LibraryImport getImport(final RobotSuiteFile suiteFile) {
        return (LibraryImport) suiteFile.findSection(RobotSettingsSection.class)
                .get()
                .getChildren()
                .get(0)
                .getLinkedElement();
    }

    private RobotSuiteFile createSuiteFileForRemoteImport(final String librarySetting) throws Exception {
        return model.createSuiteFile(
                projectProvider.createFile("suite.robot", "*** Settings ***", librarySetting, "*** Test Cases ***"));
    }
}

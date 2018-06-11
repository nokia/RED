/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.libs;

import static org.assertj.core.api.Assertions.assertThat;

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

        assertThat(resolver.getUriToken()).isEmpty();
        assertThat(resolver.getUri()).contains(RemoteLocation.DEFAULT_ADDRESS);
    }

    @Test
    public void defaultUriIsFound_whenRemoteArgumentsListContainsNoArgumentsAndAlias() throws Exception {
        final RobotSuiteFile suite = createSuiteFileForRemoteImport("Library  Remote  WITH NAME  alias");
        final LibraryImport libImport = getImport(suite);
        final List<RobotToken> arguments = libImport.getArguments();
        final RemoteArgumentsResolver resolver = new RemoteArgumentsResolver(arguments);

        assertThat(resolver.getUriToken()).isEmpty();
        assertThat(resolver.getUri()).contains(RemoteLocation.DEFAULT_ADDRESS);
    }

    @Test
    public void defaultUriIsFound_whenRemoteArgumentsListContainsOnlyTimeout() throws Exception {
        final RobotSuiteFile suite = createSuiteFileForRemoteImport("Library  Remote  timeout=30");
        final LibraryImport libImport = getImport(suite);
        final List<RobotToken> arguments = libImport.getArguments();
        final RemoteArgumentsResolver resolver = new RemoteArgumentsResolver(arguments);

        assertThat(resolver.getUriToken()).isEmpty();
        assertThat(resolver.getUri()).contains(RemoteLocation.DEFAULT_ADDRESS);
    }

    @Test
    public void defaultUriIsFound_whenRemoteArgumentsListContainsOnlyTimeoutAndAlias() throws Exception {
        final RobotSuiteFile suite = createSuiteFileForRemoteImport("Library  Remote  timeout=30  WITH NAME  alias");
        final LibraryImport libImport = getImport(suite);
        final List<RobotToken> arguments = libImport.getArguments();
        final RemoteArgumentsResolver resolver = new RemoteArgumentsResolver(arguments);

        assertThat(resolver.getUriToken()).isEmpty();
        assertThat(resolver.getUri()).contains(RemoteLocation.DEFAULT_ADDRESS);
    }

    @Test
    public void uriArgumentIsFound_whenRemoteArgumentsListContainsOnlyPositionalUriWithProtocol() throws Exception {
        final RobotSuiteFile suite = createSuiteFileForRemoteImport("Library  Remote  http://127.0.0.1.9000/");
        final LibraryImport libImport = getImport(suite);
        final List<RobotToken> arguments = libImport.getArguments();
        final RemoteArgumentsResolver resolver = new RemoteArgumentsResolver(arguments);

        assertThat(resolver.getUriToken().get().getText()).isEqualTo("http://127.0.0.1.9000/");
        assertThat(resolver.getUri()).contains("http://127.0.0.1.9000/");
    }

    @Test
    public void uriArgumentIsFound_whenRemoteArgumentsListContainsOnlyPositionalUriWithProtocolAndAlias()
            throws Exception {
        final RobotSuiteFile suite = createSuiteFileForRemoteImport(
                "Library  Remote  http://127.0.0.1.9000/  WITH NAME  alias");
        final LibraryImport libImport = getImport(suite);
        final List<RobotToken> arguments = libImport.getArguments();
        final RemoteArgumentsResolver resolver = new RemoteArgumentsResolver(arguments);

        assertThat(resolver.getUriToken().get().getText()).isEqualTo("http://127.0.0.1.9000/");
        assertThat(resolver.getUri()).contains("http://127.0.0.1.9000/");
    }

    @Test
    public void uriArgumentIsFound_whenRemoteArgumentsListContainsOnlyPositionalUriWithoutProtocol() throws Exception {
        final RobotSuiteFile suite = createSuiteFileForRemoteImport("Library  Remote  127.0.0.1.9000/");
        final LibraryImport libImport = getImport(suite);
        final List<RobotToken> arguments = libImport.getArguments();
        final RemoteArgumentsResolver resolver = new RemoteArgumentsResolver(arguments);

        assertThat(resolver.getUriToken().get().getText()).isEqualTo("127.0.0.1.9000/");
        assertThat(resolver.getUri()).contains("http://127.0.0.1.9000/");
    }

    @Test
    public void uriArgumentIsFound_whenRemoteArgumentsListContainsOnlyNamedUriWithProtocol() throws Exception {
        final RobotSuiteFile suite = createSuiteFileForRemoteImport("Library  Remote  uri=http://127.0.0.1.9000/");
        final LibraryImport libImport = getImport(suite);
        final List<RobotToken> arguments = libImport.getArguments();
        final RemoteArgumentsResolver resolver = new RemoteArgumentsResolver(arguments);

        assertThat(resolver.getUriToken().get().getText()).isEqualTo("uri=http://127.0.0.1.9000/");
        assertThat(resolver.getUri()).contains("http://127.0.0.1.9000/");
    }

    @Test
    public void uriArgumentIsFound_whenRemoteArgumentsListContainsOnlyNamedUriWithoutProtocol() throws Exception {
        final RobotSuiteFile suite = createSuiteFileForRemoteImport("Library  Remote  uri=127.0.0.1.9000/");
        final LibraryImport libImport = getImport(suite);
        final List<RobotToken> arguments = libImport.getArguments();
        final RemoteArgumentsResolver resolver = new RemoteArgumentsResolver(arguments);

        assertThat(resolver.getUriToken().get().getText()).isEqualTo("uri=127.0.0.1.9000/");
        assertThat(resolver.getUri()).contains("http://127.0.0.1.9000/");
    }

    @Test
    public void uriArgumentIsFound_whenRemoteArgumentsListContainsPositionalTimeoutAndUriWithoutProtocol()
            throws Exception {
        final RobotSuiteFile suite = createSuiteFileForRemoteImport("Library  Remote  127.0.0.1.9000/  30");
        final LibraryImport libImport = getImport(suite);
        final List<RobotToken> arguments = libImport.getArguments();
        final RemoteArgumentsResolver resolver = new RemoteArgumentsResolver(arguments);

        assertThat(resolver.getUriToken().get().getText()).isEqualTo("127.0.0.1.9000/");
        assertThat(resolver.getUri()).contains("http://127.0.0.1.9000/");
    }

    @Test
    public void uriArgumentIsFound_whenRemoteArgumentsListContainsPositionalTimeoutAndUriWithoutProtocolAndAlias()
            throws Exception {
        final RobotSuiteFile suite = createSuiteFileForRemoteImport(
                "Library  Remote  127.0.0.1.9000/  30  WITH NAME  alias");
        final LibraryImport libImport = getImport(suite);
        final List<RobotToken> arguments = libImport.getArguments();
        final RemoteArgumentsResolver resolver = new RemoteArgumentsResolver(arguments);

        assertThat(resolver.getUriToken().get().getText()).isEqualTo("127.0.0.1.9000/");
        assertThat(resolver.getUri()).contains("http://127.0.0.1.9000/");
    }

    @Test
    public void uriArgumentIsFound_whenRemoteArgumentsListContainsPositionalTimeoutAndUriWithProtocol()
            throws Exception {
        final RobotSuiteFile suite = createSuiteFileForRemoteImport("Library  Remote  http://127.0.0.1.9000/  30");
        final LibraryImport libImport = getImport(suite);
        final List<RobotToken> arguments = libImport.getArguments();
        final RemoteArgumentsResolver resolver = new RemoteArgumentsResolver(arguments);

        assertThat(resolver.getUriToken().get().getText()).isEqualTo("http://127.0.0.1.9000/");
        assertThat(resolver.getUri()).contains("http://127.0.0.1.9000/");
    }

    @Test
    public void uriArgumentIsFound_whenRemoteArgumentsListContainsNamedTimeoutAndUriWithProtocol() throws Exception {
        final RobotSuiteFile suite = createSuiteFileForRemoteImport(
                "Library  Remote  uri=http://127.0.0.1.9000/  timeout=30");
        final LibraryImport libImport = getImport(suite);
        final List<RobotToken> arguments = libImport.getArguments();
        final RemoteArgumentsResolver resolver = new RemoteArgumentsResolver(arguments);

        assertThat(resolver.getUriToken().get().getText()).isEqualTo("uri=http://127.0.0.1.9000/");
        assertThat(resolver.getUri()).contains("http://127.0.0.1.9000/");
    }

    @Test
    public void uriArgumentIsFound_whenRemoteArgumentsListContainsNamedTimeoutAndUriWithoutProtocol() throws Exception {
        final RobotSuiteFile suite = createSuiteFileForRemoteImport("Library  Remote  uri=127.0.0.1.9000/  timeout=30");
        final LibraryImport libImport = getImport(suite);
        final List<RobotToken> arguments = libImport.getArguments();
        final RemoteArgumentsResolver resolver = new RemoteArgumentsResolver(arguments);

        assertThat(resolver.getUriToken().get().getText()).isEqualTo("uri=127.0.0.1.9000/");
        assertThat(resolver.getUri()).contains("http://127.0.0.1.9000/");
    }

    @Test
    public void uriArgumentIsFound_whenRemoteArgumentsListContainsNamedTimeoutAndUriWithoutProtocolAndAlias()
            throws Exception {
        final RobotSuiteFile suite = createSuiteFileForRemoteImport(
                "Library  Remote  uri=127.0.0.1.9000/  timeout=30  WITH NAME  alias");
        final LibraryImport libImport = getImport(suite);
        final List<RobotToken> arguments = libImport.getArguments();
        final RemoteArgumentsResolver resolver = new RemoteArgumentsResolver(arguments);

        assertThat(resolver.getUriToken().get().getText()).isEqualTo("uri=127.0.0.1.9000/");
        assertThat(resolver.getUri()).contains("http://127.0.0.1.9000/");
    }

    @Test
    public void uriArgumentIsFound_whenRemoteArgumentsListContainsInvertedNamedTimeoutAndUriWithProtocol()
            throws Exception {
        final RobotSuiteFile suite = createSuiteFileForRemoteImport(
                "Library  Remote  timeout=30  uri=http://127.0.0.1.9000/");
        final LibraryImport libImport = getImport(suite);
        final List<RobotToken> arguments = libImport.getArguments();
        final RemoteArgumentsResolver resolver = new RemoteArgumentsResolver(arguments);

        assertThat(resolver.getUriToken().get().getText()).isEqualTo("uri=http://127.0.0.1.9000/");
        assertThat(resolver.getUri()).contains("http://127.0.0.1.9000/");
    }

    @Test
    public void uriArgumentIsFound_whenRemoteArgumentsListContainsInvertedNamedTimeoutAndUriWithoutProtocol()
            throws Exception {
        final RobotSuiteFile suite = createSuiteFileForRemoteImport("Library  Remote  timeout=30  uri=127.0.0.1.9000/");
        final LibraryImport libImport = getImport(suite);
        final List<RobotToken> arguments = libImport.getArguments();
        final RemoteArgumentsResolver resolver = new RemoteArgumentsResolver(arguments);

        assertThat(resolver.getUriToken().get().getText()).isEqualTo("uri=127.0.0.1.9000/");
        assertThat(resolver.getUri()).contains("http://127.0.0.1.9000/");
    }

    @Test
    public void timeoutArgumentIsFound_whenRemoteArgumentsListContainsOnlyNamedTimeout() throws Exception {
        final RobotSuiteFile suite = createSuiteFileForRemoteImport("Library  Remote  timeout=30s");
        final LibraryImport libImport = getImport(suite);
        final List<RobotToken> arguments = libImport.getArguments();
        final RemoteArgumentsResolver resolver = new RemoteArgumentsResolver(arguments);

        assertThat(resolver.getTimeoutToken().get().getText()).isEqualTo("timeout=30s");
        assertThat(resolver.getTimeout()).contains("30s");
    }

    @Test
    public void timeoutArgumentIsFound_whenRemoteArgumentsListContainsNamedTimeoutAndUri() throws Exception {
        final RobotSuiteFile suite = createSuiteFileForRemoteImport("Library  Remote  uri=127.0.0.1.9000  timeout=30");
        final LibraryImport libImport = getImport(suite);
        final List<RobotToken> arguments = libImport.getArguments();
        final RemoteArgumentsResolver resolver = new RemoteArgumentsResolver(arguments);

        assertThat(resolver.getTimeoutToken().get().getText()).isEqualTo("timeout=30");
        assertThat(resolver.getTimeout()).contains("30");
    }

    @Test
    public void timeoutArgumentIsFound_whenRemoteArgumentsListContainsInvertedNamedTimeoutAndUri() throws Exception {
        final RobotSuiteFile suite = createSuiteFileForRemoteImport("Library  Remote  timeout=30  uri=127.0.0.1.9000");
        final LibraryImport libImport = getImport(suite);
        final List<RobotToken> arguments = libImport.getArguments();
        final RemoteArgumentsResolver resolver = new RemoteArgumentsResolver(arguments);

        assertThat(resolver.getTimeoutToken().get().getText()).isEqualTo("timeout=30");
        assertThat(resolver.getTimeout()).contains("30");
    }

    @Test
    public void timeoutArgumentIsFound_whenRemoteArgumentsListContainsPositionalTimeoutAndUri() throws Exception {
        final RobotSuiteFile suite = createSuiteFileForRemoteImport(
                "Library  Remote  127.0.0.1.9000  30 hours 20 minutes");
        final LibraryImport libImport = getImport(suite);
        final List<RobotToken> arguments = libImport.getArguments();
        final RemoteArgumentsResolver resolver = new RemoteArgumentsResolver(arguments);

        assertThat(resolver.getTimeoutToken().get().getText()).isEqualTo("30 hours 20 minutes");
        assertThat(resolver.getTimeout()).contains("30 hours 20 minutes");
    }

    @Test
    public void uriAndTimeoutArgumentsAreNotFound_whenRemoteArgumentsListContainsTooManyNamedArguments()
            throws Exception {
        final RobotSuiteFile suite = createSuiteFileForRemoteImport(
                "Library  Remote  timeout=30  timeout=30  uri=127.0.0.1.9000/");
        final LibraryImport libImport = getImport(suite);
        final List<RobotToken> arguments = libImport.getArguments();
        final RemoteArgumentsResolver resolver = new RemoteArgumentsResolver(arguments);

        assertThat(resolver.getUriToken()).isEmpty();
        assertThat(resolver.getUri()).isEmpty();
        assertThat(resolver.getTimeoutToken()).isEmpty();
        assertThat(resolver.getTimeout()).isEmpty();
    }

    @Test
    public void uriAndTimeoutArgumentsAreNotFound_whenRemoteArgumentsListContainsTooManyNamedArgumentsAndAlias()
            throws Exception {
        final RobotSuiteFile suite = createSuiteFileForRemoteImport(
                "Library  Remote  timeout=30  timeout=30  uri=127.0.0.1.9000/  WITH NAME  alias");
        final LibraryImport libImport = getImport(suite);
        final List<RobotToken> arguments = libImport.getArguments();
        final RemoteArgumentsResolver resolver = new RemoteArgumentsResolver(arguments);

        assertThat(resolver.getUriToken()).isEmpty();
        assertThat(resolver.getUri()).isEmpty();
        assertThat(resolver.getTimeoutToken()).isEmpty();
        assertThat(resolver.getTimeout()).isEmpty();
    }

    @Test
    public void uriAndTimeoutArgumentsAreNotFound_whenRemoteArgumentsListContainsTooManyPositionalArguments()
            throws Exception {
        final RobotSuiteFile suite = createSuiteFileForRemoteImport("Library  Remote  http://127.0.0.1.9000/  30  60");
        final LibraryImport libImport = getImport(suite);
        final List<RobotToken> arguments = libImport.getArguments();
        final RemoteArgumentsResolver resolver = new RemoteArgumentsResolver(arguments);

        assertThat(resolver.getUriToken()).isEmpty();
        assertThat(resolver.getUri()).isEmpty();
        assertThat(resolver.getTimeoutToken()).isEmpty();
        assertThat(resolver.getTimeout()).isEmpty();
    }

    @Test
    public void uriAndTimeoutArgumentsAreNotFound_whenRemoteArgumentsListContainsTooManyPositionalArgumentsAndAlias()
            throws Exception {
        final RobotSuiteFile suite = createSuiteFileForRemoteImport(
                "Library  Remote  http://127.0.0.1.9000/  30  60  WITH NAME  alias");
        final LibraryImport libImport = getImport(suite);
        final List<RobotToken> arguments = libImport.getArguments();
        final RemoteArgumentsResolver resolver = new RemoteArgumentsResolver(arguments);

        assertThat(resolver.getUriToken()).isEmpty();
        assertThat(resolver.getUri()).isEmpty();
        assertThat(resolver.getTimeoutToken()).isEmpty();
        assertThat(resolver.getTimeout()).isEmpty();
    }

    @Test
    public void uriAndTimeoutArgumentsAreNotFound_whenRemoteArgumentsListContainsTwoNamedTokens() throws Exception {
        final RobotSuiteFile suite = createSuiteFileForRemoteImport("Library  Remote  timeout=30   timeout=60");
        final LibraryImport libImport = getImport(suite);
        final List<RobotToken> arguments = libImport.getArguments();
        final RemoteArgumentsResolver resolver = new RemoteArgumentsResolver(arguments);

        assertThat(resolver.getUriToken()).isEmpty();
        assertThat(resolver.getUri()).isEmpty();
        assertThat(resolver.getTimeoutToken()).isEmpty();
        assertThat(resolver.getTimeout()).isEmpty();
    }

    @Test
    public void uriArgumentIsNotFound_whenRemoteArgumentsListContainsTwoNamedUris() throws Exception {
        final RobotSuiteFile suite = createSuiteFileForRemoteImport(
                "Library  Remote  uri=http://127.0.0.1.9000/   uri=http://127.0.0.1.10000/");
        final LibraryImport libImport = getImport(suite);
        final List<RobotToken> arguments = libImport.getArguments();
        final RemoteArgumentsResolver resolver = new RemoteArgumentsResolver(arguments);

        assertThat(resolver.getUriToken()).isEmpty();
        assertThat(resolver.getUri()).isEmpty();
    }

    @Test
    public void timeoutArgumentIsNotFound_whenRemoteArgumentsListContainsNoArguments() throws Exception {
        final RobotSuiteFile suite = createSuiteFileForRemoteImport("Library  Remote");
        final LibraryImport libImport = getImport(suite);
        final List<RobotToken> arguments = libImport.getArguments();
        final RemoteArgumentsResolver resolver = new RemoteArgumentsResolver(arguments);

        assertThat(resolver.getTimeoutToken()).isEmpty();
        assertThat(resolver.getTimeout()).isEmpty();
    }

    @Test
    public void uriArgumentIsFound_whenRemoteArgumentsListContainsInvalidPositionalUriWithoutScheme()
            throws Exception {
        final RobotSuiteFile suite = createSuiteFileForRemoteImport("Library  Remote  ://127.0.0.1:8270/");
        final LibraryImport libImport = getImport(suite);
        final List<RobotToken> arguments = libImport.getArguments();
        final RemoteArgumentsResolver resolver = new RemoteArgumentsResolver(arguments);

        assertThat(resolver.getUriToken().get().getText()).isEqualTo("://127.0.0.1:8270/");
        assertThat(resolver.getUri()).contains("://127.0.0.1:8270/");
    }

    @Test
    public void uriArgumentIsFound_whenRemoteArgumentsListContainsInvalidPositionalUriWithoutAddress()
            throws Exception {
        final RobotSuiteFile suite = createSuiteFileForRemoteImport("Library  Remote  http://");
        final LibraryImport libImport = getImport(suite);
        final List<RobotToken> arguments = libImport.getArguments();
        final RemoteArgumentsResolver resolver = new RemoteArgumentsResolver(arguments);

        assertThat(resolver.getUriToken().get().getText()).isEqualTo("http://");
        assertThat(resolver.getUri()).contains("http://");
    }

    @Test
    public void uriArgumentIsFound_whenRemoteArgumentsListContainsInvalidNamedUriWithoutScheme() throws Exception {
        final RobotSuiteFile suite = createSuiteFileForRemoteImport("Library  Remote  uri=://127.0.0.1:8270/");
        final LibraryImport libImport = getImport(suite);
        final List<RobotToken> arguments = libImport.getArguments();
        final RemoteArgumentsResolver resolver = new RemoteArgumentsResolver(arguments);

        assertThat(resolver.getUriToken().get().getText()).isEqualTo("uri=://127.0.0.1:8270/");
        assertThat(resolver.getUri()).contains("://127.0.0.1:8270/");
    }

    @Test
    public void uriArgumentIsFound_whenRemoteArgumentsListContainsInvalidNamedUriWithoutAddress() throws Exception {
        final RobotSuiteFile suite = createSuiteFileForRemoteImport("Library  Remote  uri=http://");
        final LibraryImport libImport = getImport(suite);
        final List<RobotToken> arguments = libImport.getArguments();
        final RemoteArgumentsResolver resolver = new RemoteArgumentsResolver(arguments);

        assertThat(resolver.getUriToken().get().getText()).isEqualTo("uri=http://");
        assertThat(resolver.getUri()).contains("http://");
    }

    @Test
    public void uriArgumentIsFound_whenRemoteArgumentsListContainsInvalidPositionalUri1() throws Exception {
        final RobotSuiteFile suite = createSuiteFileForRemoteImport(
                "Library  Remote  urrri=http://127.0.0.1:8270/");
        final LibraryImport libImport = getImport(suite);
        final List<RobotToken> arguments = libImport.getArguments();
        final RemoteArgumentsResolver resolver = new RemoteArgumentsResolver(arguments);

        assertThat(resolver.getUriToken().get().getText())
                .isEqualTo("urrri=http://127.0.0.1:8270/");
        assertThat(resolver.getUri()).contains("urrri=http://127.0.0.1:8270/");
    }

    @Test
    public void uriArgumentIsFound_whenRemoteArgumentsListContainsInvalidPositionalUri2() throws Exception {
        final RobotSuiteFile suite = createSuiteFileForRemoteImport(
                "Library  Remote  ^://127.0.0.1:8270/");
        final LibraryImport libImport = getImport(suite);
        final List<RobotToken> arguments = libImport.getArguments();
        final RemoteArgumentsResolver resolver = new RemoteArgumentsResolver(arguments);

        assertThat(resolver.getUriToken().get().getText()).isEqualTo("^://127.0.0.1:8270/");
        assertThat(resolver.getUri()).contains("^://127.0.0.1:8270/");
    }

    @Test
    public void uriArgumentIsFound_whenRemoteArgumentsListContainsInvalidPositionalUri3() throws Exception {
        final RobotSuiteFile suite = createSuiteFileForRemoteImport(
                "Library  Remote  [urihttp://127.0.0.1:8270/");
        final LibraryImport libImport = getImport(suite);
        final List<RobotToken> arguments = libImport.getArguments();
        final RemoteArgumentsResolver resolver = new RemoteArgumentsResolver(arguments);

        assertThat(resolver.getUriToken().get().getText()).isEqualTo("[urihttp://127.0.0.1:8270/");
        assertThat(resolver.getUri()).contains("[urihttp://127.0.0.1:8270/");
    }

    @Test
    public void uriArgumentIsFound_whenRemoteArgumentsListContainsInvalidPositionalUri4() throws Exception {
        final RobotSuiteFile suite = createSuiteFileForRemoteImport("Library  Remote  http://127.0.0.1:8270/%");
        final LibraryImport libImport = getImport(suite);
        final List<RobotToken> arguments = libImport.getArguments();
        final RemoteArgumentsResolver resolver = new RemoteArgumentsResolver(arguments);

        assertThat(resolver.getUriToken().get().getText())
                .isEqualTo("http://127.0.0.1:8270/%");
        assertThat(resolver.getUri()).contains("http://127.0.0.1:8270/%");
    }

    @Test
    public void uriArgumentIsFound_whenRemoteArgumentsListContainsInvalidPositionalUriAndTimeout() throws Exception {
        final RobotSuiteFile suite = createSuiteFileForRemoteImport(
                "Library  Remote  urrri=http://127.0.0.1:8270/  60");
        final LibraryImport libImport = getImport(suite);
        final List<RobotToken> arguments = libImport.getArguments();
        final RemoteArgumentsResolver resolver = new RemoteArgumentsResolver(arguments);

        assertThat(resolver.getUriToken().get().getText())
                .isEqualTo("urrri=http://127.0.0.1:8270/");
        assertThat(resolver.getUri()).contains("urrri=http://127.0.0.1:8270/");
    }

    @Test
    public void uriArgumentIsFound_whenRemoteArgumentsListContainsInvalidNamedUri1() throws Exception {
        final RobotSuiteFile suite = createSuiteFileForRemoteImport(
                "Library  Remote  uri=urrri(http://127.0.0.1:8270/");
        final LibraryImport libImport = getImport(suite);
        final List<RobotToken> arguments = libImport.getArguments();
        final RemoteArgumentsResolver resolver = new RemoteArgumentsResolver(arguments);

        assertThat(resolver.getUriToken().get().getText())
                .isEqualTo("uri=urrri(http://127.0.0.1:8270/");
        assertThat(resolver.getUri()).contains("urrri(http://127.0.0.1:8270/");
    }

    @Test
    public void uriArgumentIsFound_whenRemoteArgumentsListContainsInvalidNamedUri2() throws Exception {
        final RobotSuiteFile suite = createSuiteFileForRemoteImport(
                "Library  Remote  uri=}://127.0.0.1:8270/");
        final LibraryImport libImport = getImport(suite);
        final List<RobotToken> arguments = libImport.getArguments();
        final RemoteArgumentsResolver resolver = new RemoteArgumentsResolver(arguments);

        assertThat(resolver.getUriToken().get().getText()).isEqualTo("uri=}://127.0.0.1:8270/");
        assertThat(resolver.getUri()).contains("}://127.0.0.1:8270/");
    }

    @Test
    public void uriArgumentIsFound_whenRemoteArgumentsListContainsInvalidNamedUri3() throws Exception {
        final RobotSuiteFile suite = createSuiteFileForRemoteImport(
                "Library  Remote  uri=1urihttp://127.0.0.1:8270/");
        final LibraryImport libImport = getImport(suite);
        final List<RobotToken> arguments = libImport.getArguments();
        final RemoteArgumentsResolver resolver = new RemoteArgumentsResolver(arguments);

        assertThat(resolver.getUriToken().get().getText())
                .isEqualTo("uri=1urihttp://127.0.0.1:8270/");
        assertThat(resolver.getUri()).contains("1urihttp://127.0.0.1:8270/");
    }

    @Test
    public void uriArgumentIsFound_whenRemoteArgumentsListContainsInvalidNamedUri4() throws Exception {
        final RobotSuiteFile suite = createSuiteFileForRemoteImport("Library  Remote  uri=http://127.0.0.1:8270/%");
        final LibraryImport libImport = getImport(suite);
        final List<RobotToken> arguments = libImport.getArguments();
        final RemoteArgumentsResolver resolver = new RemoteArgumentsResolver(arguments);

        assertThat(resolver.getUriToken().get().getText()).isEqualTo(
                "uri=http://127.0.0.1:8270/%");
        assertThat(resolver.getUri()).contains("http://127.0.0.1:8270/%");
    }

    @Test
    public void uriArgumentIsFound_whenRemoteArgumentsListContainsInvalidNamedUriAndTimeout() throws Exception {
        final RobotSuiteFile suite = createSuiteFileForRemoteImport(
                "Library  Remote  uri=urrri=http://127.0.0.1:8270/  timeout=60");
        final LibraryImport libImport = getImport(suite);
        final List<RobotToken> arguments = libImport.getArguments();
        final RemoteArgumentsResolver resolver = new RemoteArgumentsResolver(arguments);

        assertThat(resolver.getUriToken().get().getText())
                .isEqualTo("uri=urrri=http://127.0.0.1:8270/");
        assertThat(resolver.getUri()).contains("urrri=http://127.0.0.1:8270/");
    }

    @Test
    public void uriArgumentIsFound_whenRemoteArgumentsListContainsInvertedInvalidNamedUriAndTimeout()
            throws Exception {
        final RobotSuiteFile suite = createSuiteFileForRemoteImport(
                "Library  Remote  timeout=60  uri=;urrrihttp://127.0.0.1:8270/");
        final LibraryImport libImport = getImport(suite);
        final List<RobotToken> arguments = libImport.getArguments();
        final RemoteArgumentsResolver resolver = new RemoteArgumentsResolver(arguments);

        assertThat(resolver.getUriToken().get().getText())
                .isEqualTo("uri=;urrrihttp://127.0.0.1:8270/");
        assertThat(resolver.getUri()).contains(";urrrihttp://127.0.0.1:8270/");
    }

    private RobotSuiteFile createSuiteFileForRemoteImport(final String librarySetting) throws Exception {
        return model.createSuiteFile(
                projectProvider.createFile("suite.robot", "*** Settings ***", librarySetting, "*** Test Cases ***"));
    }

    private static LibraryImport getImport(final RobotSuiteFile suiteFile) {
        return (LibraryImport) suiteFile.findSection(RobotSettingsSection.class)
                .get()
                .getChildren()
                .get(0)
                .getLinkedElement();
    }

}

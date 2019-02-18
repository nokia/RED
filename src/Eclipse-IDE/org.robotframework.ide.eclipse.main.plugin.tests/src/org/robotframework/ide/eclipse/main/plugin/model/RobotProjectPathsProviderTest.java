/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.net.URISyntaxException;

import org.eclipse.core.resources.IFile;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.rf.ide.core.project.ImportPath;
import org.rf.ide.core.project.ImportSearchPaths.MarkedUri;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.VariableMapping;
import org.robotframework.red.junit.ProjectProvider;

public class RobotProjectPathsProviderTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(RobotProjectPathsProviderTest.class);

    private static IFile suite;

    private RobotProject robotProject;

    @BeforeClass
    public static void beforeSuite() throws Exception {
        projectProvider.createDir("dirA");
        projectProvider.createDir("dirA/dirB");
        projectProvider.createFile("dirA/dirB/lib.py");
        suite = projectProvider.createFile("file.robot", "");
    }

    @Before
    public void before() throws Exception {
        projectProvider.configure();
        robotProject = new RobotModel().createRobotProject(projectProvider.getProject());
    }

    @Test
    public void exceptionIsThrown_whenAbsoluteUriIsFoundByPathWithIncorrectSyntax() throws Exception {
        final RobotProjectPathsProvider pathsProvider = new RobotProjectPathsProvider(robotProject);
        final ImportPath importPath = ImportPath.from("{}incorrect");

        assertThatExceptionOfType(URISyntaxException.class)
                .isThrownBy(() -> pathsProvider.findAbsoluteUri(suite, importPath));
    }

    @Test
    public void emptyOptionalIsReturned_whenAbsoluteUriIsFoundByPathWithIncorrectSyntax() throws Exception {
        final RobotProjectPathsProvider pathsProvider = new RobotProjectPathsProvider(robotProject);
        final ImportPath importPath = ImportPath.from("{}incorrect");

        assertThat(pathsProvider.tryToFindAbsoluteUri(suite, importPath)).isNotPresent();
    }

    @Test
    public void emptyOptionalIsReturned_whenAbsoluteUriIsFoundByPathPointingToNotExistingResource() throws Exception {
        final RobotProjectPathsProvider pathsProvider = new RobotProjectPathsProvider(robotProject);
        final ImportPath importPath = ImportPath.from("non_existing.robot");

        assertThat(pathsProvider.tryToFindAbsoluteUri(suite, importPath)).isNotPresent();
    }

    @Test
    public void emptyOptionalIsReturned_whenAbsoluteUriIsFoundByPathWithUnresolvedVariables() throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        config.setVariableMappings(newArrayList(VariableMapping.create("${var1}", "dirA")));
        projectProvider.configure(config);

        final RobotProjectPathsProvider pathsProvider = new RobotProjectPathsProvider(robotProject);
        final ImportPath importPath = ImportPath.from("${var1}/${var2}/lib.py");

        assertThat(pathsProvider.tryToFindAbsoluteUri(suite, importPath)).isNotPresent();
    }

    @Test
    public void absoluteUriIsReturned_whenAbsoluteUriIsFoundByPathWithResolvedVariables() throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        config.setVariableMappings(
                newArrayList(VariableMapping.create("${var1}", "dirA"), VariableMapping.create("${var2}", "dirB")));
        projectProvider.configure(config);

        final RobotProjectPathsProvider pathsProvider = new RobotProjectPathsProvider(robotProject);
        final ImportPath importPath = ImportPath.from("${var1}/${var2}/lib.py");

        assertThat(pathsProvider.tryToFindAbsoluteUri(suite, importPath))
                .hasValue(projectProvider.getFile("dirA/dirB/lib.py").getLocationURI());
    }

    @Test
    public void absoluteUriIsReturned_whenAbsoluteUriIsFoundByAbsolutePath() throws Exception {
        final RobotProjectPathsProvider pathsProvider = new RobotProjectPathsProvider(robotProject);
        final ImportPath importPath = ImportPath
                .from(projectProvider.getFile("dirA/dirB/lib.py").getRawLocation().toFile().getAbsolutePath());

        assertThat(pathsProvider.tryToFindAbsoluteUri(suite, importPath))
                .hasValue(projectProvider.getFile("dirA/dirB/lib.py").getLocationURI());
    }

    @Test
    public void exceptionIsThrown_whenAbsoluteMarkedUriIsFoundByPathWithIncorrectSyntax() throws Exception {
        final RobotProjectPathsProvider pathsProvider = new RobotProjectPathsProvider(robotProject);
        final ImportPath importPath = ImportPath.from("{}incorrect");

        assertThatExceptionOfType(URISyntaxException.class)
                .isThrownBy(() -> pathsProvider.findAbsoluteMarkedUri(suite, importPath));
    }

    @Test
    public void emptyOptionalIsReturned_whenAbsoluteMarkedUriIsFoundByPathWithIncorrectSyntax() throws Exception {
        final RobotProjectPathsProvider pathsProvider = new RobotProjectPathsProvider(robotProject);
        final ImportPath importPath = ImportPath.from("{}incorrect");

        assertThat(pathsProvider.tryToFindAbsoluteMarkedUri(suite, importPath)).isNotPresent();
    }

    @Test
    public void emptyOptionalIsReturned_whenAbsoluteMarkedUriIsFoundByPathPointingToNotExistingResource()
            throws Exception {
        final RobotProjectPathsProvider pathsProvider = new RobotProjectPathsProvider(robotProject);
        final ImportPath importPath = ImportPath.from("non_existing.robot");

        assertThat(pathsProvider.tryToFindAbsoluteMarkedUri(suite, importPath)).isNotPresent();
    }

    @Test
    public void emptyOptionalIsReturned_whenAbsoluteMarkedUriIsFoundByPathWithUnresolvedVariables() throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        config.setVariableMappings(newArrayList(VariableMapping.create("${var1}", "dirA")));
        projectProvider.configure(config);

        final RobotProjectPathsProvider pathsProvider = new RobotProjectPathsProvider(robotProject);
        final ImportPath importPath = ImportPath.from("${var1}/${var2}/lib.py");

        assertThat(pathsProvider.tryToFindAbsoluteMarkedUri(suite, importPath)).isNotPresent();
    }

    @Test
    public void absoluteUriIsReturned_whenAbsoluteMarkedUriIsFoundByPathWithResolvedVariables() throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        config.setVariableMappings(
                newArrayList(VariableMapping.create("${var1}", "dirA"), VariableMapping.create("${var2}", "dirB")));
        projectProvider.configure(config);

        final RobotProjectPathsProvider pathsProvider = new RobotProjectPathsProvider(robotProject);
        final ImportPath importPath = ImportPath.from("${var1}/${var2}/lib.py");

        assertThat(pathsProvider.tryToFindAbsoluteMarkedUri(suite, importPath).map(MarkedUri::getPath))
                .hasValue(projectProvider.getFile("dirA/dirB/lib.py").getLocationURI());
    }

    @Test
    public void absoluteUriIsReturned_whenAbsoluteMarkedUriIsFoundByAbsolutePath() throws Exception {
        final RobotProjectPathsProvider pathsProvider = new RobotProjectPathsProvider(robotProject);
        final ImportPath importPath = ImportPath
                .from(projectProvider.getFile("dirA/dirB/lib.py").getRawLocation().toFile().getAbsolutePath());

        assertThat(pathsProvider.tryToFindAbsoluteMarkedUri(suite, importPath).map(MarkedUri::getPath))
                .hasValue(projectProvider.getFile("dirA/dirB/lib.py").getLocationURI());
    }
}

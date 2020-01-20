/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.robotframework.red.junit.jupiter.ProjectExtension.configure;
import static org.robotframework.red.junit.jupiter.ProjectExtension.getFile;

import java.net.URISyntaxException;

import org.eclipse.core.resources.IProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.rf.ide.core.project.ImportPath;
import org.rf.ide.core.project.ImportSearchPaths.MarkedUri;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.VariableMapping;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;

@ExtendWith(ProjectExtension.class)
public class RobotProjectPathsProviderTest {

    @Project(dirs = { "dirA", "dirA/dirB" }, files = { "dirA/dirB/lib.py", "file.robot" })
    static IProject project;

    private RobotProject robotProject;

    @BeforeEach
    public void before() throws Exception {
        configure(project);
        robotProject = new RobotModel().createRobotProject(project);
    }

    @Test
    public void exceptionIsThrown_whenAbsoluteUriIsFoundByPathWithIncorrectSyntax() throws Exception {
        final RobotProjectPathsProvider pathsProvider = new RobotProjectPathsProvider(robotProject);
        final ImportPath importPath = ImportPath.from("{}incorrect");

        assertThatExceptionOfType(URISyntaxException.class)
                .isThrownBy(() -> pathsProvider.findAbsoluteUri(getFile(project, "file.robot"), importPath));
    }

    @Test
    public void emptyOptionalIsReturned_whenAbsoluteUriIsFoundByPathWithIncorrectSyntax() throws Exception {
        final RobotProjectPathsProvider pathsProvider = new RobotProjectPathsProvider(robotProject);
        final ImportPath importPath = ImportPath.from("{}incorrect");

        assertThat(pathsProvider.tryToFindAbsoluteUri(getFile(project, "file.robot"), importPath)).isNotPresent();
    }

    @Test
    public void emptyOptionalIsReturned_whenAbsoluteUriIsFoundByPathPointingToNotExistingResource() throws Exception {
        final RobotProjectPathsProvider pathsProvider = new RobotProjectPathsProvider(robotProject);
        final ImportPath importPath = ImportPath.from("non_existing.robot");

        assertThat(pathsProvider.tryToFindAbsoluteUri(getFile(project, "file.robot"), importPath)).isNotPresent();
    }

    @Test
    public void emptyOptionalIsReturned_whenAbsoluteUriIsFoundByPathWithUnresolvedVariables() throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        config.setVariableMappings(newArrayList(VariableMapping.create("${var1}", "dirA")));
        configure(project, config);

        final RobotProjectPathsProvider pathsProvider = new RobotProjectPathsProvider(robotProject);
        final ImportPath importPath = ImportPath.from("${var1}/${var2}/lib.py");

        assertThat(pathsProvider.tryToFindAbsoluteUri(getFile(project, "file.robot"), importPath)).isNotPresent();
    }

    @Test
    public void absoluteUriIsReturned_whenAbsoluteUriIsFoundByPathWithResolvedVariables() throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        config.setVariableMappings(
                newArrayList(VariableMapping.create("${var1}", "dirA"), VariableMapping.create("${var2}", "dirB")));
        configure(project, config);

        final RobotProjectPathsProvider pathsProvider = new RobotProjectPathsProvider(robotProject);
        final ImportPath importPath = ImportPath.from("${var1}/${var2}/lib.py");

        assertThat(pathsProvider.tryToFindAbsoluteUri(getFile(project, "file.robot"), importPath))
                .hasValue(getFile(project, "dirA/dirB/lib.py").getLocationURI());
    }

    @Test
    public void absoluteUriIsReturned_whenAbsoluteUriIsFoundByAbsolutePath() throws Exception {
        final RobotProjectPathsProvider pathsProvider = new RobotProjectPathsProvider(robotProject);
        final ImportPath importPath = ImportPath
                .from(getFile(project, "dirA/dirB/lib.py").getRawLocation().toFile().getAbsolutePath());

        assertThat(pathsProvider.tryToFindAbsoluteUri(getFile(project, "file.robot"), importPath))
                .hasValue(getFile(project, "dirA/dirB/lib.py").getLocationURI());
    }

    @Test
    public void exceptionIsThrown_whenAbsoluteMarkedUriIsFoundByPathWithIncorrectSyntax() throws Exception {
        final RobotProjectPathsProvider pathsProvider = new RobotProjectPathsProvider(robotProject);
        final ImportPath importPath = ImportPath.from("{}incorrect");

        assertThatExceptionOfType(URISyntaxException.class)
                .isThrownBy(() -> pathsProvider.findAbsoluteMarkedUri(getFile(project, "file.robot"), importPath));
    }

    @Test
    public void emptyOptionalIsReturned_whenAbsoluteMarkedUriIsFoundByPathWithIncorrectSyntax() throws Exception {
        final RobotProjectPathsProvider pathsProvider = new RobotProjectPathsProvider(robotProject);
        final ImportPath importPath = ImportPath.from("{}incorrect");

        assertThat(pathsProvider.tryToFindAbsoluteMarkedUri(getFile(project, "file.robot"), importPath)).isNotPresent();
    }

    @Test
    public void emptyOptionalIsReturned_whenAbsoluteMarkedUriIsFoundByPathPointingToNotExistingResource()
            throws Exception {
        final RobotProjectPathsProvider pathsProvider = new RobotProjectPathsProvider(robotProject);
        final ImportPath importPath = ImportPath.from("non_existing.robot");

        assertThat(pathsProvider.tryToFindAbsoluteMarkedUri(getFile(project, "file.robot"), importPath)).isNotPresent();
    }

    @Test
    public void emptyOptionalIsReturned_whenAbsoluteMarkedUriIsFoundByPathWithUnresolvedVariables() throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        config.setVariableMappings(newArrayList(VariableMapping.create("${var1}", "dirA")));
        configure(project, config);

        final RobotProjectPathsProvider pathsProvider = new RobotProjectPathsProvider(robotProject);
        final ImportPath importPath = ImportPath.from("${var1}/${var2}/lib.py");

        assertThat(pathsProvider.tryToFindAbsoluteMarkedUri(getFile(project, "file.robot"), importPath)).isNotPresent();
    }

    @Test
    public void absoluteUriIsReturned_whenAbsoluteMarkedUriIsFoundByPathWithResolvedVariables() throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        config.setVariableMappings(
                newArrayList(VariableMapping.create("${var1}", "dirA"), VariableMapping.create("${var2}", "dirB")));
        configure(project, config);

        final RobotProjectPathsProvider pathsProvider = new RobotProjectPathsProvider(robotProject);
        final ImportPath importPath = ImportPath.from("${var1}/${var2}/lib.py");

        assertThat(pathsProvider.tryToFindAbsoluteMarkedUri(getFile(project, "file.robot"), importPath)
                .map(MarkedUri::getPath)).hasValue(getFile(project, "dirA/dirB/lib.py").getLocationURI());
    }

    @Test
    public void absoluteUriIsReturned_whenAbsoluteMarkedUriIsFoundByAbsolutePath() throws Exception {
        final RobotProjectPathsProvider pathsProvider = new RobotProjectPathsProvider(robotProject);
        final ImportPath importPath = ImportPath
                .from(getFile(project, "dirA/dirB/lib.py").getRawLocation().toFile().getAbsolutePath());

        assertThat(pathsProvider.tryToFindAbsoluteMarkedUri(getFile(project, "file.robot"), importPath)
                .map(MarkedUri::getPath)).hasValue(getFile(project, "dirA/dirB/lib.py").getLocationURI());
    }
}

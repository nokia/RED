/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.refactoring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.red.junit.jupiter.ProjectExtension.configure;
import static org.robotframework.red.junit.jupiter.ProjectExtension.getDir;
import static org.robotframework.red.junit.jupiter.ProjectExtension.getFile;

import java.util.Optional;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ui.IEditorPart;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.ExcludedPath;
import org.robotframework.ide.eclipse.main.plugin.project.RedEclipseProjectConfigReader;
import org.robotframework.red.junit.Editors;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;

@ExtendWith(ProjectExtension.class)
public class RedXmlChangesCollectorTest {

    @Project(dirs = { "a", "a/b", "c" })
    static IProject project;

    @BeforeEach
    public void beforeTest() throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        config.addExcludedPath("a");
        config.addExcludedPath("a/b");
        config.addExcludedPath("c");
        configure(project, config);
    }

    @AfterEach
    public void afterTest() {
        Editors.closeAll();
    }

    @Test
    public void whenBothProjectEditorAndTextEditorAreOpened_theChangesAreOnlyGatheredForProjectEditor()
            throws Exception {
        final IFile redXmlFile = getFile(project, "red.xml");

        final IEditorPart textEditor = Editors.openInTextEditor(redXmlFile);
        final IEditorPart projectEditor = Editors.openInProjectEditor(redXmlFile);

        final Optional<Change> change = new RedXmlChangesCollector().collect(getDir(project, "a"),
                Optional.of(new Path(project.getName() + "/moved")));

        change.get().perform(new NullProgressMonitor());

        assertThat(projectEditor.isDirty()).isTrue();
        assertThat(textEditor.isDirty()).isTrue();
        assertThatConfigFileHasOriginalContent(redXmlFile);
    }

    @Test
    public void whenProjectEditorIsOpenedButTextEditorIsNot_theChangesAreOnlyGatheredForProjectEditor()
            throws Exception {
        final IFile redXmlFile = getFile(project, "red.xml");

        final IEditorPart projectEditor = Editors.openInProjectEditor(redXmlFile);

        final Optional<Change> change = new RedXmlChangesCollector().collect(getDir(project, "a"),
                Optional.of(new Path(project.getName() + "/moved")));

        change.get().perform(new NullProgressMonitor());

        assertThat(projectEditor.isDirty()).isTrue();
        assertThatConfigFileHasOriginalContent(redXmlFile);
    }

    @Test
    public void whenTextEditorIsOpenedButProjectEditorIsNot_theChangesAreOnlyGatheredForTextEditor() throws Exception {
        final IFile redXmlFile = getFile(project, "red.xml");

        final IEditorPart textEditor = Editors.openInTextEditor(redXmlFile);

        final Optional<Change> change = new RedXmlChangesCollector().collect(getDir(project, "a"),
                Optional.of(new Path(project.getName() + "/moved")));

        change.get().perform(new NullProgressMonitor());

        assertThat(textEditor.isDirty()).isTrue();
        assertThatConfigFileHasOriginalContent(redXmlFile);
    }

    @Test
    public void whenNoEditorIsOpened_theChangesAreMadeDirectlyInFile() throws Exception {
        final IFile redXmlFile = getFile(project, "red.xml");
        assertThat(Editors.isAnyEditorOpened()).isFalse();

        final Optional<Change> change = new RedXmlChangesCollector().collect(getDir(project, "a"),
                Optional.of(new Path(project.getName() + "/moved")));

        change.get().perform(new NullProgressMonitor());

        assertThatConfigFileModifiedContent(redXmlFile);
    }

    private static void assertThatConfigFileHasOriginalContent(final IFile redXmlFile) {
        final RobotProjectConfig config = new RedEclipseProjectConfigReader().readConfiguration(redXmlFile);
        assertThat(config.getExcludedPaths()).containsOnly(ExcludedPath.create("a"), ExcludedPath.create("a/b"),
                ExcludedPath.create("c"));
    }

    private static void assertThatConfigFileModifiedContent(final IFile redXmlFile) {
        final RobotProjectConfig config = new RedEclipseProjectConfigReader().readConfiguration(redXmlFile);
        assertThat(config.getExcludedPaths()).containsOnly(ExcludedPath.create("moved"), ExcludedPath.create("moved/b"),
                ExcludedPath.create("c"));
    }

}

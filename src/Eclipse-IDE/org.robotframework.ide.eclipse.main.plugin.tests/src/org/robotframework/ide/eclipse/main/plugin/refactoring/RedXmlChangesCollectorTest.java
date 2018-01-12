/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.refactoring;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ui.IEditorPart;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.ExcludedFolderPath;
import org.robotframework.ide.eclipse.main.plugin.project.RedEclipseProjectConfigReader;
import org.robotframework.red.junit.Editors;
import org.robotframework.red.junit.ProjectProvider;

public class RedXmlChangesCollectorTest {

    private static final String PROJECT_NAME = RedXmlChangesCollectorTest.class.getSimpleName();

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(PROJECT_NAME);

    @BeforeClass
    public static void beforeSuite() throws Exception {
        projectProvider.createDir(new Path("a"));
        projectProvider.createDir(new Path("a/b"));
        projectProvider.createDir(new Path("c"));
    }

    @Before
    public void beforeTest() throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        config.addExcludedPath("a");
        config.addExcludedPath("a/b");
        config.addExcludedPath("c");
        projectProvider.configure(config);
    }

    @After
    public void afterTest() {
        Editors.closeAll();
    }

    @Test
    public void whenBothProjectEditorAndTextEditorAreOpened_theChangesAreOnlyGatheredForProjectEditor()
            throws Exception {
        final IFile redXmlFile = projectProvider.getFile("red.xml");

        final IEditorPart textEditor = Editors.openInTextEditor(redXmlFile);
        final IEditorPart projectEditor = Editors.openInProjectEditor(redXmlFile);

        final Optional<Change> change = new RedXmlChangesCollector().collect(projectProvider.getDir("a"),
                Optional.of(new Path(PROJECT_NAME + "/moved")));

        change.get().perform(new NullProgressMonitor());

        assertThat(projectEditor.isDirty()).isTrue();
        assertThat(textEditor.isDirty()).isTrue();
        assertThatConfigFileHasOriginalContent(redXmlFile);
    }

    @Test
    public void whenProjectEditorIsOpenedButTextEditorIsNot_theChangesAreOnlyGatheredForProjectEditor()
            throws Exception {
        final IFile redXmlFile = projectProvider.getFile("red.xml");

        final IEditorPart projectEditor = Editors.openInProjectEditor(redXmlFile);

        final Optional<Change> change = new RedXmlChangesCollector().collect(projectProvider.getDir("a"),
                Optional.of(new Path(PROJECT_NAME + "/moved")));

        change.get().perform(new NullProgressMonitor());

        assertThat(projectEditor.isDirty()).isTrue();
        assertThatConfigFileHasOriginalContent(redXmlFile);
    }

    @Test
    public void whenTextEditorIsOpenedButProjectEditorIsNot_theChangesAreOnlyGatheredForTextEditor() throws Exception {
        final IFile redXmlFile = projectProvider.getFile("red.xml");

        final IEditorPart textEditor = Editors.openInTextEditor(redXmlFile);

        final Optional<Change> change = new RedXmlChangesCollector().collect(projectProvider.getDir("a"),
                Optional.of(new Path(PROJECT_NAME + "/moved")));

        change.get().perform(new NullProgressMonitor());

        assertThat(textEditor.isDirty()).isTrue();
        assertThatConfigFileHasOriginalContent(redXmlFile);
    }

    @Test
    public void whenNoEditorIsOpened_theChangesAreMadeDirectlyInFile() throws Exception {
        final IFile redXmlFile = projectProvider.getFile("red.xml");
        assertThat(Editors.isAnyEditorOpened()).isFalse();

        final Optional<Change> change = new RedXmlChangesCollector().collect(projectProvider.getDir("a"),
                Optional.of(new Path(PROJECT_NAME + "/moved")));

        change.get().perform(new NullProgressMonitor());

        assertThatConfigFileModifiedContent(redXmlFile);
    }

    private static void assertThatConfigFileHasOriginalContent(final IFile redXmlFile) {
        final RobotProjectConfig config = new RedEclipseProjectConfigReader().readConfiguration(redXmlFile);
        assertThat(config.getExcludedPath()).containsOnly(ExcludedFolderPath.create("a"),
                ExcludedFolderPath.create("a/b"), ExcludedFolderPath.create("c"));
    }

    private static void assertThatConfigFileModifiedContent(final IFile redXmlFile) {
        final RobotProjectConfig config = new RedEclipseProjectConfigReader().readConfiguration(redXmlFile);
        assertThat(config.getExcludedPath()).containsOnly(ExcludedFolderPath.create("moved"),
                ExcludedFolderPath.create("moved/b"), ExcludedFolderPath.create("c"));
    }

}

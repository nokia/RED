/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.refactoring;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ExcludedFolderPath;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigReader;
import org.robotframework.red.junit.ProjectProvider;

import com.google.common.base.Optional;

public class RedXmlChangesCollectorTest {

    private static final String PROJECT_NAME = RedXmlInProjectEditorChangesCollectorTest.class.getSimpleName();

    @Rule
    public ProjectProvider projectProvider = new ProjectProvider(PROJECT_NAME);

    @Before
    public void beforeTest() throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        config.addExcludedPath(new Path("a"));
        config.addExcludedPath(new Path("a/b"));
        config.addExcludedPath(new Path("c"));
        projectProvider.configure(config);

        projectProvider.createDir(new Path("a"));
        projectProvider.createDir(new Path("a/b"));
        projectProvider.createDir(new Path("c"));
    }

    @After
    public void afterTest() {
        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeAllEditors(false);
    }

    @Test
    public void whenBothProjectEditorAndTextEditorAreOpened_theChangesAreOnlyGatheredForProjectEditor()
            throws Exception {
        final IFile redXmlFile = projectProvider.getFile("red.xml");

        final IEditorPart textEditor = Editors.openInTextEditor(redXmlFile);
        final IEditorPart projectEditor = Editors.openInProjectEditor(redXmlFile);

        final Optional<Change> change = new RedXmlChangesCollector().collect(projectProvider.getDir("a"),
                Optional.<IPath> of(new Path(PROJECT_NAME + "/moved")));

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
                Optional.<IPath> of(new Path(PROJECT_NAME + "/moved")));

        change.get().perform(new NullProgressMonitor());

        assertThat(projectEditor.isDirty()).isTrue();
        assertThatConfigFileHasOriginalContent(redXmlFile);
    }

    @Test
    public void whenTextEditorIsOpenedButProjectEditorIsNot_theChangesAreOnlyGatheredForTextEditor() throws Exception {
        final IFile redXmlFile = projectProvider.getFile("red.xml");

        final IEditorPart textEditor = Editors.openInTextEditor(redXmlFile);

        final Optional<Change> change = new RedXmlChangesCollector().collect(projectProvider.getDir("a"),
                Optional.<IPath> of(new Path(PROJECT_NAME + "/moved")));

        change.get().perform(new NullProgressMonitor());

        assertThat(textEditor.isDirty()).isTrue();
        assertThatConfigFileHasOriginalContent(redXmlFile);
    }

    @Test
    public void whenNoEditorIsOpened_theChangesAreMadeDirectlyInFile() throws Exception {
        final IFile redXmlFile = projectProvider.getFile("red.xml");
        assertThat(Editors.isAnyEditorOpened()).isFalse();

        final Optional<Change> change = new RedXmlChangesCollector().collect(projectProvider.getDir("a"),
                Optional.<IPath> of(new Path(PROJECT_NAME + "/moved")));

        change.get().perform(new NullProgressMonitor());

        assertThatConfigFileModifiedContent(redXmlFile);
    }

    private static void assertThatConfigFileHasOriginalContent(final IFile redXmlFile) {
        final RobotProjectConfigReader reader = new RobotProjectConfigReader();
        final RobotProjectConfig config = reader.readConfiguration(redXmlFile);
        assertThat(config.getExcludedPath()).containsOnly(ExcludedFolderPath.create("a"),
                ExcludedFolderPath.create("a/b"), ExcludedFolderPath.create("c"));
    }

    private static void assertThatConfigFileModifiedContent(final IFile redXmlFile) {
        final RobotProjectConfigReader reader = new RobotProjectConfigReader();
        final RobotProjectConfig config = reader.readConfiguration(redXmlFile);
        assertThat(config.getExcludedPath()).containsOnly(ExcludedFolderPath.create("moved"),
                ExcludedFolderPath.create("moved/b"), ExcludedFolderPath.create("c"));
    }

}

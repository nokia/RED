/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.refactoring;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
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

import com.google.common.base.Optional;

public class RedXmlInProjectEditorChangesCollectorTest {

    private static final String PROJECT_NAME = RedXmlInProjectEditorChangesCollectorTest.class.getSimpleName();

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
        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeAllEditors(false);
    }

    @Test
    public void noChangeIsCollected_whenTheFileIsNotOpened() {
        final IFile redXmlFile = projectProvider.getFile(new Path("red.xml"));

        final RedXmlInProjectEditorChangesCollector collector = new RedXmlInProjectEditorChangesCollector(redXmlFile,
                new Path(PROJECT_NAME + "/a"), Optional.<IPath> absent());
        final Optional<Change> change = collector.collect();

        assertThat(change.isPresent()).isFalse();
    }

    @Test
    public void noChangeIsCollected_whenTheFileIsOpenedInTextEditor() throws Exception {
        final IFile redXmlFile = projectProvider.getFile(new Path("red.xml"));
        Editors.openInTextEditor(redXmlFile);

        final RedXmlInProjectEditorChangesCollector collector = new RedXmlInProjectEditorChangesCollector(redXmlFile,
                new Path(PROJECT_NAME + "/a"), Optional.<IPath> absent());
        final Optional<Change> change = collector.collect();

        assertThat(change.isPresent()).isFalse();
    }

    @Test
    public void noChangeIsCollected_whenRemovedResourceDoesNotAffectExcludedFolders() throws Exception {
        final IFile redXmlFile = projectProvider.getFile(new Path("red.xml"));
        Editors.openInProjectEditor(redXmlFile);

        final RedXmlInFileChangesCollector collector = new RedXmlInFileChangesCollector(redXmlFile,
                new Path(PROJECT_NAME + "/x"), Optional.<IPath> absent());

        assertThat(collector.collect().isPresent()).isFalse();
    }

    @Test
    public void noChangeIsCollected_whenMovedResourceDoesNotAffectExcludedFolders() throws Exception {
        final IFile redXmlFile = projectProvider.getFile(new Path("red.xml"));
        Editors.openInProjectEditor(redXmlFile);

        final RedXmlInFileChangesCollector collector = new RedXmlInFileChangesCollector(redXmlFile,
                new Path(PROJECT_NAME + "/x"), Optional.<IPath> of(new Path(PROJECT_NAME + "/renamed")));
        final Optional<Change> change = collector.collect();

        assertThat(change.isPresent()).isFalse();
    }

    @Test
    public void changeIsCollected_whenFileIsOpenedInProjectConfigEditorAndResourceRemoveAffectsExcludedPaths()
            throws Exception {
        final IFile redXmlFile = projectProvider.getFile(new Path("red.xml"));
        final IEditorPart editor = Editors.openInProjectEditor(redXmlFile);

        final RedXmlInProjectEditorChangesCollector collector = new RedXmlInProjectEditorChangesCollector(redXmlFile,
                new Path(PROJECT_NAME + "/a"), Optional.<IPath> absent());
        final Optional<Change> change = collector.collect();

        assertThat(change.isPresent()).isTrue();

        change.get().perform(new NullProgressMonitor());

        assertThat(editor.isDirty()).isTrue();

        // no change in file yet
        final RedEclipseProjectConfigReader reader = new RedEclipseProjectConfigReader();
        assertThat(reader.readConfiguration(redXmlFile).getExcludedPath()).containsOnly(ExcludedFolderPath.create("a"),
                ExcludedFolderPath.create("a/b"), ExcludedFolderPath.create("c"));

        // after saving the change should be written to the file
        editor.doSave(new NullProgressMonitor());
        redXmlFile.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
        assertThat(reader.readConfiguration(redXmlFile).getExcludedPath()).containsOnly(ExcludedFolderPath.create("c"));
    }

    @Test
    public void changeIsCollected_whenFileIsOpenedInProjectConfigEditorAndResourceMoveAffectsExcludedPaths()
            throws Exception {
        final IFile redXmlFile = projectProvider.getFile(new Path("red.xml"));
        final IEditorPart editor = Editors.openInProjectEditor(redXmlFile);

        final RedXmlInProjectEditorChangesCollector collector = new RedXmlInProjectEditorChangesCollector(redXmlFile,
                new Path(PROJECT_NAME + "/a"), Optional.<IPath> of(new Path(PROJECT_NAME + "/moved")));
        final Optional<Change> change = collector.collect();

        assertThat(change.isPresent()).isTrue();

        change.get().perform(new NullProgressMonitor());

        assertThat(editor.isDirty()).isTrue();

        // no change in file yet
        final RedEclipseProjectConfigReader reader = new RedEclipseProjectConfigReader();
        assertThat(reader.readConfiguration(redXmlFile).getExcludedPath()).containsOnly(ExcludedFolderPath.create("a"),
                ExcludedFolderPath.create("a/b"), ExcludedFolderPath.create("c"));

        // after saving the change should be written to the file
        editor.doSave(new NullProgressMonitor());
        redXmlFile.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
        assertThat(reader.readConfiguration(redXmlFile).getExcludedPath()).containsOnly(
                ExcludedFolderPath.create("moved"), ExcludedFolderPath.create("moved/b"),
                ExcludedFolderPath.create("c"));
    }
}

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
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.ExcludedFolderPath;
import org.robotframework.ide.eclipse.main.plugin.project.RedEclipseProjectConfigReader;
import org.robotframework.red.junit.ProjectProvider;

import com.google.common.base.Optional;

public class RedXmlInFileChangesCollectorTest {

    private static final String PROJECT_NAME = RedXmlInFileChangesCollectorTest.class.getSimpleName();

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

    @Test
    public void noChangeIsCollected_whenRemovedResourceDoesNotAffectExcludedFolders() {
        final IFile redXmlFile = projectProvider.getFile(new Path("red.xml"));
        
        final RedXmlInFileChangesCollector collector = new RedXmlInFileChangesCollector(redXmlFile,
                new Path(PROJECT_NAME + "/x"), Optional.<IPath> absent());

        assertThat(collector.collect().isPresent()).isFalse();
    }

    @Test
    public void noChangeIsCollected_whenMovedResourceDoesNotAffectExcludedFolders() {
        final IFile redXmlFile = projectProvider.getFile(new Path("red.xml"));

        final RedXmlInFileChangesCollector collector = new RedXmlInFileChangesCollector(redXmlFile,
                new Path(PROJECT_NAME + "/x"), Optional.<IPath> of(new Path(PROJECT_NAME + "/renamed")));
        final Optional<Change> change = collector.collect();

        assertThat(change.isPresent()).isFalse();
    }

    @Test
    public void textFileChangeIsCollected_whenRemovedResourceAffectsExcludedFolders() throws Exception {
        final IFile redXmlFile = projectProvider.getFile(new Path("red.xml"));

        final RedXmlInFileChangesCollector collector = new RedXmlInFileChangesCollector(redXmlFile,
                new Path(PROJECT_NAME + "/a"), Optional.<IPath> absent());
        final Optional<Change> change = collector.collect();

        assertThat(change.isPresent()).isTrue();

        change.get().perform(new NullProgressMonitor());
        final RobotProjectConfig config = new RedEclipseProjectConfigReader().readConfiguration(redXmlFile);
        assertThat(config.getExcludedPath()).containsOnly(ExcludedFolderPath.create("c"));
    }

    @Test
    public void textFileChangeIsCollected_whenMovedResourceAffectsExcludedFolders() throws Exception {
        final IFile redXmlFile = projectProvider.getFile(new Path("red.xml"));

        final RedXmlInFileChangesCollector collector = new RedXmlInFileChangesCollector(redXmlFile,
                new Path(PROJECT_NAME + "/a"), Optional.<IPath> of(new Path(PROJECT_NAME + "/moved")));
        final Optional<Change> change = collector.collect();

        assertThat(change.isPresent()).isTrue();

        change.get().perform(new NullProgressMonitor());
        final RobotProjectConfig config = new RedEclipseProjectConfigReader().readConfiguration(redXmlFile);
        assertThat(config.getExcludedPath()).containsOnly(ExcludedFolderPath.create("moved"),
                ExcludedFolderPath.create("moved/b"), ExcludedFolderPath.create("c"));
    }
}

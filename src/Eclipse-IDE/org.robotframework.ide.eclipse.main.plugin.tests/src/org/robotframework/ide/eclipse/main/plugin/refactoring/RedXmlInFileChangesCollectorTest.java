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
import org.junit.Rule;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ExcludedFolderPath;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigReader;
import org.robotframework.red.junit.ProjectProvider;

import com.google.common.base.Optional;

public class RedXmlInFileChangesCollectorTest {

    private static final String PROJECT_NAME = RedXmlInFileChangesCollectorTest.class.getSimpleName();

    @Rule
    public ProjectProvider projectProvider = new ProjectProvider(PROJECT_NAME);

    @Before
    public void beforeTest() throws Exception {
        projectProvider.addRobotNature();
        final RobotProjectConfig config = new RobotProjectConfig();
        config.addExcludedPath(new Path("a"));
        config.addExcludedPath(new Path("a/b"));
        config.addExcludedPath(new Path("c"));
        projectProvider.configure(config);

        projectProvider.createDir(new Path("a"));
        projectProvider.createDir(new Path("a/b"));
        projectProvider.createDir(new Path("c"));
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
        final RobotProjectConfigReader reader = new RobotProjectConfigReader();
        final RobotProjectConfig config = reader.readConfiguration(redXmlFile);
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
        final RobotProjectConfigReader reader = new RobotProjectConfigReader();
        final RobotProjectConfig config = reader.readConfiguration(redXmlFile);
        assertThat(config.getExcludedPath()).containsOnly(ExcludedFolderPath.create("moved"),
                ExcludedFolderPath.create("moved/b"), ExcludedFolderPath.create("c"));
    }
}

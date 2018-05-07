/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.refactoring;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ltk.core.refactoring.Change;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;
import org.robotframework.ide.eclipse.main.plugin.RedWorkspace;
import org.robotframework.red.junit.ProjectProvider;
import org.robotframework.red.junit.ResourceCreator;

public class LinkedResourceLocationChangeTest {

    public static ProjectProvider projectProvider = new ProjectProvider(
            LinkedResourceLocationChangeTest.class);

    public static TemporaryFolder tempFolder = new TemporaryFolder();

    @ClassRule
    public static TestRule rulesChain = RuleChain.outerRule(projectProvider).around(tempFolder);

    @Rule
    public ResourceCreator resourceCreator = new ResourceCreator();

    @BeforeClass
    public static void beforeSuite() throws Exception {
        projectProvider.createDir("directory");
    }

    @Test
    public void checkChangeName() {
        final IWorkspace workspace = projectProvider.getProject().getWorkspace();

        final LinkedResourceLocationChange change = new LinkedResourceLocationChange(workspace);

        assertThat(change.getName()).isEqualTo("Linked resource locations will be refreshed");
        assertThat(change.getModifiedElement()).isSameAs(workspace);
    }

    @Test
    public void linkedResourceLocationsAreRefreshed_whenChangeIsPerformed() throws Exception {
        final IWorkspace workspace = projectProvider.getProject().getWorkspace();
        final File nonWorkspaceFile = tempFolder.newFile("linkedRes.robot");
        resourceCreator.createLink(nonWorkspaceFile.toURI(), projectProvider.getFile(nonWorkspaceFile.getName()));

        assertThat(findResource(workspace, nonWorkspaceFile)).isEqualTo(projectProvider.getFile("linkedRes.robot"));

        moveResource("linkedRes.robot",
                new Path("/LinkedResourceLocationChangeTest/directory").append(nonWorkspaceFile.getName()));

        assertThat(findResource(workspace, nonWorkspaceFile)).isNull();

        final LinkedResourceLocationChange change = new LinkedResourceLocationChange(workspace, 0);
        change.initializeValidationData(null);
        assertThat(change.isValid(null).isOK()).isTrue();
        final Change undoOperation = change.perform(null);
        change.getLocationsUpdateJob().join();

        assertThat(findResource(workspace, nonWorkspaceFile))
                .isEqualTo(projectProvider.getFile("directory/linkedRes.robot"));

        moveResource("directory/linkedRes.robot",
                new Path("/LinkedResourceLocationChangeTest").append(nonWorkspaceFile.getName()));

        assertThat(findResource(workspace, nonWorkspaceFile)).isNull();

        undoOperation.perform(null);
        ((LinkedResourceLocationChange) undoOperation).getLocationsUpdateJob().join();

        assertThat(findResource(workspace, nonWorkspaceFile)).isEqualTo(projectProvider.getFile("linkedRes.robot"));
    }

    private IResource findResource(final IWorkspace workspace, final File file) {
        final RedWorkspace redWorkspace = new RedWorkspace(workspace.getRoot());
        return redWorkspace.forUri(file.toURI());
    }

    private void moveResource(final String sourcePath, final IPath destination) throws CoreException {
        projectProvider.getFile(sourcePath).move(destination, IResource.KEEP_HISTORY | IResource.SHALLOW, null);
    }
}

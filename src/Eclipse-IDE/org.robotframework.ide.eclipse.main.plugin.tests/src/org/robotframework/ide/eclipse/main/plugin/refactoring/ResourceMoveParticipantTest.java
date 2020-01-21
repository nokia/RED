/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.refactoring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.io.File;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;
import org.robotframework.red.junit.jupiter.RedTempDirectory;
import org.robotframework.red.junit.jupiter.StatefulProject;

@ExtendWith({ ProjectExtension.class, RedTempDirectory.class })
public class ResourceMoveParticipantTest {

    @Project(cleanUpAfterEach = true)
    static StatefulProject project;

    @TempDir
    static File tempFolder;

    @Test
    public void checkLabelTest() {
        final ResourceMoveParticipant participant = new ResourceMoveParticipant();

        assertThat(participant.getName()).isEqualTo("Robot resource move participant");
    }

    @Test
    public void statusIsAlwaysOk() {
        final ResourceMoveParticipant participant = new ResourceMoveParticipant();

        assertThat(participant.checkConditions(new NullProgressMonitor(), mock(CheckConditionsContext.class)).isOK())
                .isTrue();
    }

    @Test
    public void changeIsNotCreatedForNotLinkedResources() throws Exception {
        final ResourceMoveParticipant participant = new ResourceMoveParticipant();
        participant.initialize(project.createFile("res.robot"));

        assertThat(participant.createChange(new NullProgressMonitor())).isNull();
    }

    @Test
    public void changeIsCreatedForLinkedResources() throws Exception {
        final File nonWorkspaceFile = RedTempDirectory.createNewFile(tempFolder, "res.robot");
        project.createFileLink("linkedRes.robot", nonWorkspaceFile.toURI());
        final ResourceMoveParticipant participant = new ResourceMoveParticipant();
        participant.initialize(project.getFile("linkedRes.robot"));

        assertThat(participant.createChange(new NullProgressMonitor()))
                .isExactlyInstanceOf(LinkedResourceLocationChange.class);
    }

    // TODO : write more tests when this participant will do more than collecting changes for
    // red.xml file. This single thing is already tested by other junits.
}

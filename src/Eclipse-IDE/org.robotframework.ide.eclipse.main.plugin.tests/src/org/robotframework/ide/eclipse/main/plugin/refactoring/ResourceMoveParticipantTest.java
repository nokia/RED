/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.refactoring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;
import org.robotframework.red.junit.ProjectProvider;
import org.robotframework.red.junit.ResourceCreator;

public class ResourceMoveParticipantTest {

    public static ProjectProvider projectProvider = new ProjectProvider(ResourceMoveParticipantTest.class);

    public static TemporaryFolder tempFolder = new TemporaryFolder();

    @ClassRule
    public static TestRule rulesChain = RuleChain.outerRule(projectProvider).around(tempFolder);

    @Rule
    public ResourceCreator resourceCreator = new ResourceCreator();

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
        participant.initialize(projectProvider.createFile("res.robot"));

        assertThat(participant.createChange(new NullProgressMonitor())).isNull();
    }

    @Test
    public void changeIsCreatedForLinkedResources() throws Exception {
        final File nonWorkspaceFile = tempFolder.newFile("res.robot");
        final IFile linkedFile = projectProvider.getFile("linkedRes.robot");
        resourceCreator.createLink(nonWorkspaceFile.toURI(), linkedFile);
        final ResourceMoveParticipant participant = new ResourceMoveParticipant();
        participant.initialize(linkedFile);

        assertThat(participant.createChange(new NullProgressMonitor()))
                .isExactlyInstanceOf(LinkedResourceLocationChange.class);
    }

    // TODO : write more tests when this participant will do more than collecting changes for
    // red.xml file. This single thing is already tested by other junits.
}

/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.junit.ClassRule;
import org.junit.Test;
import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.RobotParser;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.red.junit.ProjectProvider;

public class RedSourceDoubleClickStrategyTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(RedSourceDoubleClickStrategyTest.class);

    @Test
    public void partOfCellIsHighlighted_whenThereIsVariableInCell_1() throws InterruptedException {
        final RobotDocument doc = createDocument();
        final int offset = doc.getNewestModel().getFileContent().get(3).getLineElements().get(3).getStartOffset();

        final IRegion region = new RedSourceDoubleClickStrategy(false).findWord(doc, offset + 1);
        assertThat(region).isEqualTo(new Region(offset, 3));
    }

    @Test
    public void partOfCellIsHighlighted_whenThereIsVariableInCell_2() throws InterruptedException {
        final RobotDocument doc = createDocument();
        final int offset = doc.getNewestModel().getFileContent().get(3).getLineElements().get(3).getStartOffset();

        final IRegion region = new RedSourceDoubleClickStrategy(false).findWord(doc, offset + 6);
        assertThat(region).isEqualTo(new Region(offset + 5, 4));

    }

    @Test
    public void partOfCellIsHighlighted_whenThereIsVariableInCell_3() throws InterruptedException {
        final RobotDocument doc = createDocument();
        final int offset = doc.getNewestModel().getFileContent().get(3).getLineElements().get(3).getStartOffset();

        final IRegion region = new RedSourceDoubleClickStrategy(false).findWord(doc, offset + 12);
        assertThat(region).isEqualTo(new Region(offset + 10, 5));
    }

    @Test
    public void wholeCellIsHighlighted_whenThereIsNoVariableInCell() throws InterruptedException {
        final RobotDocument doc = createDocument();
        final int offset = doc.getNewestModel().getFileContent().get(2).getLineElements().get(1).getStartOffset();

        final IRegion region = new RedSourceDoubleClickStrategy(false).findWord(doc, offset + 1);
        assertThat(region).isEqualTo(new Region(offset, 26));
    }

    @Test
    public void onlySingleCharacterIsHighlighted_whenInSeparator() throws InterruptedException {
        final RobotDocument doc = createDocument();
        final int offset = doc.getNewestModel().getFileContent().get(2).getLineElements().get(0).getStartOffset();

        final IRegion region = new RedSourceDoubleClickStrategy(false).findWord(doc, offset + 1);
        assertThat(region).isEqualTo(new Region(offset, 1));
    }

    private static RobotDocument createDocument() {
        return createDocument("*** Test Cases ***", "test", "    keyword with spaces inside", "    kw    abc${var1}defgh");
    }

    private static RobotDocument createDocument(final String... lines) {
        final RobotProject robotProject = new RobotModel().createRobotProject(projectProvider.getProject());

        final RobotParser parser = new RobotParser(robotProject.getRobotProjectHolder(), new RobotVersion(3, 1));
        final File file = new File("file.robot");

        final RobotDocument document = new RobotDocument(parser, file);
        document.set(String.join("\n", lines));
        return document;
    }
}

/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.formatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.red.junit.jupiter.ProjectExtension.getFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.robotframework.ide.eclipse.main.plugin.mockdocument.Document;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;

import com.google.common.base.Charsets;

@ExtendWith(ProjectExtension.class)
public class SuiteSourceEditorDifferenceFinderTest {

    @Project(files = { "file.txt" })
    static IProject project;

    private static IFile file;

    @BeforeAll
    public static void beforeSuite() throws Exception {
        file = getFile(project, "file.txt");
    }

    @Test
    public void changedLinesAreEmpty_whenCurrentDocumentIsNotChanged() throws Exception {
        setFileContentsKeepingHistory(file, "line1", "line2", "line3");

        final Document currentDocument = new Document("line1", "line2", "line3");

        assertThat(calculateChangedLines(currentDocument)).isEmpty();
    }

    @Test
    public void changedLinesAreEmpty_whenSeveralLinesAreDeletedInCurrentDocument() throws Exception {
        setFileContentsKeepingHistory(file, "line1", "line2", "line3");

        final Document currentDocument = new Document("line1", "line3");

        assertThat(calculateChangedLines(currentDocument)).isEmpty();
    }

    @Test
    public void changedLinesAreNotEmpty_whenCurrentDocumentIsEmpty() throws Exception {
        setFileContentsKeepingHistory(file, "line1", "line2");

        final Document currentDocument = new Document();

        assertThat(calculateChangedLines(currentDocument)).containsExactly(0);
    }

    @Test
    public void changedLinesAreNotEmpty_whenNewLinesAreAddedInCurrentDocument() throws Exception {
        setFileContentsKeepingHistory(file, "line1", "line2", "line3");

        final Document currentDocument = new Document("new1", "line1", "line2", "new2", "line3", "new3", "new4");

        assertThat(calculateChangedLines(currentDocument)).containsExactly(0, 3, 5, 6);
    }

    @Test
    public void changedLinesAreNotEmpty_whenWholeDocumentIsChanged() throws Exception {
        setFileContentsKeepingHistory(file, "line1", "line2", "line3");

        final Document currentDocument = new Document("new1", "new2", "new3", "new4");

        assertThat(calculateChangedLines(currentDocument)).containsExactly(0, 1, 2, 3);
    }

    @Test
    public void changedLinesAreNotEmpty_whenSeveralLinesAreChangedInCurrentDocument() throws Exception {
        setFileContentsKeepingHistory(file, "line1", "line2", "line3", "line4", "endLine");

        final Document currentDocument = new Document("line1", "updated", "line3 appended", "line4", " endLine   ");

        assertThat(calculateChangedLines(currentDocument)).containsExactly(1, 2, 4);
    }

    @Test
    public void changedLinesAreNotEmpty_whenSeveralLinesAreChangedAndDeletedInCurrentDocument() throws Exception {
        setFileContentsKeepingHistory(file, "line1", "line2", "line3", "line4", "line5", "endLine");

        final Document currentDocument = new Document("line1", " A B C ", "line4", "end");

        assertThat(calculateChangedLines(currentDocument)).containsExactly(1, 3);
    }

    @Test
    public void changedLinesAreNotEmpty_whenTwoLinesSwitchedPlacesInCurrentDocument() throws Exception {
        setFileContentsKeepingHistory(file, "line1", "line2", "line3", "line4", "line5");

        final Document currentDocument = new Document("line1", "line3", "line2", "line4", "line5");

        assertThat(calculateChangedLines(currentDocument)).containsExactly(1);
    }

    private void setFileContentsKeepingHistory(final IFile file, final String... lines) throws Exception {
        try (InputStream source = new ByteArrayInputStream(String.join("\n", lines).getBytes(Charsets.UTF_8))) {
            file.setContents(source, true, true, null);
        }
    }

    private List<Integer> calculateChangedLines(final Document currentDocument) {
        return SuiteSourceEditorDifferenceFinder.calculateChangedLines(file, currentDocument, null);
    }
}

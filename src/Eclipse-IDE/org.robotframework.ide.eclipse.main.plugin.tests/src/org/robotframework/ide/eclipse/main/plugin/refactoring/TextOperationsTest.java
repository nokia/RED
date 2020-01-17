/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.refactoring;


import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.red.junit.jupiter.ProjectExtension.createFile;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.IDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.FileRegion;
import org.rf.ide.core.testdata.text.read.EndOfLineBuilder.EndOfLineTypes;
import org.robotframework.ide.eclipse.main.plugin.mockdocument.Document;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;

@ExtendWith(ProjectExtension.class)
public class TextOperationsTest {

    @Project
    static IProject project;

    @Test
    public void singleLineRegionIsProperlyTranslated_whenFileWithLfsOnlyIsGiven() throws Exception {
        final IFile file = createFile(project, "file.txt", "line1", "line2", "line3");

        final FileRegion region = new FileRegion(new FilePosition(2, 1), new FilePosition(2, 4));
        final FileRegion affectedRegion = TextOperations.getAffectedRegion(region, file);

        assertThat(affectedRegion).isEqualTo(new FileRegion(new FilePosition(2, 1, 6), new FilePosition(2, 4, 9)));
    }

    @Test
    public void multilineRegionIsProperlyTranslated_whenFileWithLfsOnlyIsGiven() throws Exception {
        final IFile file = createFile(project, "file.txt", "line1", "line2", "line3");

        final FileRegion region = new FileRegion(new FilePosition(1, 2), new FilePosition(3, 4));
        final FileRegion affectedRegion = TextOperations.getAffectedRegion(region, file);

        assertThat(affectedRegion).isEqualTo(new FileRegion(new FilePosition(1, 2, 1), new FilePosition(3, 4, 15)));
    }

    @Test
    public void singleLineRegionIsProperlyTranslated_whenFileWithCrLfsOnlyIsGiven() throws Exception {
        final IFile file = createFile(project, "file.txt", EndOfLineTypes.CRLF, "line1", "line2", "line3");

        final FileRegion region = new FileRegion(new FilePosition(2, 1), new FilePosition(2, 4));
        final FileRegion affectedRegion = TextOperations.getAffectedRegion(region, file);

        assertThat(affectedRegion).isEqualTo(new FileRegion(new FilePosition(2, 1, 7), new FilePosition(2, 4, 10)));
    }

    @Test
    public void multilineRegionIsProperlyTranslated_whenFileWithCrLfsOnlyIsGiven() throws Exception {
        final IFile file = createFile(project, "file.txt", EndOfLineTypes.CRLF, "line1", "line2", "line3");

        final FileRegion region = new FileRegion(new FilePosition(1, 2), new FilePosition(3, 4));
        final FileRegion affectedRegion = TextOperations.getAffectedRegion(region, file);

        assertThat(affectedRegion).isEqualTo(new FileRegion(new FilePosition(1, 2, 1), new FilePosition(3, 4, 17)));
    }

    @Test
    public void singleLineRegionIsProperlyTranslated_whenFileWithMixedCrAndCrLfsOnlyIsGiven() throws Exception {
        final IFile file = createFile(project, "file.txt", EndOfLineTypes.CRLF, "line1", "line2", "line3");

        final FileRegion region = new FileRegion(new FilePosition(2, 1), new FilePosition(2, 4));
        final FileRegion affectedRegion = TextOperations.getAffectedRegion(region, file);

        assertThat(affectedRegion).isEqualTo(new FileRegion(new FilePosition(2, 1, 7), new FilePosition(2, 4, 10)));
    }

    @Test
    public void multilineRegionIsProperlyTranslated_whenFileWithMixedCrAndCrLfsOnlyIsGiven() throws Exception {
        final IFile file = createFile(project, "file.txt", EndOfLineTypes.LF, "line1\r", "line2", "line3");

        final FileRegion region = new FileRegion(new FilePosition(1, 2), new FilePosition(3, 4));
        final FileRegion affectedRegion = TextOperations.getAffectedRegion(region, file);

        assertThat(affectedRegion).isEqualTo(new FileRegion(new FilePosition(1, 2, 1), new FilePosition(3, 4, 16)));
    }

    @Test
    public void singleLineRegionIsProperlyTranslated_whenDocumentWithLfsOnlyIsGiven() throws Exception {
        final IDocument document = new Document("line1\nline2\nline3");

        final FileRegion region = new FileRegion(new FilePosition(2, 1), new FilePosition(2, 4));
        final FileRegion affectedRegion = TextOperations.getAffectedRegion(region, document);

        assertThat(affectedRegion).isEqualTo(new FileRegion(new FilePosition(2, 1, 6), new FilePosition(2, 4, 9)));
    }

    @Test
    public void multilineRegionIsProperlyTranslated_whenDocumentWithLfsOnlyIsGiven() throws Exception {
        final IDocument document = new Document("line1\nline2\nline3");

        final FileRegion region = new FileRegion(new FilePosition(1, 2), new FilePosition(3, 4));
        final FileRegion affectedRegion = TextOperations.getAffectedRegion(region, document);

        assertThat(affectedRegion).isEqualTo(new FileRegion(new FilePosition(1, 2, 1), new FilePosition(3, 4, 15)));
    }

    @Test
    public void singleLineRegionIsProperlyTranslated_whenDocumentWithCrLfsOnlyIsGiven() throws Exception {
        final IDocument document = new Document("line1\r\nline2\r\nline3");

        final FileRegion region = new FileRegion(new FilePosition(2, 1), new FilePosition(2, 4));
        final FileRegion affectedRegion = TextOperations.getAffectedRegion(region, document);

        assertThat(affectedRegion).isEqualTo(new FileRegion(new FilePosition(2, 1, 7), new FilePosition(2, 4, 10)));
    }

    @Test
    public void multilineRegionIsProperlyTranslated_whenDocumentWithCrLfsOnlyIsGiven() throws Exception {
        final IDocument document = new Document("line1\r\nline2\r\nline3");

        final FileRegion region = new FileRegion(new FilePosition(1, 2), new FilePosition(3, 4));
        final FileRegion affectedRegion = TextOperations.getAffectedRegion(region, document);

        assertThat(affectedRegion).isEqualTo(new FileRegion(new FilePosition(1, 2, 1), new FilePosition(3, 4, 17)));
    }

    @Test
    public void singleLineRegionIsProperlyTranslated_whenDocumentWithMixedCrAndCrLfsOnlyIsGiven() throws Exception {
        final IDocument document = new Document("line1\r\nline2\nline3");

        final FileRegion region = new FileRegion(new FilePosition(2, 1), new FilePosition(2, 4));
        final FileRegion affectedRegion = TextOperations.getAffectedRegion(region, document);

        assertThat(affectedRegion).isEqualTo(new FileRegion(new FilePosition(2, 1, 7), new FilePosition(2, 4, 10)));
    }

    @Test
    public void multilineRegionIsProperlyTranslated_whenDocumentFileWithMixedCrAndCrLfsOnlyIsGiven() throws Exception {
        final IDocument document = new Document("line1\r\nline2\nline3");

        final FileRegion region = new FileRegion(new FilePosition(1, 2), new FilePosition(3, 4));
        final FileRegion affectedRegion = TextOperations.getAffectedRegion(region, document);

        assertThat(affectedRegion).isEqualTo(new FileRegion(new FilePosition(1, 2, 1), new FilePosition(3, 4, 16)));
    }
}

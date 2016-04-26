/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.refactoring;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig;
import org.robotframework.red.junit.ProjectProvider;

import com.google.common.base.Optional;

public class RedXmlEditsCollectorTest {

    private static final String PROJECT_NAME = RedXmlEditsCollectorTest.class.getSimpleName();
    
    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(PROJECT_NAME);

    @BeforeClass
    public static void beforeSuite() throws Exception {
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
    public void deleteEditsAreReturnedForDocument_whenPathAfterRefactoringDoesNotExist() throws Exception {
        final Document document = new Document(projectProvider.getFileContent(new Path("red.xml")));
        final String documentContent = document.get();

        final RedXmlEditsCollector editsCollector = new RedXmlEditsCollector(new Path(PROJECT_NAME + "/a"),
                Optional.<IPath> absent());

        final List<TextEdit> edits = editsCollector.collectEditsInExcludedPaths(PROJECT_NAME, document);
        assertThat(edits).hasSize(2);

        assertThat(edits.get(0)).isInstanceOf(DeleteEdit.class);
        assertThat(editContent(documentContent, edits.get(0))).contains("<excludedPath path=\"a\"/>");

        assertThat(edits.get(1)).isInstanceOf(DeleteEdit.class);
        assertThat(editContent(documentContent, edits.get(1))).contains("<excludedPath path=\"a/b\"/>");
    }

    @Test
    public void noDeleteEditsAreReturnedForDocument_whenExcludedPathsAreNotRelatedToDeletedOne() throws Exception {
        final Document document = new Document(projectProvider.getFileContent(new Path("red.xml")));

        final RedXmlEditsCollector editsCollector = new RedXmlEditsCollector(new Path(PROJECT_NAME + "/x"),
                Optional.<IPath> absent());

        assertThat(editsCollector.collectEditsInExcludedPaths(PROJECT_NAME, document)).isEmpty();
    }

    @Test
    public void noDeleteEditsAreReturnedForDocument_whenFileWasRemovedInSomeOtherProject() throws Exception {
        final Document document = new Document(projectProvider.getFileContent(new Path("red.xml")));

        final RedXmlEditsCollector editsCollector = new RedXmlEditsCollector(new Path("otherProject/a"),
                Optional.<IPath> absent());

        assertThat(editsCollector.collectEditsInExcludedPaths(PROJECT_NAME, document)).isEmpty();
    }

    @Test
    public void replaceEditsAreReturnedForDocument_whenPathAfterRefactoringChanges() throws Exception {
        final Document document = new Document(projectProvider.getFileContent(new Path("red.xml")));
        final String documentContent = document.get();

        final RedXmlEditsCollector editsCollector = new RedXmlEditsCollector(new Path(PROJECT_NAME + "/a"),
                Optional.<IPath> of(new Path(PROJECT_NAME + "/renamed")));

        final List<TextEdit> edits = editsCollector.collectEditsInExcludedPaths(PROJECT_NAME, document);
        assertThat(edits).hasSize(2);

        assertThat(edits.get(0)).isInstanceOf(ReplaceEdit.class);
        assertThat(editContent(documentContent, edits.get(0))).isEqualTo("<excludedPath path=\"a\"/>");
        assertThat(((ReplaceEdit) edits.get(0)).getText()).isEqualTo("<excludedPath path=\"renamed\"/>");

        assertThat(edits.get(1)).isInstanceOf(ReplaceEdit.class);
        assertThat(editContent(documentContent, edits.get(1))).isEqualTo("<excludedPath path=\"a/b\"/>");
        assertThat(((ReplaceEdit) edits.get(1)).getText()).isEqualTo("<excludedPath path=\"renamed/b\"/>");
    }

    @Test
    public void noReplaceEditsAreReturnedForDocument_whenExcludedPathsAreNotRelatedToModifiedOne() throws Exception {
        final Document document = new Document(projectProvider.getFileContent(new Path("red.xml")));

        final RedXmlEditsCollector editsCollector = new RedXmlEditsCollector(new Path(PROJECT_NAME + "/x"),
                Optional.<IPath> of(new Path(PROJECT_NAME + "/renamed")));

        assertThat(editsCollector.collectEditsInExcludedPaths(PROJECT_NAME, document)).isEmpty();
    }

    @Test
    public void noReplaceEditsAreReturnedForDocument_whenFileWasRemovedInSomeOtherProject() throws Exception {
        final Document document = new Document(projectProvider.getFileContent(new Path("red.xml")));

        final RedXmlEditsCollector editsCollector = new RedXmlEditsCollector(new Path("otherProject/a"),
                Optional.<IPath> of(new Path(PROJECT_NAME + "/renamed")));

        assertThat(editsCollector.collectEditsInExcludedPaths(PROJECT_NAME, document)).isEmpty();
    }

    @Test
    public void deleteEditsAreReturnedForFile_whenPathAfterRefactoringDoesNotExist() throws Exception {
        final IFile file = projectProvider.getFile(new Path("red.xml"));
        final String fileContent = projectProvider.getFileContent("red.xml");

        final RedXmlEditsCollector editsCollector = new RedXmlEditsCollector(new Path(PROJECT_NAME + "/a"),
                Optional.<IPath> absent());

        final List<TextEdit> edits = editsCollector.collectEditsInExcludedPaths(PROJECT_NAME, file);
        assertThat(edits).hasSize(2);

        assertThat(edits.get(0)).isInstanceOf(DeleteEdit.class);
        assertThat(editContent(fileContent, edits.get(0))).contains("<excludedPath path=\"a\"/>");

        assertThat(edits.get(1)).isInstanceOf(DeleteEdit.class);
        assertThat(editContent(fileContent, edits.get(1))).contains("<excludedPath path=\"a/b\"/>");
    }

    @Test
    public void noDeleteEditsAreReturnedForFile_whenExcludedPathsAreNotRelatedToDeletedOne() throws Exception {
        final IFile file = projectProvider.getFile(new Path("red.xml"));

        final RedXmlEditsCollector editsCollector = new RedXmlEditsCollector(new Path(PROJECT_NAME + "/x"),
                Optional.<IPath> absent());

        assertThat(editsCollector.collectEditsInExcludedPaths(PROJECT_NAME, file)).isEmpty();
    }

    @Test
    public void noDeleteEditsAreReturnedForFile_whenFileWasRemovedInSomeOtherProject() throws Exception {
        final IFile file = projectProvider.getFile(new Path("red.xml"));

        final RedXmlEditsCollector editsCollector = new RedXmlEditsCollector(new Path("otherProject/a"),
                Optional.<IPath> absent());

        assertThat(editsCollector.collectEditsInExcludedPaths(PROJECT_NAME, file)).isEmpty();
    }

    @Test
    public void replaceEditsAreReturnedForFile_whenPathAfterRefactoringChanges() throws Exception {
        final IFile file = projectProvider.getFile("red.xml");
        final String fileContent = projectProvider.getFileContent("red.xml");

        final RedXmlEditsCollector editsCollector = new RedXmlEditsCollector(new Path(PROJECT_NAME + "/a"),
                Optional.<IPath> of(new Path(PROJECT_NAME + "/renamed")));

        final List<TextEdit> edits = editsCollector.collectEditsInExcludedPaths(PROJECT_NAME, file);
        assertThat(edits).hasSize(2);

        assertThat(edits.get(0)).isInstanceOf(ReplaceEdit.class);
        assertThat(editContent(fileContent, edits.get(0))).isEqualTo("<excludedPath path=\"a\"/>");
        assertThat(((ReplaceEdit) edits.get(0)).getText()).isEqualTo("<excludedPath path=\"renamed\"/>");

        assertThat(edits.get(1)).isInstanceOf(ReplaceEdit.class);
        assertThat(editContent(fileContent, edits.get(1))).isEqualTo("<excludedPath path=\"a/b\"/>");
        assertThat(((ReplaceEdit) edits.get(1)).getText()).isEqualTo("<excludedPath path=\"renamed/b\"/>");
    }

    @Test
    public void noReplaceEditsAreReturnedForFile_whenExcludedPathsAreNotRelatedToModifiedOne() throws Exception {
        final IFile file = projectProvider.getFile(new Path("red.xml"));

        final RedXmlEditsCollector editsCollector = new RedXmlEditsCollector(new Path(PROJECT_NAME + "/x"),
                Optional.<IPath> of(new Path(PROJECT_NAME + "/renamed")));

        assertThat(editsCollector.collectEditsInExcludedPaths(PROJECT_NAME, file)).isEmpty();
    }

    @Test
    public void noReplaceEditsAreReturnedForFile_whenFileWasRemovedInSomeOtherProject() throws Exception {
        final IFile file = projectProvider.getFile(new Path("red.xml"));

        final RedXmlEditsCollector editsCollector = new RedXmlEditsCollector(new Path("otherProject/a"),
                Optional.<IPath> of(new Path(PROJECT_NAME + "/renamed")));

        assertThat(editsCollector.collectEditsInExcludedPaths(PROJECT_NAME, file)).isEmpty();
    }

    private static String editContent(final String text, final TextEdit edit) {
        return text.substring(edit.getOffset(), edit.getOffset() + edit.getLength());
    }
}

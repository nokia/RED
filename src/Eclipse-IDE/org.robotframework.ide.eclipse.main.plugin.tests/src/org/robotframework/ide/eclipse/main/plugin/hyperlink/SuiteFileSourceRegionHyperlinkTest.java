/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.hyperlink;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourceEditor;
import org.robotframework.red.junit.ProjectProvider;

public class SuiteFileSourceRegionHyperlinkTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(FileHyperlinkTest.class);

    @BeforeClass
    public static void beforeSuite() throws Exception {
        projectProvider.createFile("f.robot", "*** Test Cases ***");
    }

    @AfterClass
    public static void afterSuite() {
        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeAllEditors(false);
    }

    @Test
    public void testSuiteFileRegionHyperlinkProperties_1() {
        final RobotSuiteFile sourceModel = new RobotSuiteFileCreator().build();
        final SuiteFileSourceRegionHyperlink link = new SuiteFileSourceRegionHyperlink(new Region(20, 10), sourceModel,
                new Region(10, 3));

        assertThat(link.getTypeLabel()).isNull();
        assertThat(link.getHyperlinkRegion()).isEqualTo(new Region(20, 10));
        assertThat(link.getDestinationFile()).isSameAs(sourceModel);
        assertThat(link.getDestinationRegion()).isEqualTo(new Region(10, 3));
        assertThat(link.getHyperlinkText()).isEqualTo("Open Definition");
        assertThat(link.getLabelForCompoundHyperlinksDialog()).isEqualTo("file.robot");
        assertThat(link.additionalLabelDecoration()).isEmpty();
        assertThat(link.getImage()).isEqualTo(RedImages.getImageForFileWithExtension("robot"));
    }

    @Test
    public void testSuiteFileRegionHyperlinkProperties_2() {
        final RobotSuiteFile sourceModel = new RobotSuiteFileCreator().build();
        final SuiteFileSourceRegionHyperlink link = new SuiteFileSourceRegionHyperlink(new Region(20, 10), sourceModel,
                new Region(10, 3), "decoration");

        assertThat(link.getTypeLabel()).isNull();
        assertThat(link.getHyperlinkRegion()).isEqualTo(new Region(20, 10));
        assertThat(link.getDestinationFile()).isSameAs(sourceModel);
        assertThat(link.getDestinationRegion()).isEqualTo(new Region(10, 3));
        assertThat(link.getHyperlinkText()).isEqualTo("Open Definition");
        assertThat(link.getLabelForCompoundHyperlinksDialog()).isEqualTo("file.robot");
        assertThat(link.additionalLabelDecoration()).isEqualTo("decoration");
        assertThat(link.getImage()).isEqualTo(RedImages.getImageForFileWithExtension("robot"));
    }

    @Test
    public void testIfFileProperlyOpensInEditor() {
        final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

        assertThat(page.getEditorReferences()).isEmpty();

        final RobotSuiteFile sourceModel = new RobotModel().createSuiteFile(projectProvider.getFile("f.robot"));
        final SuiteFileSourceRegionHyperlink link = new SuiteFileSourceRegionHyperlink(new Region(20, 10), sourceModel,
                new Region(9, 5));
        link.open();
                
        assertThat(page.getEditorReferences()).hasSize(1);
        final IEditorPart editor = page.getEditorReferences()[0].getEditor(true);
        assertThat(editor).isInstanceOf(RobotFormEditor.class);
        final RobotFormEditor redEditor = (RobotFormEditor) editor;
        final IEditorPart activePart = redEditor.getActiveEditor();
        assertThat(activePart).isInstanceOf(SuiteSourceEditor.class);
        final SourceViewer viewer = ((SuiteSourceEditor) activePart).getViewer();
        final StyledText textWidget = viewer.getTextWidget();
        assertThat(textWidget.getSelectionText()).isEqualTo("Cases");
    }
}

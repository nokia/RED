/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.hyperlink;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.robotframework.red.junit.jupiter.ProjectExtension.createFile;
import static org.robotframework.red.junit.jupiter.ProjectExtension.getFile;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotContainer;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.CasesEditorPart;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;

@ExtendWith(ProjectExtension.class)
public class SuiteFileTableElementHyperlinkTest {

    @Project
    static IProject project;

    private static RobotModel robotModel;

    @BeforeAll
    public static void beforeSuite() throws Exception {
        robotModel = RedPlugin.getModelManager().getModel();

        createFile(project, "f.robot",
                "*** Test Cases ***",
                "tc1",
                "tc2",
                "  Log  a");
    }

    @AfterAll
    public static void afterSuite() {
        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeAllEditors(false);
        RedPlugin.getModelManager().dispose();
    }

    @Test
    public void testSuiteFileRegionHyperlinkProperties() {
        final RobotSuiteFile sourceModel = new RobotSuiteFileCreator().build();
        final RobotFileInternalElement element = mock(RobotFileInternalElement.class);

        final SuiteFileTableElementHyperlink link = new SuiteFileTableElementHyperlink(new Region(20, 10), sourceModel,
                element, "label");

        assertThat(link.getTypeLabel()).isNull();
        assertThat(link.getHyperlinkRegion()).isEqualTo(new Region(20, 10));
        assertThat(link.getDestinationFile()).isSameAs(sourceModel);
        assertThat(link.getDestinationElement()).isSameAs(element);
        assertThat(link.getHyperlinkText()).isEqualTo("Open Definition label");
        assertThat(link.getLabelForCompoundHyperlinksDialog()).isEqualTo("file.robot");
        assertThat(link.additionalLabelDecoration()).isEmpty();
        assertThat(link.getImage()).isEqualTo(RedImages.getImageForFileWithExtension("robot"));
    }

    @Test
    public void testIfTestCaseProperlyOpensInEditor() {
        final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

        assertThat(page.getEditorReferences()).isEmpty();

        final RobotSuiteFile sourceModel = robotModel.createSuiteFile(getFile(project, "f.robot"));
        final RobotCase testCase = sourceModel.findSection(RobotCasesSection.class).get().getChildren().get(1);

        final SuiteFileTableElementHyperlink link = new SuiteFileTableElementHyperlink(new Region(20, 10), sourceModel,
                testCase, null);
        link.open();

        assertThat(page.getEditorReferences()).hasSize(1);

        final IEditorPart editor = page.getEditorReferences()[0].getEditor(true);
        assertThat(editor).isInstanceOf(RobotFormEditor.class);
        final RobotFormEditor redEditor = (RobotFormEditor) editor;

        final IEditorPart activePart = redEditor.getActiveEditor();
        assertThat(activePart).isInstanceOf(CasesEditorPart.class);
        final CasesEditorPart casesSectionPart = (CasesEditorPart) activePart;

        casesSectionPart.getSelectionLayerAccessor().getSelectedPositions();
        final IStructuredSelection selection = (IStructuredSelection) editor.getEditorSite()
                .getSelectionProvider()
                .getSelection();
        assertThat(selection.size()).isEqualTo(1);

        // TODO : this is not required when local model i used instead of global
        ((RobotContainer) sourceModel.getParent()).getChildren().remove(sourceModel);
    }
}

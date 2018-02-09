/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.handler;

import java.util.Optional;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.IDocumentationHolder;
import org.rf.ide.core.testdata.model.ModelType;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorSources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.SelectionLayerAccessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.handler.ShowInDocViewHandler.E4ShowInDocViewHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.DocumentUtilities;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourceEditor;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.DocumentationView;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.DocumentationViewWrapper;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

/**
 * @author mmarzec
 *
 */
public class ShowInDocViewHandler extends DIParameterizedHandler<E4ShowInDocViewHandler> {

    public ShowInDocViewHandler() {
        super(E4ShowInDocViewHandler.class);
    }

    public static class E4ShowInDocViewHandler {

        @Execute
        public void showInDocView(
                @org.eclipse.e4.core.di.annotations.Optional @Named(Selections.SELECTION) final IStructuredSelection selection,
                @Named(ISources.ACTIVE_EDITOR_NAME) final RobotFormEditor editor,
                @Named(RobotEditorSources.SUITE_FILE_MODEL) final RobotSuiteFile suiteModel) {

            final DocumentationView view = initDocumentationView();
            if (view == null) {
                return;
            }

            if (selection != null) { // some kind of table view open
                final Optional<RobotFileInternalElement> selectedElement = Selections
                        .getOptionalFirstElement(selection, RobotFileInternalElement.class);
                if (selectedElement.isPresent()) {
                    final SelectionLayerAccessor selectionLayerAccessor = ((RobotFormEditor) (PlatformUI.getWorkbench()
                            .getActiveWorkbenchWindow().getActivePage().getActiveEditor())).getSelectionLayerAccessor();
                    final PositionCoordinate[] coordinates = selectionLayerAccessor.getSelectedPositions();
                    final String cellLabel = coordinates.length > 0
                            ? selectionLayerAccessor.getLabelFromCell(coordinates[0].getRowPosition(),
                                    coordinates[0].getColumnPosition())
                            : null;
                    showDoc(view, selectedElement.get(), cellLabel);
                }
            } else if (editor.getActiveEditor() instanceof SuiteSourceEditor) {
                final SuiteSourceEditor sourceEditor = (SuiteSourceEditor) editor.getActiveEditor();
                final int offset = sourceEditor.getViewer().getTextWidget().getCaretOffset();
                final Optional<? extends RobotElement> element = suiteModel.findElement(offset);
                if (element.isPresent()) {
                    String cellLabel = null;
                    try {
                        final Optional<IRegion> activeCellRegion = DocumentUtilities
                                .findCellRegion(editor.getSourceEditor().getDocument(), suiteModel.isTsvFile(), offset);
                        if (activeCellRegion.isPresent()) {
                            cellLabel = editor.getSourceEditor().getDocument().get(activeCellRegion.get().getOffset(),
                                    activeCellRegion.get().getLength());
                        }
                    } catch (final BadLocationException e) {
                        throw new IllegalStateException("Could not find selected position in document", e);
                    }
                    showDoc(view, (RobotFileInternalElement) element.get(), cellLabel);
                }
            }
        }

        private void showDoc(final DocumentationView view, final RobotFileInternalElement robotFileInternalElement,
                final String cellLabel) {
            final Object linkedElement = robotFileInternalElement.getLinkedElement();
            if (linkedElement != null && linkedElement instanceof AModelElement<?>) {
                final ModelType modelType = ((AModelElement<?>) linkedElement).getModelType();

                if (modelType == ModelType.USER_KEYWORD_EXECUTABLE_ROW
                        || modelType == ModelType.TEST_CASE_EXECUTABLE_ROW) {
                    view.setShowLibdocEnabled();
                    view.showLibdoc(robotFileInternalElement, cellLabel);
                } else if (modelType == ModelType.SUITE_DOCUMENTATION) {
                    view.showDocumentation((IDocumentationHolder) linkedElement,
                            robotFileInternalElement.getSuiteFile());
                } else {
                    RobotCodeHoldingElement<?> parent = null;
                    if (modelType == ModelType.USER_KEYWORD || modelType == ModelType.TEST_CASE) {
                        parent = (RobotCodeHoldingElement<?>) robotFileInternalElement;
                    } else {
                        final RobotElement robotElement = robotFileInternalElement.getParent();
                        if (robotElement instanceof RobotCodeHoldingElement<?>) {
                            parent = (RobotCodeHoldingElement<?>) robotFileInternalElement.getParent();
                        }
                    }
                    if (parent != null) {
                        final Optional<RobotDefinitionSetting> docSettingFromParent = parent
                                .findSetting(ModelType.TEST_CASE_DOCUMENTATION, ModelType.USER_KEYWORD_DOCUMENTATION);
                        view.showDocumentation(docSettingFromParent.orElse(null));
                    }
                }
            }
        }

        @SuppressWarnings("restriction")
        private DocumentationView initDocumentationView() {
            final IWorkbench workbench = PlatformUI.getWorkbench();
            final IWorkbenchWindow activeWorkbenchWindow = workbench.getActiveWorkbenchWindow();
            if (activeWorkbenchWindow == null) {
                return null;
            }
            final IWorkbenchPage page = activeWorkbenchWindow.getActivePage();
            if (page == null) {
                return null;
            }
            final IViewPart docViewPart = page.findView(DocumentationView.ID);
            if (docViewPart == null || !page.isPartVisible(docViewPart)) {
                try {
                    return ((DocumentationViewWrapper) page.showView(DocumentationView.ID)).getComponent();
                } catch (final PartInitException e) {
                    RedPlugin.logError("Unable to show Documentation View.", e);
                    return null;
                }
            } else {
                return ((DocumentationViewWrapper) docViewPart).getComponent();
            }
        }
    }
}

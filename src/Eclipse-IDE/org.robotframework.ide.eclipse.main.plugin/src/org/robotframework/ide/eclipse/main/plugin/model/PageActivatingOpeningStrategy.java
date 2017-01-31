/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement.DefinitionPosition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement.ElementOpenMode;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement.OpenStrategy;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionEditorPart;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor.RobotEditorOpeningException;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditorActivePageSaver;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourceEditor;

import com.google.common.base.Optional;

class PageActivatingOpeningStrategy extends OpenStrategy {

    private final IFile file;

    private final RobotFileInternalElement elementToReveal;

    PageActivatingOpeningStrategy(final RobotFileInternalElement elementToReveal) {
        this.file = elementToReveal.getSuiteFile().getFile();
        this.elementToReveal = elementToReveal;
    }

    @Override
    protected void run(final IWorkbenchPage page, final Optional<ElementOpenMode> mode,
            final String labelWhichShouldBeInSelectedCell) {

        final IEditorRegistry editorRegistry = PlatformUI.getWorkbench().getEditorRegistry();
        final IEditorDescriptor robotEditorDescriptor = editorRegistry.findEditor(RobotFormEditor.ID);

        try {
            if (mode.isPresent() && mode.get() == ElementOpenMode.OPEN_IN_TABLES) {
                openInTablePage(page, robotEditorDescriptor, labelWhichShouldBeInSelectedCell);

            } else if (mode.isPresent() && mode.get() == ElementOpenMode.OPEN_IN_SOURCE) {
                openInSourcePage(page, robotEditorDescriptor);

            } else {
                openDependingOnPreferences(page, robotEditorDescriptor, labelWhichShouldBeInSelectedCell);
            }

        } catch (final PartInitException e) {
            throw new RobotEditorOpeningException("Unable to open editor for file: " + file.getName(), e);
        }
    }

    private void openDependingOnPreferences(final IWorkbenchPage page, final IEditorDescriptor robotEditorDescriptor,
            final String labelWhichShouldBeInSelectedCell) throws PartInitException {
        final FileEditorInput editorInput = new FileEditorInput(file);

        if (isEditorAlreadyOpen(page, editorInput)) {
            final IEditorPart editor = page.openEditor(editorInput, robotEditorDescriptor.getId());
            if (editor instanceof RobotFormEditor) {
                final RobotFormEditor suiteEditor = (RobotFormEditor) editor;

                final ElementOpenMode openMode = getOpenModeForOpenedEditor(suiteEditor);
                openInMode(page, robotEditorDescriptor, openMode, labelWhichShouldBeInSelectedCell);
            }

        } else {
            final ElementOpenMode openMode = getOpenModeForClosedEditor();
            openInMode(page, robotEditorDescriptor, openMode, labelWhichShouldBeInSelectedCell);
        }
    }

    private boolean isEditorAlreadyOpen(final IWorkbenchPage page, final FileEditorInput editorInput) {
        final IEditorReference[] editors = page.findEditors(editorInput, RobotFormEditor.ID,
                IWorkbenchPage.MATCH_INPUT | IWorkbenchPage.MATCH_ID);
        return editors.length > 0;
    }

    private ElementOpenMode getOpenModeForClosedEditor() {
        final String pageToActivate = RobotFormEditorActivePageSaver.getLastActivePageId(file);

        // there is no page we stored for activate on next open
        if (pageToActivate == null) {
            return RedPlugin.getDefault().getPreferences().getElementOpenMode();
        } else {
            return pageToActivate.isEmpty() ? ElementOpenMode.OPEN_IN_SOURCE : ElementOpenMode.OPEN_IN_TABLES;
        }
    }

    private ElementOpenMode getOpenModeForOpenedEditor(final RobotFormEditor robotEditor) {
        final IEditorPart activePage = robotEditor.getActiveEditor();
        return activePage instanceof ISectionEditorPart ? ElementOpenMode.OPEN_IN_TABLES
                : ElementOpenMode.OPEN_IN_SOURCE;
    }

    private void openInMode(final IWorkbenchPage page, final IEditorDescriptor robotEditorDescriptor,
            final ElementOpenMode openMode, final String labelWhichShouldBeInSelectedCell) throws PartInitException {
        if (openMode == ElementOpenMode.OPEN_IN_TABLES) {
            openInTablePage(page, robotEditorDescriptor, labelWhichShouldBeInSelectedCell);

        } else if (openMode == ElementOpenMode.OPEN_IN_SOURCE) {
            openInSourcePage(page, robotEditorDescriptor);
        }
    }

    private void openInTablePage(final IWorkbenchPage page, final IEditorDescriptor robotEditorDescriptor,
            final String labelWhichShouldBeInSelectedCell) throws PartInitException {

        final IEditorPart editor = page.openEditor(new FileEditorInput(file), robotEditorDescriptor.getId());

        // it can be ErrorEditorPart if something went wrong
        if (editor instanceof RobotFormEditor) {
            final RobotFormEditor robotEditor = (RobotFormEditor) editor;

            final ISectionEditorPart activatedPage = robotEditor.activatePage(getSection(elementToReveal));
            activatedPage.revealElementAndFocus(elementToReveal);

            if (labelWhichShouldBeInSelectedCell != null) {
                robotEditor.getSelectionLayerAccessor().selectCellContaining(labelWhichShouldBeInSelectedCell);
            }
        }
    }

    private RobotSuiteFileSection getSection(final RobotElement element) {
        RobotElement current = element;
        while (current != null && !(current instanceof RobotSuiteFileSection)) {
            current = current.getParent();
        }
        return (RobotSuiteFileSection) current;
    }

    private void openInSourcePage(final IWorkbenchPage page, final IEditorDescriptor robotEditorDescriptor)
            throws PartInitException {

        final IEditorPart editor = page.openEditor(new FileEditorInput(file), robotEditorDescriptor.getId());

        // it can be ErrorEditorPart if something went wrong
        if (editor instanceof RobotFormEditor) {
            final RobotFormEditor robotEditor = (RobotFormEditor) editor;

            final SuiteSourceEditor activatedPage = robotEditor.activateSourcePage();
            activatedPage.setFocus();
            activatedPage.getSelectionProvider().setSelection(positionOf(elementToReveal));
        }
    }

    private static TextSelection positionOf(final RobotFileInternalElement element) {
        final DefinitionPosition position = element.getDefinitionPosition();
        return new TextSelection(position.getOffset(), position.getLength());
    }
}

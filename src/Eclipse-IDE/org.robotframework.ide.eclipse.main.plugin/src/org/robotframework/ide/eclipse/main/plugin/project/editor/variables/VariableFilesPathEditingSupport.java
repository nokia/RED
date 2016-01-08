/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.variables;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.viewers.AlwaysDeactivatingCellEditor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.robotframework.ide.eclipse.main.plugin.PathsConverter;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedVariableFile;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigEvents;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput;
import org.robotframework.red.viewers.ElementsAddingEditingSupport;


/**
 * @author Michal Anglart
 *
 */
class VariableFilesPathEditingSupport extends ElementsAddingEditingSupport {

    VariableFilesPathEditingSupport(final ColumnViewer viewer,
            final NewElementsCreator<ReferencedVariableFile> creator) {
        super(viewer, 0, creator);
    }

    @Override
    protected int getColumnShift() {
        return 1;
    }

    @Override
    protected CellEditor getCellEditor(final Object element) {
        return new AlwaysDeactivatingCellEditor((Composite) getViewer().getControl());
    }

    @Override
    protected Object getValue(final Object element) {
        return null;
    }

    @Override
    protected void setValue(final Object element, final Object value) {
        if (element instanceof ReferencedVariableFile) {
            final VariableFileCreator variableFileCreator = (VariableFileCreator) creator;
            scheduleViewerRefreshAndEditorActivation(
                    variableFileCreator.modifyExisting((ReferencedVariableFile) element), getColumnShift());
        } else {
            super.setValue(element, value);
        }
    }

    private static IEventBroker getEventBroker() {
        return PlatformUI.getWorkbench().getService(IEventBroker.class);
    }

    static class VariableFileCreator extends NewElementsCreator<ReferencedVariableFile> {

        private final Shell shell;

        private final RedProjectEditorInput editorInput;

        VariableFileCreator(final Shell shell, final RedProjectEditorInput editorInput) {
            this.shell = shell;
            this.editorInput = editorInput;
        }

        @Override
        public ReferencedVariableFile createNew() {
            final FileDialog dialog = new FileDialog(shell, SWT.OPEN | SWT.MULTI);
            dialog.setFilterExtensions(new String[] { "*.py", "*.*" });
            dialog.setFilterPath(editorInput.getRobotProject().getProject().getLocation().toPortableString());

            ReferencedVariableFile firstFile = null;

            if (dialog.open() != null) {
                final List<ReferencedVariableFile> variableFiles  = new ArrayList<>();
                final String[] chosenFiles = dialog.getFileNames();
                for (final String file : chosenFiles) {
                    final IPath path = PathsConverter.toWorkspaceRelativeIfPossible(new Path(dialog.getFilterPath()))
                            .append(file);

                    final ReferencedVariableFile variableFile = new ReferencedVariableFile();
                    variableFile.setArguments(new ArrayList<String>());
                    variableFile.setPath(path.toPortableString());
                    variableFiles.add(variableFile);
                }
                for (final ReferencedVariableFile variableFile : variableFiles) {
                    if (firstFile == null) {
                        firstFile = variableFile;
                    }
                    editorInput.getProjectConfiguration().addReferencedVariableFile(variableFile);
                }
                getEventBroker().send(RobotProjectConfigEvents.ROBOT_CONFIG_VAR_FILE_STRUCTURE_CHANGED, variableFiles);
            }
            return firstFile;
        }

        ReferencedVariableFile modifyExisting(final ReferencedVariableFile varFile) {
            final FileDialog dialog = new FileDialog(shell, SWT.OPEN | SWT.MULTI);
            dialog.setFilterExtensions(new String[] { "*.py", "*.*" });
            final IPath startingPath = PathsConverter
                    .toAbsoluteFromWorkspaceRelativeIfPossible(new Path(varFile.getPath())).removeLastSegments(1);
            dialog.setFilterPath(startingPath.toPortableString());

            final String chosenFile = dialog.open();
            if (chosenFile != null) {
                final IPath path = PathsConverter.toWorkspaceRelativeIfPossible(new Path(chosenFile));
                varFile.setPath(path.toPortableString());

                getEventBroker().send(RobotProjectConfigEvents.ROBOT_CONFIG_VAR_FILE_PATH_CHANGED, varFile);
            }
            return varFile;
        }
    }
}

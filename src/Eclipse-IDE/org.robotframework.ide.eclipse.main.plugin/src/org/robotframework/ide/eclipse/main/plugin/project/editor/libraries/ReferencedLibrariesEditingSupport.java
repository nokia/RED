/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.rf.ide.core.environment.IRuntimeEnvironment;
import org.rf.ide.core.environment.SuiteExecutor;
import org.rf.ide.core.libraries.LibrarySpecificationReader;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibraryArgumentsVariant;
import org.rf.ide.core.project.RobotProjectConfig.RemoteLocation;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.project.RedProjectConfigEventData;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigEvents;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput;
import org.robotframework.red.jface.viewers.ActivationCharPreservingTextCellEditor;
import org.robotframework.red.viewers.ElementsAddingEditingSupport;

import com.google.common.annotations.VisibleForTesting;

/**
 * @author Michal Anglart
 */
class ReferencedLibrariesEditingSupport extends ElementsAddingEditingSupport {

    private final RedProjectEditorInput editorInput;

    private final IEventBroker eventBroker;

    ReferencedLibrariesEditingSupport(final ColumnViewer viewer, final Supplier<ReferencedLibrary> elementsCreator,
            final RedProjectEditorInput editorInput, final IEventBroker eventBroker) {
        super(viewer, 0, elementsCreator);
        this.editorInput = editorInput;
        this.eventBroker = eventBroker;
    }

    @Override
    protected int getColumnShift() {
        return 1;
    }

    @Override
    protected CellEditor getCellEditor(final Object element) {
        if (element instanceof RemoteLocation || element instanceof ReferencedLibraryArgumentsVariant) {
            final Composite parent = (Composite) getViewer().getControl();
            return new ActivationCharPreservingTextCellEditor(getViewer().getColumnViewerEditor(), parent,
                    RedPlugin.DETAILS_EDITING_CONTEXT_ID);
        } else {
            return super.getCellEditor(element);
        }
    }

    @Override
    protected Object getValue(final Object element) {
        if (element instanceof RemoteLocation) {
            return ((RemoteLocation) element).getUri();
        } else if (element instanceof ReferencedLibraryArgumentsVariant) {
            return ((ReferencedLibraryArgumentsVariant) element).getArgsStream().collect(joining("::"));
        } else {
            return null;
        }
    }

    @Override
    protected void setValue(final Object element, final Object value) {
        if (element instanceof RemoteLocation || element instanceof ReferencedLibraryArgumentsVariant) {
            try {
                final String oldValue = (String) getValue(element);
                final String newValue = (String) value;

                if (!newValue.equals(oldValue)) {
                    if (element instanceof RemoteLocation) {
                        ((RemoteLocation) element).setUri(newValue);
                    } else if (element instanceof ReferencedLibraryArgumentsVariant) {
                        ((ReferencedLibraryArgumentsVariant) element).setArguments(Arrays.asList(newValue.split("::")));
                    }
                    eventBroker.send(RobotProjectConfigEvents.ROBOT_CONFIG_LIBRARIES_ARGUMENTS_CHANGED,
                            new RedProjectConfigEventData<>(editorInput.getFile(), newArrayList(element)));
                }
            } catch (final IllegalArgumentException e) {
                // uri syntax was wrong...
            }
        } else {
            super.setValue(element, value);
        }
    }

    static class ReferencedLibraryCreator implements Supplier<ReferencedLibrary> {

        private final Shell shell;

        private final RedProjectEditorInput editorInput;

        private final IEventBroker eventBroker;

        private final Supplier<IRuntimeEnvironment> environmentSupplier;

        public ReferencedLibraryCreator(final Shell shell, final RedProjectEditorInput editorInput,
                final IEventBroker eventBroker, final Supplier<IRuntimeEnvironment> environmentSupplier) {
            this.shell = shell;
            this.editorInput = editorInput;
            this.eventBroker = eventBroker;
            this.environmentSupplier = environmentSupplier;
        }

        @Override
        public ReferencedLibrary get() {
            final List<ReferencedLibrary> referencedLibraries = selectLibraries();

            ReferencedLibrary firstAdded = null;
            boolean wasAdded = false;
            for (final ReferencedLibrary library : referencedLibraries) {
                wasAdded |= editorInput.getProjectConfiguration().addReferencedLibrary(library);
                if (firstAdded == null && wasAdded) {
                    firstAdded = library;
                }
            }
            if (wasAdded) {
                eventBroker.send(RobotProjectConfigEvents.ROBOT_CONFIG_LIBRARY_ADDED_REMOVED,
                        new RedProjectConfigEventData<>(editorInput.getFile(), referencedLibraries));
            }

            return firstAdded;
        }

        private List<ReferencedLibrary> selectLibraries() {
            final FileDialog dialog = new FileDialog(shell, SWT.OPEN | SWT.MULTI);
            dialog.setFilterPath(editorInput.getRobotProject().getProject().getLocation().toOSString());
            dialog.setFilterExtensions(new String[] { "*.py", "*.jar", "*.xml", "*.*" });

            final List<ReferencedLibrary> libs = new ArrayList<>();
            final String chosenFilePath = dialog.open();
            if (chosenFilePath != null) {
                final ReferencedLibraryImporter importer = new ReferencedLibraryImporter(shell);
                final String[] chosenFiles = dialog.getFileNames();
                for (final String file : chosenFiles) {
                    // add separator when filterPath is e.g. 'D:'
                    final IPath path = new Path(dialog.getFilterPath()).addTrailingSeparator().append(file);
                    libs.addAll(importLibrary(importer, path));
                }
            }
            return libs;
        }

        @VisibleForTesting
        Collection<ReferencedLibrary> importLibrary(final ReferencedLibraryImporter importer, final IPath path) {
            if (LibrarySpecificationReader.readSpecification(path.toFile()).isPresent()) {
                return newArrayList(importer.importLibFromSpecFile(path.toFile()));
            }

            final IRuntimeEnvironment environment = environmentSupplier.get();
            return environment.getInterpreter() == SuiteExecutor.Jython
                    && ("jar".equals(path.getFileExtension().toLowerCase())
                            || "zip".equals(path.getFileExtension().toLowerCase()))
                    ? importer.importJavaLib(environment, editorInput.getRobotProject().getProject(),
                            editorInput.getProjectConfiguration(), path.toFile())
                    : importer.importPythonLib(environment, editorInput.getRobotProject().getProject(),
                            editorInput.getProjectConfiguration(), path.toFile());
        }
    }
}

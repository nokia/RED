/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor;

import java.util.List;

import javax.inject.Inject;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.tools.services.IDirtyProviderService;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerColumnsFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Section;
import org.robotframework.ide.core.executor.RobotRuntimeEnvironment;
import org.robotframework.ide.core.executor.SuiteExecutor;
import org.robotframework.ide.eclipse.main.plugin.PathsConverter;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.project.editor.JarStructureBuilder.JarClass;
import org.robotframework.ide.eclipse.main.plugin.project.editor.PythonLibStructureBuilder.PythonClass;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionFormFragment;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.ImportSettingFilePathResolver;
import org.robotframework.red.forms.RedFormToolkit;
import org.robotframework.red.viewers.Selections;

class ReferencedLibrariesFormFragment implements ISectionFormFragment {

    @Inject
    private RedFormToolkit toolkit;

    @Inject
    private IDirtyProviderService dirtyProviderService;

    @Inject
    private RedProjectEditorInput editorInput;
    
    private RobotProject currentProject;

    private TableViewer viewer;

    private Button addPythonLibButton;

    private Button addJavaLibButton;

    private Button addLibspecButton;

    private Button removeButton;

    private ControlDecoration decoration;

    @Override
    public void initialize(final Composite parent) {
        currentProject = editorInput.getRobotProject();
        
        final Section section = toolkit.createSection(parent, ExpandableComposite.EXPANDED
                | ExpandableComposite.TITLE_BAR | Section.DESCRIPTION | ExpandableComposite.TWISTIE);
        section.setText("Referenced libraries");
        section.setDescription("In this section referenced libraries can be specified. Those libraries will "
                + "be available for all suites within project.");
        GridDataFactory.fillDefaults().grab(true, true).applyTo(section);

        final Composite internalComposite = toolkit.createComposite(section);
        section.setClient(internalComposite);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(internalComposite);
        GridLayoutFactory.fillDefaults().numColumns(2).applyTo(internalComposite);

        createViewer(internalComposite);
        createButtons(internalComposite);

        setInput();
    }

    private void createViewer(final Composite parent) {
        viewer = new TableViewer(parent);
        GridDataFactory.fillDefaults().grab(true, true).span(1, 4).applyTo(viewer.getTable());
        viewer.getTable().setEnabled(false);

        viewer.setContentProvider(new ReferencedLibrariesContentProvider());
        
        ViewerColumnsFactory.newColumn("").withWidth(100)
            .shouldGrabAllTheSpaceLeft(true).withMinWidth(100)
            .labelsProvidedBy(new ReferencedLibrariesLabelProvider())
            .createFor(viewer);

        final ISelectionChangedListener selectionChangedListener = new ISelectionChangedListener() {
            @Override
            public void selectionChanged(final SelectionChangedEvent event) {
                removeButton.setEnabled(!event.getSelection().isEmpty());
            }
        };
        viewer.addSelectionChangedListener(selectionChangedListener);
        viewer.getTable().addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(final DisposeEvent e) {
                viewer.removeSelectionChangedListener(selectionChangedListener);
            }
        });
    }

    private void createButtons(final Composite parent) {
        addPythonLibButton = toolkit.createButton(parent, "Add Python library", SWT.PUSH);
        addPythonLibButton.setEnabled(false);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).applyTo(addPythonLibButton);
        addPythonHandler();

        addJavaLibButton = toolkit.createButton(parent, "Add Java library", SWT.PUSH);
        addJavaLibButton.setEnabled(false);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).applyTo(addJavaLibButton);
        addJavaHandler();

        addLibspecButton = toolkit.createButton(parent, "Add libspec file", SWT.PUSH);
        addLibspecButton.setEnabled(false);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).applyTo(addLibspecButton);
        addLibspecHandler();
        
        removeButton = toolkit.createButton(parent, "Remove", SWT.PUSH);
        removeButton.setEnabled(false);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).applyTo(removeButton);
        addRemoveHandler();
    }
    
    private void addPythonHandler() {
        addPythonLibButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                
                final FileDialog dialog = createReferencedLibFileDialog();
                dialog.setFilterExtensions(new String[] { "*.py", "*.*" });
                final String chosenFilePath = dialog.open();
                if (chosenFilePath != null) {
                    final PythonLibStructureBuilder pythonLibStructureBuilder = new PythonLibStructureBuilder();
                    final List<PythonClass> pythonClasses = pythonLibStructureBuilder.provideEntriesFromFile(chosenFilePath);
                    
                    boolean added = false;
                    if(pythonLibStructureBuilder.isArchive()) {
                        final ElementListSelectionDialog classesDialog = createReferencedLibClassesDialog(addPythonLibButton,
                                pythonClasses, new PythonClassesLabelProvider());
                        if (classesDialog.open() == Window.OK) {
                            final Object[] result = classesDialog.getResult();
                            
                            for (final Object selectedClass : result) {
                                final PythonClass pythonClass = (PythonClass) selectedClass;
                                editorInput.getProjectConfiguration().addReferencedLibraryInPython(
                                        pythonClass.getQualifiedName(),
                                                ImportSettingFilePathResolver.createFileRelativePath(
                                                        PathsConverter.toWorkspaceRelativeIfPossible(
                                                                new Path(chosenFilePath)),
                                                currentProject.getProject().getLocation()));
                                added = true;
                            }
                        } 
                    } else {
                        for (final PythonClass pythonClass : pythonClasses) {
                            editorInput.getProjectConfiguration().addReferencedLibraryInPython(
                                    pythonClass.getQualifiedName(),
                                    PathsConverter.toWorkspaceRelativeIfPossible(new Path(chosenFilePath)));
                            added = true;
                        }
                    }
                    if (added) {
                        dirtyProviderService.setDirtyState(true);
                        viewer.refresh();
                    }
                }
            }
        });
    }

    private void addJavaHandler() {
        addJavaLibButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                
                final FileDialog dialog = createReferencedLibFileDialog();
                dialog.setFilterExtensions(new String[] { "*.jar" });
                final String chosenFilePath = dialog.open();
                if (chosenFilePath != null) {
                    final List<JarClass> classesFromJar = new JarStructureBuilder()
                            .provideEntriesFromJarFile(chosenFilePath);
                    final ElementListSelectionDialog classesDialog = createReferencedLibClassesDialog(addJavaLibButton,
                            classesFromJar, new JarClassesLabelProvider());

                    if (classesDialog.open() == Window.OK) {
                        final Object[] result = classesDialog.getResult();

                        boolean added = false;
                        for (final Object selectedClass : result) {
                            final JarClass jarClass = (JarClass) selectedClass;
                            editorInput.getProjectConfiguration().addReferencedLibraryInJava(
                                    jarClass.getQualifiedName(),
                                    PathsConverter.toWorkspaceRelativeIfPossible(new Path(chosenFilePath)));
                            added = true;
                        }
                        if (added) {
                            dirtyProviderService.setDirtyState(true);
                            viewer.refresh();
                        }
                    }
                }
            }
        });
    }
    
    private FileDialog createReferencedLibFileDialog() {
        final String startingPath = currentProject.getProject().getLocation().toOSString();
        final FileDialog dialog = new FileDialog(viewer.getTable().getShell(), SWT.OPEN);
        dialog.setFilterPath(startingPath);
        return dialog;
    }

    private ElementListSelectionDialog createReferencedLibClassesDialog(final Button button, final List<?> classes,
            final LabelProvider labelProvider) {
        final ElementListSelectionDialog classesDialog = new ElementListSelectionDialog(button.getShell(),
                labelProvider);
        classesDialog.setMultipleSelection(true);
        classesDialog.setTitle("Select library class");
        classesDialog.setMessage("Select the class(es) which defines library:");
        classesDialog.setElements(classes.toArray());

        return classesDialog;
    }

    private void addLibspecHandler() {
        addLibspecButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                final FileDialog dialog = createReferencedLibFileDialog();
                dialog.setFilterExtensions(new String[] { "*.xml", "*.*" });
                final String chosenFilePath = dialog.open();
                if (chosenFilePath != null) {
                    final IPath path = PathsConverter.toWorkspaceRelativeIfPossible(new Path(chosenFilePath));

                    editorInput.getProjectConfiguration().addReferencedLibrarySpecification(path);
                    dirtyProviderService.setDirtyState(true);
                    viewer.refresh();
                }
            }
        });
    }

    private void addRemoveHandler() {
        removeButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                final List<ReferencedLibrary> selectedLibs = Selections.getElements(
                        (IStructuredSelection) viewer.getSelection(), ReferencedLibrary.class);
                editorInput.getProjectConfiguration().removeLibraries(selectedLibs);
                dirtyProviderService.setDirtyState(true);
                viewer.refresh();
            }
        });
    }

    void whenEnvironmentWasLoaded(final RobotRuntimeEnvironment env) {
        final boolean isEditable = editorInput.isEditable();
        final boolean projectIsInterpretedByJython = env.getInterpreter() == SuiteExecutor.Jython;

        addPythonLibButton.setEnabled(isEditable);
        addJavaLibButton.setEnabled(isEditable && projectIsInterpretedByJython);
        addLibspecButton.setEnabled(isEditable);
        removeButton.setEnabled(false);
        viewer.getTable().setEnabled(isEditable);

        if (!addJavaLibButton.isEnabled()) {
            decoration = new ControlDecoration(addJavaLibButton, SWT.RIGHT | SWT.TOP);
            decoration.setDescriptionText("Project is configured to use " + env.getInterpreter().toString()
                    + " interpreter, but Jython is needed for Java libraries.");
            decoration.setImage(FieldDecorationRegistry.getDefault()
                    .getFieldDecoration(FieldDecorationRegistry.DEC_INFORMATION).getImage());
        } else if (decoration != null) {
            decoration.dispose();
            decoration = null;
        }
    }

    void whenConfigurationFiledChanged() {
        addPythonLibButton.setEnabled(false);
        addJavaLibButton.setEnabled(false);
        addLibspecButton.setEnabled(false);
        removeButton.setEnabled(false);
        viewer.getTable().setEnabled(false);
    }
    
    private void setInput() {
        final List<ReferencedLibrary> libspecs = editorInput.getProjectConfiguration().getLibraries();
        viewer.setInput(libspecs);
    }

    @Override
    public void setFocus() {
        viewer.getTable().setFocus();
    }

    @Override
    public MatchesCollection collectMatches(final String filter) {
        return null;
    }
    
    @Inject
    @Optional
    private void changeEvent(@UIEventTopic(RobotModelEvents.ROBOT_SETTING_LIBRARY_CHANGED_IN_SUITE) final String string) {
        editorInput.refreshProjectConfiguration();
        setInput();
        viewer.refresh();
    }
}

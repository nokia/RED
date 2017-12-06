/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.popup;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.Stylers;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewersConfigurator;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.settings.CreateFreshSettingCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.settings.DeleteSettingCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.settings.SetSettingArgumentCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.viewers.RedCommonLabelProvider;
import org.robotframework.red.viewers.Selections;
import org.robotframework.red.viewers.StructuredContentProvider;


public class ImportResourcesComposite {

    private final RobotEditorCommandsStack commandsStack;
    private static IProject currentProject;
    private final FormToolkit formToolkit;
    private final Shell shell;
    private TableViewer resourcesViewer;
    private ISelectionChangedListener selectionChangedListener;
    private final RobotSettingsSection settingsSection;
    
    public ImportResourcesComposite(final RobotEditorCommandsStack commandsStack, final RobotSuiteFile fileModel,
            final FormToolkit formToolkit, final Shell shell) {
        this.commandsStack = commandsStack;
        this.formToolkit = formToolkit;
        this.shell = shell;
        
        currentProject = fileModel.getProject().getProject();
        this.settingsSection = fileModel.findSection(RobotSettingsSection.class).get();
    }
    
    public Composite createImportResourcesComposite(final Composite parent) {
        final Composite resourcesComposite = formToolkit.createComposite(parent);
        GridLayoutFactory.fillDefaults().numColumns(2).margins(3, 3).extendedMargins(0, 0, 0, 3)
                .applyTo(resourcesComposite);
        final Label titleLabel = formToolkit.createLabel(resourcesComposite,
                "Imported resources:");
        titleLabel.setFont(JFaceResources.getBannerFont());
        titleLabel.setForeground(formToolkit.getColors().getColor(IFormColors.TITLE));
        GridDataFactory.fillDefaults().grab(true, false).span(2, 1).hint(700, SWT.DEFAULT).applyTo(titleLabel);

        resourcesViewer = new TableViewer(resourcesComposite);
        resourcesViewer.setContentProvider(new ImportResourcesContentProvider());
        resourcesViewer.setLabelProvider(new DelegatingStyledCellLabelProvider(new ResourcesLabelProvider()));
        GridDataFactory.fillDefaults().grab(true, true).hint(220, 250).applyTo(resourcesViewer.getControl());
        
        final Composite addResourceButtons = formToolkit.createComposite(resourcesComposite);
        GridLayoutFactory.fillDefaults().numColumns(1).applyTo(addResourceButtons);
        GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.BEGINNING).applyTo(addResourceButtons);
        
        final Button addResourceFromWorkspaceBtn = formToolkit.createButton(addResourceButtons, "Add Resource", SWT.PUSH);
        GridDataFactory.fillDefaults().grab(false, true).applyTo(addResourceFromWorkspaceBtn);
        addResourceFromWorkspaceBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                final Shell newShell = new Shell(shell);
                final ElementTreeSelectionDialog dialog = createAddResourceSelectionDialog(newShell, true, null);
                if (dialog.open() == Window.OK) {
                    final Object[] results = dialog.getResult();
                    if (results != null) {
                        final List<String> resourcesPaths = newArrayList();
                        for (int i = 0; i < results.length; i++) {
                            final IResource resource = (IResource) results[i];
                            final String path = ImportSettingFilePathResolver.createResourceRelativePath(resource, currentProject);
                            resourcesPaths.add(path);
                        }
                        handleResourceAdd(resourcesPaths);
                    }
                }
                newShell.dispose();
            }
        });
        
        final Button addExternalResourceBtn = formToolkit.createButton(addResourceButtons, "Add External Resource",
                SWT.PUSH);
        addExternalResourceBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                final Shell newShell = new Shell(shell);
                final FileDialog dialog = new FileDialog(newShell, SWT.OPEN);
                dialog.setFilterExtensions(new String[] { "*.*", "*.robot", "*.tsv", "*.txt" });
                dialog.setFilterPath(currentProject.getLocation().toOSString());
                final String chosenFilePath = dialog.open();
                if (chosenFilePath != null) {
                    handleResourceAdd(newArrayList(ImportSettingFilePathResolver.createFileRelativePath(
                            new Path(chosenFilePath), currentProject.getLocation()).toPortableString()));
                }
                newShell.dispose();
            }
        });
        
        final Button editResourceBtn = formToolkit.createButton(addResourceButtons, "Edit",
                SWT.PUSH);
        GridDataFactory.fillDefaults().indent(0, 10).applyTo(editResourceBtn);
        editResourceBtn.setEnabled(false);
        editResourceBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                final Settings resources = (Settings) resourcesViewer.getInput();
                final IPath path = Selections.getSingleElement(
                        (IStructuredSelection) resourcesViewer.getSelection(), IPath.class);
                String filePath = null;
                final Shell newShell = new Shell(shell);
                final IResource initialProjectSelection = currentProject.findMember(path);
                if (initialProjectSelection == null) {
                    final FileDialog dialog = new FileDialog(newShell, SWT.OPEN);
                    dialog.setFilterExtensions(new String[] { "*.*", "*.robot", "*.tsv", "*.txt" });
                    final IPath initialExtSelection = ImportSettingFilePathResolver.createFileAbsolutePath(path, currentProject);
                    dialog.setFilterPath(initialExtSelection.removeLastSegments(1).toOSString());
                    dialog.setFileName(initialExtSelection.lastSegment());
                    final String chosenFilePath = dialog.open();
                    if (chosenFilePath != null) {
                        filePath = ImportSettingFilePathResolver.createFileRelativePath(
                                new Path(chosenFilePath), currentProject.getLocation()).toPortableString();
                    }
                } else {
                    final ElementTreeSelectionDialog dialog = createAddResourceSelectionDialog(newShell, false, initialProjectSelection);
                    if (dialog.open() == Window.OK) {
                        final Object result = dialog.getFirstResult();
                        if (result != null) {
                            final IResource resource = (IResource) result;
                            filePath = ImportSettingFilePathResolver.createResourceRelativePath(resource, currentProject);
                        }
                    }
                }
                newShell.dispose();
                if(filePath != null) {
                    handleResourceEdit(resources, path, filePath);
                }
            }
        });
        
        final Button removeResourceBtn = formToolkit.createButton(addResourceButtons, "Remove",
                SWT.PUSH);
        GridDataFactory.fillDefaults().applyTo(removeResourceBtn);
        removeResourceBtn.setEnabled(false);
        removeResourceBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                final Settings resources = (Settings) resourcesViewer.getInput();
                final List<IPath> paths = Selections.getElements(
                        (IStructuredSelection) resourcesViewer.getSelection(), IPath.class);
                handleResourceRemove(resources, paths);
            }
        });
        createViewerSelectionListener(removeResourceBtn, editResourceBtn);
        ViewersConfigurator.enableDeselectionPossibility(resourcesViewer);
        return resourcesComposite;
    }
    
    public TableViewer getResourcesViewer() {
        return resourcesViewer;
    }
    
    public ISelectionChangedListener getSelectionChangedListener() {
        return selectionChangedListener;
    }
    
    private ElementTreeSelectionDialog createAddResourceSelectionDialog(final Shell shell, final boolean allowMultiple, final IResource initialSelection) {
        final ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(shell, new WorkbenchLabelProvider(),
                new BaseWorkbenchContentProvider());
        dialog.setAllowMultiple(allowMultiple);
        dialog.setTitle("Select resource file");
        dialog.setMessage("Select the resource file to import:");
        dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());
        if(initialSelection != null) {
            dialog.setInitialSelection(initialSelection);
        }
        return dialog;
    }

    private void handleResourceAdd(final List<String> paths) {
        final List<IPath> currentPaths = ((Settings) resourcesViewer.getInput()).getImportedResources();
        for (final String newPathString : paths) {
            final IPath newPath = new Path(newPathString);
            if (!currentPaths.contains(newPath) && !newPathString.isEmpty()) {
                final List<String> args = newArrayList(newPathString);
                commandsStack.execute(new CreateFreshSettingCommand(settingsSection, "Resource", args));
                currentPaths.add(newPath);
            }
        }
        resourcesViewer.refresh();
    }
    
    private void handleResourceRemove(final Settings importedSettings, final List<IPath> resourcesToRemove) {
        final List<RobotSetting> settingsToRemove = newArrayList();
        final List<RobotSetting> currentResources = settingsSection.getResourcesSettings();
        for (final RobotSetting setting : currentResources) {
            final List<String> args = setting.getArguments();
            if(!args.isEmpty() && resourcesToRemove.contains(new Path(args.get(0)))) {
                settingsToRemove.add(setting);
            }
        }
        importedSettings.getImportedResources().removeAll(resourcesToRemove);
        commandsStack.execute(new DeleteSettingCommand(settingsToRemove));
        resourcesViewer.refresh();
    }
    
    private void handleResourceEdit(final Settings importedSettings, final IPath oldPath, final String newPath) {
        final List<RobotSetting> currentResources = settingsSection.getResourcesSettings();
        for (final RobotSetting setting : currentResources) {
            final List<String> args = setting.getArguments();
            if(!args.isEmpty() && oldPath.equals(new Path(args.get(0)))) {
                commandsStack.execute(new SetSettingArgumentCommand(setting, 0, newPath));
                break;
            }
        }
        final List<IPath> resources = importedSettings.getImportedResources();
        final int index = resources.indexOf(oldPath);
        if(index >= 0) {
            resources.set(index, new Path(newPath));
        }
        resourcesViewer.refresh();
    }
    
    private void createViewerSelectionListener(final Button removeBtn, final Button editBtn) {
        selectionChangedListener = new ISelectionChangedListener() {
            @Override
            public void selectionChanged(final SelectionChangedEvent event) {
                final boolean isSettingSelected = resourcesViewer.getTable().getSelectionCount() == 1;
                removeBtn.setEnabled(isSettingSelected);
                editBtn.setEnabled(isSettingSelected);
            }
        };
        resourcesViewer.addSelectionChangedListener(selectionChangedListener);
    }
    
    protected void setInitialSelection(final RobotSetting initialSetting) {
        if (!initialSetting.getArguments().isEmpty()) {
            final Path selectedArgument = new Path(initialSetting.getArguments().get(0));
            resourcesViewer.setSelection(new StructuredSelection(selectedArgument));
        }
    }
    
    private static class ResourcesLabelProvider extends RedCommonLabelProvider {

        @Override
        public Image getImage(final Object element) {
            return ImagesManager.getImage(RedImages.getResourceImage());
        }

        @Override
        public StyledString getStyledText(final Object element) {
            final IPath path = (IPath) element;
            final StyledString text = new StyledString(path.lastSegment() != null ? path.lastSegment() : "");
            final String parentPath = path.segmentCount() > 1 ? path.removeLastSegments(1).toString()
                    : currentProject.getName();
            text.append(" - " + parentPath, Stylers.Common.ECLIPSE_DECORATION_STYLER);
            return text;
        }
    }
    
    private static class ImportResourcesContentProvider extends StructuredContentProvider {

        @Override
        public Object[] getElements(final Object inputElement) {
            final List<IPath> libraries = ((Settings) inputElement).getImportedResources();
            Collections.sort(libraries, (p1, p2) -> p1.lastSegment().compareTo(p2.lastSegment()));
            return libraries.toArray();
        }
    }
}

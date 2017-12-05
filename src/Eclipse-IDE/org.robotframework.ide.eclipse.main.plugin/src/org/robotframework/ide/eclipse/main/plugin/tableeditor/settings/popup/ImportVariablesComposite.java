/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.popup;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.joining;

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
import org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.popup.Settings.ImportArguments;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.viewers.RedCommonLabelProvider;
import org.robotframework.red.viewers.Selections;
import org.robotframework.red.viewers.StructuredContentProvider;

public class ImportVariablesComposite {

    private static IProject currentProject;

    private final RobotEditorCommandsStack commandsStack;

    private final FormToolkit formToolkit;

    private final Shell shell;

    private final RobotSettingsSection settingsSection;

    private TableViewer variablesViewer;

    private ISelectionChangedListener selectionChangedListener;

    public ImportVariablesComposite(final RobotEditorCommandsStack commandsStack, final RobotSuiteFile fileModel,
            final FormToolkit formToolkit, final Shell shell) {
        this.commandsStack = commandsStack;
        this.formToolkit = formToolkit;
        this.shell = shell;

        currentProject = fileModel.getProject().getProject();
        this.settingsSection = fileModel.findSection(RobotSettingsSection.class).get();
    }

    public Composite createImportVariablesComposite(final Composite parent) {
        final Composite variablesComposite = formToolkit.createComposite(parent);
        GridLayoutFactory.fillDefaults()
                .numColumns(2)
                .margins(3, 3)
                .extendedMargins(0, 0, 0, 3)
                .applyTo(variablesComposite);
        final Label titleLabel = formToolkit.createLabel(variablesComposite, "Imported variables files:");
        titleLabel.setFont(JFaceResources.getBannerFont());
        titleLabel.setForeground(formToolkit.getColors().getColor(IFormColors.TITLE));
        GridDataFactory.fillDefaults().grab(true, false).span(2, 1).hint(700, SWT.DEFAULT).applyTo(titleLabel);

        variablesViewer = new TableViewer(variablesComposite);
        variablesViewer.setContentProvider(new ImportVariablesContentProvider());
        variablesViewer.setLabelProvider(new DelegatingStyledCellLabelProvider(new VariablesLabelProvider()));
        GridDataFactory.fillDefaults().grab(true, true).hint(220, 250).applyTo(variablesViewer.getControl());

        final Composite addVariablesButtons = formToolkit.createComposite(variablesComposite);
        GridLayoutFactory.fillDefaults().numColumns(1).applyTo(addVariablesButtons);
        GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.BEGINNING).applyTo(addVariablesButtons);

        final Button addVariableFromWorkspaceBtn = formToolkit.createButton(addVariablesButtons, "Add Variables",
                SWT.PUSH);
        GridDataFactory.fillDefaults().grab(false, true).applyTo(addVariableFromWorkspaceBtn);
        addVariableFromWorkspaceBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                final Shell newShell = new Shell(shell);
                final ElementTreeSelectionDialog dialog = createAddVariableSelectionDialog(newShell, true, null);
                if (dialog.open() == Window.OK) {
                    final Object[] results = dialog.getResult();
                    if (results != null) {
                        final List<String> variablesPaths = newArrayList();
                        for (int i = 0; i < results.length; i++) {
                            final IResource resource = (IResource) results[i];
                            final String path = ImportSettingFilePathResolver.createResourceRelativePath(resource,
                                    currentProject);
                            variablesPaths.add(path);
                        }
                        handleVariableAdd(variablesPaths);
                    }
                }
                newShell.dispose();
            }
        });

        final Button addExternalVariablesBtn = formToolkit.createButton(addVariablesButtons, "Add External Variables",
                SWT.PUSH);
        addExternalVariablesBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                final Shell newShell = new Shell(shell);
                final FileDialog dialog = new FileDialog(newShell, SWT.OPEN);
                dialog.setFilterExtensions(new String[] { "*.py", "*.*" });
                dialog.setFilterPath(currentProject.getLocation().toOSString());
                final String chosenFilePath = dialog.open();
                if (chosenFilePath != null) {
                    handleVariableAdd(newArrayList(ImportSettingFilePathResolver.createFileRelativePath(
                            new Path(chosenFilePath), currentProject.getProject().getLocation()).toPortableString()));
                }
                newShell.dispose();
            }
        });

        final Button editVariablesBtn = formToolkit.createButton(addVariablesButtons, "Edit File Path", SWT.PUSH);
        GridDataFactory.fillDefaults().indent(0, 10).applyTo(editVariablesBtn);
        editVariablesBtn.setEnabled(false);
        editVariablesBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                final ImportArguments variablesFile = Selections
                        .getSingleElement((IStructuredSelection) variablesViewer.getSelection(), ImportArguments.class);
                final List<String> args = variablesFile.getArgs();
                final Shell newShell = new Shell(shell);
                if (args != null && !args.isEmpty()) {
                    final IPath path = new Path(args.get(0));
                    final IResource initialProjectSelection = currentProject.findMember(path);
                    String filePath = null;
                    if (initialProjectSelection == null) {
                        final FileDialog dialog = new FileDialog(newShell, SWT.OPEN);
                        dialog.setFilterExtensions(new String[] { "*.py", "*.*" });
                        final IPath initialExtSelection = ImportSettingFilePathResolver.createFileAbsolutePath(path, currentProject);
                        dialog.setFilterPath(initialExtSelection.removeLastSegments(1).toOSString());
                        dialog.setFileName(initialExtSelection.lastSegment());
                        final String chosenFilePath = dialog.open();
                        if (chosenFilePath != null) {
                            filePath = ImportSettingFilePathResolver.createFileRelativePath(
                                    new Path(chosenFilePath), currentProject.getLocation())
                                    .toPortableString();
                        }
                    } else {
                        final ElementTreeSelectionDialog dialog = createAddVariableSelectionDialog(newShell, false,
                                initialProjectSelection);
                        if (dialog.open() == Window.OK) {
                            final Object result = dialog.getFirstResult();
                            if (result != null) {
                                final IResource resource = (IResource) result;
                                filePath = ImportSettingFilePathResolver.createResourceRelativePath(resource, currentProject);
                            }
                        }
                    }
                    if (filePath != null) {
                        handleVariablesPathEdit(variablesFile, filePath);
                    }
                }
                newShell.dispose();
            }
        });

        final Button editVariablesArgsBtn = formToolkit.createButton(addVariablesButtons, "Edit File Arguments",
                SWT.PUSH);
        GridDataFactory.fillDefaults().applyTo(editVariablesArgsBtn);
        editVariablesArgsBtn.setEnabled(false);
        editVariablesArgsBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                final ImportArguments variablesFile = Selections
                        .getSingleElement((IStructuredSelection) variablesViewer.getSelection(), ImportArguments.class);
                if (!variablesFile.getArgs().isEmpty()) {
                    final List<String> args = variablesFile.getArgs().subList(1, variablesFile.getArgs().size());
                    final Shell newShell = new Shell(shell);
                    final ImportSettingFileArgumentsDialog dialog = new ImportSettingFileArgumentsDialog(newShell, args);
                    if (dialog.open() == Window.OK) {
                        final List<String> newArgs = dialog.getArguments();
                        handleVariablesArgsEdit(variablesFile, newArgs);
                    }
                    newShell.dispose();
                }
            }
        });

        final Button removeVariablesBtn = formToolkit.createButton(addVariablesButtons, "Remove", SWT.PUSH);
        GridDataFactory.fillDefaults().applyTo(removeVariablesBtn);
        removeVariablesBtn.setEnabled(false);
        removeVariablesBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                final Settings variables = (Settings) variablesViewer.getInput();
                final List<ImportArguments> paths = Selections
                        .getElements((IStructuredSelection) variablesViewer.getSelection(), ImportArguments.class);
                handleVariableRemove(variables, paths);
            }
        });

        createViewerSelectionListener(removeVariablesBtn, editVariablesBtn, editVariablesArgsBtn);
        ViewersConfigurator.enableDeselectionPossibility(variablesViewer);
        return variablesComposite;
    }

    public TableViewer getVariablesViewer() {
        return variablesViewer;
    }

    public ISelectionChangedListener getSelectionChangedListener() {
        return selectionChangedListener;
    }

    private ElementTreeSelectionDialog createAddVariableSelectionDialog(final Shell shell, final boolean allowMultiple,
            final IResource initialSelection) {
        final ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(shell, new WorkbenchLabelProvider(),
                new BaseWorkbenchContentProvider());
        dialog.setAllowMultiple(allowMultiple);
        dialog.setTitle("Select variables file");
        dialog.setMessage("Select the variables file to import:");
        dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());
        if (initialSelection != null) {
            dialog.setInitialSelection(initialSelection);
        }
        return dialog;
    }

    private void handleVariableAdd(final List<String> paths) {
        final List<ImportArguments> currentVariables = ((Settings) variablesViewer.getInput())
                .getImportedVariablesArguments();
        for (final String newPathString : paths) {
            if (!newPathString.isEmpty()) {
                final List<String> args = newArrayList(newPathString);
                commandsStack.execute(new CreateFreshSettingCommand(settingsSection, "Variables", args));
                currentVariables.add(new ImportArguments(args));
            }
        }
        variablesViewer.refresh();
    }

    private void handleVariableRemove(final Settings importedSettings,
            final List<ImportArguments> resourcesToRemove) {
        final List<RobotSetting> settingsToRemove = newArrayList();
        final List<RobotSetting> currentVariables = settingsSection.getVariablesSettings();
        for (final RobotSetting setting : currentVariables) {
            final List<String> args = setting.getArguments();
            if (!args.isEmpty()) {
                if (resourcesToRemove.contains(new ImportArguments(args))) {
                    settingsToRemove.add(setting);
                }
            }
        }
        importedSettings.getImportedVariablesArguments().removeAll(resourcesToRemove);
        commandsStack.execute(new DeleteSettingCommand(settingsToRemove));
        variablesViewer.refresh();
    }

    private void handleVariablesPathEdit(final ImportArguments variablesFile,
            final String newPath) {
        final List<RobotSetting> currentVariables = settingsSection.getVariablesSettings();
        for (final RobotSetting setting : currentVariables) {
            final List<String> args = setting.getArguments();
            if (!args.isEmpty() && args.equals(variablesFile.getArgs())) {
                args.set(0, newPath);
                commandsStack.execute(new SetSettingArgumentCommand(setting, 0, newPath));
                variablesFile.setArgs(args);
                break;
            }
        }
        variablesViewer.refresh();
    }

    private void handleVariablesArgsEdit(final ImportArguments variablesFile, final List<String> newArgs) {
        final List<RobotSetting> currentVariables = settingsSection.getVariablesSettings();
        for (final RobotSetting setting : currentVariables) {
            final List<String> args = setting.getArguments();
            if (!args.isEmpty() && args.equals(variablesFile.getArgs())) {
                final List<String> newVariablesArguments = newArrayList(args.get(0));
                newVariablesArguments.addAll(newArgs);
                for (int i = 0; i < newArgs.size(); i++) {
                    commandsStack.execute(new SetSettingArgumentCommand(setting, i + 1, newArgs.get(i))); // set arg after variable file path
                }
                variablesFile.setArgs(newVariablesArguments);
                break;
            }
        }
        variablesViewer.refresh();
    }

    private void createViewerSelectionListener(final Button removeBtn, final Button editPathBtn,
            final Button editArgsBtn) {
        selectionChangedListener = new ISelectionChangedListener() {

            @Override
            public void selectionChanged(final SelectionChangedEvent event) {
                final boolean isSettingSelected = variablesViewer.getTable().getSelectionCount() == 1;
                removeBtn.setEnabled(isSettingSelected);
                editPathBtn.setEnabled(isSettingSelected);
                editArgsBtn.setEnabled(isSettingSelected);
            }
        };
        variablesViewer.addSelectionChangedListener(selectionChangedListener);
    }

    protected void setInitialSelection(final RobotSetting initialSetting) {
        final ImportArguments selectedImport = new ImportArguments(
                initialSetting.getArguments());
        variablesViewer.setSelection(new StructuredSelection(selectedImport));
    }

    private static class VariablesLabelProvider extends RedCommonLabelProvider {

        @Override
        public Image getImage(final Object element) {
            return ImagesManager.getImage(RedImages.getRobotScalarVariableImage());
        }

        @Override
        public StyledString getStyledText(final Object element) {
            final StyledString text = new StyledString("");

            final ImportArguments importedVariable = (ImportArguments) element;
            final List<String> args = importedVariable.getArgs();
            if (!args.isEmpty()) {
                final IPath path = new Path(args.get(0));
                if(path.lastSegment() != null) {
                    text.append(path.lastSegment());
                }
                final String parentPath = path.segmentCount() > 1 ? path.removeLastSegments(1).toString()
                        : currentProject.getName();
                final String fileArgs = args.stream().skip(1).collect(joining(":", ":", ""));
                text.append(fileArgs + " - " + parentPath, Stylers.Common.ECLIPSE_DECORATION_STYLER);
            }
            return text;
        }
    }

    private static class ImportVariablesContentProvider extends StructuredContentProvider {

        @Override
        public Object[] getElements(final Object inputElement) {
            return ((Settings) inputElement).getImportedVariablesArguments().toArray();
        }
    }
}

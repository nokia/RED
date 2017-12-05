/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.popup;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.dialogs.MessageDialog;
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
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting.SettingsGroup;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.settings.CreateFreshSettingCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.settings.DeleteSettingCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.settings.SetSettingArgumentCommand;
import org.robotframework.ide.eclipse.main.plugin.project.RedEclipseProjectConfigWriter;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.viewers.RedCommonLabelProvider;
import org.robotframework.red.viewers.Selections;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class ImportLibraryComposite {

    private final RobotEditorCommandsStack commandsStack;

    private final FormToolkit formToolkit;

    private final Shell shell;

    private final RobotSuiteFile fileModel;

    private TableViewer leftViewer;

    private TableViewer rightViewer;

    private ISelectionChangedListener leftViewerSelectionChangedListener;

    private ISelectionChangedListener rightViewerSelectionChangedListener;

    private final RobotProject robotProject;

    private final IEventBroker eventBroker;

    public ImportLibraryComposite(final RobotEditorCommandsStack commandsStack, final RobotSuiteFile fileModel,
            final FormToolkit formToolkit, final Shell shell) {
        this.commandsStack = commandsStack;
        this.formToolkit = formToolkit;
        this.fileModel = fileModel;
        this.shell = shell;
        robotProject = fileModel.getProject();
        eventBroker = PlatformUI.getWorkbench().getService(IEventBroker.class);
    }

    public Composite createImportResourcesComposite(final Composite parent) {
        final Composite librariesComposite = formToolkit.createComposite(parent);
        GridLayoutFactory.fillDefaults()
                .numColumns(4)
                .margins(3, 3)
                .extendedMargins(0, 0, 0, 3)
                .applyTo(librariesComposite);

        final Label titleLabel = formToolkit.createLabel(librariesComposite, "Libraries available in '"
                + fileModel.getProject().getName() + "' project");
        titleLabel.setFont(JFaceResources.getBannerFont());
        titleLabel.setForeground(formToolkit.getColors().getColor(IFormColors.TITLE));
        GridDataFactory.fillDefaults().grab(true, false).span(4, 1).hint(700, SWT.DEFAULT).applyTo(titleLabel);

        leftViewer = new TableViewer(librariesComposite);
        leftViewer.setContentProvider(new LibrariesToImportContentProvider());
        GridDataFactory.fillDefaults().span(1, 2).grab(true, true).hint(220, 250).applyTo(leftViewer.getControl());
        leftViewer.setLabelProvider(new DelegatingStyledCellLabelProvider(new LibrariesLabelProvider()));
        leftViewer.addDoubleClickListener(event -> Selections
                .getOptionalFirstElement((IStructuredSelection) event.getSelection(), LibrarySpecification.class)
                .ifPresent(spec -> handleLibraryAdd((Settings) leftViewer.getInput(), newArrayList(spec))));

        final Composite moveBtnsComposite = formToolkit.createComposite(librariesComposite);
        GridLayoutFactory.fillDefaults().numColumns(1).margins(3, 3).applyTo(moveBtnsComposite);

        final Button toImported = formToolkit.createButton(moveBtnsComposite, ">>", SWT.PUSH);
        toImported.setEnabled(false);
        toImported.addSelectionListener(createToImportedListener());
        GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.BEGINNING).applyTo(toImported);

        final Button fromImported = formToolkit.createButton(moveBtnsComposite, "<<", SWT.PUSH);
        fromImported.setEnabled(false);
        fromImported.addSelectionListener(createFromImportedListener());
        GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.BEGINNING).applyTo(fromImported);

        rightViewer = new TableViewer(librariesComposite);
        rightViewer.setContentProvider(new LibrariesAlreadyImportedContentProvider());
        GridDataFactory.fillDefaults().span(1, 2).grab(true, true).hint(220, 250).applyTo(rightViewer.getControl());
        rightViewer.setLabelProvider(new DelegatingStyledCellLabelProvider(new LibrariesLabelProvider()));
        rightViewer.addDoubleClickListener(event -> Selections
                .getOptionalFirstElement((IStructuredSelection) event.getSelection(), LibrarySpecification.class)
                .ifPresent(spec -> handleLibraryRemove((Settings) rightViewer.getInput(), newArrayList(spec))));

        final Composite newLibBtnsComposite = formToolkit.createComposite(librariesComposite);
        GridLayoutFactory.fillDefaults().numColumns(1).applyTo(newLibBtnsComposite);

        final Button addNewLibBtn = formToolkit.createButton(newLibBtnsComposite, "Add Library", SWT.PUSH);
        addNewLibBtn.addSelectionListener(createAddNewLibListener());
        GridDataFactory.fillDefaults().applyTo(addNewLibBtn);

        final Button addNewExternalLibBtn = formToolkit.createButton(newLibBtnsComposite, "Add External Library", SWT.PUSH);
        addNewExternalLibBtn.addSelectionListener(createAddNewExternalLibListener());
        GridDataFactory.fillDefaults().applyTo(addNewExternalLibBtn);

        final Button editLibPathBtn = formToolkit.createButton(newLibBtnsComposite, "Edit File Path", SWT.PUSH);
        editLibPathBtn.setEnabled(false);
        editLibPathBtn.addSelectionListener(createEditLibPathListener());
        GridDataFactory.fillDefaults().indent(0, 10).applyTo(editLibPathBtn);

        final Button editLibArgsBtn = formToolkit.createButton(newLibBtnsComposite, "Edit Arguments", SWT.PUSH);
        editLibArgsBtn.setEnabled(false);
        editLibArgsBtn.addSelectionListener(createEditLibArgsListener());
        GridDataFactory.fillDefaults().applyTo(editLibArgsBtn);

        final Label separator = formToolkit.createLabel(librariesComposite, "", SWT.SEPARATOR | SWT.HORIZONTAL);
        GridDataFactory.fillDefaults().indent(0, 5).grab(false, false).span(4, 1).applyTo(separator);
        final Label tooltipLabel = formToolkit.createLabel(librariesComposite,
                "Choose libraries to import. Only libraries which are imported by project are available. "
                        + "Edit project properties if you need other libraries", SWT.WRAP);
        GridDataFactory.fillDefaults().span(4, 1).hint(500, SWT.DEFAULT).applyTo(tooltipLabel);

        createLeftViewerSelectionListener(toImported);
        createRightViewerSelectionListener(fromImported, editLibArgsBtn, editLibPathBtn);

        return librariesComposite;
    }

    public ISelectionChangedListener getLeftViewerSelectionChangedListener() {
        return leftViewerSelectionChangedListener;
    }

    public ISelectionChangedListener getRightViewerSelectionChangedListener() {
        return rightViewerSelectionChangedListener;
    }

    public TableViewer getLeftViewer() {
        return leftViewer;
    }

    public TableViewer getRightViewer() {
        return rightViewer;
    }

    private SelectionListener createToImportedListener() {
        return new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                final Settings libs = (Settings) leftViewer.getInput();
                final List<LibrarySpecification> specs = Selections.getElements(
                        (IStructuredSelection) leftViewer.getSelection(), LibrarySpecification.class);

                handleLibraryAdd(libs, specs);
            }
        };
    }

    private SelectionListener createFromImportedListener() {
        return new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                final Settings libs = (Settings) rightViewer.getInput();
                final List<LibrarySpecification> specs = Selections.getElements(
                        (IStructuredSelection) rightViewer.getSelection(), LibrarySpecification.class);

                handleLibraryRemove(libs, specs);
            }
        };
    }

    private SelectionListener createAddNewLibListener() {
        return new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                final Shell newShell = new Shell(shell);
                final ElementTreeSelectionDialog dialog = createAddVariableSelectionDialog(shell, null);
                if (dialog.open() == Window.OK) {
                    final Object result = dialog.getFirstResult();
                    if (result != null) {
                        final IResource resource = (IResource) result;
                        final String nameWithoutExtension = ImportSettingFilePathResolver.createFileNameWithoutExtension(resource.getFullPath());
                        addNewLibraryToProjectConfiguration(
                                new Path(ImportSettingFilePathResolver.createResourceParentRelativePath(resource,
                                        robotProject.getProject())), nameWithoutExtension);
                        addNewLibraryToSettingsSection(nameWithoutExtension);
                    }
                }
                newShell.dispose();
            }
        };
    }

    private SelectionListener createAddNewExternalLibListener() {
        return new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                final Shell newShell = new Shell(shell);
                final FileDialog dialog = new FileDialog(newShell, SWT.OPEN);
                dialog.setFilterExtensions(new String[] { "*.py", "*.*" });
                final String chosenFilePath = dialog.open();
                if (chosenFilePath != null) {
                    final IPath path = new Path(chosenFilePath);
                    final String nameWithoutExtension = ImportSettingFilePathResolver.createFileNameWithoutExtension(path);
                    addNewLibraryToProjectConfiguration(
                            ImportSettingFilePathResolver.createFileParentRelativePath(path,
                                    robotProject.getProject().getLocation()), nameWithoutExtension);
                    addNewLibraryToSettingsSection(nameWithoutExtension);
                }
                newShell.dispose();
            }
        };
    }

    private void addNewLibraryToProjectConfiguration(final IPath path, final String nameWithoutExtension) {
        final RobotProjectConfig config = robotProject.getRobotProjectConfig();
        final ReferencedLibrary referencedLibrary = ReferencedLibrary.create(LibraryType.PYTHON, nameWithoutExtension,
                path.toPortableString());
        config.addReferencedLibrary(referencedLibrary);
        saveConfiguration(config);
    }

    private void saveConfiguration(final RobotProjectConfig config) {
        robotProject.clearConfiguration();
        robotProject.clearKwSources();
        new RedEclipseProjectConfigWriter().writeConfiguration(config, robotProject);
        eventBroker.send(RobotModelEvents.ROBOT_SETTING_LIBRARY_CHANGED_IN_SUITE, "");
    }

    private void addNewLibraryToSettingsSection(final String nameWithoutExtension) {
        if (!isLibraryAvailable(nameWithoutExtension)) {
            final List<LibrarySpecification> specs = newArrayList();
            final Collection<LibrarySpecification> referencedLibraries = robotProject.getReferencedLibraries().values();
            for (final LibrarySpecification librarySpecification : referencedLibraries) {
                if (librarySpecification != null && librarySpecification.getName().equals(nameWithoutExtension)) {
                    specs.add(librarySpecification);
                    break;
                }
            }
            final Settings libs = (Settings) rightViewer.getInput();
            handleLibraryAdd(libs, specs);
        } else {
            MessageDialog.openError(shell, "Error", "Given library name '" + nameWithoutExtension
                    + "' already exists in current project.");
        }
    }

    private boolean isLibraryAvailable(final String libName) {
        final Settings libs = (Settings) rightViewer.getInput();
        for(final LibrarySpecification spec : libs.getImportedLibraries()) {
            if(spec.getName().equals(libName)) {
                return true;
            }
        }
        for(final LibrarySpecification spec : libs.getLibrariesToImport()) {
            if(spec.getName().equals(libName)) {
                return true;
            }
        }
        return false;
    }

    private SelectionListener createEditLibPathListener() {
        return new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                final LibrarySpecification spec = Selections.getSingleElement(
                        (IStructuredSelection) rightViewer.getSelection(), LibrarySpecification.class);
                final IPath oldPath = new Path(spec.getSecondaryKey());
                final Shell newShell = new Shell(shell);
                final IResource initialProjectSelection = robotProject.getProject().findMember(
                        oldPath + "/" + spec.getName() + ".py");    //TODO: check file extension
                String nameWithoutExtension = "";
                String newPath = null;
                if (initialProjectSelection == null) {
                    final FileDialog dialog = new FileDialog(newShell, SWT.OPEN);
                    dialog.setFilterExtensions(new String[] { "*.py", "*.*" });
                    final IPath initialExtSelection = ImportSettingFilePathResolver.createFileAbsolutePath(oldPath,
                            robotProject.getProject());
                    dialog.setFilterPath(initialExtSelection.toOSString());
                    final String chosenFilePath = dialog.open();
                    if (chosenFilePath != null) {
                        final IPath path = new Path(chosenFilePath);
                        nameWithoutExtension = ImportSettingFilePathResolver.createFileNameWithoutExtension(path);
                        newPath = ImportSettingFilePathResolver.createFileParentRelativePath(path,
                                robotProject.getProject().getLocation()).toPortableString();
                    }
                } else {
                    final ElementTreeSelectionDialog dialog = createAddVariableSelectionDialog(newShell, initialProjectSelection);
                    if (dialog.open() == Window.OK) {
                        final Object result = dialog.getFirstResult();
                        if (result != null) {
                            final IResource resource = (IResource) result;
                            nameWithoutExtension = ImportSettingFilePathResolver.createFileNameWithoutExtension(resource.getFullPath());
                            newPath = ImportSettingFilePathResolver.createResourceParentRelativePath(resource,
                                    robotProject.getProject());
                        }
                    }
                }

                newShell.dispose();
                if(newPath != null) {
                    if (!spec.getName().equals(nameWithoutExtension)) {
                        MessageDialog.openError(shell, "Error", "Libraries names are not equal.");
                    } else {
                        editLibraryInProjectConfiguration(oldPath, newPath, nameWithoutExtension);
                        spec.setSecondaryKey(newPath);
                        rightViewer.refresh();
                    }
                }
            }
        };
    }

    private void editLibraryInProjectConfiguration(final IPath oldPath, final String newPath,
            final String nameWithoutExtension) {
        final RobotProjectConfig config = robotProject.getRobotProjectConfig();
        final List<ReferencedLibrary> libs = config.getLibraries();
        for (final ReferencedLibrary referencedLibrary : libs) {
            if (referencedLibrary.getName().equals(nameWithoutExtension)
                    && referencedLibrary.getPath().equals(oldPath.toPortableString())) {
                referencedLibrary.setPath(newPath);
                break;
            }
        }
        saveConfiguration(config);
    }

    private SelectionListener createEditLibArgsListener() {
        return new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                final LibrarySpecification spec = Selections.getSingleElement(
                        (IStructuredSelection) rightViewer.getSelection(), LibrarySpecification.class);
                final Optional<RobotSettingsSection> section = fileModel.findSection(RobotSettingsSection.class);
                List<String> libArgs = newArrayList();
                RobotSetting setting = null;
                final List<RobotSetting> settings = section.get().getImportSettings();
                for (final RobotSetting element : settings) {
                    setting = element;
                    if (setting.getGroup() == SettingsGroup.LIBRARIES) {
                        final List<String> args = setting.getArguments();
                        if (args != null && !args.isEmpty() && args.get(0).equals(spec.getName())) {
                            if (args.size() > 1) {
                                libArgs = args.subList(1, args.size());
                            }
                            break;
                        }
                    }
                    setting = null;
                }

                if (setting != null) {
                    final Shell newShell = new Shell(shell);
                    final ImportSettingFileArgumentsDialog dialog = new ImportSettingFileArgumentsDialog(newShell,
                            libArgs);
                    if (dialog.open() == Window.OK) {
                        handleEditLibraryArgs(setting, dialog.getArguments());
                    }
                    newShell.dispose();
                }
            }
        };
    }

    private void handleLibraryAdd(final Settings libs, final List<LibrarySpecification> specs) {
        libs.getLibrariesToImport().removeAll(specs);
        libs.getImportedLibraries().addAll(specs);

        final Optional<RobotSettingsSection> section = fileModel.findSection(RobotSettingsSection.class);
        for (final LibrarySpecification spec : specs) {
            final List<String> args = newArrayList(spec.getName());
            if (spec.isRemote()) {
                String host = spec.getSecondaryKey();
                if (!host.startsWith("http://")) {
                    host = "http://" + host;
                }
                args.add(host);
            }
            commandsStack.execute(new CreateFreshSettingCommand(section.get(), "Library", args));
        }

        leftViewer.refresh();
        rightViewer.refresh();
    }

    private void handleLibraryRemove(final Settings libs, final List<LibrarySpecification> specs) {
        if (!doesNotContainAlwaysAccessible(specs)) {
            return;
        }

        libs.getImportedLibraries().removeAll(specs);
        libs.getLibrariesToImport().addAll(specs);

        final Optional<RobotSettingsSection> section = fileModel.findSection(RobotSettingsSection.class);
        final List<RobotSetting> settingsToRemove = getSettingsToRemove(section.get(), specs);
        commandsStack.execute(new DeleteSettingCommand(settingsToRemove));

        leftViewer.refresh();
        rightViewer.refresh();
    }

    private void handleEditLibraryArgs(final RobotSetting setting, final List<String> newArgs) {
        for (int i = 0; i < newArgs.size(); i++) {
            commandsStack.execute(new SetSettingArgumentCommand(setting, i + 1, newArgs.get(i))); // set arg after keyword name
        }
    }

    private List<RobotSetting> getSettingsToRemove(final RobotSettingsSection settingsSection,
            final List<LibrarySpecification> specs) {
        final List<RobotSetting> settings = newArrayList();
        final List<String> specNames = Lists.transform(specs, new Function<LibrarySpecification, String>() {

            @Override
            public String apply(final LibrarySpecification spec) {
                return spec.getName();
            }
        });
        for (final RobotElement element : settingsSection.getImportSettings()) {
            final RobotSetting setting = (RobotSetting) element;
            final String name = setting.getArguments().isEmpty() ? null : setting.getArguments().get(0);
            if (specNames.contains(name)) {
                settings.add(setting);
            }
        }
        return settings;
    }

    private boolean doesNotContainAlwaysAccessible(final List<LibrarySpecification> specs) {
        for (final LibrarySpecification spec : specs) {
            if (spec.isAccessibleWithoutImport()) {
                return false;
            }
        }
        return true;
    }

    private void createLeftViewerSelectionListener(final Button buttonToActivate) {
        leftViewerSelectionChangedListener = new ISelectionChangedListener() {

            @Override
            public void selectionChanged(final SelectionChangedEvent event) {
                final List<LibrarySpecification> specs = Selections.getElements(
                        (IStructuredSelection) event.getSelection(), LibrarySpecification.class);
                buttonToActivate.setEnabled(!specs.isEmpty() && doesNotContainAlwaysAccessible(specs));
            }
        };
        leftViewer.addSelectionChangedListener(leftViewerSelectionChangedListener);
    }

    private void createRightViewerSelectionListener(final Button importBtn, final Button editArgsBtn, final Button editPathBtn) {
        rightViewerSelectionChangedListener = new ISelectionChangedListener() {

            @Override
            public void selectionChanged(final SelectionChangedEvent event) {
                final List<LibrarySpecification> specs = Selections.getElements(
                        (IStructuredSelection) event.getSelection(), LibrarySpecification.class);
                importBtn.setEnabled(!specs.isEmpty() && doesNotContainAlwaysAccessible(specs));

                if (rightViewer.getTable().getSelectionCount() == 1) {
                    final LibrarySpecification spec = specs.get(0);
                    editPathBtn.setEnabled(spec.isReferenced());
                    editArgsBtn.setEnabled(!spec.isAccessibleWithoutImport());
                } else {
                    editPathBtn.setEnabled(false);
                    editArgsBtn.setEnabled(false);
                }

            }
        };
        rightViewer.addSelectionChangedListener(rightViewerSelectionChangedListener);
    }

    protected void setInitialSelection(final RobotSetting initialSetting) {
        final Settings libs = (Settings) rightViewer.getInput();
        final List<LibrarySpecification> libSpecs = libs.getImportedLibraries();
        if (!initialSetting.getArguments().isEmpty()) {
            final String name = initialSetting.getArguments().get(0);
            for (final LibrarySpecification librarySpecification : libSpecs) {
                if (librarySpecification.getName().equals(name)) {
                    rightViewer.setSelection(new StructuredSelection(librarySpecification));
                    return;
                }
            }
        }
    }

    private ElementTreeSelectionDialog createAddVariableSelectionDialog(final Shell shell,
            final IResource initialSelection) {
        final ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(shell, new WorkbenchLabelProvider(),
                new BaseWorkbenchContentProvider());
        dialog.setAllowMultiple(false);
        dialog.setTitle("Select library file");
        dialog.setMessage("Select the library file to import:");
        dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());
        if (initialSelection != null) {
            dialog.setInitialSelection(initialSelection);
        }
        return dialog;
    }

    private static class LibrariesLabelProvider extends RedCommonLabelProvider {

        @Override
        public Image getImage(final Object element) {
            return ImagesManager.getImage(RedImages.getBookImage());
        }

        @Override
        public StyledString getStyledText(final Object element) {
            final LibrarySpecification spec = (LibrarySpecification) element;

            final StyledString text = new StyledString(spec.getName());
            if (spec.isAccessibleWithoutImport()) {
                text.append(" always accessible", Stylers.Common.ECLIPSE_DECORATION_STYLER);
            } else if (!spec.getSecondaryKey().equals("")) {
                text.append(" - " + spec.getSecondaryKey(), Stylers.Common.ECLIPSE_DECORATION_STYLER);
            }
            return text;
        }
    }

}

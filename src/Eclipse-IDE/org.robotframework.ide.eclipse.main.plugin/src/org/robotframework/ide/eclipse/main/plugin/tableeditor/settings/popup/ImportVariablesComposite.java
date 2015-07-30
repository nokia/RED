package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.popup;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerColumnsFactory;
import org.eclipse.jface.viewers.ViewersConfigurator;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.RedTheme;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile.ImportedVariablesFile;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.CreateSettingKeywordCallCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.DeleteSettingKeywordCallCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetSettingKeywordCallCommand;
import org.robotframework.ide.eclipse.main.plugin.project.editor.VariableFilesArgumentsEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.viewers.Selections;

import com.google.common.base.Optional;


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
        final Optional<RobotElement> section = fileModel.findSection(RobotSettingsSection.class);
        this.settingsSection = (RobotSettingsSection) section.get();
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
        GridDataFactory.fillDefaults().grab(true, true).hint(220, 250).applyTo(variablesViewer.getControl());
        ViewerColumnsFactory.newColumn("").shouldGrabAllTheSpaceLeft(true).withWidth(200)
                .labelsProvidedBy(new VariablesLabelProvider())
                .createFor(variablesViewer);
        
        final Composite addVariablesButtons = formToolkit.createComposite(variablesComposite);
        GridLayoutFactory.fillDefaults().numColumns(1).applyTo(addVariablesButtons);
        GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.BEGINNING).applyTo(addVariablesButtons);
        
        final Button addVariableFromWorkspaceBtn = formToolkit.createButton(addVariablesButtons, "Add Variables", SWT.PUSH);
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
                            final String path = extractResourcePath(resource);
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
                    handleVariableAdd(newArrayList(new Path(chosenFilePath).toPortableString()));
                }
                newShell.dispose();
            }
        });
        
        final Button editVariablesBtn = formToolkit.createButton(addVariablesButtons, "Edit File Path",
                SWT.PUSH);
        GridDataFactory.fillDefaults().indent(0, 10).applyTo(editVariablesBtn);
        editVariablesBtn.setEnabled(false);
        editVariablesBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                final Settings variables = (Settings) variablesViewer.getInput();
                final ImportedVariablesFile variablesFile = Selections.getSingleElement(
                        (IStructuredSelection) variablesViewer.getSelection(), ImportedVariablesFile.class);
                final List<String> args = variablesFile.getArgs();
                final Shell newShell = new Shell(shell);
                if (args != null && !args.isEmpty()) {
                    final IPath path = new Path(args.get(0));
                    String filePath = null;
                    if (path.isAbsolute()) {
                        final FileDialog dialog = new FileDialog(newShell, SWT.OPEN);
                        dialog.setFilterExtensions(new String[] { "*.py", "*.*" });
                        dialog.setFilterPath(path.toOSString());
                        final String chosenFilePath = dialog.open();
                        if (chosenFilePath != null) {
                            filePath = new Path(chosenFilePath).toPortableString();
                        }
                    } else {
                        final IResource initialSelection = currentProject.findMember(path);
                        final ElementTreeSelectionDialog dialog = createAddVariableSelectionDialog(newShell, false,
                                initialSelection);
                        if (dialog.open() == Window.OK) {
                            final Object result = dialog.getFirstResult();
                            if (result != null) {
                                final IResource resource = (IResource) result;
                                filePath = extractResourcePath(resource);
                            }
                        }
                    }
                    if (filePath != null) {
                        handleVariablesPathEdit(variables, variablesFile, filePath);
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
                final Settings variables = (Settings) variablesViewer.getInput();
                final ImportedVariablesFile variablesFile = Selections.getSingleElement(
                        (IStructuredSelection) variablesViewer.getSelection(), ImportedVariablesFile.class);
                final List<String> args = variablesFile.getArgs().subList(1, variablesFile.getArgs().size());
                final Shell newShell = new Shell(shell);
                final EditArgumentsDialog dialog = new EditArgumentsDialog(newShell, args);
                if (dialog.open() == Window.OK) {
                    final List<String> newArgs = dialog.getArguments();
                    handleVariablesArgsEdit(variables, variablesFile, newArgs);
                }
                newShell.dispose();
            }
        });
        
        final Button removeVariablesBtn = formToolkit.createButton(addVariablesButtons, "Remove",
                SWT.PUSH);
        GridDataFactory.fillDefaults().applyTo(removeVariablesBtn);
        removeVariablesBtn.setEnabled(false);
        removeVariablesBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                final Settings variables = (Settings) variablesViewer.getInput();
                final List<ImportedVariablesFile> paths = Selections.getElements(
                        (IStructuredSelection) variablesViewer.getSelection(), ImportedVariablesFile.class);
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

    private ElementTreeSelectionDialog createAddVariableSelectionDialog(final Shell shell, final boolean allowMultiple, final IResource initialSelection) {
        final ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(shell, new WorkbenchLabelProvider(),
                new BaseWorkbenchContentProvider());
        dialog.setAllowMultiple(allowMultiple);
        dialog.setTitle("Select variables file");
        dialog.setMessage("Select the variables file to import:");
        dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());
        if(initialSelection != null) {
            dialog.setInitialSelection(initialSelection);
        }
        return dialog;
    }
    
    private String extractResourcePath(final IResource resource) {
        if (resource.getProject().equals(currentProject)) {
            return resource.getProjectRelativePath().toString();
        } else {
            return ".." + resource.getFullPath().toString();
        }
    }
    
    private void handleVariableAdd(final List<String> paths) {
        final List<ImportedVariablesFile> currentVariables = ((Settings) variablesViewer.getInput()).getImportedVariables();
        for (final String newPathString : paths) {
            final ArrayList<String> args = newArrayList(newPathString);
            commandsStack.execute(new CreateSettingKeywordCallCommand(settingsSection, "Variables", args));
            currentVariables.add(new ImportedVariablesFile(args));
        }
        variablesViewer.refresh();
    }
    
    private void handleVariableRemove(final Settings importedSettings, final List<ImportedVariablesFile> resourcesToRemove) {
        final List<RobotSetting> settingsToRemove = newArrayList();
        final List<RobotKeywordCall> currentVariables = settingsSection.getVariablesSettings();
        for (final RobotElement element : currentVariables) {
            final RobotSetting setting = (RobotSetting) element;
            final List<String> args = setting.getArguments();
            if(!args.isEmpty()) {
                if(resourcesToRemove.contains(new ImportedVariablesFile(args))) {
                    settingsToRemove.add(setting);
                }
            }
        }
        importedSettings.getImportedVariables().removeAll(resourcesToRemove);
        commandsStack.execute(new DeleteSettingKeywordCallCommand(settingsToRemove));
        variablesViewer.refresh();
    }
    
    private void handleVariablesPathEdit(final Settings importedSettings, final ImportedVariablesFile variablesFile, final String newPath) {
        final List<RobotKeywordCall> currentVariables = settingsSection.getVariablesSettings();
        for (final RobotElement element : currentVariables) {
            final RobotSetting setting = (RobotSetting) element;
            final List<String> args = setting.getArguments();
            if (!args.isEmpty() && args.equals(variablesFile.getArgs())) {
                args.set(0, newPath);
                commandsStack.execute(new SetSettingKeywordCallCommand(setting, args));
                variablesFile.setArgs(args);
                break;
            }
        }
        variablesViewer.refresh();
    }
    
    private void handleVariablesArgsEdit(final Settings importedSettings, final ImportedVariablesFile variablesFile, final List<String> newArgs) {
        final List<RobotKeywordCall> currentVariables = settingsSection.getVariablesSettings();
        for (final RobotElement element : currentVariables) {
            final RobotSetting setting = (RobotSetting) element;
            final List<String> args = setting.getArguments();
            if(!args.isEmpty() && args.equals(variablesFile.getArgs())) {
                final List<String> newVariablesArguments = newArrayList(args.get(0));
                newVariablesArguments.addAll(newArgs);
                commandsStack.execute(new SetSettingKeywordCallCommand(setting, newVariablesArguments));
                variablesFile.setArgs(newVariablesArguments);
                break;
            }
        }
        variablesViewer.refresh();
    }
    
    private void createViewerSelectionListener(final Button removeBtn, final Button editPathBtn, final Button editArgsBtn) {
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
    
    private static class VariablesLabelProvider extends ColumnLabelProvider implements IStyledLabelProvider {

        @Override
        public Image getImage(final Object element) {
            return ImagesManager.getImage(RedImages.getRobotScalarVariableImage());
        }

        @Override
        public String getText(final Object element) {
            return "";
        }

        @Override
        public StyledString getStyledText(final Object element) {
            final ImportedVariablesFile importedVariable = (ImportedVariablesFile) element;
            final List<String> args = importedVariable.getArgs();
            final StyledString text = new StyledString("");
            if(args != null && !args.isEmpty()) {
                final IPath path = new Path(args.get(0));
                final String parentPath = path.segmentCount() > 1 ? path.removeLastSegments(1).toString() : currentProject.getName();
                final StringBuilder fileArgs = new StringBuilder("");
                for (int i = 1; i < args.size(); i++) {
                    fileArgs.append(":" + args.get(i));
                }
                
                text.append(path.lastSegment());
                text.append(fileArgs.toString() + " - " + parentPath, new Styler() {
                    @Override
                    public void applyStyles(final TextStyle textStyle) {
                        textStyle.foreground = RedTheme.getEclipseDecorationColor();
                    }
                });
            }
            return text;
        }
    }
    
    private static class ImportVariablesContentProvider implements IStructuredContentProvider {

        @Override
        public void dispose() {
            // nothing to do
        }

        @Override
        public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
            // nothing to do
        }

        @Override
        public Object[] getElements(final Object inputElement) {
            final List<ImportedVariablesFile> variables = ((Settings) inputElement).getImportedVariables();
//            Collections.sort(libraries, new Comparator<ImportedVariablesFile>() {
//
//                @Override
//                public int compare(final ImportedVariablesFile spec1, final ImportedVariablesFile spec2) {
//                    return spec1.compareTo(spec2);
//                }
//            });
            return variables.toArray();
        }

    }
    
    private static class EditArgumentsDialog extends Dialog {

        private List<String> currentArgs;
        
        private VariableFilesArgumentsEditor argsEditor;

        protected EditArgumentsDialog(final Shell parentShell, final List<String> currentArgs) {
            super(parentShell);
            this.currentArgs = currentArgs;
        }

        @Override
        public void create() {
            super.create();
            getShell().setText("Edit Arguments");
        }

        @Override
        protected Control createDialogArea(final Composite parent) {
            final Composite dialogComposite = (Composite) super.createDialogArea(parent);
            GridLayoutFactory.fillDefaults().numColumns(1).margins(3, 3).applyTo(dialogComposite);
            GridDataFactory.fillDefaults().grab(true, true).minSize(400, 200).applyTo(dialogComposite);

            argsEditor = new VariableFilesArgumentsEditor();
            argsEditor.createArgumentsEditor(dialogComposite, currentArgs);

            return dialogComposite;
        }

        @Override
        protected void okPressed() {
            currentArgs = argsEditor.getArguments();
            super.okPressed();
        }

        @Override
        public boolean close() {
            return super.close();
        }

        public List<String> getArguments() {
            return currentArgs;
        }
    }
}

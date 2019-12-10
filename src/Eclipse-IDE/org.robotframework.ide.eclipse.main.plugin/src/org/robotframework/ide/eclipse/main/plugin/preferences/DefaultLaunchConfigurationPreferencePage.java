/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.preferences;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static org.robotframework.red.swt.Listeners.keyPressedAdapter;
import static org.robotframework.red.swt.Listeners.menuShownAdapter;
import static org.robotframework.red.swt.Listeners.widgetSelectedAdapter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import org.eclipse.compare.internal.TabFolderLayout;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.rf.ide.core.execution.server.AgentConnectionServer;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.launch.tabs.BrowseButtons;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.CellsActivationStrategy;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.CellsActivationStrategy.RowTabbingStrategy;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.jface.dialogs.ScriptExportDialog;
import org.robotframework.red.jface.preferences.MultiLineStringFieldEditor;
import org.robotframework.red.jface.preferences.ParameterizedFilePathStringFieldEditor;
import org.robotframework.red.jface.preferences.RegexValidatedMultilineStringFieldEditor;
import org.robotframework.red.jface.viewers.ViewerColumnsFactory;
import org.robotframework.red.jface.viewers.ViewersConfigurator;
import org.robotframework.red.viewers.ElementAddingToken;
import org.robotframework.red.viewers.ElementsAddingEditingSupport;
import org.robotframework.red.viewers.RedCommonLabelProvider;
import org.robotframework.red.viewers.Selections;
import org.robotframework.red.viewers.StructuredContentProvider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DefaultLaunchConfigurationPreferencePage extends RedFieldEditorPreferencePage {

    private static final String MULTILINE_PATTERN = "[^\t]*";

    private final List<EnvVariable> envVars = new ArrayList<>();

    private TableViewer envVarsViewer;

    public DefaultLaunchConfigurationPreferencePage() {
        setDescription("Configure default robot launch configurations");
    }

    @Override
    protected void createFieldEditors() {
        final Composite parent = getFieldEditorParent();

        final TabFolder folder = new TabFolder(parent, SWT.NONE);
        folder.setLayout(new TabFolderLayout());
        folder.setLayoutData(new GridData(GridData.FILL_BOTH));

        final TabItem robotTab = new TabItem(folder, SWT.NONE);
        robotTab.setText("Robot");
        robotTab.setControl(createRobotLaunchConfigurationPreferences(folder));

        final TabItem listenerTab = new TabItem(folder, SWT.NONE);
        listenerTab.setText("Listener");
        listenerTab.setControl(createListenerLaunchConfigurationPreferences(folder));

        final TabItem executorTab = new TabItem(folder, SWT.NONE);
        executorTab.setText("Executor");
        executorTab.setControl(createExecutorLaunchConfigurationPreferences(folder));

        final TabItem environmentTab = new TabItem(folder, SWT.NONE);
        environmentTab.setText("Environment");
        environmentTab.setControl(createEnvironmentLaunchConfigurationPreferences(folder));
    }

    @Override
    public boolean performOk() {
        try {
            final Map<String, String> varsMap = new LinkedHashMap<>();
            envVars.forEach(var -> varsMap.put(var.getName(), var.getValue()));
            final String jsonMapping = new ObjectMapper().writeValueAsString(varsMap);
            getPreferenceStore().putValue(RedPreferences.LAUNCH_ENVIRONMENT_VARIABLES, jsonMapping);
        } catch (final JsonProcessingException e) {
            throw new IllegalStateException();
        }

        return super.performOk();
    }

    @Override
    protected void performDefaults() {
        final IPreferenceStore store = getPreferenceStore();
        store.putValue(RedPreferences.LAUNCH_ENVIRONMENT_VARIABLES,
                store.getDefaultString(RedPreferences.LAUNCH_ENVIRONMENT_VARIABLES));

        initializeEnvironmentVariablesValues();

        super.performDefaults();
    }

    private Control createRobotLaunchConfigurationPreferences(final Composite parent) {
        final Composite composite = new Composite(parent, SWT.NONE);

        final MultiLineStringFieldEditor additionalRobotArguments = new RegexValidatedMultilineStringFieldEditor(
                RedPreferences.LAUNCH_ADDITIONAL_ROBOT_ARGUMENTS, "Additional Robot Framework arguments:",
                MultiLineStringFieldEditor.UNLIMITED, 4, MultiLineStringFieldEditor.VALIDATE_ON_KEY_STROKE,
                MULTILINE_PATTERN, composite);
        additionalRobotArguments.setErrorMessage("Tabulators are not allowed in arguments editor");
        GridDataFactory.fillDefaults().span(2, 1).applyTo(additionalRobotArguments.getLabelControl(composite));
        additionalRobotArguments.load();
        addField(additionalRobotArguments);

        addButtons(additionalRobotArguments.getTextControl(composite), composite);

        return composite;
    }

    private Control createListenerLaunchConfigurationPreferences(final Composite parent) {
        final Composite composite = new Composite(parent, SWT.NONE);

        final StringFieldEditor remoteHost = new StringFieldEditor(RedPreferences.LAUNCH_AGENT_CONNECTION_HOST,
                "Server IP:", composite);
        remoteHost.setEmptyStringAllowed(false);
        remoteHost.setErrorMessage("Server IP cannot be empty");
        remoteHost.load();
        addField(remoteHost);
        GridDataFactory.fillDefaults().indent(0, 5).applyTo(remoteHost.getTextControl(composite));

        final IntegerFieldEditor remotePort = new IntegerFieldEditor(RedPreferences.LAUNCH_AGENT_CONNECTION_PORT,
                "Server port:", composite);
        remotePort.setValidRange(AgentConnectionServer.MIN_CONNECTION_PORT, AgentConnectionServer.MAX_CONNECTION_PORT);
        remotePort.load();
        addField(remotePort);
        GridDataFactory.fillDefaults().indent(0, 5).applyTo(remotePort.getTextControl(composite));

        final IntegerFieldEditor remoteTimeout = new IntegerFieldEditor(RedPreferences.LAUNCH_AGENT_CONNECTION_TIMEOUT,
                "Server connection timeout [s]:", composite);
        remoteTimeout.setValidRange(AgentConnectionServer.MIN_CONNECTION_TIMEOUT,
                AgentConnectionServer.MAX_CONNECTION_TIMEOUT);
        remoteTimeout.load();
        addField(remoteTimeout);
        GridDataFactory.fillDefaults().indent(0, 5).applyTo(remoteTimeout.getTextControl(composite));

        final Button exportBtn = new Button(composite, SWT.PUSH);
        GridDataFactory.swtDefaults().applyTo(exportBtn);
        exportBtn.setText("Export Client Script");
        exportBtn.addSelectionListener(
                widgetSelectedAdapter(e -> new ScriptExportDialog(getShell(), "TestRunnerAgent.py").open()));

        return composite;
    }

    private Control createExecutorLaunchConfigurationPreferences(final Composite parent) {
        final Composite composite = new Composite(parent, SWT.NONE);

        final MultiLineStringFieldEditor additionalInterpreterArguments = new RegexValidatedMultilineStringFieldEditor(
                RedPreferences.LAUNCH_ADDITIONAL_INTERPRETER_ARGUMENTS, "Additional interpreter arguments:",
                MultiLineStringFieldEditor.UNLIMITED, 4, MultiLineStringFieldEditor.VALIDATE_ON_KEY_STROKE,
                MULTILINE_PATTERN, composite);
        additionalInterpreterArguments.setErrorMessage("Tabulators are not allowed in arguments editor");
        GridDataFactory.fillDefaults().span(2, 1).applyTo(additionalInterpreterArguments.getLabelControl(composite));
        additionalInterpreterArguments.load();
        addField(additionalInterpreterArguments);

        addButtons(additionalInterpreterArguments.getTextControl(composite), composite);

        final ParameterizedFilePathStringFieldEditor scriptPathEditor = new ParameterizedFilePathStringFieldEditor(
                RedPreferences.LAUNCH_EXECUTABLE_FILE_PATH, "Executable file to run Robot Framework tests:", composite);
        GridDataFactory.fillDefaults().span(2, 1).applyTo(scriptPathEditor.getLabelControl(composite));
        GridDataFactory.fillDefaults().span(2, 1).applyTo(scriptPathEditor.getTextControl(composite));
        scriptPathEditor.setErrorMessage("Value must be an existing file");
        scriptPathEditor.load();
        addField(scriptPathEditor);

        addButtons(scriptPathEditor, composite);

        final MultiLineStringFieldEditor additionalScriptArguments = new RegexValidatedMultilineStringFieldEditor(
                RedPreferences.LAUNCH_ADDITIONAL_EXECUTABLE_FILE_ARGUMENTS, "Additional executable file arguments:",
                MultiLineStringFieldEditor.UNLIMITED, 4, MultiLineStringFieldEditor.VALIDATE_ON_KEY_STROKE,
                MULTILINE_PATTERN, composite);
        additionalScriptArguments.setErrorMessage("Tabulators are not allowed in arguments editor");
        GridDataFactory.fillDefaults().span(2, 1).applyTo(additionalScriptArguments.getLabelControl(composite));
        additionalScriptArguments.load();
        addField(additionalScriptArguments);

        addButtons(additionalScriptArguments.getTextControl(composite), composite);

        return composite;
    }

    private void addButtons(final Text textControl, final Composite parent) {
        final Composite buttonsParent = new Composite(parent, SWT.NONE);
        GridLayoutFactory.fillDefaults().numColumns(2).applyTo(buttonsParent);
        GridDataFactory.fillDefaults().span(2, 1).align(SWT.END, SWT.FILL).applyTo(buttonsParent);
        BrowseButtons.selectVariableButton(buttonsParent, textControl::insert);
    }

    private void addButtons(final ParameterizedFilePathStringFieldEditor editor, final Composite composite) {
        final Composite buttonsParent = new Composite(composite, SWT.NONE);
        GridLayoutFactory.fillDefaults().numColumns(4).applyTo(buttonsParent);
        GridDataFactory.fillDefaults().span(2, 1).align(SWT.END, SWT.FILL).applyTo(buttonsParent);
        BrowseButtons.selectWorkspaceFileButton(buttonsParent, editor::setStringValue,
                "Select executor file to run Robot Framework tests:");
        BrowseButtons.selectSystemFileButton(buttonsParent, editor::setStringValue,
                BrowseButtons.getSystemDependentExecutableFileExtensions());
        BrowseButtons.selectVariableButton(buttonsParent, editor::insertValue);
    }

    private Control createEnvironmentLaunchConfigurationPreferences(final Composite parent) {
        final Composite composite = new Composite(parent, SWT.NULL);
        GridLayoutFactory.fillDefaults().applyTo(composite);
        GridDataFactory.fillDefaults().grab(true, true).indent(0, 10).span(2, 1).applyTo(composite);

        final Label envVarsTableDescription = new Label(composite, SWT.WRAP);
        envVarsTableDescription.setText("Environment variables to set:");
        GridDataFactory.fillDefaults()
                .grab(true, false)
                .applyTo(envVarsTableDescription);
        envVarsViewer = createEnvironmentVariablesViewer(composite);
        initializeEnvironmentVariablesValues();

        return composite;
    }

    private TableViewer createEnvironmentVariablesViewer(final Composite parent) {
        final TableViewer viewer = new TableViewer(parent,
                SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
        CellsActivationStrategy.addActivationStrategy(viewer, RowTabbingStrategy.MOVE_TO_NEXT);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(viewer.getTable());
        viewer.getTable().setLinesVisible(true);
        viewer.getTable().setHeaderVisible(true);

        final Supplier<EnvVariable> newVarsSupplier = () -> {
            final EnvVariable newVar = new EnvVariable("", "");
            envVars.add(newVar);
            return newVar;
        };
        viewer.setContentProvider(new EnvVarsContentProvider());
        ViewerColumnsFactory.newColumn("Variable")
                .withWidth(200)
                .labelsProvidedBy(new EnvVarsNamesLabelProvider())
                .editingEnabled()
                .editingSupportedBy(new EnvVarsNamesEditingSupport(viewer, newVarsSupplier))
                .createFor(viewer);
        ViewerColumnsFactory.newColumn("Value")
                .withWidth(120)
                .withMinWidth(80)
                .shouldGrabAllTheSpaceLeft(true)
                .labelsProvidedBy(new EnvVarsValuesLabelProvider())
                .editingEnabled()
                .editingSupportedBy(new EnvVarsValuesEditingSupport(viewer, newVarsSupplier))
                .createFor(viewer);

        ViewersConfigurator.enableDeselectionPossibility(viewer);
        ViewersConfigurator.disableContextMenuOnHeader(viewer);

        final Runnable selectionRemover = () -> {
            final List<EnvVariable> varsToRemove = Selections.getElements((IStructuredSelection) viewer.getSelection(),
                    EnvVariable.class);
            envVars.removeAll(varsToRemove);
            viewer.refresh();
            validateEnvironmentVariables();
        };
        final Menu menu = new Menu(viewer.getTable());
        final MenuItem deleteMenuItem = new MenuItem(menu, SWT.PUSH);
        deleteMenuItem.setText("Delete\tDel");
        deleteMenuItem.setImage(ImagesManager.getImage(RedImages.getDeleteImage()));
        deleteMenuItem.addSelectionListener(widgetSelectedAdapter(e -> selectionRemover.run()));

        viewer.getTable().setMenu(menu);
        menu.addMenuListener(menuShownAdapter(e -> {
            final boolean anyVarSelected = !Selections
                    .getElements((IStructuredSelection) viewer.getSelection(), EnvVariable.class)
                    .isEmpty();
            deleteMenuItem.setEnabled(anyVarSelected);
        }));
        viewer.getTable().addKeyListener(keyPressedAdapter(e -> {
            if (e.keyCode == SWT.DEL) {
                selectionRemover.run();
            }
        }));
        return viewer;
    }

    private void initializeEnvironmentVariablesValues() {
        envVars.clear();
        RedPlugin.getDefault().getPreferences().getLaunchEnvironmentVariables().forEach((name, value) -> {
            envVars.add(new EnvVariable(name, value));
        });

        envVarsViewer.setInput(envVars);

        validateEnvironmentVariables();
    }

    private void validateEnvironmentVariables() {
        final Map<String, Long> nameCounts = envVars.stream().collect(groupingBy(EnvVariable::getName, counting()));

        for (final Entry<String, Long> entry : nameCounts.entrySet()) {
            if (entry.getValue() > 1) {
                setValid(false);
                setErrorMessage("There are duplicated environment variables definitions");
                return;
            } else if (entry.getKey().isEmpty()) {
                setValid(false);
                setErrorMessage("Empty environment variable names are not allowed");
                return;
            } else if (entry.getKey().contains("=")) {
                setValid(false);
                setErrorMessage("Equal sign is not allowed in environment variable name");
                return;
            }
        }

        setValid(true);
        setErrorMessage(null);
    }

    private static class EnvVarsContentProvider extends StructuredContentProvider {

        @Override
        public Object[] getElements(final Object inputElement) {
            final List<Object> all = new ArrayList<>();
            all.addAll((List<?>) inputElement);
            all.add(new ElementAddingToken("environment variable", true));
            return all.toArray();
        }
    }

    private static final class EnvVariable {

        private String name;

        private String value;

        public EnvVariable(final String name, final String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(final String value) {
            this.value = value;
        }
    }

    private static class EnvVarsNamesLabelProvider extends RedCommonLabelProvider {

        @Override
        public Image getImage(final Object element) {
            if (element instanceof ElementAddingToken) {
                return ImagesManager.getImage(RedImages.getAddImage());
            }
            return null;
        }

        @Override
        public StyledString getStyledText(final Object element) {
            if (element instanceof EnvVariable) {
                final EnvVariable var = (EnvVariable) element;
                return new StyledString(var.getName());
            } else {
                return ((ElementAddingToken) element).getStyledText();
            }
        }
    }

    private class EnvVarsNamesEditingSupport extends ElementsAddingEditingSupport {

        public EnvVarsNamesEditingSupport(final ColumnViewer viewer, final Supplier<?> creator) {
            super(viewer, 0, creator);
        }

        @Override
        protected CellEditor getCellEditor(final Object element) {
            if (element instanceof EnvVariable) {
                return new TextCellEditor((Composite) getViewer().getControl());
            }
            return super.getCellEditor(element);
        }

        @Override
        protected Object getValue(final Object element) {
            if (element instanceof EnvVariable) {
                return ((EnvVariable) element).getName();
            }
            return "";
        }

        @Override
        protected void setValue(final Object element, final Object value) {
            if (element instanceof EnvVariable) {
                ((EnvVariable) element).setName((String) value);

                validateEnvironmentVariables();
                getViewer().refresh();
            } else {
                super.setValue(element, value);
            }
        }
    }

    private static class EnvVarsValuesLabelProvider extends RedCommonLabelProvider {

        @Override
        public StyledString getStyledText(final Object element) {
            if (element instanceof EnvVariable) {
                final EnvVariable var = (EnvVariable) element;
                return new StyledString(var.getValue());
            }
            return new StyledString();
        }
    }

    private static class EnvVarsValuesEditingSupport extends ElementsAddingEditingSupport {

        public EnvVarsValuesEditingSupport(final ColumnViewer viewer, final Supplier<?> creator) {
            super(viewer, 0, creator);
        }

        @Override
        protected CellEditor getCellEditor(final Object element) {
            if (element instanceof EnvVariable) {
                return new TextCellEditor((Composite) getViewer().getControl());
            }
            return super.getCellEditor(element);
        }

        @Override
        protected Object getValue(final Object element) {
            if (element instanceof EnvVariable) {
                return ((EnvVariable) element).getValue();
            }
            return "";
        }

        @Override
        protected void setValue(final Object element, final Object value) {
            if (element instanceof EnvVariable) {
                ((EnvVariable) element).setValue((String) value);

                getViewer().refresh();
            } else {
                super.setValue(element, value);
            }
        }
    }

}

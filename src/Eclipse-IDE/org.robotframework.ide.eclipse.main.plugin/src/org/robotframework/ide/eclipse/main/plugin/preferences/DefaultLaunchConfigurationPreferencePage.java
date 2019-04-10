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
import java.util.Objects;
import java.util.function.Supplier;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.RowExposingTableViewer;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.ViewerColumnsFactory;
import org.eclipse.jface.viewers.ViewersConfigurator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.rf.ide.core.execution.server.AgentConnectionServer;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.launch.tabs.BrowseButtons;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.CellsActivationStrategy;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.CellsActivationStrategy.RowTabbingStrategy;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.jface.dialogs.ScriptExportDialog;
import org.robotframework.red.jface.preferences.ParameterizedFilePathStringFieldEditor;
import org.robotframework.red.viewers.ElementAddingToken;
import org.robotframework.red.viewers.ElementsAddingEditingSupport;
import org.robotframework.red.viewers.RedCommonLabelProvider;
import org.robotframework.red.viewers.Selections;
import org.robotframework.red.viewers.StructuredContentProvider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DefaultLaunchConfigurationPreferencePage extends RedFieldEditorPreferencePage {

    private final List<EnvVariable> envVars = new ArrayList<>();

    private TableViewer envVarsViewer;

    public DefaultLaunchConfigurationPreferencePage() {
        setDescription("Configure default robot launch configurations");
    }

    @Override
    protected void createFieldEditors() {
        final Composite parent = getFieldEditorParent();

        createRobotLaunchConfigurationPreferences(parent);
        createListenerLaunchConfigurationPreferences(parent);
        createExecutorLaunchConfigurationPreferences(parent);
        createEnvironmentLaunchConfigurationPreferences(parent);
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

    private void createRobotLaunchConfigurationPreferences(final Composite parent) {
        final Group group = new Group(parent, SWT.NONE);
        group.setText("Robot tab");
        GridLayoutFactory.fillDefaults().applyTo(group);
        GridDataFactory.fillDefaults().grab(true, false).indent(0, 10).span(2, 1).applyTo(group);

        final StringFieldEditor additionalRobotArguments = new StringFieldEditor(
                RedPreferences.LAUNCH_ADDITIONAL_ROBOT_ARGUMENTS, "Additional Robot Framework arguments:", group);
        GridDataFactory.fillDefaults().span(2, 1).applyTo(additionalRobotArguments.getLabelControl(group));
        additionalRobotArguments.load();
        addField(additionalRobotArguments);

        BrowseButtons.selectVariableButton(group, additionalRobotArguments.getTextControl(group)::insert);
    }

    private void createListenerLaunchConfigurationPreferences(final Composite parent) {
        final Group group = new Group(parent, SWT.NONE);
        group.setText("Listener tab");
        GridLayoutFactory.fillDefaults().applyTo(group);
        GridDataFactory.fillDefaults().grab(true, false).indent(0, 10).span(2, 1).applyTo(group);

        final StringFieldEditor remoteHost = new StringFieldEditor(RedPreferences.LAUNCH_AGENT_CONNECTION_HOST,
                "Server IP:", group);
        remoteHost.setEmptyStringAllowed(false);
        remoteHost.setErrorMessage("Server IP cannot be empty");
        remoteHost.load();
        addField(remoteHost);

        final IntegerFieldEditor remotePort = new IntegerFieldEditor(RedPreferences.LAUNCH_AGENT_CONNECTION_PORT,
                "Server port:", group);
        remotePort.setValidRange(AgentConnectionServer.MIN_CONNECTION_PORT, AgentConnectionServer.MAX_CONNECTION_PORT);
        remotePort.load();
        addField(remotePort);

        final IntegerFieldEditor remoteTimeout = new IntegerFieldEditor(RedPreferences.LAUNCH_AGENT_CONNECTION_TIMEOUT,
                "Server connection timeout [s]:", group);
        remoteTimeout.setValidRange(AgentConnectionServer.MIN_CONNECTION_TIMEOUT,
                AgentConnectionServer.MAX_CONNECTION_TIMEOUT);
        remoteTimeout.load();
        addField(remoteTimeout);

        final Button exportBtn = new Button(group, SWT.PUSH);
        GridDataFactory.swtDefaults().applyTo(exportBtn);
        exportBtn.setText("Export Client Script");
        exportBtn.addSelectionListener(
                widgetSelectedAdapter(e -> new ScriptExportDialog(getShell(), "TestRunnerAgent.py").open()));
    }

    private void createExecutorLaunchConfigurationPreferences(final Composite parent) {
        final Group group = new Group(parent, SWT.NONE);
        group.setText("Executor tab");
        GridLayoutFactory.fillDefaults().applyTo(group);
        GridDataFactory.fillDefaults().grab(true, false).indent(0, 10).span(2, 1).applyTo(group);

        final StringFieldEditor additionalInterpreterArguments = new StringFieldEditor(
                RedPreferences.LAUNCH_ADDITIONAL_INTERPRETER_ARGUMENTS, "Additional interpreter arguments:", group);
        GridDataFactory.fillDefaults().span(2, 1).applyTo(additionalInterpreterArguments.getLabelControl(group));
        additionalInterpreterArguments.load();
        addField(additionalInterpreterArguments);

        BrowseButtons.selectVariableButton(group, additionalInterpreterArguments.getTextControl(group)::insert);

        final ParameterizedFilePathStringFieldEditor scriptPathEditor = new ParameterizedFilePathStringFieldEditor(
                RedPreferences.LAUNCH_EXECUTABLE_FILE_PATH, "Executable file to run Robot Framework tests:", group);
        GridDataFactory.fillDefaults().span(2, 1).applyTo(scriptPathEditor.getLabelControl(group));
        GridDataFactory.fillDefaults().span(2, 1).applyTo(scriptPathEditor.getTextControl(group));
        scriptPathEditor.setErrorMessage("Value must be an existing file");
        scriptPathEditor.load();
        addField(scriptPathEditor);

        final Composite buttonsParent = new Composite(group, SWT.NONE);
        GridLayoutFactory.fillDefaults().numColumns(3).applyTo(buttonsParent);
        GridDataFactory.fillDefaults().span(2, 1).align(SWT.END, SWT.FILL).applyTo(buttonsParent);
        BrowseButtons.selectWorkspaceFileButton(buttonsParent, scriptPathEditor::setStringValue,
                "Select executor file to run Robot Framework tests:");
        BrowseButtons.selectSystemFileButton(buttonsParent, scriptPathEditor::setStringValue,
                BrowseButtons.getSystemDependentExecutableFileExtensions());
        BrowseButtons.selectVariableButton(buttonsParent, scriptPathEditor::insertValue);

        final StringFieldEditor additionalScriptArguments = new StringFieldEditor(
                RedPreferences.LAUNCH_ADDITIONAL_EXECUTABLE_FILE_ARGUMENTS, "Additional executable file arguments:",
                group);
        GridDataFactory.fillDefaults().span(2, 1).applyTo(additionalScriptArguments.getLabelControl(group));
        additionalScriptArguments.load();
        addField(additionalScriptArguments);

        BrowseButtons.selectVariableButton(group, additionalScriptArguments.getTextControl(group)::insert);
    }

    private void createEnvironmentLaunchConfigurationPreferences(final Composite parent) {
        final Group group = new Group(parent, SWT.NONE);
        group.setText("Environment tab");
        GridLayoutFactory.fillDefaults().applyTo(group);
        GridDataFactory.fillDefaults().grab(true, true).indent(0, 10).span(2, 1).applyTo(group);

        final Label envVarsTableDescription = new Label(group, SWT.WRAP);
        envVarsTableDescription.setText("Environment variables to set:");
        GridDataFactory.fillDefaults()
                .grab(true, false)
                .applyTo(envVarsTableDescription);
        envVarsViewer = createEnvironmentVariablesViewer(group);
        initializeEnvironmentVariablesValues();
    }

    private TableViewer createEnvironmentVariablesViewer(final Composite parent) {
        final RowExposingTableViewer viewer = new RowExposingTableViewer(parent,
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
                .withWidth(150)
                .shouldGrabAllTheSpaceLeft(true)
                .labelsProvidedBy(new EnvVarsNamesLabelProvider())
                .editingEnabled()
                .editingSupportedBy(new EnvVarsNamesEditingSupport(viewer, newVarsSupplier))
                .createFor(viewer);
        ViewerColumnsFactory.newColumn("Value")
                .withWidth(120)
                .withMinWidth(80)
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

        @Override
        public boolean equals(final Object obj) {
            if (obj != null && obj.getClass() == EnvVariable.class) {
                final EnvVariable that = (EnvVariable) obj;
                return Objects.equals(this.name, that.name) && Objects.equals(this.value, that.value);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, value);
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

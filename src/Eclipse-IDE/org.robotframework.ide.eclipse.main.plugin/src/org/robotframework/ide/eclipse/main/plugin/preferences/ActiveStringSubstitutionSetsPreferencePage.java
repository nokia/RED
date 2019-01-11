/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.preferences;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.robotframework.red.swt.Listeners.keyPressedAdapter;
import static org.robotframework.red.swt.Listeners.menuShownAdapter;
import static org.robotframework.red.swt.Listeners.widgetSelectedAdapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.RowExposingTreeViewer;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.Stylers;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerColumnsFactory;
import org.eclipse.jface.viewers.ViewersConfigurator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences.OverriddenVariable;
import org.robotframework.ide.eclipse.main.plugin.launch.variables.RedStringVariablesManager;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.CellsActivationStrategy;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.CellsActivationStrategy.RowTabbingStrategy;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.viewers.ElementAddingToken;
import org.robotframework.red.viewers.ElementsAddingEditingSupport;
import org.robotframework.red.viewers.RedCommonLabelProvider;
import org.robotframework.red.viewers.Selections;
import org.robotframework.red.viewers.TreeContentProvider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Objects;

public class ActiveStringSubstitutionSetsPreferencePage extends RedPreferencePage {

    public static final String ID = "org.robotframework.ide.eclipse.main.plugin.preferences.launch.activeVarsSets";

    private TreeViewer viewer;

    private final RedStringVariablesManager variablesManager;

    private final List<StringVariablesCollection> variableSets;

    private StringVariablesCollection activeVariablesSet;

    public ActiveStringSubstitutionSetsPreferencePage() {
        super("Active String Substitution sets");
        this.variablesManager = new RedStringVariablesManager();
        this.variableSets = new ArrayList<>();
        this.activeVariablesSet = null;
    }

    @Override
    protected Control createContents(final Composite parent) {
        GridLayoutFactory.fillDefaults().numColumns(2).applyTo(parent);
        createLink(parent);
        viewer = createSetsViewer(parent);

        new Label(parent, SWT.NONE);
        initializeValues();

        return parent;
    }

    private void createLink(final Composite parent) {
        final Link link = new Link(parent, SWT.NONE);
        GridDataFactory.fillDefaults().grab(true, false).span(2, 1).hint(600, SWT.DEFAULT).applyTo(link);

        final String stringSubstitutionPageId = "org.eclipse.debug.ui.StringVariablePreferencePage";

        final String text = "Create string substitution variables set and choose the active one (if any). "
                + "This can be used to quickly switch between different values for already defined " + "<a href=\""
                + stringSubstitutionPageId + "\">String Substitution</a> variables.";
        link.setText(text);
        link.addSelectionListener(widgetSelectedAdapter(e -> {
            if (stringSubstitutionPageId.equals(e.text)) {
                PreferencesUtil.createPreferenceDialogOn(parent.getShell(), e.text, null, null);
            }
        }));
    }

    private TreeViewer createSetsViewer(final Composite parent) {
        final RowExposingTreeViewer viewer = new RowExposingTreeViewer(parent,
                SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
        CellsActivationStrategy.addActivationStrategy(viewer, RowTabbingStrategy.MOVE_TO_NEXT);
        GridDataFactory.fillDefaults().indent(10, 0).grab(true, true).span(2, 1).applyTo(viewer.getTree());
        viewer.getTree().setLinesVisible(true);
        viewer.getTree().setHeaderVisible(true);

        viewer.setContentProvider(new StringVariablesSetsContentProvider());

        final Supplier<?> newElementsSupplier = new NewElementsSupplier();
        ViewerColumnsFactory.newColumn("Variable")
                .withWidth(200)
                .withMinWidth(80)
                .labelsProvidedBy(new NamesColumnLabelProvider())
                .editingEnabled()
                .editingSupportedBy(new NamesEditingSupport(viewer, newElementsSupplier))
                .createFor(viewer);
        ViewerColumnsFactory.newColumn("Value")
                .withWidth(150)
                .withMinWidth(80)
                .shouldGrabAllTheSpaceLeft(true)
                .labelsProvidedBy(new ValuesLabelProvider())
                .editingEnabled()
                .editingSupportedBy(new ValuesEditingSupport(viewer, newElementsSupplier))
                .createFor(viewer);

        ViewersConfigurator.enableDeselectionPossibility(viewer);
        ViewersConfigurator.disableContextMenuOnHeader(viewer);

        createViewerMenu(viewer);

        return viewer;
    }

    private void createViewerMenu(final RowExposingTreeViewer viewer) {
        final Runnable selectionRemover = () -> {
            final IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
            final List<StringVariablesCollection> setsToRemove = Selections.getElements(selection,
                    StringVariablesCollection.class);
            variableSets.removeAll(setsToRemove);
            if (setsToRemove.contains(activeVariablesSet)) {
                activeVariablesSet = null;
            }
            validate();
            viewer.refresh();
        };
        final Menu menu = new Menu(viewer.getTree());

        final MenuItem activateMenuItem = new MenuItem(menu, SWT.PUSH);
        activateMenuItem.setText("Activate");
        activateMenuItem.addSelectionListener(widgetSelectedAdapter(e -> {
            final IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
            activeVariablesSet = Selections.getSingleElement(selection, StringVariablesCollection.class);

            viewer.refresh();
        }));

        final MenuItem deactivateMenuItem = new MenuItem(menu, SWT.PUSH);
        deactivateMenuItem.setText("Deactivate");
        deactivateMenuItem.addSelectionListener(widgetSelectedAdapter(e -> {
            activeVariablesSet = null;

            viewer.refresh();
        }));

        new MenuItem(menu, SWT.SEPARATOR);

        final MenuItem cleanMenuItem = new MenuItem(menu, SWT.PUSH);
        cleanMenuItem.setText("Clean overridden value(s)");
        cleanMenuItem.addSelectionListener(widgetSelectedAdapter(e -> {
            final IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();

            final List<StringVariable> variablesToClean = new ArrayList<>();
            Selections.getElementsStream(selection, StringVariablesCollection.class)
                    .map(StringVariablesCollection::getVariables)
                    .forEach(variablesToClean::addAll);
            Selections.getElementsStream(selection, StringVariable.class).forEach(variablesToClean::add);

            variablesToClean.stream().filter(var -> var.isOverridden()).forEach(var -> var.setOverriddenValue(null));

            viewer.refresh();
        }));

        final MenuItem deleteMenuItem = new MenuItem(menu, SWT.PUSH);
        deleteMenuItem.setText("Delete\tDel");
        deleteMenuItem.setImage(ImagesManager.getImage(RedImages.getDeleteImage()));
        deleteMenuItem.addSelectionListener(widgetSelectedAdapter(e -> selectionRemover.run()));

        viewer.getTree().setMenu(menu);
        menu.addMenuListener(menuShownAdapter(e -> {
            final List<Object> selected = Selections.getElements((IStructuredSelection) viewer.getSelection(),
                    Object.class);
            activateMenuItem.setEnabled(selected.size() == 1 && selected.stream()
                    .allMatch(obj -> obj instanceof StringVariablesCollection && obj != activeVariablesSet));
            deactivateMenuItem.setEnabled(selected.size() == 1 && selected.stream()
                    .allMatch(obj -> obj instanceof StringVariablesCollection && obj == activeVariablesSet));
            deleteMenuItem.setEnabled(
                    !selected.isEmpty() && selected.stream().allMatch(obj -> obj instanceof StringVariablesCollection));
            cleanMenuItem.setEnabled(!selected.isEmpty() && (selected.stream()
                    .allMatch(obj -> obj instanceof StringVariablesCollection
                            && ((StringVariablesCollection) obj).getVariables()
                                    .stream()
                                    .anyMatch(StringVariable::isOverridden))
                    || selected.stream()
                            .allMatch(obj -> obj instanceof StringVariable && ((StringVariable) obj).isOverridden())));
        }));
        viewer.getTree().addKeyListener(keyPressedAdapter(e -> {
            if (e.keyCode == SWT.DEL) {
                selectionRemover.run();
            }
        }));
    }

    private void initializeValues() {
        final RedPreferences preferences = RedPlugin.getDefault().getPreferences();

        final Map<String, String> overridableVars = variablesManager.getOverridableVariables();
        preferences.getOverriddenVariablesSets().forEach((setName, overriddenVars) -> {
            variableSets.add(createSet(setName, overridableVars, overriddenVars));
        });

        final String active = preferences.getActiveVariablesSet().orElse(null);
        activeVariablesSet = variableSets.stream().filter(set -> set.getName().equals(active)).findFirst().orElse(null);

        viewer.setInput(variableSets);
        viewer.expandAll();

        validate();
    }

    private void validate() {
        final List<String> names = variableSets.stream().map(StringVariablesCollection::getName).collect(toList());
        final Set<String> namesSet = new HashSet<>(names);

        if (names.size() != namesSet.size()) {
            setValid(false);
            setErrorMessage("Names of variable sets are duplicated");
            return;
        }
        setValid(true);
        setErrorMessage(null);
    }

    private StringVariablesCollection createSet(final String setName, final Map<String, String> overridableVars,
            final List<OverriddenVariable> overriddenVars) {
        final StringVariablesCollection collection = new StringVariablesCollection(setName);

        overridableVars.forEach((varName, varValue) -> {
            final String overriddenValue = overriddenVars.stream()
                    .filter(v -> v.getName().equals(varName))
                    .findFirst()
                    .map(OverriddenVariable::getValue)
                    .orElse(null);
            collection.getVariables().add(new StringVariable(varName, varValue, overriddenValue));
        });
        return collection;
    }

    @Override
    public boolean performOk() {
        final Map<String, List<List<String>>> result = new LinkedHashMap<>();
        for (final StringVariablesCollection set : variableSets) {
            result.put(set.getName(), new ArrayList<>());
            set.getVariables()
                    .stream()
                    .filter(StringVariable::isOverridden)
                    .map(var -> newArrayList(var.getName(), var.getOverriddenValue()))
                    .forEach(var -> result.get(set.getName()).add(var));
        }

        try {
            final String jsonMapping = new ObjectMapper().writeValueAsString(result);
            getPreferenceStore().putValue(RedPreferences.STRING_VARIABLES_SETS, jsonMapping);
            getPreferenceStore().putValue(RedPreferences.STRING_VARIABLES_ACTIVE_SET,
                    activeVariablesSet == null ? "" : activeVariablesSet.getName());

        } catch (final JsonProcessingException e) {
            throw new IllegalStateException();
        }
        return true;
    }

    @Override
    protected void performDefaults() {
        variableSets.clear();
        activeVariablesSet = null;

        final IPreferenceStore store = getPreferenceStore();
        store.putValue(RedPreferences.STRING_VARIABLES_SETS,
                store.getDefaultString(RedPreferences.STRING_VARIABLES_SETS));
        store.putValue(RedPreferences.STRING_VARIABLES_ACTIVE_SET,
                store.getDefaultString(RedPreferences.STRING_VARIABLES_ACTIVE_SET));

        initializeValues();
        super.performDefaults();
    }

    private final class NewElementsSupplier implements Supplier<Object> {

        @Override
        public Object get() {
            final Set<String> currentNames = variableSets.stream()
                    .map(StringVariablesCollection::getName)
                    .collect(toSet());

            final Map<String, String> overridableVars = variablesManager.getOverridableVariables();
            final StringVariablesCollection newElem = createSet(chooseName("set", currentNames), overridableVars,
                    new ArrayList<>());
            variableSets.add(newElem);
            return newElem;
        }

        private String chooseName(final String startName, final Set<String> currentNames) {
            String chosenName = startName;
            for (int i = 1; i < Integer.MAX_VALUE; i++) {
                if (!currentNames.contains(chosenName)) {
                    return chosenName;
                }
                chosenName = startName + " " + i;
            }
            throw new IllegalStateException("Unable to generate unique name for new variable set");
        }
    }

    private static final class StringVariable {

        private final String name;

        private final String originalValue;

        private String overriddenValue;

        public StringVariable(final String name, final String originalValue, final String overriddenValue) {
            this.name = name;
            this.originalValue = originalValue;
            this.overriddenValue = Objects.equal(originalValue, overriddenValue) ? null : overriddenValue;
        }

        public String getName() {
            return name;
        }

        public String getOverriddenValue() {
            return overriddenValue;
        }

        public void setOverriddenValue(final String value) {
            this.overriddenValue = Objects.equal(originalValue, value) ? null : value;
        }

        public String getValueInUse() {
            return overriddenValue != null ? overriddenValue : originalValue;
        }

        public boolean isOverridden() {
            return overriddenValue != null;
        }
    }

    private static final class StringVariablesCollection {

        private String name;

        private final List<StringVariable> variables;

        public StringVariablesCollection(final String name) {
            this.name = name;
            this.variables = new ArrayList<>();
        }

        public List<StringVariable> getVariables() {
            return variables;
        }

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }
    }

    private class StringVariablesSetsContentProvider extends TreeContentProvider {

        @Override
        public Object[] getElements(final Object inputElement) {
            final List<Object> all = new ArrayList<>();
            all.addAll((List<?>) inputElement);
            all.add(new ElementAddingToken("variables set", true));
            return all.toArray();
        }

        @Override
        public Object getParent(final Object element) {
            return null;
        }

        @Override
        public boolean hasChildren(final Object element) {
            return element instanceof StringVariablesCollection;
        }

        @Override
        public Object[] getChildren(final Object parentElement) {
            if (parentElement instanceof StringVariablesCollection) {
                final StringVariablesCollection set = (StringVariablesCollection) parentElement;
                return set.getVariables().toArray();
            }
            return new Object[0];
        }
    }

    private class NamesColumnLabelProvider extends RedCommonLabelProvider {

        @Override
        public Image getImage(final Object element) {
            if (element instanceof ElementAddingToken) {
                return ImagesManager.getImage(RedImages.getAddImage());
            }
            return null;
        }

        @Override
        public StyledString getStyledText(final Object element) {
            if (element instanceof StringVariablesCollection) {
                final StringVariablesCollection set = (StringVariablesCollection) element;
                if (set == activeVariablesSet) {
                    final StyledString label = new StyledString();
                    label.append("[active] ", Stylers.Common.ECLIPSE_DECORATION_STYLER);
                    label.append(set.getName());
                    return label;

                } else {
                    return new StyledString(set.getName());
                }

            } else if (element instanceof StringVariable) {
                final StringVariable stringVar = (StringVariable) element;

                final Styler styler = stringVar.isOverridden() ? Stylers.Common.BOLD_STYLER
                        : Stylers.Common.EMPTY_STYLER;
                return new StyledString(stringVar.getName(), styler);

            } else {
                return ((ElementAddingToken) element).getStyledText();
            }
        }
    }

    private class NamesEditingSupport extends ElementsAddingEditingSupport {

        public NamesEditingSupport(final ColumnViewer viewer, final Supplier<?> creator) {
            super(viewer, 0, creator);
        }

        @Override
        protected CellEditor getCellEditor(final Object element) {
            if (element instanceof StringVariablesCollection) {
                final Composite parent = (Composite) getViewer().getControl();
                return new TextCellEditor(parent);
            }
            return super.getCellEditor(element);
        }

        @Override
        protected Object getValue(final Object element) {
            if (element instanceof StringVariablesCollection) {
                return ((StringVariablesCollection) element).getName();
            }
            return "";
        }

        @Override
        protected void setValue(final Object element, final Object value) {
            if (element instanceof StringVariablesCollection) {
                ((StringVariablesCollection) element).setName((String) value);

                validate();
                getViewer().refresh();

            } else {
                super.setValue(element, value);
            }
        }

        @Override
        protected void performPriorToEdit(final Object addedElement) {
            ((TreeViewer) getViewer()).setExpandedState(addedElement, true);
        }
    }

    private static class ValuesLabelProvider extends RedCommonLabelProvider {

        @Override
        public StyledString getStyledText(final Object element) {
            if (element instanceof StringVariable) {
                final StringVariable stringVar = (StringVariable) element;

                final Styler styler = stringVar.isOverridden() ? Stylers.Common.BOLD_STYLER
                        : Stylers.Common.EMPTY_STYLER;
                return new StyledString(stringVar.getValueInUse(), styler);
            }
            return new StyledString("");
        }
    }

    private static class ValuesEditingSupport extends ElementsAddingEditingSupport {

        public ValuesEditingSupport(final ColumnViewer viewer, final Supplier<?> creator) {
            super(viewer, 0, creator);
        }

        @Override
        protected CellEditor getCellEditor(final Object element) {
            if (element instanceof StringVariable) {
                return new TextCellEditor((Composite) getViewer().getControl());
            }
            return super.getCellEditor(element);
        }

        @Override
        protected Object getValue(final Object element) {
            if (element instanceof StringVariable) {
                return ((StringVariable) element).getValueInUse();
            }
            return "";
        }

        @Override
        protected void setValue(final Object element, final Object value) {
            if (element instanceof StringVariable) {
                ((StringVariable) element).setOverriddenValue((String) value);

                getViewer().refresh(element);

            } else {
                super.setValue(element, value);
            }
        }

        @Override
        protected void performPriorToEdit(final Object addedElement) {
            ((TreeViewer) getViewer()).setExpandedState(addedElement, true);
        }
    }
}

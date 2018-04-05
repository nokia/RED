/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.preferences;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.robotframework.red.swt.Listeners.keyPressedAdapter;
import static org.robotframework.red.swt.Listeners.menuShownAdapter;
import static org.robotframework.red.swt.Listeners.widgetSelectedAdapter;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.viewers.AlwaysDeactivatingCellEditor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.RowExposingTableViewer;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.Stylers;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.ViewerColumnsFactory;
import org.eclipse.jface.viewers.ViewersConfigurator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.rf.ide.core.rflint.RfLintRule;
import org.rf.ide.core.rflint.RfLintViolationSeverity;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.launch.tabs.BrowseButtons;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.CellsActivationStrategy;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.CellsActivationStrategy.RowTabbingStrategy;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.viewers.ElementAddingToken;
import org.robotframework.red.viewers.ElementsAddingEditingSupport;
import org.robotframework.red.viewers.RedCommonLabelProvider;
import org.robotframework.red.viewers.Selections;
import org.robotframework.red.viewers.StructuredContentProvider;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

public class RfLintValidationPreferencePage extends RedPreferencePage {

    private final List<RfLintRule> rules = new ArrayList<>();
    private TableViewer rulesViewer;

    private final List<RulesFile> rulesFiles = new ArrayList<>();
    private TableViewer rulesFilesViewer;

    private StringFieldEditor argumentsEditor;

    public RfLintValidationPreferencePage() {
        super("RfLint validation settings");
    }

    @Override
    protected Control createContents(final Composite parent) {
        final Label desc1 = new Label(parent, SWT.NONE);
        desc1.setText("Configure rules and their severity");
        rulesViewer = createRulesViewer(parent);

        final Label desc2 = new Label(parent, SWT.NONE);
        desc2.setText("Additional rules files");
        rulesFilesViewer = createRulesFilesViewer(parent);

        argumentsEditor = createAdditionalArgumentsEditor(parent);

        initializeValues();
        return parent;
    }

    private TableViewer createRulesViewer(final Composite parent) {
        final RowExposingTableViewer viewer = new RowExposingTableViewer(parent,
                SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
        CellsActivationStrategy.addActivationStrategy(viewer, RowTabbingStrategy.MOVE_TO_NEXT);
        GridDataFactory.fillDefaults().indent(10, 0).grab(true, true).applyTo(viewer.getTable());
        viewer.getTable().setLinesVisible(true);
        viewer.getTable().setHeaderVisible(true);

        final Supplier<RfLintRule> newRulesSupplier = () -> {
            final RfLintRule newRule = new RfLintRule("Rule", RfLintViolationSeverity.DEFAULT, "");
            rules.add(newRule);
            return newRule;
        };
        viewer.setContentProvider(new RulesAndFilesContentProvider("rule"));
        ViewerColumnsFactory.newColumn("Rule")
                .withWidth(150)
                .labelsProvidedBy(new RuleNamesLabelProvider())
                .editingEnabled()
                .editingSupportedBy(new RuleNamesEditingSupport(viewer, newRulesSupplier))
                .createFor(viewer);
        ViewerColumnsFactory.newColumn("Severity")
                .withWidth(80)
                .withMinWidth(30)
                .labelsProvidedBy(new RuleSeverityLabelProvider())
                .editingEnabled()
                .editingSupportedBy(new RuleSeverityEditingSupport(viewer, newRulesSupplier))
                .createFor(viewer);
        ViewerColumnsFactory.newColumn("Configuration")
                .withWidth(200)
                .shouldGrabAllTheSpaceLeft(true)
                .withMinWidth(30)
                .labelsProvidedBy(new RuleConfigurationLabelProvider())
                .editingEnabled()
                .editingSupportedBy(new RuleConfigurationEditingSupport(viewer, newRulesSupplier))
                .createFor(viewer);

        ViewersConfigurator.enableDeselectionPossibility(viewer);
        ViewersConfigurator.disableContextMenuOnHeader(viewer);

        final Runnable selectionRemover = () -> {
            final List<RfLintRule> rulesToRemove = Selections.getElements((IStructuredSelection) viewer.getSelection(),
                    RfLintRule.class);
            rules.removeAll(rulesToRemove);
            rulesViewer.refresh();
        };
        final Menu menu = new Menu(viewer.getTable());
        final MenuItem deleteMenuItem = new MenuItem(menu, SWT.PUSH);
        deleteMenuItem.setText("Delete\tDel");
        deleteMenuItem.setImage(ImagesManager.getImage(RedImages.getDeleteImage()));
        deleteMenuItem.addSelectionListener(widgetSelectedAdapter(e -> selectionRemover.run()));

        viewer.getTable().setMenu(menu);
        menu.addMenuListener(menuShownAdapter(e -> {
                final boolean anyRuleSelected = !Selections
                        .getElements((IStructuredSelection) viewer.getSelection(), RfLintRule.class)
                        .isEmpty();
                deleteMenuItem.setEnabled(anyRuleSelected);
        }));
        viewer.getTable().addKeyListener(keyPressedAdapter(e -> {
            if (e.keyCode == SWT.DEL) {
                selectionRemover.run();
            }
        }));
        return viewer;
    }

    private TableViewer createRulesFilesViewer(final Composite parent) {
        final RowExposingTableViewer viewer = new RowExposingTableViewer(parent,
                SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
        CellsActivationStrategy.addActivationStrategy(viewer, RowTabbingStrategy.MOVE_TO_NEXT);
        GridDataFactory.fillDefaults().indent(10, 0).grab(true, true).applyTo(viewer.getTable());
        viewer.getTable().setLinesVisible(false);
        viewer.getTable().setHeaderVisible(false);

        viewer.setContentProvider(new RulesAndFilesContentProvider("rules file"));
        ViewerColumnsFactory.newColumn("")
                .withWidth(200)
                .shouldGrabAllTheSpaceLeft(true)
                .withMinWidth(30)
                .labelsProvidedBy(new RulesFilesLabelProvider())
                .editingEnabled()
                .editingSupportedBy(new RulesFileEditingSupport(viewer,
                        RulesFileEditingSupport.openRuleFilesDialog(parent.getShell(),
                                ResourcesPlugin.getWorkspace().getRoot().getLocationURI(),
                                files -> rulesFiles.addAll(files))))
                .createFor(viewer);

        ViewersConfigurator.enableDeselectionPossibility(viewer);
        ViewersConfigurator.disableContextMenuOnHeader(viewer);

        final Runnable selectionRemover = () -> {
            final List<RulesFile> rulesFilesToRemove = Selections
                    .getElements((IStructuredSelection) viewer.getSelection(), RulesFile.class);
            rulesFiles.removeAll(rulesFilesToRemove);
            rulesFilesViewer.refresh();
        };
        final Menu menu = new Menu(viewer.getTable());
        final MenuItem deleteMenuItem = new MenuItem(menu, SWT.PUSH);
        deleteMenuItem.setText("Delete\tDel");
        deleteMenuItem.setImage(ImagesManager.getImage(RedImages.getDeleteImage()));
        deleteMenuItem.addSelectionListener(widgetSelectedAdapter(e -> selectionRemover.run()));

        viewer.getTable().setMenu(menu);
        menu.addMenuListener(menuShownAdapter(e -> {
            final boolean anyRulesFilesSelected = !Selections
                    .getElements((IStructuredSelection) viewer.getSelection(), RulesFile.class)
                    .isEmpty();
            deleteMenuItem.setEnabled(anyRulesFilesSelected);
        }));
        viewer.getTable().addKeyListener(keyPressedAdapter(e -> {
            if (e.keyCode == SWT.DEL) {
                selectionRemover.run();
            }
        }));
        return viewer;
    }

    private StringFieldEditor createAdditionalArgumentsEditor(final Composite parent) {
        final Composite group = new Composite(parent, SWT.NONE);
        GridDataFactory.fillDefaults().applyTo(group);

        final StringFieldEditor argumentsEditor = new StringFieldEditor(RedPreferences.RFLINT_ADDITIONAL_ARGUMENTS,
                "Additional arguments", group);
        GridDataFactory.fillDefaults().span(2, 1).applyTo(argumentsEditor.getLabelControl(group));
        GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).indent(10, 0).applyTo(
                argumentsEditor.getTextControl(group));
        argumentsEditor.setPreferenceStore(getPreferenceStore());
        argumentsEditor.setPage(this);

        BrowseButtons.selectVariableButton(group, argumentsEditor.getTextControl(group)::insert);

        return argumentsEditor;
    }

    private void initializeValues() {
        final RedPreferences preferences = RedPlugin.getDefault().getPreferences();
        rulesFiles.addAll(preferences.getRfLintRulesFiles().stream().map(RulesFile::new).collect(toList()));
        rules.addAll(preferences.getRfLintRules());

        rulesViewer.setInput(rules);
        rulesFilesViewer.setInput(rulesFiles);
        argumentsEditor.setStringValue(preferences.getRfLintAdditionalArguments());
    }

    @Override
    public boolean performOk() {
        final String allRulesFiles = rulesFiles.stream().map(RulesFile::getPath).collect(joining(";"));
        final String allRulesNames = rules.stream().map(RfLintRule::getRuleName).collect(joining(";"));
        final String allRulesSeverities = rules.stream()
                .map(RfLintRule::getSeverity)
                .map(RfLintViolationSeverity::name)
                .collect(joining(";"));
        final String allRulesArgs = rules.stream().map(RfLintRule::getConfiguration).collect(joining(";"));
        final String additionalArguments = argumentsEditor.getStringValue();

        getPreferenceStore().putValue(RedPreferences.RFLINT_RULES_FILES, allRulesFiles);
        getPreferenceStore().putValue(RedPreferences.RFLINT_RULES_CONFIG_NAMES, allRulesNames);
        getPreferenceStore().putValue(RedPreferences.RFLINT_RULES_CONFIG_SEVERITIES, allRulesSeverities);
        getPreferenceStore().putValue(RedPreferences.RFLINT_RULES_CONFIG_ARGS, allRulesArgs);
        getPreferenceStore().putValue(RedPreferences.RFLINT_ADDITIONAL_ARGUMENTS, additionalArguments);
        return true;
    }

    @Override
    protected void performDefaults() {
        rules.clear();
        rulesFiles.clear();
        rulesViewer.refresh();
        rulesFilesViewer.refresh();
        argumentsEditor.loadDefault();
        super.performDefaults();
    }

    private static class RulesFile {

        private String path;

        public RulesFile(final String path) {
            this.path = path;
        }

        public String getPath() {
            return path;
        }

        public void setPath(final String path) {
            this.path = path;
        }
    }

    private static class RulesAndFilesContentProvider extends StructuredContentProvider {

        private final String title;

        public RulesAndFilesContentProvider(final String title) {
            this.title = title;
        }

        @Override
        public Object[] getElements(final Object inputElement) {
            final List<Object> all = new ArrayList<>();
            all.addAll((List<?>) inputElement);
            all.add(new ElementAddingToken(title, true));
            return all.toArray();
        }
    }

    private static class RuleNamesLabelProvider extends RedCommonLabelProvider {

        @Override
        public Image getImage(final Object element) {
            if (element instanceof ElementAddingToken) {
                return ImagesManager.getImage(RedImages.getAddImage());
            }
            return null;
        }

        @Override
        public StyledString getStyledText(final Object element) {
            if (element instanceof RfLintRule) {
                final RfLintRule rule = (RfLintRule) element;
                final Styler styler = rule.isDead() ? Stylers.withForeground(200, 200, 200)
                        : Stylers.Common.EMPTY_STYLER;
                return new StyledString(rule.getRuleName(), styler);
            } else {
                return ((ElementAddingToken) element).getStyledText();
            }
        }
    }

    private static class RuleNamesEditingSupport extends ElementsAddingEditingSupport {

        public RuleNamesEditingSupport(final ColumnViewer viewer, final Supplier<?> creator) {
            super(viewer, 0, creator);
        }

        @Override
        protected CellEditor getCellEditor(final Object element) {
            if (element instanceof RfLintRule) {
                return new TextCellEditor((Composite) getViewer().getControl());
            }
            return super.getCellEditor(element);
        }

        @Override
        protected Object getValue(final Object element) {
            if (element instanceof RfLintRule) {
                return ((RfLintRule) element).getRuleName();
            }
            return "";
        }

        @Override
        protected void setValue(final Object element, final Object value) {
            if (element instanceof RfLintRule) {
                ((RfLintRule) element).setRuleName((String) value);
                getViewer().refresh(element);
            } else {
                super.setValue(element, value);
            }
        }
    }

    private static class RuleSeverityLabelProvider extends RedCommonLabelProvider {

        static final BiMap<RfLintViolationSeverity, String> SEVERITIES_LABELS = ImmutableBiMap
                .of(RfLintViolationSeverity.DEFAULT, "default", RfLintViolationSeverity.ERROR, "Error",
                        RfLintViolationSeverity.WARNING, "Warning", RfLintViolationSeverity.IGNORE, "Ignore");

        @Override
        public StyledString getStyledText(final Object element) {
            if (element instanceof RfLintRule) {
                final RfLintRule rule = (RfLintRule) element;
                final Styler styler = rule.isDead() ? Stylers.withForeground(200, 200, 200)
                        : Stylers.Common.EMPTY_STYLER;
                return new StyledString(SEVERITIES_LABELS.get(rule.getSeverity()), styler);
            }
            return new StyledString();
        }
    }

    private static class RuleSeverityEditingSupport extends ElementsAddingEditingSupport {

        private final List<RfLintViolationSeverity> indexes = newArrayList(RfLintViolationSeverity.DEFAULT,
                RfLintViolationSeverity.ERROR, RfLintViolationSeverity.WARNING, RfLintViolationSeverity.IGNORE);

        public RuleSeverityEditingSupport(final ColumnViewer viewer, final Supplier<?> creator) {
            super(viewer, 0, creator);
        }

        @Override
        protected CellEditor getCellEditor(final Object element) {
            if (element instanceof RfLintRule) {
                return new ComboBoxCellEditor((Composite) getViewer().getControl(),
                        RuleSeverityLabelProvider.SEVERITIES_LABELS.values().toArray(new String[0]));
            }
            return super.getCellEditor(element);
        }

        @Override
        protected Object getValue(final Object element) {
            if (element instanceof RfLintRule) {
                final RfLintRule rule = (RfLintRule) element;
                return indexes.indexOf(rule.getSeverity());
            }
            return "";
        }

        @Override
        protected void setValue(final Object element, final Object value) {
            if (element instanceof RfLintRule) {
                final int index = (int) value;
                final RfLintRule rule = (RfLintRule) element;
                rule.setSeverity(index == -1 ? RfLintViolationSeverity.DEFAULT : indexes.get(index));
                getViewer().refresh(element);
            } else {
                super.setValue(element, value);
            }
        }
    }

    private static class RuleConfigurationLabelProvider extends RedCommonLabelProvider {

        @Override
        public StyledString getStyledText(final Object element) {
            if (element instanceof RfLintRule) {
                final RfLintRule rule = (RfLintRule) element;
                final Styler styler = rule.isDead() ? Stylers.withForeground(200, 200, 200)
                        : Stylers.Common.EMPTY_STYLER;
                return new StyledString(rule.getConfiguration(), styler);
            }
            return new StyledString();
        }
    }

    private static class RuleConfigurationEditingSupport extends ElementsAddingEditingSupport {

        public RuleConfigurationEditingSupport(final ColumnViewer viewer, final Supplier<?> creator) {
            super(viewer, 0, creator);
        }

        @Override
        protected CellEditor getCellEditor(final Object element) {
            if (element instanceof RfLintRule) {
                return new TextCellEditor((Composite) getViewer().getControl());
            }
            return super.getCellEditor(element);
        }

        @Override
        protected Object getValue(final Object element) {
            if (element instanceof RfLintRule) {
                return ((RfLintRule) element).getConfiguration();
            }
            return "";
        }

        @Override
        protected void setValue(final Object element, final Object value) {
            if (element instanceof RfLintRule) {
                ((RfLintRule) element).setConfiguration((String) value);
                getViewer().refresh(element);
            } else {
                super.setValue(element, value);
            }
        }
    }

    private static class RulesFilesLabelProvider extends RedCommonLabelProvider {

        @Override
        public Image getImage(final Object element) {
            if (element instanceof ElementAddingToken) {
                return ImagesManager.getImage(RedImages.getAddImage());
            }
            return null;
        }

        @Override
        public StyledString getStyledText(final Object element) {
            if (element instanceof RulesFile) {
                final RulesFile rulesFile = (RulesFile) element;
                return new StyledString(rulesFile.getPath());
            } else {
                return ((ElementAddingToken) element).getStyledText();
            }
        }
    }

    private static class RulesFileEditingSupport extends ElementsAddingEditingSupport {

        public RulesFileEditingSupport(final ColumnViewer viewer, final Supplier<?> creator) {
            super(viewer, 0, creator);
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
            if (element instanceof RulesFile) {
                final RulesFile rulesFile = (RulesFile) element;

                final Shell shell = getViewer().getControl().getShell();
                scheduleViewerRefreshAndEditorActivation(
                        openRuleFilesDialog(shell, new File(rulesFile.getPath()).toURI(), files -> {
                            if (!files.isEmpty()) {
                                rulesFile.setPath(files.get(0).getPath());
                            }
                }).get());
            } else {
                super.setValue(element, value);
            }
        }

        static Supplier<?> openRuleFilesDialog(final Shell shell, final URI startingUri,
                final Consumer<List<RulesFile>> ruleFilesConsumer) {
            return () -> {
                final FileDialog dialog = new FileDialog(shell, SWT.OPEN | SWT.MULTI);
                dialog.setFilterExtensions(new String[] { "*.py", "*.*" });
                dialog.setFilterPath(new File(startingUri).getAbsolutePath());

                if (dialog.open() != null) {
                    final String[] chosenFiles = dialog.getFileNames();
                    final File root = new File(dialog.getFilterPath());

                    final List<RulesFile> rules = Stream.of(chosenFiles)
                            .map(path -> new File(root, path))
                            .map(File::getAbsolutePath)
                            .map(RulesFile::new)
                            .collect(toList());
                    ruleFilesConsumer.accept(rules);
                    return null;
                }
                return null;
            };
        }
    }
}

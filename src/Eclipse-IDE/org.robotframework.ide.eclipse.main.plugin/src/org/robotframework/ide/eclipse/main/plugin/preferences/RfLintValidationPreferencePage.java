/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.preferences;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.robotframework.red.swt.Listeners.keyPressedAdapter;
import static org.robotframework.red.swt.Listeners.menuShownAdapter;
import static org.robotframework.red.swt.Listeners.widgetSelectedAdapter;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.ScrolledFormText;
import org.rf.ide.core.environment.IRuntimeEnvironment;
import org.rf.ide.core.rflint.RfLintRule;
import org.rf.ide.core.rflint.RfLintRuleConfiguration;
import org.rf.ide.core.rflint.RfLintRules;
import org.rf.ide.core.rflint.RfLintViolationSeverity;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.launch.tabs.BrowseButtons;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.CellsActivationStrategy;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.CellsActivationStrategy.RowTabbingStrategy;
import org.robotframework.red.graphics.ColorsManager;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.jface.viewers.AlwaysDeactivatingCellEditor;
import org.robotframework.red.jface.viewers.Stylers;
import org.robotframework.red.jface.viewers.ViewerColumnsFactory;
import org.robotframework.red.jface.viewers.ViewersConfigurator;
import org.robotframework.red.swt.Listeners;
import org.robotframework.red.swt.SwtThread;
import org.robotframework.red.viewers.ElementAddingToken;
import org.robotframework.red.viewers.ElementsAddingEditingSupport;
import org.robotframework.red.viewers.ListInputStructuredContentProvider;
import org.robotframework.red.viewers.RedCommonLabelProvider;
import org.robotframework.red.viewers.Selections;
import org.robotframework.red.viewers.StructuredContentProvider;

import com.google.common.base.CaseFormat;
import com.google.common.base.Converter;

public class RfLintValidationPreferencePage extends RedPreferencePage {

    private final List<RfLintRule> rules = new ArrayList<>();
    private TableViewer rulesViewer;
    private IWebBrowser browser;

    private final List<RulesFile> rulesFiles = new ArrayList<>();
    private TableViewer rulesFilesViewer;

    private StringFieldEditor argumentsEditor;

    private Composite progressParent;

    private Composite topLevelParent;

    private Link rulesViewerDescription;


    public RfLintValidationPreferencePage() {
        super("RfLint validation settings");
    }

    @Override
    protected Control createContents(final Composite parent) {
        this.topLevelParent = parent;

        final TabFolder tabs = new TabFolder(parent, SWT.NONE);
        GridDataFactory.fillDefaults().grab(true, true).hint(700, 500).applyTo(tabs);

        final TabItem generalTab = new TabItem(tabs, SWT.NONE);
        generalTab.setText("General");
        final Composite generalParent = new Composite(tabs, SWT.NONE);
        GridLayoutFactory.fillDefaults().margins(10, 10).applyTo(generalParent);

        rulesFilesViewer = createRulesFilesViewer(generalParent);
        argumentsEditor = createAdditionalArgumentsEditor(generalParent);
        generalTab.setControl(generalParent);


        final TabItem rulesTab = new TabItem(tabs, SWT.NONE);
        rulesTab.setText("Rules");
        final Composite rulesParent = new Composite(tabs, SWT.NONE);
        GridLayoutFactory.fillDefaults().margins(10, 10).applyTo(rulesParent);

        rulesViewer = createRulesViewer(rulesParent);
        rulesTab.setControl(rulesParent);


        progressParent = new Composite(parent, SWT.NONE);
        GridLayoutFactory.fillDefaults().applyTo(progressParent);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.END).grab(true, false).applyTo(progressParent);

        final ProgressBar progressBar = new ProgressBar(progressParent, SWT.SMOOTH | SWT.INDETERMINATE);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.END).grab(true, false).applyTo(progressBar);

        hideProgress();
        initializeValues();

        return parent;
    }

    private TableViewer createRulesFilesViewer(final Composite parent) {
        final Label description = new Label(parent, SWT.NONE);
        description.setText("Additional rules files");

        final TableViewer viewer = new TableViewer(parent,
                SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
        CellsActivationStrategy.addActivationStrategy(viewer, RowTabbingStrategy.MOVE_TO_NEXT);
        GridDataFactory.fillDefaults().indent(15, 0).grab(true, true).applyTo(viewer.getTable());
        viewer.getTable().setLinesVisible(false);
        viewer.getTable().setHeaderVisible(false);

        viewer.setContentProvider(new RulesFilesContentProvider());

        final Consumer<List<RulesFile>> ruleFilesConsumer = files -> {
            rulesFiles.addAll(files);
            reloadRules();
        };
        ViewerColumnsFactory.newColumn("")
                .withWidth(300)
                .shouldGrabAllTheSpaceLeft(true)
                .withMinWidth(100)
                .labelsProvidedBy(new RulesFilesLabelProvider())
                .editingEnabled()
                .editingSupportedBy(new RulesFileEditingSupport(viewer,
                        openRuleFilesDialog(parent.getShell(),
                                ResourcesPlugin.getWorkspace().getRoot().getLocationURI(), ruleFilesConsumer)))
                .createFor(viewer);

        ViewersConfigurator.enableDeselectionPossibility(viewer);
        ViewersConfigurator.disableContextMenuOnHeader(viewer);

        final Runnable selectionRemover = () -> {
            final List<RulesFile> rulesFilesToRemove = Selections
                    .getElements((IStructuredSelection) viewer.getSelection(), RulesFile.class);
            rulesFiles.removeAll(rulesFilesToRemove);
            rulesFilesViewer.refresh();

            reloadRules();
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
                "Additional arguments for RfLint", group);
        GridDataFactory.fillDefaults().span(2, 1).applyTo(argumentsEditor.getLabelControl(group));
        GridDataFactory.fillDefaults()
                .grab(true, false)
                .align(SWT.FILL, SWT.CENTER)
                .indent(15, 0)
                .applyTo(argumentsEditor.getTextControl(group));
        argumentsEditor.setPreferenceStore(getPreferenceStore());
        argumentsEditor.setPage(this);

        BrowseButtons.selectVariableButton(group, argumentsEditor.getTextControl(group)::insert);

        return argumentsEditor;
    }

    private TableViewer createRulesViewer(final Composite parent) {
        rulesViewerDescription = new Link(parent, SWT.NONE);
        rulesViewerDescription.setText("Configure rules and their severity");
        rulesViewerDescription.addSelectionListener(Listeners.widgetSelectedAdapter(e -> {
            if (InstalledRobotsPreferencesPage.ID.equals(e.text)) {
                PreferencesUtil.createPreferenceDialogOn(parent.getShell(), e.text, null, null);
            }
        }));
        GridDataFactory.fillDefaults()
                .align(SWT.FILL, SWT.BEGINNING)
                .hint(150, SWT.DEFAULT)
                .span(2, 1)
                .grab(true, false)
                .applyTo(rulesViewerDescription);

        final Table table = new Table(parent,
                SWT.CHECK | SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
        final CheckboxTableViewer viewer = new CheckboxTableViewer(table);
        CellsActivationStrategy.addActivationStrategy(viewer, RowTabbingStrategy.MOVE_TO_NEXT);
        GridDataFactory.fillDefaults().indent(15, 0).grab(true, true).applyTo(viewer.getTable());
        table.setLinesVisible(true);
        table.setHeaderVisible(true);

        viewer.setCheckStateProvider(new RulesCheckedStateProvider());
        viewer.addCheckStateListener(event -> {
            final RfLintRule rule = (RfLintRule) event.getElement();
            final RfLintViolationSeverity newSeverity = event.getChecked() ? null : RfLintViolationSeverity.IGNORE;
            rule.configure(newSeverity);

            viewer.refresh(rule);
        });
        viewer.setContentProvider(new ListInputStructuredContentProvider());
        ViewerColumnsFactory.newColumn("Rule")
                .withWidth(250)
                .labelsProvidedBy(new RuleNamesLabelProvider())
                .createFor(viewer);
        ViewerColumnsFactory.newColumn("Severity")
                .withWidth(100)
                .withMinWidth(30)
                .labelsProvidedBy(new RuleSeverityLabelProvider())
                .editingEnabled()
                .editingSupportedBy(new RuleSeverityEditingSupport(viewer))
                .createFor(viewer);
        ViewerColumnsFactory.newColumn("Configuration")
                .withWidth(100)
                .shouldGrabAllTheSpaceLeft(true)
                .withMinWidth(30)
                .labelsProvidedBy(new RuleConfigurationLabelProvider())
                .editingEnabled()
                .editingSupportedBy(new RuleConfigurationEditingSupport(viewer))
                .createFor(viewer);

        ViewersConfigurator.enableDeselectionPossibility(viewer);
        ViewersConfigurator.disableContextMenuOnHeader(viewer);

        final Runnable selectionRemover = () -> {
            final List<RfLintRule> rulesToRemove = Selections.getElements((IStructuredSelection) viewer.getSelection(),
                    RfLintRule.class);
            rules.removeAll(rulesToRemove);
            rulesViewer.refresh();
        };
        final Menu menu = new Menu(table);
        final MenuItem deleteMenuItem = new MenuItem(menu, SWT.PUSH);
        deleteMenuItem.setText("Delete\tDel");
        deleteMenuItem.setImage(ImagesManager.getImage(RedImages.getDeleteImage()));
        deleteMenuItem.addSelectionListener(widgetSelectedAdapter(e -> selectionRemover.run()));

        table.setMenu(menu);
        menu.addMenuListener(menuShownAdapter(e -> {
            final List<RfLintRule> selectedRules = Selections.getElements((IStructuredSelection) viewer.getSelection(),
                    RfLintRule.class);
            deleteMenuItem.setEnabled(
                    !selectedRules.isEmpty() && selectedRules.stream().allMatch(MissingRule.class::isInstance));
        }));
        table.addKeyListener(keyPressedAdapter(e -> {
            if (e.keyCode == SWT.DEL) {
                selectionRemover.run();
            }
        }));

        final ScrolledFormText documentationText = new ScrolledFormText(parent,
                SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER, true);
        documentationText.setBackground(parent.getBackground());
        GridDataFactory.fillDefaults().grab(true, false).indent(15, 0).hint(0, 80).applyTo(documentationText);
        final IHyperlinkListener linkListener = Listeners.linkActivatedAdapter(e -> {
            if (browser == null) {
                final IWorkbenchBrowserSupport browserSupport = PlatformUI.getWorkbench().getBrowserSupport();
                try {
                    browser = browserSupport.createBrowser(null);
                } catch (final PartInitException e1) {
                }
            }
            if (browser != null) {
                try {
                    browser.openURL(new URI((String) e.getHref()).toURL());
                } catch (PartInitException | MalformedURLException | URISyntaxException e1) {
                }
            }
        });
        documentationText.getFormText().addHyperlinkListener(linkListener);
        documentationText
                .addDisposeListener(e -> documentationText.getFormText().removeHyperlinkListener(linkListener));
        documentationText.getFormText().setColor("error", ColorsManager.getColor(255, 0, 0));

        final ISelectionChangedListener selListener = e -> {
            final String doc = Selections
                    .getOptionalFirstElement((IStructuredSelection) viewer.getSelection(), RfLintRule.class)
                    .map(rule -> rule instanceof MissingRule
                            ? "<p><span color=\"error\">Missing rule: " + rule.getRuleName()
                                    + " is not available in currently active environment</span></p>"
                            : rule.getDocumentation())
                    .map(text -> "<form>" + text.replaceAll("\\\n", "<br/>") + "</form>")
                    .orElse("<form></form>");

            documentationText.getFormText().setText(doc, true, true);
            documentationText.reflow(true);
        };
        viewer.addSelectionChangedListener(selListener);
        table.addDisposeListener(e -> viewer.removeSelectionChangedListener(selListener));

        return viewer;
    }

    private void showProgress() {
        ((GridData) progressParent.getLayoutData()).exclude = false;
        progressParent.setVisible(true);
        topLevelParent.layout();
    }

    private void hideProgress() {
        ((GridData) progressParent.getLayoutData()).exclude = true;
        progressParent.setVisible(false);
        topLevelParent.layout();
    }

    private void initializeValues() {
        final RedPreferences preferences = RedPlugin.getDefault().getPreferences();

        RfLintRules.getInstance().dispose();
        initializeRules(preferences.getRfLintRulesFiles(), preferences.getRfLintRulesConfigs());
        preferences.getRfLintRulesFiles().stream().map(RulesFile::new).forEach(rulesFiles::add);
        rulesFilesViewer.setInput(rulesFiles);
        argumentsEditor.setStringValue(preferences.getRfLintAdditionalArguments());
    }

    private void reloadRules() {
        RfLintRules.getInstance().dispose();

        final List<String> currentFiles = rulesFiles.stream().map(RulesFile::getPath).collect(toList());
        final Map<String, RfLintRuleConfiguration> currentRulesConfigs = rules.stream()
                .filter(RfLintRule::isConfigured)
                .collect(toMap(RfLintRule::getRuleName, RfLintRule::getConfiguration));
        
        initializeRules(currentFiles, currentRulesConfigs);
    }

    private void initializeRules(final List<String> files,
            final Map<String, RfLintRuleConfiguration> ruleConfigs) {
        rules.clear();
        final Job rulesReadingJob = Job.createSystem("Reading RfLint rules", monitor -> {
            final IRuntimeEnvironment env = RedPlugin.getDefault().getActiveRobotInstallation();
            final Map<String, RfLintRule> envRules = RfLintRules.getInstance()
                    .loadRules(() -> env.getRfLintRules(files));
            
            rules.addAll(envRules.values());
            ruleConfigs.entrySet()
                    .stream()
                    .filter(entry -> !envRules.containsKey(entry.getKey()))
                    .map(entry -> new MissingRule(entry.getKey(), entry.getValue()))
                    .forEach(rules::add);

            rules.forEach(rule -> rule.configure(ruleConfigs.get(rule.getRuleName())));
            rules.sort((r1, r2) -> r1.getRuleName().compareTo(r2.getRuleName()));
        });
        rulesReadingJob.addJobChangeListener(new JobChangeAdapter() {
            @Override
            public void done(final IJobChangeEvent event) {
                if (topLevelParent.isDisposed()) {
                    return;
                }
                SwtThread.asyncExec(topLevelParent.getDisplay(), () -> {
                    if (topLevelParent.isDisposed()) {
                        return;
                    }
                    final IRuntimeEnvironment env = RedPlugin.getDefault().getActiveRobotInstallation();
                    final String path = Optional.of(env)
                            .map(IRuntimeEnvironment::getFile)
                            .map(File::getAbsolutePath)
                            .orElse("<unknown>");

                    rulesViewerDescription.setText("Configure rules and their severity. "
                            + "Following rules are available for RfLint installed in <a href=\""
                            + InstalledRobotsPreferencesPage.ID + "\">" + path + "</a> environment:");

                    rulesViewer.setInput(rules);
                    hideProgress();
                    rulesViewer.getControl().setEnabled(true);
                    rulesFilesViewer.getControl().setEnabled(true);
                });
            };
        });
        showProgress();
        rulesViewer.getControl().setEnabled(false);
        rulesFilesViewer.getControl().setEnabled(false);
        rulesReadingJob.schedule();
    }

    @Override
    public boolean performOk() {
        final String allRulesFiles = rulesFiles.stream().map(RulesFile::getPath).collect(joining(";"));
        final String allRulesNames = rules.stream()
                .filter(RfLintRule::isConfigured)
                .map(RfLintRule::getRuleName)
                .collect(joining(";"));
        final String allRulesSeverities = rules.stream()
                .filter(RfLintRule::isConfigured)
                .map(RfLintRule::getConfiguredSeverity)
                .map(RfLintViolationSeverity::name)
                .collect(joining(";"));
        final String allRulesArgs = rules.stream()
                .filter(RfLintRule::isConfigured)
                .map(RfLintRule::getConfiguredArguments)
                .collect(joining(";"));
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
        rulesFiles.clear();
        rulesFilesViewer.refresh();

        rules.forEach(rule -> rule.configure((RfLintRuleConfiguration) null));
        reloadRules();

        argumentsEditor.loadDefault();

        super.performDefaults();
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
            }
            return null;
        };
    }

    private static class MissingRule extends RfLintRule {

        public MissingRule(final String ruleName, final RfLintRuleConfiguration configuration) {
            super(ruleName, RfLintViolationSeverity.ERROR, "", "", configuration);
        }
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

    private static class RulesCheckedStateProvider implements ICheckStateProvider {

        @Override
        public boolean isChecked(final Object element) {
            return ((RfLintRule) element).getConfiguredSeverity() != RfLintViolationSeverity.IGNORE;
        }

        @Override
        public boolean isGrayed(final Object element) {
            return false;
        }
    }

    private static class RuleNamesLabelProvider extends RedCommonLabelProvider {

        @Override
        public StyledString getStyledText(final Object element) {
            final RfLintRule rule = (RfLintRule) element;
            return new StyledString(rule.getRuleName(), getRuleStyler(rule));
        }

        static Styler getRuleStyler(final RfLintRule rule) {
            if (rule instanceof MissingRule) {
                return Stylers.Common.ERROR_STYLER;
            } else if (rule.isConfigured()) {
                return Stylers.Common.BOLD_STYLER;
            } else {
                return Stylers.Common.EMPTY_STYLER;
            }
        }
    }

    private static class RuleSeverityLabelProvider extends RedCommonLabelProvider {

        static Converter<String, String> severityLabelConverter = CaseFormat.UPPER_UNDERSCORE
                .converterTo(CaseFormat.UPPER_CAMEL);

        @Override
        public StyledString getStyledText(final Object element) {
            final RfLintRule rule = (RfLintRule) element;

            return new StyledString(severityLabelConverter.convert(rule.getConfiguredSeverity().name()),
                    RuleNamesLabelProvider.getRuleStyler(rule));
        }
    }

    private static class RuleSeverityEditingSupport extends EditingSupport {

        private final List<RfLintViolationSeverity> severities = newArrayList(RfLintViolationSeverity.ERROR,
                RfLintViolationSeverity.WARNING, RfLintViolationSeverity.IGNORE);

        public RuleSeverityEditingSupport(final ColumnViewer viewer) {
            super(viewer);
        }

        @Override
        protected boolean canEdit(final Object element) {
            return true;
        }

        @Override
        protected CellEditor getCellEditor(final Object element) {
            final String[] items = severities.stream()
                    .map(RfLintViolationSeverity::name)
                    .map(RuleSeverityLabelProvider.severityLabelConverter::convert)
                    .toArray(String[]::new);
            return new ComboBoxCellEditor((Composite) getViewer().getControl(), items);
        }

        @Override
        protected Object getValue(final Object element) {
            final RfLintRule rule = (RfLintRule) element;
            return severities.indexOf(rule.getSeverity());
        }

        @Override
        protected void setValue(final Object element, final Object value) {
            final int index = (int) value;
            final RfLintRule rule = (RfLintRule) element;
            rule.configure(index == -1 ? null : severities.get(index));
            getViewer().refresh(rule);
        }
    }

    private static class RuleConfigurationLabelProvider extends RedCommonLabelProvider {

        @Override
        public StyledString getStyledText(final Object element) {
            final RfLintRule rule = (RfLintRule) element;
            return new StyledString(rule.getConfiguredArguments(), RuleNamesLabelProvider.getRuleStyler(rule));
        }
    }

    private static class RuleConfigurationEditingSupport extends EditingSupport {

        public RuleConfigurationEditingSupport(final ColumnViewer viewer) {
            super(viewer);
        }

        @Override
        protected boolean canEdit(final Object element) {
            return true;
        }

        @Override
        protected CellEditor getCellEditor(final Object element) {
            return new TextCellEditor((Composite) getViewer().getControl());
        }

        @Override
        protected Object getValue(final Object element) {
            final RfLintRule rule = (RfLintRule) element;
            return rule.getConfiguredArguments();
        }

        @Override
        protected void setValue(final Object element, final Object value) {
            final RfLintRule rule = (RfLintRule) element;
            rule.configure((String) value);
            getViewer().refresh(element);
        }
    }

    private static class RulesFilesContentProvider extends StructuredContentProvider {

        @Override
        public Object[] getElements(final Object inputElement) {
            final List<Object> all = new ArrayList<>();
            all.addAll((List<?>) inputElement);
            all.add(new ElementAddingToken("rules file", true));
            return all.toArray();
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

    private class RulesFileEditingSupport extends ElementsAddingEditingSupport {

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
                final Consumer<List<RulesFile>> ruleFilesConsumer = files -> {
                    if (!files.isEmpty()) {
                        rulesFile.setPath(files.get(0).getPath());
                    }
                    reloadRules();
                };
                final URI uri = new File(rulesFile.getPath()).toURI();
                scheduleViewerRefreshAndEditorActivation(openRuleFilesDialog(shell, uri, ruleFilesConsumer).get());

            } else {
                super.setValue(element, value);
            }
        }
    }
}

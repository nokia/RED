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
import java.util.Comparator;
import java.util.LinkedHashMap;
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
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
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
import org.robotframework.red.viewers.RedCommonLabelProvider;
import org.robotframework.red.viewers.Selections;
import org.robotframework.red.viewers.TreeContentProvider;

import com.google.common.base.CaseFormat;
import com.google.common.base.Converter;

public class RfLintValidationPreferencePage extends RedPreferencePage {

    private Composite progressParent;
    private Composite topLevelParent;
    private Link description;
    
    private final List<RulesFile> rulesFiles = new ArrayList<>();
    private final Map<RulesFile, List<RfLintRule>> rules = new LinkedHashMap<>();

    private CheckboxTreeViewer rulesViewer;
    private IWebBrowser browser;
    private StringFieldEditor argumentsEditor;


    public RfLintValidationPreferencePage() {
        super("RfLint validation settings");
    }

    @Override
    protected Control createContents(final Composite parent) {
        this.topLevelParent = parent;

        description = new Link(parent, SWT.NONE);
        description.setText("Configure rules and their severity");
        description.addSelectionListener(Listeners.widgetSelectedAdapter(e -> {
            if (InstalledRobotsPreferencesPage.ID.equals(e.text)) {
                PreferencesUtil.createPreferenceDialogOn(parent.getShell(), e.text, null, null);
            }
        }));
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).grab(true, false).applyTo(description);

        rulesViewer = createRulesViewer(parent);
        createDocumentationForm(parent);
        argumentsEditor = createAdditionalArgumentsEditor(parent);
        progressParent = createProgressBar(parent);

        hideProgress();
        initializeValues();

        return parent;
    }

    private CheckboxTreeViewer createRulesViewer(final Composite parent) {
        final Tree tree = new Tree(parent,
                SWT.CHECK | SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
        final CheckboxTreeViewer viewer = new CheckboxTreeViewer(tree);
        CellsActivationStrategy.addActivationStrategy(viewer, RowTabbingStrategy.MOVE_TO_NEXT);
        GridDataFactory.fillDefaults().indent(15, 0).grab(true, true).applyTo(viewer.getTree());
        tree.setLinesVisible(true);
        tree.setHeaderVisible(true);

        viewer.setCheckStateProvider(new CheckedStateProvider());
        viewer.addCheckStateListener(new CheckStateListener());
        viewer.setContentProvider(new RulesContentProvider());

        final Supplier<?> ruleFilesSupplier = ruleFilesSupplier(parent);
        ViewerColumnsFactory.newColumn("Rule")
                .withWidth(500)
                .labelsProvidedBy(new NamesLabelProvider())
                .editingEnabled()
                .editingSupportedBy(new NamesEditingSupport(viewer, ruleFilesSupplier))
                .createFor(viewer);
        ViewerColumnsFactory.newColumn("Severity")
                .withWidth(100)
                .withMinWidth(100)
                .labelsProvidedBy(new SeverityLabelProvider())
                .editingEnabled()
                .editingSupportedBy(new SeverityEditingSupport(viewer, ruleFilesSupplier))
                .createFor(viewer);
        ViewerColumnsFactory.newColumn("Configuration")
                .withWidth(100)
                .shouldGrabAllTheSpaceLeft(true)
                .withMinWidth(100)
                .labelsProvidedBy(new ConfigurationLabelProvider())
                .editingEnabled()
                .editingSupportedBy(new ConfigurationEditingSupport(viewer, ruleFilesSupplier))
                .createFor(viewer);

        ViewersConfigurator.enableDeselectionPossibility(viewer);
        ViewersConfigurator.disableContextMenuOnHeader(viewer);

        setupContextMenu(viewer);
        return viewer;
    }

    private void setupContextMenu(final CheckboxTreeViewer viewer) {
        final Tree tree = viewer.getTree();

        final Runnable selectionRemover = () -> {
            final IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
            final List<MissingRule> selectedRules = Selections.getElements(selection, MissingRule.class);
            final List<RulesFile> selectedFiles = Selections.getElements(selection, RulesFile.class);

            if (!selectedRules.isEmpty()) {
                rules.get(RulesFile.MISSING_SOURCE).removeAll(selectedRules);
            } else if (!selectedFiles.isEmpty()) {
                rulesFiles.removeAll(selectedFiles);
                selectedFiles.forEach(rules::remove);
            }
            rulesViewer.refresh();
        };
        final Menu menu = new Menu(tree);
        final MenuItem deleteMenuItem = new MenuItem(menu, SWT.PUSH);
        deleteMenuItem.setText("Delete\tDel");
        deleteMenuItem.setImage(ImagesManager.getImage(RedImages.getDeleteImage()));
        deleteMenuItem.addSelectionListener(widgetSelectedAdapter(e -> selectionRemover.run()));

        tree.setMenu(menu);
        menu.addMenuListener(menuShownAdapter(e -> {
            final IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
            final List<RfLintRule> selectedRules = Selections.getElements(selection, RfLintRule.class);
            final List<RulesFile> selectedFiles = Selections.getElements(selection, RulesFile.class);

            if (!selectedFiles.isEmpty() && selectedRules.isEmpty()) {
                deleteMenuItem.setEnabled(selectedFiles.stream().allMatch(file -> !isBuiltInFile(file)));

            } else if (!selectedRules.isEmpty() && selectedFiles.isEmpty()) {
                deleteMenuItem.setEnabled(selectedRules.stream().allMatch(MissingRule.class::isInstance));
            } else {
                deleteMenuItem.setEnabled(false);
            }
        }));
        tree.addKeyListener(keyPressedAdapter(e -> {
            if (e.keyCode == SWT.DEL && deleteMenuItem.isEnabled()) {
                selectionRemover.run();
            }
        }));
    }

    private void createDocumentationForm(final Composite parent) {
        final Tree tree = rulesViewer.getTree();
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
            final IStructuredSelection selection = (IStructuredSelection) rulesViewer.getSelection();
            final Optional<String> ruleDoc = Selections.getOptionalFirstElement(selection, RfLintRule.class)
                    .map(rule -> rule instanceof MissingRule
                            ? "<p><span color=\"error\">Missing rule: " + rule.getRuleName()
                                    + " is not available in currently active environment.</span></p>"
                                    + "<p>This entry can be safely deleted if it is no longer needed.</p>"
                            : "<p><b>" + rule.getFilepath() + "</b></p>" + rule.getDocumentation())
                    .map(text -> "<form>" + text.replaceAll("\\\n", "<br/>") + "</form>");
            String doc;
            if (ruleDoc.isPresent()) {
                doc = ruleDoc.get();
            } else {
                doc = Selections.getOptionalFirstElement(selection, RulesFile.class)
                        .filter(f -> f != RulesFile.MISSING_SOURCE && f.getExists())
                        .map(f -> "<p><span color=\"error\">The file '" + f.getPath() + "' does not exist</span></p>")
                        .map(text -> "<form>" + text.replaceAll("\\\n", "<br/>") + "</form>")
                        .orElse("<form></form>");
            }

            documentationText.getFormText().setText(doc, true, true);
            documentationText.reflow(true);
        };
        rulesViewer.addSelectionChangedListener(selListener);
        tree.addDisposeListener(e -> rulesViewer.removeSelectionChangedListener(selListener));
    }

    private StringFieldEditor createAdditionalArgumentsEditor(final Composite parent) {
        final Composite group = new Composite(parent, SWT.NONE);
        GridDataFactory.fillDefaults().applyTo(group);

        final StringFieldEditor argumentsEditor = new StringFieldEditor(RedPreferences.RFLINT_ADDITIONAL_ARGUMENTS,
                "Additional arguments for RfLint", group);
        ((GridLayout) group.getLayout()).marginBottom = 30;
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

    private Composite createProgressBar(final Composite parent) {
        final Composite progressParent = new Composite(parent, SWT.NONE);
        GridLayoutFactory.fillDefaults().applyTo(progressParent);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.END).grab(true, false).applyTo(progressParent);

        final ProgressBar progressBar = new ProgressBar(progressParent, SWT.SMOOTH | SWT.INDETERMINATE);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.END).grab(true, false).applyTo(progressBar);
        return progressParent;
    }

    private boolean isBuiltInFile(final RulesFile rulesFile) {
        return !rulesFiles.contains(rulesFile) && rulesFile != RulesFile.MISSING_SOURCE;
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
        argumentsEditor.setStringValue(preferences.getRfLintAdditionalArguments());
    }

    private void reloadRules() {
        RfLintRules.getInstance().dispose();

        final List<String> currentFiles = rulesFiles.stream().map(RulesFile::getPath).collect(toList());
        final Map<String, RfLintRuleConfiguration> currentRulesConfigs = rules.values()
                .stream()
                .flatMap(List::stream)
                .filter(RfLintRule::isConfigured)
                .collect(toMap(RfLintRule::getRuleName, RfLintRule::getConfiguration));
        
        initializeRules(currentFiles, currentRulesConfigs);
    }

    private void initializeRules(final List<String> files,
            final Map<String, RfLintRuleConfiguration> ruleConfigs) {
        final Object[] expanded = rulesViewer.getExpandedElements();

        rules.clear();
        final Job rulesReadingJob = Job.createSystem("Reading RfLint rules", monitor -> {
            final IRuntimeEnvironment env = RedPlugin.getDefault().getActiveRobotInstallation();
            final Map<String, RfLintRule> envRules = RfLintRules.getInstance()
                    .loadRules(() -> env.getRfLintRules(files));
            
            final List<RfLintRule> missingRules = ruleConfigs.entrySet()
                    .stream()
                    .filter(entry -> !envRules.containsKey(entry.getKey()))
                    .map(entry -> new MissingRule(entry.getKey(), entry.getValue()))
                    .collect(toList());
            if (!missingRules.isEmpty()) {
                rules.put(RulesFile.MISSING_SOURCE, missingRules);
            }

            for (final RfLintRule rule : envRules.values()) {
                final RulesFile key = new RulesFile(rule.getFilepath());
                if (!rules.containsKey(key)) {
                    rules.put(key, new ArrayList<>());
                }
                rules.get(key).add(rule);
            }
            for (final RulesFile file : rulesFiles) {
                if (!rules.containsKey(file)) {
                    rules.put(file, new ArrayList<>());
                }
            }

            rules.values()
                    .stream()
                    .flatMap(List::stream)
                    .forEach(rule -> rule.configure(ruleConfigs.get(rule.getRuleName())));

            validateRulesFiles();
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

                    description.setText("Configure rules and their severity. "
                            + "Following rules are available for RfLint installed in <a href=\""
                            + InstalledRobotsPreferencesPage.ID + "\">" + path + "</a> environment:");

                    rulesViewer.setInput(rules);
                    hideProgress();
                    rulesViewer.getControl().setEnabled(true);
                    rulesViewer.setExpandedElements(expanded);
                });
            };
        });
        showProgress();
        rulesViewer.getControl().setEnabled(false);
        rulesReadingJob.schedule();
    }

    private void validateRulesFiles() {
        for (final RulesFile file : rules.keySet()) {
            file.setExists(file == RulesFile.MISSING_SOURCE || !new File(file.getPath()).exists());
        }
    }

    @Override
    public boolean performOk() {
        final String allRulesFiles = rulesFiles.stream().map(RulesFile::getPath).collect(joining(";"));
        final String allRulesNames = rules.values()
                .stream()
                .flatMap(List::stream)
                .filter(RfLintRule::isConfigured)
                .map(RfLintRule::getRuleName)
                .collect(joining(";"));
        final String allRulesSeverities = rules.values()
                .stream()
                .flatMap(List::stream)
                .filter(RfLintRule::isConfigured)
                .map(RfLintRule::getConfiguredSeverity)
                .map(RfLintViolationSeverity::name)
                .collect(joining(";"));
        final String allRulesArgs = rules.values()
                .stream()
                .flatMap(List::stream)
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

        rules.values().stream().flatMap(List::stream).forEach(rule -> rule.configure((RfLintRuleConfiguration) null));
        reloadRules();

        argumentsEditor.loadDefault();

        super.performDefaults();
    }

    private Supplier<?> ruleFilesSupplier(final Composite parent) {
        final Consumer<List<RulesFile>> ruleFilesConsumer = files -> {
            boolean added = false;
            for (final RulesFile file : files) {
                if (!rulesFiles.contains(file)) {
                    rulesFiles.add(file);
                    added = true;
                }
            }
            if (added) {
                reloadRules();
            }
        };
        return openRuleFilesDialog(parent.getShell(),
                ResourcesPlugin.getWorkspace().getRoot().getLocationURI(), ruleFilesConsumer);
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

    private static Styler getRuleStyler(final RfLintRule rule) {
        if (rule instanceof MissingRule) {
            return Stylers.Common.ERROR_STYLER;
        } else if (rule.getConfiguredSeverity() == RfLintViolationSeverity.IGNORE) {
            return Stylers.withForeground(200, 200, 200);
        } else if (rule.isConfigured()) {
            return Stylers.Common.BOLD_STYLER;
        } else {
            return Stylers.Common.EMPTY_STYLER;
        }
    }

    private static final class MissingRule extends RfLintRule {

        public MissingRule(final String ruleName, final RfLintRuleConfiguration configuration) {
            super(ruleName, null, "", "", configuration);
        }

        @Override
        public RfLintRule configure(final RfLintViolationSeverity severity) {
            // we do not know what was the default original severity in missing rule,
            // so when removing configured severity we just set to ERROR
            return super.configure(severity == null ? RfLintViolationSeverity.ERROR : severity);
        }
    }

    private static final class RulesFile {
        
        private static final RulesFile MISSING_SOURCE = new RulesFile("Unknown source");

        private String path;

        private boolean exists;

        public RulesFile(final String path) {
            this.path = path;
        }

        public String getPath() {
            return path;
        }

        public void setPath(final String path) {
            this.path = path;
        }

        public void setExists(final boolean exists) {
            this.exists = exists;
        }

        public boolean getExists() {
            return exists;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj != null && obj.getClass() == RulesFile.class) {
                final RulesFile that = (RulesFile) obj;
                return this.path.equals(that.path);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return path.hashCode();
        }
    }

    private class RulesContentProvider extends TreeContentProvider {

        @Override
        public Object[] getElements(final Object inputElement) {
            final List<Object> all = new ArrayList<>();
            final Comparator<RulesFile> comparator = (f1, f2) -> {
                if (f1 == RulesFile.MISSING_SOURCE) {
                    return -1;
                } else if (f2 == RulesFile.MISSING_SOURCE) {
                    return 1;
                } else {
                    return new File(f1.getPath()).compareTo(new File(f2.getPath()));
                }
            };
            Stream.concat(rules.keySet().stream().filter(f -> !rulesFiles.contains(f)).sorted(comparator),
                    rulesFiles.stream().sorted(comparator))
                    .forEach(all::add);
            all.add(new ElementAddingToken("rules file", true));
            return all.toArray();
        }

        @Override
        public boolean hasChildren(final Object element) {
            return element instanceof RulesFile;
        }

        @Override
        public Object[] getChildren(final Object element) {
            if (element instanceof RulesFile) {
                final RulesFile file = (RulesFile) element;
                final List<RfLintRule> rs = rules.get(file);
                return rs == null ? new Object[0]
                        : rs.stream().sorted((r1, r2) -> r1.getRuleName().compareTo(r2.getRuleName())).toArray();
            }
            return new Object[0];
        }

        @Override
        public Object getParent(final Object element) {
            if (element instanceof RulesFile || element instanceof ElementAddingToken) {
                return null;
            }
            final RfLintRule rule = (RfLintRule) element;
            return rules.keySet()
                    .stream()
                    .filter(f -> f.getPath().equals(rule.getFilepath()))
                    .findFirst()
                    .orElse(null);
        }
    }

    private class CheckedStateProvider implements ICheckStateProvider {

        @Override
        public boolean isChecked(final Object element) {
            if (element instanceof RulesFile) {
                final List<RfLintRule> rs = rules.get(element);
                return rs.isEmpty()
                        || rs.stream().anyMatch(r -> r.getConfiguredSeverity() != RfLintViolationSeverity.IGNORE);

            } else if (element instanceof RfLintRule) {
                final RfLintRule rule = (RfLintRule) element;
                return rule.getConfiguredSeverity() != RfLintViolationSeverity.IGNORE;

            } else {
                return false;
            }
        }

        @Override
        public boolean isGrayed(final Object element) {
            if (element instanceof RulesFile) {
                return rules.get(element)
                        .stream()
                        .anyMatch(r -> r.getConfiguredSeverity() == RfLintViolationSeverity.IGNORE);
            }
            return false;
        }
    }

    private class CheckStateListener implements ICheckStateListener {

        @Override
        public void checkStateChanged(final CheckStateChangedEvent event) {
            final RfLintViolationSeverity newSeverity = event.getChecked() ? null : RfLintViolationSeverity.IGNORE;

            if (event.getElement() instanceof RfLintRule) {
                final RfLintRule rule = (RfLintRule) event.getElement();
                rule.configure(newSeverity);

                final Object parent = ((ITreeContentProvider) rulesViewer.getContentProvider()).getParent(rule);
                rulesViewer.refresh(parent);

            } else if (event.getElement() instanceof RulesFile) {
                final RulesFile rulesFile = (RulesFile) event.getElement();
                rules.get(rulesFile).forEach(rule -> rule.configure(newSeverity));

                rulesViewer.refresh(rulesFile);

            } else {
                rulesViewer.setChecked(event.getElement(), false);
            }
        }
    }

    private class NamesLabelProvider extends RedCommonLabelProvider {

        @Override
        public StyledString getStyledText(final Object element) {
            if (element instanceof RulesFile) {
                final RulesFile rulesFile = (RulesFile) element;
                final StyledString label = new StyledString(rulesFile.getPath(), getRulesFileStyler(rulesFile));
                if (isBuiltInFile(rulesFile)) {
                    label.append(" (built-in)", Stylers.Common.ECLIPSE_DECORATION_STYLER);
                }
                return label;

            } else if (element instanceof RfLintRule) {
                final RfLintRule rule = (RfLintRule) element;
                return new StyledString(rule.getRuleName(), getRuleStyler(rule));

            } else {
                return ((ElementAddingToken) element).getStyledText();
            }
        }

        private Styler getRulesFileStyler(final RulesFile rulesFile) {
            if (rulesFile.getExists()) {
                return Stylers.Common.ERROR_STYLER;
            } else if (rules.get(rulesFile).stream().anyMatch(RfLintRule::isConfigured)) {
                return Stylers.Common.BOLD_STYLER;
            } else {
                return Stylers.Common.EMPTY_STYLER;
            }
        }

        @Override
        public Image getImage(final Object element) {
            if (element instanceof RulesFile) {
                return ImagesManager.getImage(RedImages.getImageForFileWithExtension("py"));
            } else if (element instanceof RfLintRule) {
                return ImagesManager.getImage(RedImages.getRfLintRuleImage());
            } else if (element instanceof ElementAddingToken) {
                return ((ElementAddingToken) element).getImage();
            }
            return null;
        }
    }

    private class NamesEditingSupport extends ElementsAddingEditingSupport {

        public NamesEditingSupport(final ColumnViewer viewer, final Supplier<?> creator) {
            super(viewer, 0, creator);
        }

        @Override
        protected CellEditor getCellEditor(final Object element) {
            if (element instanceof RulesFile && isBuiltInFile((RulesFile) element)) {
                return null;
            }
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

    private static class SeverityLabelProvider extends RedCommonLabelProvider {

        static Converter<String, String> severityLabelConverter = CaseFormat.UPPER_UNDERSCORE
                .converterTo(CaseFormat.UPPER_CAMEL);

        @Override
        public StyledString getStyledText(final Object element) {
            if (element instanceof RfLintRule) {
                final RfLintRule rule = (RfLintRule) element;

                final RfLintViolationSeverity severity = rule.getConfiguredSeverity();
                final String severityName = severity == RfLintViolationSeverity.IGNORE ? ""
                        : severityLabelConverter.convert(severity.name());
                return new StyledString(severityName, getRuleStyler(rule));
            }
            return new StyledString();
        }
    }

    private class SeverityEditingSupport extends ElementsAddingEditingSupport {

        private final List<RfLintViolationSeverity> severities = newArrayList(RfLintViolationSeverity.ERROR,
                RfLintViolationSeverity.WARNING);

        public SeverityEditingSupport(final ColumnViewer viewer, final Supplier<?> creator) {
            super(viewer, 0, creator);
        }

        @Override
        protected CellEditor getCellEditor(final Object element) {
            if (element instanceof ElementAddingToken) {
                return super.getCellEditor(element);
            } else {
                final String[] items = severities.stream()
                        .map(RfLintViolationSeverity::name)
                        .map(SeverityLabelProvider.severityLabelConverter::convert)
                        .toArray(String[]::new);
                return new ComboBoxCellEditor((Composite) getViewer().getControl(), items);
            }
        }

        @Override
        protected Object getValue(final Object element) {
            if (element instanceof RfLintRule) {
                final RfLintRule rule = (RfLintRule) element;
                int index = severities.indexOf(rule.getConfiguredSeverity());
                if (index == -1) {
                    index = severities.indexOf(rule.getSeverity());
                }
                return index;
        
            } else if (element instanceof RulesFile) {
                return 0;
        
            } else {
                return null;
            }
        }

        @Override
        protected void setValue(final Object element, final Object value) {
            if (element instanceof RfLintRule) {
                final int index = (int) value;
                final RfLintRule rule = (RfLintRule) element;
                rule.configure(index == -1 ? null : severities.get(index));

                final Object parent = ((ITreeContentProvider) getViewer().getContentProvider()).getParent(rule);
                getViewer().refresh(parent);

            } else if (element instanceof RulesFile) {
                final RulesFile rulesFile = (RulesFile) element;
                rules.get(rulesFile).forEach(rule -> setValue(rule, value));

                getViewer().refresh(rulesFile);

            } else {
                super.setValue(element, value);
            }
        }
    }

    private static class ConfigurationLabelProvider extends RedCommonLabelProvider {

        @Override
        public StyledString getStyledText(final Object element) {
            if (element instanceof RfLintRule) {
                final RfLintRule rule = (RfLintRule) element;
                return new StyledString(rule.getConfiguredArguments(), getRuleStyler(rule));
            }
            return new StyledString();
        }
    }

    private static class ConfigurationEditingSupport extends ElementsAddingEditingSupport {

        public ConfigurationEditingSupport(final ColumnViewer viewer, final Supplier<?> creator) {
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
                final RfLintRule rule = (RfLintRule) element;
                return rule.getConfiguredArguments();
            }
            return null;
        }

        @Override
        protected void setValue(final Object element, final Object value) {
            if (element instanceof RfLintRule) {
                final RfLintRule rule = (RfLintRule) element;
                rule.configure((String) value);

                final Object parent = ((ITreeContentProvider) getViewer().getContentProvider()).getParent(rule);
                getViewer().refresh(parent);
            } else {
                super.setValue(element, value);
            }
        }
    }
}

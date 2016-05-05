/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.tools.services.IDirtyProviderService;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.RowExposingTableViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerColumnsFactory;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewersConfigurator;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Section;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElementChange;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElementChange.Kind;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting.SettingsGroup;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.CreateFreshGeneralSettingCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.DeleteSettingKeywordCallCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallArgumentCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.CellsActivationStrategy;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.CellsActivationStrategy.RowTabbingStrategy;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.FocusedViewerAccessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.FocusedViewerAccessor.ViewerColumnsManagingStrategy;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.HeaderFilterMatchesCollection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionFormFragment;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorSources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotSuiteEditorEvents;
import org.robotframework.red.forms.RedFormToolkit;
import org.robotframework.red.forms.Sections;
import org.robotframework.red.graphics.ColorsManager;
import org.robotframework.red.viewers.Viewers;

import com.google.common.base.Supplier;
import com.google.common.collect.Range;

public class GeneralSettingsFormFragment implements ISectionFormFragment {

    @Inject
    private IEditorSite site;

    @Inject
    @Named(RobotEditorSources.SUITE_FILE_MODEL)
    private RobotSuiteFile fileModel;

    @Inject
    private RobotEditorCommandsStack commandsStack;

    @Inject
    private IDirtyProviderService dirtyProviderService;

    @Inject
    private RedFormToolkit toolkit;

    private com.google.common.base.Optional<RowExposingTableViewer> viewer = com.google.common.base.Optional.absent();

    private StyledText documentation;

    private Job documenationChangeJob;

    private Section section;

    private HeaderFilterMatchesCollection matches;

    TableViewer getViewer() {
        return viewer.orNull();
    }

    @Override
    public void initialize(final Composite parent) {
        section = toolkit.createSection(parent, ExpandableComposite.TWISTIE
                | ExpandableComposite.TITLE_BAR | Section.DESCRIPTION);
        section.setExpanded(true);
        section.setText("General");
        section.setDescription("Provide test suite documentation and general settings");
        GridDataFactory.fillDefaults().grab(true, true).minSize(1, 22).indent(0, 5).applyTo(section);
        Sections.switchGridCellGrabbingOnExpansion(section);
        Sections.installMaximazingPossibility(section);

        final Composite panel = createPanel(section);
        createDocumentationControl(panel);
        if (!fileModel.isResourceFile()) {
            createViewer(panel);

            createColumns();
            createContextMenu();
            setInput();
        }
    }

    private Composite createPanel(final Section section) {
        final Composite panel = toolkit.createComposite(section);
        GridDataFactory.fillDefaults().grab(true, true).indent(0, 0).applyTo(panel);
        GridLayoutFactory.fillDefaults().applyTo(panel);
        section.setClient(panel);
        return panel;
    }

    private void createDocumentationControl(final Composite panel) {
        documentation = new StyledText(panel, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        documentation.addPaintListener(new PaintListener() {
            @Override
            public void paintControl(final PaintEvent e) {
                final StyledText text = (StyledText) e.widget;
                if (text.getText().isEmpty() && !text.isFocusControl()) {
                    final Color current = e.gc.getForeground();
                    e.gc.setForeground(e.display.getSystemColor(SWT.COLOR_GRAY));
                    e.gc.drawString("Documentation", 0, 0);
                    e.gc.setForeground(current);
                }
            }
        });
        documentation.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(final FocusEvent e) {
                documentation.redraw();
            }
            @Override
            public void focusLost(final FocusEvent e) {
                documentation.redraw();
            }
        });
        toolkit.adapt(documentation, true, false);
        toolkit.paintBordersFor(documentation);
        if (fileModel.isEditable()) {
            documentation.addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(final ModifyEvent e) {
                    if (documentation.getText().equals(getDocumentation(getSection()))) {
                        return;
                    }
                    setDirty();
                    
                    if (documenationChangeJob != null && documenationChangeJob.getState() == Job.SLEEPING) {
                        documenationChangeJob.cancel();
                    }
                    documenationChangeJob = createDocumentationChangeJob(documentation.getText());
                    documenationChangeJob.schedule(300);
                }
            });
        }

        GridDataFactory.fillDefaults().grab(true, true).minSize(SWT.DEFAULT, 60).hint(SWT.DEFAULT, 100)
                .applyTo(documentation);
    }

    private Job createDocumentationChangeJob(final String docu) {
        return new Job("changing documentation") {
            @Override
            protected IStatus run(final IProgressMonitor monitor) {
                final String newDocumentation = docu.replaceAll("\t", " ").replaceAll("  +", " ");
                final RobotSettingsSection settingsSection = getSection();
                if (settingsSection == null) {
                    return Status.OK_STATUS;
                }

                final RobotSetting docSetting = settingsSection.getSetting("Documentation");

                if (docSetting == null && !newDocumentation.isEmpty()) {
                    commandsStack.execute(new CreateFreshGeneralSettingCommand(settingsSection, "Documentation",
                            newArrayList(newDocumentation)));
                } else if (docSetting != null && newDocumentation.isEmpty()) {
                    commandsStack.execute(new DeleteSettingKeywordCallCommand(newArrayList(docSetting)));
                } else if (docSetting != null) {
                    commandsStack.execute(new SetKeywordCallArgumentCommand(docSetting, 0, newDocumentation));
                }
                return Status.OK_STATUS;
            }
        };
    }

    private void createViewer(final Composite panel) {
        viewer = com.google.common.base.Optional
                .of(new RowExposingTableViewer(panel, SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL));
        viewer.get().getTable().setLinesVisible(true);
        viewer.get().getTable().setHeaderVisible(true);
        viewer.get().getTable().addListener(SWT.MeasureItem, new Listener() {
            @Override
            public void handleEvent(final Event event) {
                event.height = Double.valueOf(event.gc.getFontMetrics().getHeight() * 1.5).intValue();
            }
        });
        viewer.get().setContentProvider(new GeneralSettingsContentProvider());
        viewer.get().setComparer(new SettingElementsComparer());
        GridDataFactory.fillDefaults().grab(true, true).applyTo(viewer.get().getTable());
        Viewers.boundViewerWithContext(viewer.get(), site,
                "org.robotframework.ide.eclipse.tableeditor.settings.general.context");
        CellsActivationStrategy.addActivationStrategy(viewer.get(), RowTabbingStrategy.MOVE_IN_CYCLE);
        ColumnViewerToolTipSupport.enableFor(viewer.get(), ToolTip.NO_RECREATE);
        ViewersConfigurator.disableContextMenuOnHeader(viewer.get());
    }

    private void createColumns() {
        final Supplier<HeaderFilterMatchesCollection> matcherProvider = getMatchesProvider();
        createNameColumn(matcherProvider);

        for (int i = 0; i < calculateLongestArgumentsLength(); i++) {
            createArgumentColumn(i, matcherProvider);
        }
        createCommentColumn(matcherProvider);
    }

    private Supplier<HeaderFilterMatchesCollection> getMatchesProvider() {
        return new Supplier<HeaderFilterMatchesCollection>() {
            @Override
            public HeaderFilterMatchesCollection get() {
                return matches;
            }
        };
    }

    private void createNameColumn(final Supplier<HeaderFilterMatchesCollection> matcherProvider) {
        ViewerColumnsFactory.newColumn("Setting").withWidth(100)
                .labelsProvidedBy(new GeneralSettingsNamesLabelProvider(matcherProvider))
                .createFor(viewer.get());
    }

    private void createArgumentColumn(final int index, final Supplier<HeaderFilterMatchesCollection> matcherProvider) {
        ViewerColumnsFactory.newColumn("").withWidth(120)
            .labelsProvidedBy(new GeneralSettingsArgsLabelProvider(matcherProvider, index))
            .editingSupportedBy(new GeneralSettingsArgsEditingSupport(viewer.get(), index, commandsStack))
            .editingEnabledOnlyWhen(fileModel.isEditable())
            .createFor(viewer.get());
    }

    private void createCommentColumn(final Supplier<HeaderFilterMatchesCollection> matcherProvider) {
        ViewerColumnsFactory.newColumn("Comment").withWidth(200)
            .shouldGrabAllTheSpaceLeft(true).withMinWidth(100)
            .labelsProvidedBy(new GeneralSettingsCommentsLabelProvider(matcherProvider))
            .editingSupportedBy(new GeneralSettingsCommentsEditingSupport(viewer.get(), commandsStack))
            .editingEnabledOnlyWhen(fileModel.isEditable())
            .createFor(viewer.get());
    }

    private int calculateLongestArgumentsLength() {
        int max = RedPlugin.getDefault().getPreferences().getMimalNumberOfArgumentColumns();
        for (final RobotElement setting : GeneralSettingsModel.findGeneralSettingsList(getSection())) {
            max = Math.max(max, ((RobotSetting) setting).getArguments().size());
        }
        return max;
    }

    private void createContextMenu() {
        final String menuId = "org.robotframework.ide.eclipse.editor.page.settings.general.contextMenu";

        final MenuManager manager = new MenuManager("Robot suite editor general settings context menu", menuId);
        final Table control = viewer.get().getTable();
        final Menu menu = manager.createContextMenu(control);
        control.setMenu(menu);
        site.registerContextMenu(menuId, manager, site.getSelectionProvider(), false);
    }

    private void setInput() {
        final RobotSettingsSection section = getSection();

        documentation.setEditable(fileModel.isEditable() && section != null);
        documentation.setText(getDocumentation(section));

        if (viewer.isPresent()) {
            viewer.get().setInput(section);
        }
    }

    private String getDocumentation(final RobotSettingsSection section) {
        if (section == null) {
            return "";
        } else {
            final RobotSetting docSetting = section.getSetting("Documentation");
            return docSetting != null && !docSetting.getArguments().isEmpty() ? docSetting.getArguments().get(0) : "";
        }
    }

    private RobotSettingsSection getSection() {
        return fileModel.findSection(RobotSettingsSection.class).orNull();
    }

    @Override
    public void setFocus() {
        if (viewer.isPresent()) {
            viewer.get().getTable().setFocus();
            // there is some problem with measuring table row height sometimes, so
            // the table needs to be laid out in its parent
            viewer.get().getTable().getParent().layout();
        }
    }

    public void revealSetting(final RobotSetting setting) {
        Sections.maximizeChosenSectionAndMinimalizeOthers(section);
        if ("Documentation".equals(setting.getName())) {
            documentation.forceFocus();
            documentation.selectAll();
            clearSettingsSelection();
        } else {
            final Object entry = getEntryForSetting(setting);
            if (entry != null && viewer.isPresent()) {
                viewer.get().setSelection(new StructuredSelection(entry));
            }
            setFocus();
        }
    }

    private Object getEntryForSetting(final RobotSetting setting) {
        final TableItem[] items = viewer.get().getTable().getItems();
        for (final TableItem item : items) {
            if (item.getData() instanceof Entry<?, ?>) {
                final Object value = ((Entry<?, ?>) item.getData()).getValue();
                if (setting == value) {
                    return item.getData();
                }
            }
        }
        return null;
    }

    public void clearSettingsSelection() {
        if (viewer.isPresent()) {
            viewer.get().setSelection(StructuredSelection.EMPTY);
        }
    }

    public FocusedViewerAccessor getFocusedViewerAccessor() {
        if (!viewer.isPresent()) {
            return null;
        }
        final ViewerColumnsManagingStrategy columnsManagingStrategy = new ViewerColumnsManagingStrategy() {
            @Override
            public void addColumn(final ColumnViewer viewer) {
                final RowExposingTableViewer tableViewer = (RowExposingTableViewer) viewer;
                final int index = tableViewer.getTable().getColumnCount() - 2;

                createArgumentColumn(index, getMatchesProvider());
                tableViewer.moveLastColumnTo(index + 1);
                tableViewer.reflowColumnsWidth();
            }

            @Override
            public void removeColumn(final ColumnViewer viewer) {
                final RowExposingTableViewer tableViewer = (RowExposingTableViewer) viewer;

                final int columnCount = tableViewer.getTable().getColumnCount();
                if (columnCount <= 2) {
                    return;
                }
                // always remove last columns which displays arguments
                final int position = columnCount - 2;
                final int orderIndexBeforeRemoving = tableViewer.getTable().getColumnOrder()[position];
                tableViewer.removeColumnAtPosition(position);
                tableViewer.reflowColumnsWidth();

                final TableViewerEditor editor = (TableViewerEditor) tableViewer.getColumnViewerEditor();
                final ViewerCell focusCell = editor.getFocusCell();
                if (focusCell.getColumnIndex() == orderIndexBeforeRemoving) {
                    tableViewer.setFocusCell(tableViewer.getTable().getColumnCount() - 2);
                }
            }
        };
        return new FocusedViewerAccessor(columnsManagingStrategy, viewer.get());
    }

    @Override
    public HeaderFilterMatchesCollection collectMatches(final String filter) {
        final SettingsMatchesCollection settingsMatches = new SettingsMatchesCollection();
        final List<RobotElement> generalSettings = GeneralSettingsModel
                .findGeneralSettingsList(viewer.isPresent() ? (RobotSettingsSection) viewer.get().getInput() : null);
        final RobotSettingsSection settingsSection = getSection();
        if (settingsSection != null) {
            final RobotSetting setting = settingsSection.getSetting("Documentation");
            if (setting != null) {
                generalSettings.add(setting);
            }
        }

        settingsMatches.collect(generalSettings, filter);
        return settingsMatches;
    }

    @Inject
    @Optional
    private void whenUserRequestedFiltering(@UIEventTopic(RobotSuiteEditorEvents.SECTION_FILTERING_TOPIC + "/"
            + RobotSettingsSection.SECTION_NAME) final HeaderFilterMatchesCollection matches) {
        this.matches = matches;

        if (matches == null) {
            clearDocumentationMatches();
        } else {
            setDocumentationMatches(matches);
        }

        if (viewer.isPresent()) {
            try {
                viewer.get().getTable().setRedraw(false);
                if (matches == null) {
                    viewer.get().setFilters(new ViewerFilter[0]);
                } else {
                    viewer.get().setFilters(new ViewerFilter[] { new SettingsMatchesFilter(matches) });
                }
            } finally {
                viewer.get().getTable().setRedraw(true);
            }
        }

    }

    private void setDocumentationMatches(final HeaderFilterMatchesCollection settingsMatches) {
        clearDocumentationMatches();

        final Collection<Range<Integer>> ranges = settingsMatches.getRanges(documentation.getText());
        for (final Range<Integer> range : ranges) {
            final Color bg = ColorsManager.getColor(255, 255, 175);
            documentation.setStyleRange(new StyleRange(range.lowerEndpoint(), range.upperEndpoint()
                    - range.lowerEndpoint(), null, bg));
        }
    }

    private void clearDocumentationMatches() {
        documentation.setStyleRange(null);
    }

    @Persist
    public void whenSaving() {
        // user could just typed something into documentation box, so the job was scheduled, we need to wait for it to
        // end in order to proceed with saving
        if (documenationChangeJob != null) {
            try {
                documenationChangeJob.join();
            } catch (final InterruptedException e) {
                RedPlugin.logError("Documentation change job was interrupted", e);
            }
        }
    }

    private void setDirty() {
        dirtyProviderService.setDirtyState(true);
    }

    @Inject
    @Optional
    private void whenSectionIsCreated(
            @UIEventTopic(RobotModelEvents.ROBOT_SUITE_SECTION_ADDED) final RobotSuiteFile file) {
        if (file == fileModel) {
            setInput();
            setDirty();
        }
    }

    @Inject
    @Optional
    private void whenSectionIsRemoved(
            @UIEventTopic(RobotModelEvents.ROBOT_SUITE_SECTION_REMOVED) final RobotSuiteFile file) {
        if (file == fileModel) {
            setInput();
            setDirty();
        }
    }

    @Inject
    @Optional
    private void whenSettingDetailsChanges(
            @UIEventTopic(RobotModelEvents.ROBOT_KEYWORD_CALL_DETAIL_CHANGE_ALL) final RobotSetting setting) {
        if (setting.getSuiteFile() == fileModel && setting.getGroup() == SettingsGroup.NO_GROUP) {
            if (viewer.isPresent()) {
                viewer.get().refresh();
            }
            setDirty();
        }
    }

    @Inject
    @Optional
    private void whenSettingIsAddedOrRemoved(
            @UIEventTopic(RobotModelEvents.ROBOT_SETTINGS_STRUCTURAL_ALL) final RobotSuiteFileSection section) {
        if (section == getSection()) {
            if (viewer.isPresent()) {
                viewer.get().refresh();
            }
            setDirty();
        }
    }

    @Inject
    @Optional
    private void whenFileChangedExternally(
            @UIEventTopic(RobotModelEvents.EXTERNAL_MODEL_CHANGE) final RobotElementChange change) {
        if (change.getKind() == Kind.CHANGED) {
            final RobotSuiteFile suite = change.getElement() instanceof RobotSuiteFile
                    ? (RobotSuiteFile) change.getElement() : null;
            if (suite == fileModel) {
                refreshEverything();
            }
        }
    }

    @Inject
    @Optional
    private void whenReconcilationWasDone(
            @UIEventTopic(RobotModelEvents.REPARSING_DONE) final RobotSuiteFile fileModel) {
        if (fileModel == this.fileModel) {
            refreshEverything();
        }
    }

    private void refreshEverything() {
        try {
            ViewerCell focusCell = null;
            if (viewer.isPresent()) {
                viewer.get().getTable().setRedraw(false);
                focusCell = viewer.get().getColumnViewerEditor().getFocusCell();
            }
            setInput();
            if (focusCell != null) {
                viewer.get().setFocusCell(focusCell.getColumnIndex());
            }
        } finally {
            if (viewer.isPresent()) {
                viewer.get().getTable().setRedraw(true);
            }
        }
    }
}

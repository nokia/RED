/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.tools.services.IDirtyProviderService;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.GridDataFactory;
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
import org.eclipse.swt.widgets.Composite;
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
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.CellsActivationStrategy;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.CellsActivationStrategy.RowTabbingStrategy;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.FocusedViewerAccessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.FocusedViewerAccessor.ViewerColumnsManagingStrategy;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.HeaderFilterMatchesCollection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionFormFragment;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorSources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotSuiteEditorEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.popup.ImportSettingsPopup;
import org.robotframework.red.forms.RedFormToolkit;
import org.robotframework.red.forms.Sections;
import org.robotframework.red.viewers.ElementsAddingEditingSupport.NewElementsCreator;
import org.robotframework.red.viewers.Viewers;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.collect.Lists;

public class ImportSettingsFormFragment implements ISectionFormFragment {

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

    private RowExposingTableViewer viewer;

    private Section section;

    private HeaderFilterMatchesCollection matches;

    TableViewer getViewer() {
        return viewer;
    }

    @Override
    public void initialize(final Composite parent) {
        section = toolkit.createSection(parent, ExpandableComposite.TWISTIE | ExpandableComposite.TITLE_BAR);
        section.setText("Imports");
        section.setExpanded(false);
        GridDataFactory.fillDefaults().grab(true, false).minSize(1, 22).applyTo(section);
        Sections.switchGridCellGrabbingOnExpansion(section);
        Sections.installMaximazingPossibility(section);

        createViewer();
        createColumns();

        createContextMenu();
        viewer.setInput(getSection());
    }

    private void createViewer() {
        viewer = new RowExposingTableViewer(section, SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(viewer.getTable());
        section.setClient(viewer.getTable());
        viewer.getTable().setLinesVisible(true);
        viewer.getTable().setHeaderVisible(true);
        ViewersConfigurator.configureRowsHeight(viewer, 1.5);
        ViewersConfigurator.disableContextMenuOnHeader(viewer);

        viewer.setContentProvider(new ImportSettingsContentProvider(fileModel.isEditable()));
        viewer.setComparer(new SettingElementsComparer());
        Viewers.boundViewerWithContext(viewer, site,
                "org.robotframework.ide.eclipse.tableeditor.settings.import.context");
        CellsActivationStrategy.addActivationStrategy(viewer, RowTabbingStrategy.MOVE_TO_NEXT);
        ColumnViewerToolTipSupport.enableFor(viewer, ToolTip.NO_RECREATE);
    }

    private void createColumns() {
        final NewElementsCreator<RobotElement> creator = newElementsCreator();
        final Supplier<HeaderFilterMatchesCollection> matchesProvider = getMatchesProvider();
        ViewerColumnsFactory.newColumn("Import").withWidth(140)
            .labelsProvidedBy(new KeywordCallNameLabelProvider(matchesProvider))
            .editingSupportedBy(new ImportSettingsEditingSupport(viewer, commandsStack, creator))
            .editingEnabledOnlyWhen(fileModel.isEditable())
            .createFor(viewer);

        for (int i = 0; i < calcualateLongestArgumentsLength(); i++) {
            final String name = i == 0 ? "Name / Path" : "";
            createArgumentColumn(name, i, matchesProvider, creator);
        }
        ViewerColumnsFactory.newColumn("Comment").withWidth(200)
            .shouldGrabAllTheSpaceLeft(true).withMinWidth(50)
            .labelsProvidedBy(new SettingsCommentsLabelProvider(matchesProvider))
            .editingSupportedBy(new SettingsCommentsEditingSupport(viewer, commandsStack, creator))
            .editingEnabledOnlyWhen(fileModel.isEditable())
            .createFor(viewer);
    }

    private Supplier<HeaderFilterMatchesCollection> getMatchesProvider() {
        return new Supplier<HeaderFilterMatchesCollection>() {
            @Override
            public HeaderFilterMatchesCollection get() {
                return matches;
            }
        };
    }

    private NewElementsCreator<RobotElement> newElementsCreator() {
        return new NewElementsCreator<RobotElement>() {
            @Override
            public RobotElement createNew() {
                new ImportSettingsPopup(viewer.getControl().getShell(), commandsStack, fileModel, null).open();
                return null;
            }
        };
    }

    private int calcualateLongestArgumentsLength() {
        int max = RedPlugin.getDefault().getPreferences().getMimalNumberOfArgumentColumns();
        final List<?> elements = (List<?>) viewer.getInput();
        if (elements != null) {
            for (final Object element : elements) {
                final RobotSetting setting = (RobotSetting) element;
                if (setting != null) {
                    max = Math.max(max, setting.getArguments().size());
                }
            }
        }
        return max;
    }

    private void createArgumentColumn(final String name, final int index,
            final Supplier<HeaderFilterMatchesCollection> matchesProvider,
            final NewElementsCreator<RobotElement> creator) {
        ViewerColumnsFactory.newColumn(name).withWidth(120)
                .labelsProvidedBy(new SettingsArgsLabelProvider(matchesProvider, index))
                .editingSupportedBy(new SettingsArgsEditingSupport(viewer, index, commandsStack, creator))
                .editingEnabledOnlyWhen(fileModel.isEditable()).createFor(viewer);
    }

    private void createContextMenu() {
        final String menuId = "org.robotframework.ide.eclipse.editor.page.settings.imports.contextMenu";

        final MenuManager manager = new MenuManager("Robot suite editor imports settings context menu", menuId);
        final Table control = viewer.getTable();
        final Menu menu = manager.createContextMenu(control);
        control.setMenu(menu);
        site.registerContextMenu(menuId, manager, site.getSelectionProvider(), false);
    }

    private void setDirty() {
        dirtyProviderService.setDirtyState(true);
    }

    private RobotSettingsSection getSection() {
        return fileModel.findSection(RobotSettingsSection.class).orNull();
    }

    @Override
    public void setFocus() {
        viewer.getTable().setFocus();
    }

    public void revealSetting(final RobotSetting setting) {
        Sections.maximizeChosenSectionAndMinimalizeOthers(section);
        viewer.setSelection(new StructuredSelection(setting));
        setFocus();
    }

    public void clearSettingsSelection() {
        viewer.setSelection(StructuredSelection.EMPTY);
    }

    public FocusedViewerAccessor getFocusedViewerAccessor() {
        final ViewerColumnsManagingStrategy columnsManagingStrategy = new ViewerColumnsManagingStrategy() {
            @Override
            public void addColumn(final ColumnViewer viewer) {
                final RowExposingTableViewer tableViewer = (RowExposingTableViewer) viewer;
                final int index = tableViewer.getTable().getColumnCount() - 2;

                final String name = index == 0 ? "Name / Path" : "";
                createArgumentColumn(name, index, getMatchesProvider(), newElementsCreator());
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
        return new FocusedViewerAccessor(columnsManagingStrategy, viewer);
    }

    @Override
    public HeaderFilterMatchesCollection collectMatches(final String filter) {
        final SettingsMatchesCollection settingsMatches = new SettingsMatchesCollection();
        settingsMatches.collect(getDisplayedSettings(), filter);
        return settingsMatches;
    }

    @Inject
    @Optional
    private void whenUserRequestedFiltering(@UIEventTopic(RobotSuiteEditorEvents.SECTION_FILTERING_TOPIC + "/"
            + RobotSettingsSection.SECTION_NAME) final HeaderFilterMatchesCollection matches) {
        this.matches = matches;

        try {
            viewer.getTable().setRedraw(false);
            if (matches == null) {
                viewer.setFilters(new ViewerFilter[0]);
            } else {
                viewer.setFilters(new ViewerFilter[] { new SettingsMatchesFilter(matches) });
            }
        } finally {
            viewer.getTable().setRedraw(true);
        }
    }

    @Inject
    @Optional
    private void whenSectionIsCreated(
            @UIEventTopic(RobotModelEvents.ROBOT_SUITE_SECTION_ADDED) final RobotSuiteFile file) {
        if (file == fileModel) {
            viewer.setInput(getSection());
            viewer.refresh();
            setDirty();
        }
    }

    @Inject
    @Optional
    private void whenSectionIsRemoved(
            @UIEventTopic(RobotModelEvents.ROBOT_SUITE_SECTION_REMOVED) final RobotSuiteFile file) {
        if (file == fileModel) {
            viewer.setInput(getSection());
            viewer.refresh();
            setDirty();
        }
    }

    @Inject
    @Optional
    private void whenSettingDetailsChanges(
            @UIEventTopic(RobotModelEvents.ROBOT_KEYWORD_CALL_DETAIL_CHANGE_ALL) final RobotSetting setting) {
        if (setting.getSuiteFile() == fileModel && getDisplayedSettings().contains(setting)) {
            viewer.update(setting, null);
            setDirty();
        }
    }

    private List<RobotElement> getDisplayedSettings() {
        final TableItem[] items = viewer.getTable().getItems();
        if (items == null) {
            return newArrayList();
        }
        final List<RobotElement> settingDisplayed = Lists.transform(newArrayList(items),
                new Function<TableItem, RobotElement>() {
                    @Override
                    public RobotSetting apply(final TableItem item) {
                        if (item.getData() instanceof RobotSetting) {
                            return (RobotSetting) item.getData();
                        }
                        return null;
                    }
                });
        return settingDisplayed;
    }

    @Inject
    @Optional
    private void whenSettingIsAddedOrRemoved(
            @UIEventTopic(RobotModelEvents.ROBOT_SETTINGS_STRUCTURAL_ALL) final RobotSuiteFileSection section) {
        if (section.getSuiteFile() == fileModel) {
            viewer.refresh();
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
            viewer.getTable().setRedraw(false);
            final ViewerCell focusCell = viewer.getColumnViewerEditor().getFocusCell();
            viewer.setInput(getSection());
            viewer.refresh();
            if (focusCell != null) {
                viewer.setFocusCell(focusCell.getColumnIndex());
            }
        } finally {
            viewer.getTable().setRedraw(true);
        }
    }

    @Inject
    @Optional
    private void whenSettingIsEdited(
            @UIEventTopic(RobotModelEvents.ROBOT_SETTING_IMPORTS_EDIT) final RobotSetting setting) {
        new ImportSettingsPopup(viewer.getControl().getShell(), commandsStack, fileModel, setting).open();
    }
}

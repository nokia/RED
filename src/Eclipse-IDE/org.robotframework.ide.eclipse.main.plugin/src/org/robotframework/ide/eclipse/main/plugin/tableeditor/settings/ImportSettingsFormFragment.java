/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.tools.services.IDirtyProviderService;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.edit.editor.ICellEditor;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsEventLayer;
import org.eclipse.nebula.widgets.nattable.grid.cell.AlternatingRowConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.hover.HoverLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.selection.EditTraversalStrategy;
import org.eclipse.nebula.widgets.nattable.selection.ITraversalStrategy;
import org.eclipse.nebula.widgets.nattable.selection.MoveCellSelectionCommandHandler;
import org.eclipse.nebula.widgets.nattable.selection.RowSelectionProvider;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.sort.ISortModel;
import org.eclipse.nebula.widgets.nattable.sort.SortHeaderLayer;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Section;
import org.osgi.service.event.Event;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences.CellWrappingStrategy;
import org.robotframework.ide.eclipse.main.plugin.hyperlink.TableHyperlinksSupport;
import org.robotframework.ide.eclipse.main.plugin.hyperlink.detectors.ITableHyperlinksDetector;
import org.robotframework.ide.eclipse.main.plugin.hyperlink.detectors.TableHyperlinksToFilesDetector;
import org.robotframework.ide.eclipse.main.plugin.hyperlink.detectors.TableHyperlinksToVariablesDetector;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElementChange;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElementChange.Kind;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting.SettingsGroup;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.settings.CreateFreshSettingCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.AddingToken;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.FilterSwitchRequest;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.HeaderFilterMatchesCollection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.HeaderFilterMatchesCollector;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionFormFragment;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.MarkersLabelAccumulator;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.MarkersSelectionLayerPainter;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RedNatTableContentTooltip;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorSources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotSuiteEditorEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.SelectionLayerAccessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.SuiteFileMarkersContainer;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableThemes;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableThemes.TableTheme;
import org.robotframework.red.forms.RedFormToolkit;
import org.robotframework.red.forms.Sections;
import org.robotframework.red.nattable.AddingElementLabelAccumulator;
import org.robotframework.red.nattable.AssistanceLabelAccumulator;
import org.robotframework.red.nattable.NewElementsCreator;
import org.robotframework.red.nattable.RedColumnHeaderDataProvider;
import org.robotframework.red.nattable.RedNattableDataProvidersFactory;
import org.robotframework.red.nattable.RedNattableLayersFactory;
import org.robotframework.red.nattable.TableCellsStrings;
import org.robotframework.red.nattable.configs.AddingElementStyleConfiguration;
import org.robotframework.red.nattable.configs.AlternatingRowsStyleConfiguration;
import org.robotframework.red.nattable.configs.ColumnHeaderStyleConfiguration;
import org.robotframework.red.nattable.configs.CommentsStyleConfiguration;
import org.robotframework.red.nattable.configs.GeneralTableStyleConfiguration;
import org.robotframework.red.nattable.configs.HeaderSortConfiguration;
import org.robotframework.red.nattable.configs.HoveredCellStyleConfiguration;
import org.robotframework.red.nattable.configs.RedTableEditConfiguration;
import org.robotframework.red.nattable.configs.RedTableResizableRowsBindingsConfiguration;
import org.robotframework.red.nattable.configs.RowHeaderStyleConfiguration;
import org.robotframework.red.nattable.configs.SelectionStyleConfiguration;
import org.robotframework.red.nattable.configs.SettingsCommentsLabelAccumulator;
import org.robotframework.red.nattable.configs.SettingsItemsLabelAccumulator;
import org.robotframework.red.nattable.configs.SettingsItemsStyleConfiguration;
import org.robotframework.red.nattable.configs.SpecialItemsStyleConfiguration;
import org.robotframework.red.nattable.configs.TableMatchesSupplierRegistryConfiguration;
import org.robotframework.red.nattable.configs.TableMenuConfiguration;
import org.robotframework.red.nattable.configs.TableStringsPositionsRegistryConfiguration;
import org.robotframework.red.nattable.configs.VariableInsideLabelAccumulator;
import org.robotframework.red.nattable.configs.VariableInsideStyleConfiguration;
import org.robotframework.red.nattable.configs.WithNameLabelAccumulator;
import org.robotframework.red.nattable.edit.CellEditorCloser;
import org.robotframework.red.nattable.painter.RedNatGridLayerPainter;
import org.robotframework.red.nattable.painter.RedTableTextPainter;
import org.robotframework.services.event.Events;

public class ImportSettingsFormFragment implements ISectionFormFragment, ISettingsFormFragment {

    @Inject
    private IEventBroker eventBroker;

    @Inject
    private IEditorSite site;

    @Inject
    @Named(RobotEditorSources.SUITE_FILE_MODEL)
    private RobotSuiteFile fileModel;

    @Inject
    private SuiteFileMarkersContainer markersContainer;

    @Inject
    private RobotEditorCommandsStack commandsStack;

    @Inject
    private IDirtyProviderService dirtyProviderService;

    @Inject
    private RedFormToolkit toolkit;

    private HeaderFilterMatchesCollection matches;

    private Section importSettingsSection;

    private NatTable table;

    private ImportSettingsDataProvider dataProvider;

    private ISortModel sortModel;

    private RowSelectionProvider<Object> selectionProvider;

    private SelectionLayerAccessor selectionLayerAccessor;

    private TableHyperlinksSupport detector;

    @Override
    public ISelectionProvider getSelectionProvider() {
        return selectionProvider;
    }

    @Override
    public SelectionLayerAccessor getSelectionLayerAccessor() {
        return selectionLayerAccessor;
    }

    @Override
    public NatTable getTable() {
        return table;
    }

    @Override
    public void initialize(final Composite parent) {
        importSettingsSection = toolkit.createSection(parent,
                ExpandableComposite.TWISTIE | ExpandableComposite.TITLE_BAR);
        importSettingsSection.setText("Imports");
        final boolean isResourceFile = fileModel.isResourceFile();
        importSettingsSection.setExpanded(isResourceFile);
        importSettingsSection.setBackground(parent.getBackground());
        GridDataFactory.fillDefaults().grab(true, isResourceFile).minSize(1, 22).applyTo(importSettingsSection);
        Sections.switchGridCellGrabbingOnExpansion(importSettingsSection);
        Sections.installMaximazingPossibility(importSettingsSection);

        final TableTheme theme = TableThemes.getTheme(parent.getBackground().getRGB());
        final Composite panel = createPanel(importSettingsSection);
        setupNatTable(panel, theme);
    }

    private Composite createPanel(final Section section) {
        final Composite panel = toolkit.createComposite(section);
        panel.setBackground(section.getBackground());
        GridDataFactory.fillDefaults().grab(true, true).indent(0, 0).applyTo(panel);
        GridLayoutFactory.fillDefaults().applyTo(panel);
        section.setClient(panel);
        return panel;
    }

    private void setupNatTable(final Composite parent, final TableTheme theme) {
        final ConfigRegistry configRegistry = new ConfigRegistry();

        final RedNattableDataProvidersFactory dataProvidersFactory = new RedNattableDataProvidersFactory();
        final RedNattableLayersFactory factory = new RedNattableLayersFactory();

        // data providers
        dataProvider = new ImportSettingsDataProvider(commandsStack, getSection());
        final IDataProvider columnHeaderDataProvider = new ImportSettingsColumnHeaderDataProvider();
        final IDataProvider rowHeaderDataProvider = dataProvidersFactory.createRowHeaderDataProvider(dataProvider);

        // body layers
        final DataLayer bodyDataLayer = factory.createDataLayer(dataProvider,
                new AssistanceLabelAccumulator(dataProvider,
                        position -> 0 < position.getColumnPosition()
                                && position.getColumnPosition() < dataProvider.getColumnCount() - 1,
                        rowObject -> rowObject instanceof RobotElement),
                new AlternatingRowConfigLabelAccumulator(),
                new AddingElementLabelAccumulator(dataProvider),
                new ImportTypesLabelAccumulator(dataProvider),
                new SettingsCommentsLabelAccumulator(dataProvider),
                new SettingsItemsLabelAccumulator(),
                new WithNameLabelAccumulator(dataProvider),
                new VariableInsideLabelAccumulator());
        final GlazedListsEventLayer<RobotKeywordCall> bodyEventLayer = factory
                .createGlazedListEventsLayer(bodyDataLayer, dataProvider.getSortedList());
        final HoverLayer bodyHoverLayer = factory.createHoverLayer(bodyEventLayer);
        final SelectionLayer bodySelectionLayer = factory.createSelectionLayer(theme, bodyHoverLayer);
        final ViewportLayer bodyViewportLayer = factory.createViewportLayer(bodySelectionLayer);

        // column header layers
        final DataLayer columnHeaderDataLayer = factory.createColumnHeaderDataLayer(columnHeaderDataProvider, theme,
                new SettingsDynamicTableColumnHeaderLabelAccumulator(dataProvider));
        final ColumnHeaderLayer columnHeaderLayer = factory.createColumnHeaderLayer(columnHeaderDataLayer,
                bodySelectionLayer, bodyViewportLayer);
        final SortHeaderLayer<RobotKeywordCall> columnHeaderSortingLayer = factory.createSortingColumnHeaderLayer(
                columnHeaderDataLayer, columnHeaderLayer, dataProvider.getPropertyAccessor(), configRegistry,
                dataProvider.getSortedList());

        // row header layers
        final RowHeaderLayer rowHeaderLayer = factory.createRowsHeaderLayer(bodySelectionLayer, bodyViewportLayer,
                rowHeaderDataProvider, new MarkersSelectionLayerPainter(theme.getHeadersGridColor()),
                new MarkersLabelAccumulator(markersContainer, dataProvider));

        // corner layer
        final ILayer cornerLayer = factory.createCornerLayer(columnHeaderDataProvider, columnHeaderSortingLayer,
                rowHeaderDataProvider, rowHeaderLayer, theme);

        // combined grid layer
        final GridLayer gridLayer = factory.createGridLayer(bodyViewportLayer, columnHeaderSortingLayer, rowHeaderLayer,
                cornerLayer);
        final boolean wrapCellContent = hasWrappedCells();
        if (wrapCellContent) {
            gridLayer.addConfiguration(new RedTableResizableRowsBindingsConfiguration());
        }
        gridLayer.addConfiguration(new RedTableEditConfiguration<>(fileModel, newElementsCreator(), wrapCellContent));
        gridLayer.addConfiguration(new ImportsSettingsEditConfiguration(fileModel, dataProvider, wrapCellContent));

        table = theme.configureScrollBars(parent, bodyViewportLayer,
                tableParent -> createTable(tableParent, theme, factory, gridLayer, bodyDataLayer, configRegistry));

        bodyViewportLayer.registerCommandHandler(new MoveCellSelectionCommandHandler(bodySelectionLayer,
                new EditTraversalStrategy(ITraversalStrategy.TABLE_CYCLE_TRAVERSAL_STRATEGY, table),
                new EditTraversalStrategy(ITraversalStrategy.AXIS_CYCLE_TRAVERSAL_STRATEGY, table)));

        sortModel = columnHeaderSortingLayer.getSortModel();
        selectionProvider = new RowSelectionProvider<>(bodySelectionLayer, dataProvider, false);
        selectionLayerAccessor = new SelectionLayerAccessor(dataProvider, bodySelectionLayer, selectionProvider);

        // tooltips support
        new RedNatTableContentTooltip(table, markersContainer, dataProvider);
    }

    private NatTable createTable(final Composite parent, final TableTheme theme, final RedNattableLayersFactory factory,
            final GridLayer gridLayer, final DataLayer dataLayer, final ConfigRegistry configRegistry) {
        final int style = SWT.NO_BACKGROUND | SWT.NO_REDRAW_RESIZE | SWT.DOUBLE_BUFFERED | SWT.V_SCROLL | SWT.H_SCROLL;
        final NatTable table = new NatTable(parent, style, gridLayer, false);
        table.setFont(theme.getFont());
        table.setConfigRegistry(configRegistry);
        table.setLayerPainter(
                new RedNatGridLayerPainter(table, theme.getBodyGridColor(), theme.getHeadersBackground(),
                        theme.getHeadersUnderlineColor(), RedNattableLayersFactory.GRID_BORDER_WIDTH,
                        RedNattableLayersFactory.ROW_HEIGHT));
        table.setBackground(theme.getBodyOddRowBackground());
        table.setForeground(theme.getBodyForeground());

        // calculate columns width
        table.addListener(SWT.Paint,
                factory.getColumnsWidthCalculatingPaintListener(table, dataProvider, dataLayer, 120, 200));

        addCustomStyling(table, theme);

        // matches support
        table.addConfiguration(new TableMatchesSupplierRegistryConfiguration(() -> matches));

        // hyperlinks support
        final TableCellsStrings tableStrings = new TableCellsStrings();
        table.addConfiguration(new TableStringsPositionsRegistryConfiguration(tableStrings));
        detector = TableHyperlinksSupport.enableHyperlinksInTable(table, tableStrings);
        detector.addDetectors(new TableHyperlinksToVariablesDetector(dataProvider),
                new TableHyperlinksToFilesDetector(dataProvider));

        // sorting
        table.addConfiguration(new HeaderSortConfiguration());
        table.addConfiguration(new SettingsDynamicTableSortingConfiguration());

        // popup menus
        table.addConfiguration(new ImportSettingsTableMenuConfiguration(site, table, selectionProvider));

        table.configure();

        table.addFocusListener(new SettingsTableFocusListener(
                "org.robotframework.ide.eclipse.tableeditor.settings.import.context", site));
        GridDataFactory.fillDefaults().grab(true, true).applyTo(table);
        return table;
    }

    private void addCustomStyling(final NatTable table, final TableTheme theme) {
        final boolean wrapCells = hasWrappedCells();
        table.addConfiguration(new GeneralTableStyleConfiguration(theme, new RedTableTextPainter(wrapCells)));
        table.addConfiguration(new HoveredCellStyleConfiguration(theme));
        table.addConfiguration(new ColumnHeaderStyleConfiguration(theme));
        table.addConfiguration(new RowHeaderStyleConfiguration(theme));
        table.addConfiguration(new AlternatingRowsStyleConfiguration(theme));
        table.addConfiguration(new ImportTypesStyleConfiguration(theme, wrapCells));
        table.addConfiguration(new SelectionStyleConfiguration(theme, table.getFont()));
        table.addConfiguration(new AddingElementStyleConfiguration(theme, fileModel.isEditable()));
        table.addConfiguration(new CommentsStyleConfiguration(theme));
        table.addConfiguration(new SettingsItemsStyleConfiguration(theme));
        table.addConfiguration(new SpecialItemsStyleConfiguration(theme));
        table.addConfiguration(new VariableInsideStyleConfiguration(theme));
    }

    private boolean hasWrappedCells() {
        final RedPreferences preferences = RedPlugin.getDefault().getPreferences();
        return preferences.getCellWrappingStrategy() == CellWrappingStrategy.WRAP;
    }

    @Override
    public void setFocus() {
        table.setFocus();
    }

    @Override
    public void invokeSaveAction() {
        onSave();
    }

    public void aboutToChangeToOtherPage() {
        CellEditorCloser.closeForcibly(table);
    }

    @Persist
    public void onSave() {
        CellEditorCloser.closeForcibly(table);
    }

    private void setDirty() {
        dirtyProviderService.setDirtyState(true);
    }

    private RobotSettingsSection getSection() {
        return fileModel.findSection(RobotSettingsSection.class).orElse(null);
    }

    public void revealSetting(final RobotSetting setting, final boolean focus) {
        Sections.maximizeChosenSectionAndMinimalizeOthers(importSettingsSection);
        if (dataProvider.isFilterSet() && !dataProvider.isProvided(setting)) {
            final String topic = RobotSuiteEditorEvents.FORM_FILTER_SWITCH_REQUEST_TOPIC + "/"
                    + RobotSettingsSection.SECTION_NAME;
            eventBroker.send(topic, new FilterSwitchRequest(RobotSettingsSection.SECTION_NAME, ""));
        }
        CellEditorCloser.closeForcibly(table);
        if (focus) {
            table.setFocus();
        }
        selectionProvider.setSelection(new StructuredSelection(new Object[] { setting }));
    }

    public void clearSettingsSelection() {
        selectionProvider.setSelection(StructuredSelection.EMPTY);
    }

    private NewElementsCreator<RobotElement> newElementsCreator() {
        return addingTokenRowIndex -> {
            final RobotSettingsSection section = dataProvider.getInput();
            commandsStack.execute(
                    new CreateFreshSettingCommand(section, SettingsGroup.LIBRARIES.getName(), new ArrayList<>()));
            return section.getChildren().get(section.getChildren().size() - 1);
        };
    }

    @Override
    public HeaderFilterMatchesCollection collectMatches(final String filter) {
        final SettingsMatchesCollection settingsMatches = new SettingsMatchesCollection();
        final List<RobotElement> settings = new ArrayList<>();
        settings.addAll(dataProvider.getInput().getImportSettings());
        settingsMatches.collect(settings, filter);
        return settingsMatches;
    }

    @Override
    public List<ITableHyperlinksDetector> getDetectors() {
        return detector.getDetectors();
    }

    @Inject
    @Optional
    private void whenUserRequestedFilteringEnabled(@UIEventTopic(RobotSuiteEditorEvents.SECTION_FILTERING_ENABLED_TOPIC
            + "/" + RobotSettingsSection.SECTION_NAME) final HeaderFilterMatchesCollection matches) {
        if (matches.getCollectors().contains(this)) {
            this.matches = matches;
            dataProvider.setFilter(new SettingsMatchesFilter(matches));
            table.refresh();
        }
    }

    @Inject
    @Optional
    private void whenUserRequestedFilteringDisabled(
            @UIEventTopic(RobotSuiteEditorEvents.SECTION_FILTERING_DISABLED_TOPIC + "/"
                    + RobotSettingsSection.SECTION_NAME) final Collection<HeaderFilterMatchesCollector> collectors) {
        if (collectors.contains(this)) {
            this.matches = null;
            dataProvider.setFilter(null);
            table.refresh();
        }
    }

    @Inject
    @Optional
    private void whenSectionIsCreated(
            @UIEventTopic(RobotModelEvents.ROBOT_SUITE_SECTION_ADDED) final RobotSuiteFile file) {
        if (file == fileModel && dataProvider.getInput() == null) {
            refreshTable();
            setDirty();
        }
    }

    @Inject
    @Optional
    private void whenSectionIsRemoved(
            @UIEventTopic(RobotModelEvents.ROBOT_SUITE_SECTION_REMOVED) final RobotSuiteFile file) {
        if (file == fileModel && dataProvider.getInput() != null) {
            final ICellEditor activeCellEditor = table.getActiveCellEditor();
            if (activeCellEditor != null && !activeCellEditor.isClosed()) {
                activeCellEditor.close();
            }
            refreshTable();
            selectionLayerAccessor.clear();
            setDirty();
        }
    }

    @Inject
    @Optional
    private void whenSettingDetailsChanges(
            @UIEventTopic(RobotModelEvents.ROBOT_KEYWORD_CALL_DETAIL_CHANGE_ALL) final RobotSetting setting) {
        if (setting.getSuiteFile() == fileModel) {
            tableInputIsReplaced().run();
        }
    }

    @Inject
    @Optional
    private void whenKeywordCallIsConverted(
            @UIEventTopic(RobotModelEvents.ROBOT_KEYWORD_CALL_CONVERTED) final Event event) {

        final RobotSettingsSection settingsSection = Events.get(event, IEventBroker.DATA, RobotSettingsSection.class);
        final RobotSetting setting = Events.get(event, RobotModelEvents.ADDITIONAL_DATA, RobotSetting.class);

        if (settingsSection != null && settingsSection.getSuiteFile() == fileModel
                && RobotSetting.SettingsGroup.getImportsGroupsSet().contains(setting.getGroup())) {
            sortModel.clear();
            selectionLayerAccessor.selectElementPreservingSelectedColumnsAfterOperation(setting,
                    tableInputIsReplaced());
        }
    }

    @Inject
    @Optional
    private void whenSettingIsAdded(
            @UIEventTopic(RobotModelEvents.ROBOT_SETTING_ADDED) final RobotSuiteFileSection section) {
        if (section.getSuiteFile() == fileModel) {
            sortModel.clear();
            selectionLayerAccessor.preserveSelectionWhen(tableInputIsReplaced());
        }
    }

    @Inject
    @Optional
    private void whenSettingIsRemoved(
            @UIEventTopic(RobotModelEvents.ROBOT_SETTING_REMOVED) final RobotSuiteFileSection section) {
        if (section.getSuiteFile() == fileModel) {
            final RobotSettingsSection settingsSection = (RobotSettingsSection) section;

            selectionLayerAccessor.preserveSelectionWhen(tableInputIsReplaced(), coordinate -> {
                if (settingsSection.getImportSettings().isEmpty()) {
                    return null;
                } else if (dataProvider.getRowObject(coordinate.getRowPosition()) instanceof AddingToken) {
                    return new PositionCoordinate(coordinate.getLayer(), coordinate.getColumnPosition(),
                            coordinate.getRowPosition() - 1);
                }
                return coordinate;
            });
        }
    }

    @Inject
    @Optional
    private void whenSettingIsMoved(
            @UIEventTopic(RobotModelEvents.ROBOT_SETTING_MOVED) final RobotSuiteFileSection section) {
        if (section.getSuiteFile() == fileModel) {
            sortModel.clear();
            selectionLayerAccessor.preserveElementSelectionWhen(tableInputIsReplaced());
        }
    }

    private Runnable tableInputIsReplaced() {
        return () -> {
            refreshTable();
            setDirty();
        };
    }

    @Inject
    @Optional
    private void whenFileChangedExternally(
            @UIEventTopic(RobotModelEvents.EXTERNAL_MODEL_CHANGE) final RobotElementChange change) {
        if (change.getKind() == Kind.CHANGED) {
            final RobotSuiteFile suite = change.getElement() instanceof RobotSuiteFile
                    ? (RobotSuiteFile) change.getElement()
                    : null;
            if (suite == fileModel) {
                refreshTable();
            }
        }
    }

    @Inject
    @Optional
    private void whenReconcilationWasDone(
            @UIEventTopic(RobotModelEvents.REPARSING_DONE) final RobotSuiteFile fileModel) {
        if (fileModel == this.fileModel) {
            commandsStack.clear();
            refreshTable();
        }
    }

    @Inject
    @Optional
    private void whenMarkersContainerWasReloaded(
            @UIEventTopic(RobotModelEvents.MARKERS_CACHE_RELOADED) final RobotSuiteFile fileModel) {
        if (fileModel == this.fileModel) {
            table.refresh();
        }
    }

    private void refreshTable() {
        dataProvider.setInput(getSection());
        table.refresh();
    }

    private class ImportSettingsColumnHeaderDataProvider extends RedColumnHeaderDataProvider {

        public ImportSettingsColumnHeaderDataProvider() {
            super(dataProvider);
        }

        @Override
        public Object getDataValue(final int columnIndex, final int rowIndex) {
            if (columnIndex == 0) {
                return "Import";
            } else if (columnIndex == 1) {
                return "Name / Path";
            } else if (isLastColumn(columnIndex)) {
                return "Comment";
            }
            return "";
        }
    }

    private static class ImportSettingsTableMenuConfiguration extends TableMenuConfiguration {

        public ImportSettingsTableMenuConfiguration(final IEditorSite site, final NatTable table,
                final ISelectionProvider selectionProvider) {
            super(site, table, selectionProvider,
                    "org.robotframework.ide.eclipse.editor.page.settings.imports.contextMenu",
                    "Robot suite editor imports settings context menu");
        }
    }
}

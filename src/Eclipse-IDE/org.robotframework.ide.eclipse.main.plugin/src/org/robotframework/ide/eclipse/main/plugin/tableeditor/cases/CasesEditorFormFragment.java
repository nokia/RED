/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.tools.services.IDirtyProviderService;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.edit.editor.ICellEditor;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsEventLayer;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsSortModel;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.tree.GlazedListTreeData;
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
import org.eclipse.nebula.widgets.nattable.tree.ITreeRowModel;
import org.eclipse.nebula.widgets.nattable.tree.TreeLayer;
import org.eclipse.nebula.widgets.nattable.tree.TreeRowModel;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorSite;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElementChange;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElementChange.Kind;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.cases.CreateCaseFreshKeywordCallCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.cases.CreateFreshCaseCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.AddingToken;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.DocumentationElementsSelectionChangedListener;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.FilterSwitchRequest;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.HeaderFilterMatchesCollection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.HeaderFilterMatchesCollector;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionFormFragment;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.MarkersLabelAccumulator;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.MarkersSelectionLayerPainter;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorSources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotSuiteEditorEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.SelectionLayerAccessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.SuiteFileMarkersContainer;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableThemes;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableThemes.TableTheme;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TreeLayerAccessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.CasesMatchesCollection.CasesFilter;
import org.robotframework.red.nattable.AddingElementLabelAccumulator;
import org.robotframework.red.nattable.NewElementsCreator;
import org.robotframework.red.nattable.RedColumnHeaderDataProvider;
import org.robotframework.red.nattable.RedNattableDataProvidersFactory;
import org.robotframework.red.nattable.RedNattableLayersFactory;
import org.robotframework.red.nattable.configs.AddingElementStyleConfiguration;
import org.robotframework.red.nattable.configs.AlternatingRowsStyleConfiguration;
import org.robotframework.red.nattable.configs.ColumnHeaderStyleConfiguration;
import org.robotframework.red.nattable.configs.GeneralTableStyleConfiguration;
import org.robotframework.red.nattable.configs.HeaderSortConfiguration;
import org.robotframework.red.nattable.configs.HoveredCellStyleConfiguration;
import org.robotframework.red.nattable.configs.RedTableEditConfiguration;
import org.robotframework.red.nattable.configs.RowHeaderStyleConfiguration;
import org.robotframework.red.nattable.configs.SelectionStyleConfiguration;
import org.robotframework.red.nattable.configs.TableMenuConfiguration;
import org.robotframework.red.nattable.edit.CellEditorCloser;
import org.robotframework.red.nattable.painter.RedNatGridLayerPainter;
import org.robotframework.red.nattable.painter.SearchMatchesTextPainter;

import com.google.common.base.Supplier;

public class CasesEditorFormFragment implements ISectionFormFragment {

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

    private HeaderFilterMatchesCollection matches;

    private NatTable table;
    
    private ISortModel sortModel;

    private CasesDataProvider dataProvider;

    private RowSelectionProvider<Object> selectionProvider;
    
    private SelectionLayerAccessor selectionLayerAccessor;
    private TreeLayerAccessor treeLayerAccessor;


    public ISelectionProvider getSelectionProvider() {
        return selectionProvider;
    }
    
    public SelectionLayerAccessor getSelectionLayerAccessor() {
        return selectionLayerAccessor;
    }

    public TreeLayerAccessor getTreeLayerAccessor() {
        return treeLayerAccessor;
    }

    public NatTable getTable() {
        return table;
    }

    @Override
    public void initialize(final Composite parent) {
        setupNatTable(parent);
    }

    private void setupNatTable(final Composite parent) {

        final TableTheme theme = TableThemes.getTheme(parent.getBackground().getRGB());

        final ConfigRegistry configRegistry = new ConfigRegistry();

        final RedNattableDataProvidersFactory dataProvidersFactory = new RedNattableDataProvidersFactory();
        final RedNattableLayersFactory factory = new RedNattableLayersFactory();

        // data providers
        dataProvider = new CasesDataProvider(commandsStack, getSection());
        
        final IDataProvider columnHeaderDataProvider = new CasesColumnHeaderDataProvider();
        final IDataProvider rowHeaderDataProvider = dataProvidersFactory.createRowHeaderDataProvider(dataProvider);

        // body layers
        final DataLayer bodyDataLayer = factory.createDataLayer(dataProvider, 270, 150,
                new AlternatingRowConfigLabelAccumulator(), new AddingElementLabelAccumulator(dataProvider),
                new CasesElementsLabelAccumulator(dataProvider));
        final GlazedListsEventLayer<Object> glazedListsEventLayer = new GlazedListsEventLayer<>(bodyDataLayer,
                dataProvider.getTreeList());
        final GlazedListTreeData<Object> treeData = new GlazedListTreeData<>(dataProvider.getTreeList());
        final ITreeRowModel<Object> treeRowModel = new TreeRowModel<>(treeData);

        final HoverLayer bodyHoverLayer = factory.createHoverLayer(glazedListsEventLayer);
        final SelectionLayer bodySelectionLayer = factory.createSelectionLayer(theme, bodyHoverLayer);

        final TreeLayer treeLayer = new TreeLayer(bodySelectionLayer, treeRowModel);
        final ViewportLayer bodyViewportLayer = new ViewportLayer(treeLayer);

        // column header layers
        final DataLayer columnHeaderDataLayer = factory.createColumnHeaderDataLayer(columnHeaderDataProvider);
        final ColumnHeaderLayer columnHeaderLayer = factory.createColumnHeaderLayer(columnHeaderDataLayer,
                bodySelectionLayer, bodyViewportLayer);
        
        sortModel = new GlazedListsSortModel<>(dataProvider.getSortedList(), dataProvider.getPropertyAccessor(),
                configRegistry, columnHeaderDataLayer);
        dataProvider.getTreeFormat().setSortModel(sortModel);
        final SortHeaderLayer<Object> columnHeaderSortingLayer = factory.createSortingColumnHeaderLayer(
                columnHeaderLayer, sortModel);

        // row header layers
        final RowHeaderLayer rowHeaderLayer = factory.createRowsHeaderLayer(bodySelectionLayer, bodyViewportLayer,
                rowHeaderDataProvider, new MarkersSelectionLayerPainter(),
                new MarkersLabelAccumulator(markersContainer, dataProvider));

        // corner layer
        final ILayer cornerLayer = factory.createCornerLayer(columnHeaderDataProvider, columnHeaderSortingLayer,
                rowHeaderDataProvider, rowHeaderLayer);

        // combined grid layer
        final GridLayer gridLayer = factory.createGridLayer(bodyViewportLayer, columnHeaderSortingLayer, rowHeaderLayer,
                cornerLayer);
        gridLayer.addConfiguration(new RedTableEditConfiguration<>(fileModel, newElementsCreator()));

        table = createTable(parent, theme, gridLayer, configRegistry);

        bodyViewportLayer.registerCommandHandler(new MoveCellSelectionCommandHandler(bodySelectionLayer,
                new EditTraversalStrategy(ITraversalStrategy.TABLE_CYCLE_TRAVERSAL_STRATEGY, table),
                new EditTraversalStrategy(ITraversalStrategy.AXIS_CYCLE_TRAVERSAL_STRATEGY, table)));

        selectionProvider = new RowSelectionProvider<>(bodySelectionLayer, dataProvider, false);
        selectionLayerAccessor = new SelectionLayerAccessor(bodySelectionLayer);
        treeLayerAccessor = new TreeLayerAccessor(treeLayer);
        
        selectionProvider.addSelectionChangedListener(new DocumentationElementsSelectionChangedListener(eventBroker));

        new CasesTableContentTooltip(table, markersContainer, dataProvider);
    }

    private NatTable createTable(final Composite parent, final TableTheme theme, final GridLayer gridLayer,
            final ConfigRegistry configRegistry) {
        final int style = SWT.NO_BACKGROUND | SWT.NO_REDRAW_RESIZE | SWT.DOUBLE_BUFFERED | SWT.V_SCROLL | SWT.H_SCROLL;
        final NatTable table = new NatTable(parent, style, gridLayer, false);
        table.setConfigRegistry(configRegistry);
        table.setLayerPainter(
                new RedNatGridLayerPainter(table, theme.getGridBorderColor(), theme.getHeadersBackground(),
                        theme.getHeadersUnderlineColor(), 2, RedNattableLayersFactory.ROW_HEIGHT));
        table.setBackground(theme.getBodyBackgroundOddRowBackground());
        table.setForeground(parent.getForeground());

        addCustomStyling(table, theme);

        // sorting
        table.addConfiguration(new HeaderSortConfiguration());
        table.addConfiguration(new CasesTableSortingConfiguration(dataProvider));
        
        // popup menus
        table.addConfiguration(new CasesTableMenuConfiguration(site, table, selectionProvider));

        table.configure();
        GridDataFactory.fillDefaults().grab(true, true).applyTo(table);

        return table;
    }

    private void addCustomStyling(final NatTable table, final TableTheme theme) {
        final Supplier<HeaderFilterMatchesCollection> matchesSupplier = new Supplier<HeaderFilterMatchesCollection>() {
            @Override
            public HeaderFilterMatchesCollection get() {
                return matches;
            }
        };
        final GeneralTableStyleConfiguration tableStyle = new GeneralTableStyleConfiguration(theme,
                new SearchMatchesTextPainter(matchesSupplier));

        table.addConfiguration(tableStyle);
        table.addConfiguration(new HoveredCellStyleConfiguration(theme));
        table.addConfiguration(new ColumnHeaderStyleConfiguration(theme));
        table.addConfiguration(new RowHeaderStyleConfiguration(theme));
        table.addConfiguration(new AlternatingRowsStyleConfiguration(theme));
        table.addConfiguration(new CasesElementsStyleConfiguration(theme, fileModel.isEditable(), matchesSupplier));
        table.addConfiguration(new SelectionStyleConfiguration(theme, table.getFont()));
        table.addConfiguration(new AddingElementStyleConfiguration(theme, fileModel.isEditable()));
    }

    @Override
    public void setFocus() {
        table.setFocus();
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

    private RobotCasesSection getSection() {
        return fileModel.findSection(RobotCasesSection.class).orNull();
    }

    public void revealElement(final RobotElement element) {
        if (dataProvider.isFilterSet() && !dataProvider.isProvided(element)) {
            final String topic = RobotSuiteEditorEvents.FORM_FILTER_SWITCH_REQUEST_TOPIC + "/"
                    + RobotCasesSection.SECTION_NAME.replaceAll(" ", "_");
            eventBroker.send(topic, new FilterSwitchRequest(RobotCasesSection.SECTION_NAME.replaceAll(" ", "_"), ""));
        }
        selectionProvider.setSelection(new StructuredSelection(new Object[] { element }));
        setFocus();
    }

    public void clearSettingsSelection() {
        selectionProvider.setSelection(StructuredSelection.EMPTY);
    }

    private NewElementsCreator<RobotElement> newElementsCreator() {
        return new NewElementsCreator<RobotElement>() {

            @Override
            public RobotElement createNew(final int addingTokenRowIndex) {
                final RobotElement createdElement;
                final AddingToken token = (AddingToken) dataProvider.getRowObject(addingTokenRowIndex);
                if (token.isNested()) {
                    final RobotCase testCase = (RobotCase) token.getParent();
                    commandsStack.execute(new CreateCaseFreshKeywordCallCommand(testCase));
                    createdElement = testCase.getChildren().get(testCase.getChildren().size() - 1);

                } else {
                    final RobotCasesSection section = dataProvider.getInput();
                    commandsStack.execute(new CreateFreshCaseCommand(section));
                    createdElement = section.getChildren().get(section.getChildren().size() - 1);
                }
                return createdElement;
            }
        };
    }

    @Override
    public HeaderFilterMatchesCollection collectMatches(final String filter) {
        final CasesMatchesCollection casesMatches = new CasesMatchesCollection();
        casesMatches.collect(dataProvider.getInput(), filter);
        return casesMatches;
    }

    @Inject
    @Optional
    private void whenUserRequestedFilteringEnabled(@UIEventTopic(RobotSuiteEditorEvents.SECTION_FILTERING_ENABLED_TOPIC
            + "/Test_Cases") final HeaderFilterMatchesCollection matches) {
        if (matches.getCollectors().contains(this)) {
            this.matches = matches;
            dataProvider.setFilter(new CasesFilter(matches));
            table.refresh();
        }
    }

    @Inject
    @Optional
    private void whenUserRequestedFilteringDisabled(
            @UIEventTopic(RobotSuiteEditorEvents.SECTION_FILTERING_DISABLED_TOPIC
                    + "/Test_Cases") final Collection<HeaderFilterMatchesCollector> collectors) {
        if (collectors.contains(this)) {
            this.matches = null;
            dataProvider.setFilter(null);
            table.refresh();
        }
    }

    @Inject
    @Optional
    private void whenCaseIsAdded(
            @UIEventTopic(RobotModelEvents.ROBOT_CASE_ADDED) final RobotSuiteFileSection section) {
        if (section.getSuiteFile() == fileModel) {
            sortModel.clear();
        }
        whenElementIsAddedOrRemoved(section);
    }

    @Inject
    @Optional
    private void whenCaseIsRemoved(
            @UIEventTopic(RobotModelEvents.ROBOT_CASE_REMOVED) final RobotSuiteFileSection section) {
        whenElementIsAddedOrRemoved(section);
        if (getSection() != null && section.getChildren().isEmpty()) {
            selectionLayerAccessor.getSelectionLayer().clear();
        }
    }

    @Inject
    @Optional
    private void whenCaseIsMoved(@UIEventTopic(RobotModelEvents.ROBOT_CASE_MOVED) final RobotSuiteFileSection section) {
        if (section.getSuiteFile() == fileModel) {
            sortModel.clear();
        }
        final ISelection oldSelection = selectionProvider.getSelection();
        whenElementIsAddedOrRemoved(section);
        selectionProvider.setSelection(oldSelection);
    }

    @Inject
    @Optional
    private void whenKeywordCallIsAdded(
            @UIEventTopic(RobotModelEvents.ROBOT_KEYWORD_CALL_ADDED) final RobotCase testCase) {
        if (testCase.getSuiteFile() == fileModel) {
            sortModel.clear();
        }
        whenElementIsAddedOrRemoved(testCase);
    }

    @Inject
    @Optional
    private void whenKeywordCallIsRemoved(
            @UIEventTopic(RobotModelEvents.ROBOT_KEYWORD_CALL_REMOVED) final RobotCase testCase) {
        whenElementIsAddedOrRemoved(testCase);
    }

    @Inject
    @Optional
    private void whenKeywordCallIsMoved(
            @UIEventTopic(RobotModelEvents.ROBOT_KEYWORD_CALL_MOVED) final RobotCase testCase) {
        final ISelection oldSelection = selectionProvider.getSelection();
        whenElementIsAddedOrRemoved(testCase);
        selectionProvider.setSelection(oldSelection);
    }

    private void whenElementIsAddedOrRemoved(final RobotFileInternalElement element) {
        if (element.getSuiteFile() == fileModel) {
            sortModel.clear();
            dataProvider.setInput(getSection());
            table.refresh();
            setDirty();
        }
    }

    @Inject
    @Optional
    private void whenCaseDetailIsChanged(
            @UIEventTopic(RobotModelEvents.ROBOT_CASE_DETAIL_CHANGE_ALL) final RobotCase testCase) {
        if (testCase.getSuiteFile() == fileModel) {
            table.update();
            table.refresh();
            setDirty();
        }
    }

    @Inject
    @Optional
    private void whenKeywordCallDetailIsChanged(
            @UIEventTopic(RobotModelEvents.ROBOT_KEYWORD_CALL_DETAIL_CHANGE_ALL) final RobotKeywordCall keywordCall) {
        if (keywordCall.getParent() instanceof RobotCase && keywordCall.getSuiteFile() == fileModel) {
            table.update();
            table.refresh();
            setDirty();
        }
    }

    @Inject
    @Optional
    private void whenSectionIsCreated(
            @UIEventTopic(RobotModelEvents.ROBOT_SUITE_SECTION_ADDED) final RobotSuiteFile file) {
        if (file == fileModel && dataProvider.getInput() == null) {
            dataProvider.setInput(getSection());
            table.refresh();
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
            dataProvider.setInput(getSection());
            selectionLayerAccessor.getSelectionLayer().clear();
            table.refresh();

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
                dataProvider.setInput(getSection());
                table.refresh();
            }
        }
    }

    @Inject
    @Optional
    private void whenReconcilationWasDone(
            @UIEventTopic(RobotModelEvents.REPARSING_DONE) final RobotSuiteFile fileModel) {
        if (fileModel == this.fileModel) {
            dataProvider.setInput(getSection());
            table.refresh();
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

    private class CasesColumnHeaderDataProvider extends RedColumnHeaderDataProvider {

        public CasesColumnHeaderDataProvider() {
            super(dataProvider);
        }

        @Override
        public Object getDataValue(final int columnIndex, final int rowIndex) {
            return isLastColumn(columnIndex) ? "Comment" : "";
        }
    }

    private static class CasesTableMenuConfiguration extends TableMenuConfiguration {

        public CasesTableMenuConfiguration(final IEditorSite site, final NatTable table,
                final ISelectionProvider selectionProvider) {
            super(site, table, selectionProvider, "org.robotframework.ide.eclipse.editor.page.cases.contextMenu",
                    "Robot suite editor cases context menu");
        }
    }
}

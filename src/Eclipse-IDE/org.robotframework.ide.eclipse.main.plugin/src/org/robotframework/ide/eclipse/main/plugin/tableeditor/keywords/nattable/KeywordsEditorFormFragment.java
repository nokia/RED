/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.nattable;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.tools.services.IDirtyProviderService;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.edit.command.EditSelectionCommand;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsEventLayer;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.tree.GlazedListTreeData;
import org.eclipse.nebula.widgets.nattable.grid.cell.AlternatingRowConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.hover.HoverLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.painter.layer.NatGridLayerPainter;
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
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElementChange;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElementChange.Kind;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.CreateFreshKeywordCallCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.CreateFreshKeywordDefinitionCommand;
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
import org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.KeywordsMatchesCollection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.nattable.KeywordsDataProvider.RobotKeywordCallAdder;
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
import org.robotframework.red.nattable.painter.SearchMatchesTextPainter;
import org.robotframework.red.swt.SwtThread;

import com.google.common.base.Supplier;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.TreeList;

public class KeywordsEditorFormFragment implements ISectionFormFragment {

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

    private KeywordsDataProvider dataProvider;

    private RowSelectionProvider<Object> selectionProvider;
    
    private SelectionLayerAccessor selectionLayerAccessor;

    public ISelectionProvider getSelectionProvider() {
        return selectionProvider;
    }
    
    public SelectionLayerAccessor getSelectionLayerAccessor() {
        return selectionLayerAccessor;
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
        dataProvider = new KeywordsDataProvider(commandsStack, getSection());
        
        final IDataProvider columnHeaderDataProvider = new KeywordsColumnHeaderDataProvider();
        final IDataProvider rowHeaderDataProvider = dataProvidersFactory.createRowHeaderDataProvider(dataProvider);

        // body layers
        final DataLayer bodyDataLayer = factory.createDataLayer(dataProvider, 270, 150,
                new AlternatingRowConfigLabelAccumulator(), new AddingElementLabelAccumulator(dataProvider),
                new KeywordElementsInTreeLabelAccumulator(dataProvider));
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
        final ColumnHeaderLayer columnHeaderLayer = factory.createColumnHeaderLayer(columnHeaderDataLayer, bodySelectionLayer,
                bodyViewportLayer);
        
        // FIXME: tree sorting
        final SortHeaderLayer<Object> columnHeaderSortingLayer = factory.createSortingColumnHeaderLayer(
                columnHeaderDataLayer, columnHeaderLayer, dataProvider.getPropertyAccessor(), configRegistry,
                new SortedList<>(GlazedLists.<Object> eventListOf(), null));

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
        gridLayer.addConfiguration(new RedTableEditConfiguration(fileModel, newElementsCreator(bodySelectionLayer)));

        table = createTable(parent, theme, gridLayer, configRegistry);

        bodyViewportLayer.registerCommandHandler(new MoveCellSelectionCommandHandler(bodySelectionLayer,
                new EditTraversalStrategy(ITraversalStrategy.TABLE_CYCLE_TRAVERSAL_STRATEGY, table),
                new EditTraversalStrategy(ITraversalStrategy.AXIS_CYCLE_TRAVERSAL_STRATEGY, table)));

        sortModel = columnHeaderSortingLayer.getSortModel();
        selectionProvider = new RowSelectionProvider<>(bodySelectionLayer, dataProvider, false);
        selectionLayerAccessor = new SelectionLayerAccessor(bodySelectionLayer);

        new RedNatTableContentTooltip(table, markersContainer, dataProvider);

    }

    private NatTable createTable(final Composite parent, final TableTheme theme, final GridLayer gridLayer,
            final ConfigRegistry configRegistry) {
        final int style = SWT.NO_BACKGROUND | SWT.NO_REDRAW_RESIZE | SWT.DOUBLE_BUFFERED | SWT.V_SCROLL | SWT.H_SCROLL;
        final NatTable table = new NatTable(parent, style, gridLayer, false);
        table.setConfigRegistry(configRegistry);
        table.setLayerPainter(
                new NatGridLayerPainter(table, theme.getGridBorderColor(), RedNattableLayersFactory.ROW_HEIGHT));
        table.setBackground(theme.getBodyBackgroundOddRowBackground());
        table.setForeground(parent.getForeground());

        addCustomStyling(table, theme);

        // sorting
        table.addConfiguration(new HeaderSortConfiguration());
        table.addConfiguration(new KeywordsTableSortingConfiguration(dataProvider));
        
        // popup menus
        table.addConfiguration(new KeywordsTableMenuConfiguration(site, table, selectionProvider));

        table.configure();
        GridDataFactory.fillDefaults().grab(true, true).applyTo(table);

        return table;
    }

    private void addCustomStyling(final NatTable table, final TableTheme theme) {
        final GeneralTableStyleConfiguration tableStyle = new GeneralTableStyleConfiguration(theme,
                new SearchMatchesTextPainter(new Supplier<HeaderFilterMatchesCollection>() {

                    @Override
                    public HeaderFilterMatchesCollection get() {
                        return matches;
                    }
                }));

        table.addConfiguration(tableStyle);
        table.addConfiguration(new HoveredCellStyleConfiguration(theme));
        table.addConfiguration(new ColumnHeaderStyleConfiguration(theme));
        table.addConfiguration(new RowHeaderStyleConfiguration(theme));
        table.addConfiguration(new AlternatingRowsStyleConfiguration(theme));
        table.addConfiguration(new SelectionStyleConfiguration(theme, table.getFont()));
        table.addConfiguration(new AddingElementStyleConfiguration(theme, fileModel.isEditable()));
        table.addConfiguration(new KeywordDefinitionElementStyleConfiguration(theme, fileModel.isEditable()));
    }

    @Override
    public void setFocus() {
        table.setFocus();
    }

    private void setDirty() {
        dirtyProviderService.setDirtyState(true);
    }

    private RobotKeywordsSection getSection() {
        return fileModel.findSection(RobotKeywordsSection.class).orNull();
    }

    public void revealElement(final RobotElement setting) {
        selectionProvider.setSelection(new StructuredSelection(new Object[] { setting }));
        setFocus();
    }

    public void clearSettingsSelection() {
        selectionProvider.setSelection(StructuredSelection.EMPTY);
    }

    private NewElementsCreator<Object> newElementsCreator(final SelectionLayer selectionLayer) {
        return new NewElementsCreator<Object>() {

            @Override
            public Object createNew() {

                dataProvider.setMatches(null);
                
                final PositionCoordinate[] selectedCellPositions = selectionLayer.getSelectedCellPositions();
                if (selectedCellPositions.length == 1) {
                    final int selectedRow = selectedCellPositions[0].getRowPosition();
                    final Object rowObject = dataProvider.getRowObject(selectedRow);
                    if (rowObject != null) {
                        if (rowObject == KeywordsDataProvider.ADDING_TOKEN) {
                            commandsStack.execute(new CreateFreshKeywordDefinitionCommand(dataProvider.getInput(), true));
                        } else if (rowObject instanceof RobotKeywordCallAdder) {
                            commandsStack.execute(new CreateFreshKeywordCallCommand(
                                    ((RobotKeywordCallAdder) rowObject).getParent()));
                        }
                    }
                }

                SwtThread.asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        table.doCommand(new EditSelectionCommand(table, table.getConfigRegistry()));
                    }
                });

                return null;
            }
        };
    }

    // @Override
    // protected Supplier<HeaderFilterMatchesCollection> getMatchesProvider() {
    // return new Supplier<HeaderFilterMatchesCollection>() {
    // @Override
    // public HeaderFilterMatchesCollection get() {
    // return matches;
    // }
    // };
    // }

    @Override
    public HeaderFilterMatchesCollection collectMatches(final String filter) {
        final KeywordsMatchesCollection keywordMatches = new KeywordsMatchesCollection();
        keywordMatches.collect(dataProvider.getInput(), filter);
        return keywordMatches;
    }

    @Inject
    @Optional
    private void whenUserRequestedFilteringEnabled(@UIEventTopic(RobotSuiteEditorEvents.SECTION_FILTERING_ENABLED_TOPIC
            + "/" + RobotKeywordsSection.SECTION_NAME) final HeaderFilterMatchesCollection matches) {
        if (matches.getCollectors().contains(this)) {
            this.matches = matches;
            dataProvider.setMatches(matches);
            table.refresh();
        }
    }

    @Inject
    @Optional
    private void whenUserRequestedFilteringDisabled(
            @UIEventTopic(RobotSuiteEditorEvents.SECTION_FILTERING_DISABLED_TOPIC + "/"
                    + RobotKeywordsSection.SECTION_NAME) final Collection<HeaderFilterMatchesCollector> collectors) {
        if (collectors.contains(this)) {
            this.matches = null;
            dataProvider.setMatches(null);
            table.refresh();
        }
    }

    @Inject
    @Optional
    private void whenKeywordIsAddedOrRemoved(
            @UIEventTopic(RobotModelEvents.ROBOT_KEYWORD_DEFINITION_STRUCTURAL_ALL) final RobotSuiteFileSection section) {
        if (section.getSuiteFile() == fileModel) {
            sortModel.clear();
            dataProvider.setInput(getSection());
            table.refresh();
            setDirty();
        }
    }

    @Inject
    @Optional
    private void whenKeywordIsAdded(
            @UIEventTopic(RobotModelEvents.ROBOT_KEYWORD_DEFINITION_ADDED) final RobotSuiteFileSection section) {
        if (section.getSuiteFile() == fileModel) {
            sortModel.clear();
        }
    }

    @Inject
    @Optional
    private void whenKeywordCallIsAddedOrRemoved(
            @UIEventTopic(RobotModelEvents.ROBOT_KEYWORD_CALL_STRUCTURAL_ALL) final RobotKeywordDefinition definition) {
        if (definition.getSuiteFile() == fileModel) {
            sortModel.clear();
            dataProvider.setInput(getSection());
            table.refresh();
            setDirty();
        }
    }

    @Inject
    @Optional
    private void whenKeywordDetailIsChanged(
            @UIEventTopic(RobotModelEvents.ROBOT_KEYWORD_DEFINITION_CHANGE_ALL) final RobotKeywordDefinition definition) {
        if (definition.getSuiteFile() == fileModel) {
            table.update();
            table.refresh();
            setDirty();
        }
    }

    @Inject
    @Optional
    private void whenKeywordCallDetailIsChanged(
            @UIEventTopic(RobotModelEvents.ROBOT_KEYWORD_CALL_DETAIL_CHANGE_ALL) final RobotKeywordCall keywordCall) {
        if (keywordCall.getParent() instanceof RobotKeywordDefinition && keywordCall.getSuiteFile() == fileModel) {
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
            dataProvider.setInput(getSection());
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

    private class KeywordsColumnHeaderDataProvider extends RedColumnHeaderDataProvider {

        public KeywordsColumnHeaderDataProvider() {
            super(dataProvider);
        }

        @Override
        public Object getDataValue(final int columnIndex, final int rowIndex) {
            return isLastColumn(columnIndex) ? "Comment" : "";
        }
    }

    static class KeywordsTreeFormat implements TreeList.Format<Object> {

        private ISortModel treeSortModel;

        @Override
        public void getPath(final List<Object> path, final Object element) {

            if (element instanceof RobotKeywordCall) {
                path.add(((RobotKeywordCall) element).getParent());
            } else if (element instanceof RobotKeywordCallAdder) {
                path.add(((RobotKeywordCallAdder) element).getParent());
            }

            path.add(element);

        }

        @Override
        public boolean allowsChildren(final Object element) {
            return true;
        }

        @Override
        public Comparator<? super Object> getComparator(final int depth) {
            // if (treeSortModel != null && depth == 0) {
            // Comparator<Object> comparator = new Comparator<Object>() {
            //
            // @Override
            // public int compare(Object o1, Object o2) {
            // if (o1 instanceof RobotKeywordDefinition && o2 instanceof RobotKeywordDefinition) {
            // RobotKeywordDefinition d1 = (RobotKeywordDefinition) o1;
            // RobotKeywordDefinition d2 = (RobotKeywordDefinition) o2;
            // return d1.getName().compareToIgnoreCase(d2.getName());
            // }
            // return 0;
            // }
            // };
            // return new SortableTreeComparator<Object>(comparator, treeSortModel);
            // }
            return null;
        }

        public void setTreeSortModel(final ISortModel treeSortModel) {
            this.treeSortModel = treeSortModel;
        }

    }

    private static class KeywordsTableMenuConfiguration extends TableMenuConfiguration {

        public KeywordsTableMenuConfiguration(final IEditorSite site, final NatTable table,
                final ISelectionProvider selectionProvider) {
            super(site, table, selectionProvider, "org.robotframework.ide.eclipse.editor.page.keywords.contextMenu",
                    "Robot suite editor keywords context menu");
        }
    }
}

/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables;

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
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsEventLayer;
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
import org.eclipse.nebula.widgets.nattable.tooltip.NatTableContentTooltip;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorSite;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElementChange;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElementChange.Kind;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.variables.CreateFreshVariableCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.HeaderFilterMatchesCollection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionFormFragment;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorSources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotSuiteEditorEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.SelectionLayerAccessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableThemes;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableThemes.TableTheme;
import org.robotframework.red.nattable.AddingElementLabelAccumulator;
import org.robotframework.red.nattable.NewElementsCreator;
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
import org.robotframework.red.nattable.painter.SearchMatchesTextPainter;
import org.robotframework.red.swt.SwtThread;

import com.google.common.base.Supplier;

public class VariablesEditorFormFragment implements ISectionFormFragment {
    
    @Inject
    private IEditorSite site;

    @Inject
    @Named(RobotEditorSources.SUITE_FILE_MODEL)
    private RobotSuiteFile fileModel;

    @Inject
    private RobotEditorCommandsStack commandsStack;

    @Inject
    private IDirtyProviderService dirtyProviderService;

    private HeaderFilterMatchesCollection matches;

    private VariablesDataProvider dataProvider;

    private NatTable table;

    private RowSelectionProvider<Object> selectionProvider;

    private SelectionLayerAccessor selectionLayerAccessor;

    private ISortModel sortModel;


    ISelectionProvider getSelectionProvider() {
        return selectionProvider;
    }

    SelectionLayerAccessor getSelectionLayerAccessor() {
        return selectionLayerAccessor;
    }

    @Override
    public void initialize(final Composite parent) {
        final TableTheme theme = TableThemes.getTheme(parent.getBackground().getRGB());
        
        final ConfigRegistry configRegistry = new ConfigRegistry();

        final RedNattableDataProvidersFactory dataProvidersFactory = new RedNattableDataProvidersFactory();
        final RedNattableLayersFactory factory = new RedNattableLayersFactory();

        // data providers
        dataProvider = new VariablesDataProvider(commandsStack, getSection());
        final IDataProvider columnHeaderDataProvider = dataProvidersFactory.createColumnHeaderDataProvider("Variable",
                "Value", "Comment");
        final IDataProvider rowHeaderDataProvider = dataProvidersFactory.createRowHeaderDataProvider(dataProvider);

        // body layers
        final DataLayer bodyDataLayer = factory.createDataLayer(dataProvider, 270, 270,
                new AlternatingRowConfigLabelAccumulator(), new AddingElementLabelAccumulator(dataProvider, true),
                new VariableTypesAndColumnsLabelAccumulator(dataProvider));
        final GlazedListsEventLayer<RobotVariable> bodyEventLayer = factory
                .createGlazedListEventsLayer(bodyDataLayer, dataProvider.getSortedList());
        final HoverLayer bodyHoverLayer = factory.createHoverLayer(bodyEventLayer);
        final SelectionLayer bodySelectionLayer = factory.createSelectionLayer(theme, bodyHoverLayer);
        final ViewportLayer bodyViewportLayer = factory.createViewportLayer(bodySelectionLayer);

        // column header layers
        final DataLayer columnHeaderDataLayer = factory.createColumnHeaderDataLayer(columnHeaderDataProvider);
        final ColumnHeaderLayer columnHeaderLayer = factory.createColumnHeaderLayer(columnHeaderDataLayer,
                bodySelectionLayer, bodyViewportLayer);
        final SortHeaderLayer<RobotVariable> columnHeaderSortingLayer = factory.createSortingColumnHeaderLayer(
                columnHeaderDataLayer, columnHeaderLayer, dataProvider.getPropertyAccessor(), configRegistry,
                dataProvider.getSortedList());
        
        // row header layers
        final RowHeaderLayer rowHeaderLayer = factory.createRowsHeaderLayer(bodySelectionLayer, bodyViewportLayer,
                rowHeaderDataProvider);

        // corner layer
        final ILayer cornerLayer = factory.createCornerLayer(columnHeaderDataProvider, columnHeaderSortingLayer,
                rowHeaderDataProvider, rowHeaderLayer);

        // combined grid layer
        final GridLayer gridLayer = factory.createGridLayer(bodyViewportLayer, columnHeaderSortingLayer, rowHeaderLayer,
                cornerLayer);
        gridLayer.addConfiguration(new VariablesTableAdderStatesConfiguration(dataProvider));
        gridLayer.addConfiguration(new RedTableEditConfiguration<>(fileModel, newElementsCreator(bodySelectionLayer)));
        gridLayer.addConfiguration(new VariableValuesEditConfiguration(theme, fileModel, dataProvider, commandsStack));

        table = createTable(parent, theme, gridLayer, configRegistry);

        bodyViewportLayer.registerCommandHandler(new MoveCellSelectionCommandHandler(bodySelectionLayer,
                new EditTraversalStrategy(ITraversalStrategy.TABLE_CYCLE_TRAVERSAL_STRATEGY, table),
                new EditTraversalStrategy(ITraversalStrategy.AXIS_CYCLE_TRAVERSAL_STRATEGY, table)));

        sortModel = columnHeaderSortingLayer.getSortModel();
        selectionProvider = new RowSelectionProvider<>(bodySelectionLayer, dataProvider, false);
        selectionLayerAccessor = new SelectionLayerAccessor(bodySelectionLayer);

        // tooltips support
        new NatTableContentTooltip(table);
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
        table.addConfiguration(new VariablesTableSortingConfiguration());

        // popup menus
        table.addConfiguration(new VariablesTableMenuConfiguration(site, table, selectionProvider));

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
    }

    private NewElementsCreator<RobotVariable> newElementsCreator(final SelectionLayer selectionLayer) {
        return new NewElementsCreator<RobotVariable>() {
            @Override
            public RobotVariable createNew() {
                final PositionCoordinate selectedCellPosition = selectionLayer.getLastSelectedCellPosition();
                final int selectedCellColumn = selectedCellPosition == null ? -1 : selectedCellPosition.columnPosition;
                final int selectedCellRow = selectedCellPosition == null ? -1 : selectedCellPosition.rowPosition;

                final RobotVariablesSection section = dataProvider.getInput();
                commandsStack.execute(
                        new CreateFreshVariableCommand(section, dataProvider.getAdderState().getVariableType()));

                SwtThread.asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        if (selectedCellColumn > 0 && selectedCellRow > 0) {
                            selectionLayer.selectCell(selectedCellColumn, selectedCellRow, false, false);
                        }
                        // table.doCommand(new EditSelectionCommand(table,
                        // table.getConfigRegistry()));
                    }
                });
                return section.getChildren().get(section.getChildren().size() - 1);
            }
        };
    }

    private RobotVariablesSection getSection() {
        return fileModel.findSection(RobotVariablesSection.class).orNull();
    }

    void revealVariable(final RobotVariable robotVariable) {
        selectionProvider.setSelection(new StructuredSelection(new Object[] { robotVariable }));
    }

    @Override
    public void setFocus() {
        table.setFocus();
    }
    
    private void setDirty() {
        dirtyProviderService.setDirtyState(true);
    }

    @Override
    public HeaderFilterMatchesCollection collectMatches(final String filter) {
        final VariablesMatchesCollection variablesMatches = new VariablesMatchesCollection();
        variablesMatches.collect(dataProvider.getInput(), filter);
        return variablesMatches;
    }

    @Inject
    @Optional
    private void whenUserRequestedFiltering(@UIEventTopic(RobotSuiteEditorEvents.SECTION_FILTERING_TOPIC + "/"
            + RobotVariablesSection.SECTION_NAME) final HeaderFilterMatchesCollection matches) {
        this.matches = matches;
        dataProvider.setMatches(matches);
        table.refresh();
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
    private void whenVariableDetailChanges(
            @UIEventTopic(RobotModelEvents.ROBOT_VARIABLE_DETAIL_CHANGE_ALL) final RobotVariable variable) {
        if (variable.getSuiteFile() == fileModel) {
            table.refresh();
            setDirty();
        }
    }

    @Inject
    @Optional
    private void whenVariableIsAddedOrRemoved(
            @UIEventTopic(RobotModelEvents.ROBOT_VARIABLE_STRUCTURAL_ALL) final RobotSuiteFileSection section) {
        if (section.getSuiteFile() == fileModel) {
            dataProvider.setInput(getSection());
            table.refresh();
            setDirty();
        }
    }

    @Inject
    @Optional
    private void whenVariableIsAdded(
            @UIEventTopic(RobotModelEvents.ROBOT_VARIABLE_ADDED) final RobotSuiteFileSection variablesSection) {
        if (variablesSection.getSuiteFile() == fileModel) {
            sortModel.clear();
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
        dataProvider.setInput(getSection());
        table.refresh();
    }
}

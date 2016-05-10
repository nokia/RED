/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.nattable;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.tools.services.IDirtyProviderService;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IEditableRule;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.edit.config.DefaultEditBindings;
import org.eclipse.nebula.widgets.nattable.edit.config.DefaultEditConfiguration;
import org.eclipse.nebula.widgets.nattable.edit.editor.TextCellEditor;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.grid.cell.AlternatingRowConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultRowHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.hover.HoverLayer;
import org.eclipse.nebula.widgets.nattable.hover.config.SimpleHoverStylingBindings;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.AggregateConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.painter.layer.NatGridLayerPainter;
import org.eclipse.nebula.widgets.nattable.selection.EditTraversalStrategy;
import org.eclipse.nebula.widgets.nattable.selection.ITraversalStrategy;
import org.eclipse.nebula.widgets.nattable.selection.MoveCellSelectionCommandHandler;
import org.eclipse.nebula.widgets.nattable.selection.RowSelectionProvider;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayerPainter;
import org.eclipse.nebula.widgets.nattable.selection.config.DefaultSelectionStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle.LineStyleEnum;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.ui.menu.DebugMenuConfiguration;
import org.eclipse.nebula.widgets.nattable.ui.menu.HeaderMenuConfiguration;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IEditorSite;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElementChange;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElementChange.Kind;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.HeaderFilterMatchesCollection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionFormFragment;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorSources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotSuiteEditorEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.nattable.TableThemes.TableTheme;
import org.robotframework.red.graphics.ColorsManager;
import org.robotframework.red.graphics.FontsManager;
import org.robotframework.red.nattable.configs.AddingElementConfiguration;
import org.robotframework.red.nattable.configs.AlternatingRowsConfiguration;
import org.robotframework.red.nattable.configs.ColumnHeaderConfiguration;
import org.robotframework.red.nattable.configs.GeneralTableConfiguration;
import org.robotframework.red.nattable.configs.HoveredCellConfiguration;
import org.robotframework.red.nattable.configs.RowHeaderConfiguration;
import org.robotframework.red.nattable.layer.AddingElementLabelAccumulator;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;

public class VariablesEditorFormFragment implements ISectionFormFragment {
    
    private final static int ROW_HEIGHT = 22;

    @Inject
    protected IEventBroker eventBroker;

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

    private RowSelectionProvider<RobotVariable> selectionProvider;

    ISelectionProvider getSelectionProvider() {
        return selectionProvider;
    }

    @Override
    public void initialize(final Composite parent) {
        final TableTheme theme = TableThemes.getTheme(parent.getBackground().getRGB());

        dataProvider = new VariablesDataProvider(commandsStack, getSection());
        final DataLayer dataLayer = new DataLayer(dataProvider);
        dataLayer.setColumnPercentageSizing(2, true);
        dataLayer.setColumnWidthByPosition(0, 270);
        dataLayer.setColumnWidthByPosition(1, 270);
        dataLayer.setDefaultRowHeight(ROW_HEIGHT);

        dataLayer.setConfigLabelAccumulator(
            aggregatedFrom(
                    new AlternatingRowConfigLabelAccumulator(),
                    new AddingElementLabelAccumulator(dataProvider)));

        final HoverLayer hoverLayer = new HoverLayer(dataLayer, false);
        hoverLayer.addConfiguration(new SimpleHoverStylingBindings(hoverLayer));

        final SelectionLayer selectionLayer = new SelectionLayer(hoverLayer);
        selectionLayer.setLayerPainter(new SelectionLayerPainter(theme.getGridBorderColor()));
        final ViewportLayer viewportLayer = new ViewportLayer(selectionLayer);

        final IDataProvider columnHeaderDataProvider = createColumnHeaderDataProvider();
        final ColumnHeaderLayer columnHeaderLayer = createColumnHeaderLayer(selectionLayer, viewportLayer,
                columnHeaderDataProvider);

        final IDataProvider rowHeaderDataProvider = new RowHeaderDataProvider(dataProvider);
        final RowHeaderLayer rowHeaderLayer = createRowsHeaderLayer(selectionLayer, viewportLayer,
                rowHeaderDataProvider);

        final ILayer cornerLayer = new CornerLayer(
                new DataLayer(new DefaultCornerDataProvider(columnHeaderDataProvider, rowHeaderDataProvider)),
                rowHeaderLayer, columnHeaderLayer);

        final GridLayer gridLayer = new GridLayer(viewportLayer, columnHeaderLayer, rowHeaderLayer, cornerLayer, false);

        table = new NatTable(parent,
                SWT.NO_BACKGROUND | SWT.NO_REDRAW_RESIZE | SWT.DOUBLE_BUFFERED | SWT.V_SCROLL | SWT.H_SCROLL,
                gridLayer, false);
        table.setLayerPainter(
                new NatGridLayerPainter(table, theme.getGridBorderColor(), ROW_HEIGHT));
        table.setBackground(theme.getBodyBackgroundOddRowBackground());
        table.setForeground(parent.getForeground());

        addCustomStyling(theme);

        gridLayer.addConfiguration(new DefaultEditBindings());
        gridLayer.addConfiguration(new DefaultEditConfiguration());
        gridLayer.addConfiguration(new AbstractRegistryConfiguration() {
            @Override
            public void configureRegistry(final IConfigRegistry configRegistry) {
                configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITABLE_RULE,
                        IEditableRule.ALWAYS_EDITABLE);
                configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITOR, new TextCellEditor(true, true),
                        DisplayMode.NORMAL, GridRegion.BODY);
                configRegistry.registerConfigAttribute(EditConfigAttributes.OPEN_ADJACENT_EDITOR, Boolean.TRUE,
                        DisplayMode.EDIT, GridRegion.BODY);
                configRegistry.registerConfigAttribute(EditConfigAttributes.ACTIVATE_EDITOR_ON_TRAVERSAL, Boolean.TRUE,
                        DisplayMode.EDIT, GridRegion.BODY);
            }
        });
        // Add popup menu - build your own popup menu using the PopupMenuBuilder
        table.addConfiguration(new HeaderMenuConfiguration(table));
        table.addConfiguration(new DebugMenuConfiguration(table));

        table.configure();

        GridDataFactory.fillDefaults().grab(true, true).applyTo(table);

        viewportLayer.registerCommandHandler(new MoveCellSelectionCommandHandler(selectionLayer,
                new EditTraversalStrategy(ITraversalStrategy.TABLE_CYCLE_TRAVERSAL_STRATEGY, table),
                new EditTraversalStrategy(ITraversalStrategy.AXIS_CYCLE_TRAVERSAL_STRATEGY, table)));

        selectionProvider = new RowSelectionProvider<>(selectionLayer, dataProvider);
        // createContextMenu();
    }

    private static IConfigLabelAccumulator aggregatedFrom(final IConfigLabelAccumulator... accumulators) {
        final AggregateConfigLabelAccumulator labelsAccumulator = new AggregateConfigLabelAccumulator();
        labelsAccumulator.add(accumulators);
        return labelsAccumulator;
    }

    private ColumnHeaderLayer createColumnHeaderLayer(final SelectionLayer selectionLayer,
            final ViewportLayer viewportLayer, final IDataProvider columnHeaderDataProvider) {
        final DataLayer columnHeaderDataLayer = new DataLayer(columnHeaderDataProvider);
        columnHeaderDataLayer.setDefaultRowHeight(ROW_HEIGHT);
        return new ColumnHeaderLayer(columnHeaderDataLayer, viewportLayer, selectionLayer, false);
    }

    private DefaultColumnHeaderDataProvider createColumnHeaderDataProvider() {
        final String[] propertyNames = { "var", "value", "comment" };
        final Map<String, String> propertyToLabelMap = ImmutableMap.<String, String> builder()
                .put(propertyNames[0], "Variable")
                .put(propertyNames[1], "Value")
                .put(propertyNames[2], "Comment")
                .build();
        return new DefaultColumnHeaderDataProvider(propertyNames, propertyToLabelMap);
    }

    private RowHeaderLayer createRowsHeaderLayer(final SelectionLayer selectionLayer,
            final ViewportLayer viewportLayer, final IDataProvider rowHeaderDataProvider) {
        final DataLayer rowHeaderDataLayer = new DefaultRowHeaderDataLayer(rowHeaderDataProvider);
        rowHeaderDataLayer.setColumnWidthByPosition(0, 15);
        rowHeaderDataLayer.setLayerPainter(new SelectionLayerPainter(ColorsManager.getColor(250, 250, 250)));
        return new RowHeaderLayer(rowHeaderDataLayer, viewportLayer, selectionLayer, false);
    }

    private void addCustomStyling(final TableTheme theme) {
        final GeneralTableConfiguration tableStyle = new GeneralTableConfiguration(theme,
                new SearchMatchesTextPainter(new Supplier<HeaderFilterMatchesCollection>() {
                    @Override
                    public HeaderFilterMatchesCollection get() {
                        return matches;
                    }
                }));

        final DefaultSelectionStyleConfiguration selectionStyle = new DefaultSelectionStyleConfiguration();
        selectionStyle.selectionFont = FontsManager.getFont(FontDescriptor.createFrom(table.getFont()));
        selectionStyle.selectionBgColor = theme.getBodySelectedCellBackground();
        selectionStyle.selectionFgColor = theme.getBodyForeground();
        selectionStyle.selectedHeaderFont = FontsManager.getFont(FontDescriptor.createFrom(table.getFont()));
        selectionStyle.selectedHeaderBorderStyle = new BorderStyle(0, ColorsManager.getColor(SWT.COLOR_DARK_GRAY),
                LineStyleEnum.SOLID);
        selectionStyle.selectedHeaderBgColor = theme.getHighlightedHeadersBackground();
        selectionStyle.selectedHeaderFgColor = theme.getBodyForeground();
        selectionStyle.anchorBgColor = theme.getBodyAnchoredCellBackground();
        selectionStyle.anchorFgColor = theme.getBodyForeground();
        selectionStyle.anchorBorderStyle = new BorderStyle(0, ColorsManager.getColor(SWT.COLOR_DARK_GRAY),
                LineStyleEnum.SOLID);
        selectionStyle.anchorGridBorderStyle = new BorderStyle(0, ColorsManager.getColor(SWT.COLOR_DARK_GRAY),
                LineStyleEnum.SOLID);

        table.addConfiguration(tableStyle);
        table.addConfiguration(new HoveredCellConfiguration(theme));
        table.addConfiguration(new ColumnHeaderConfiguration(theme));
        table.addConfiguration(new RowHeaderConfiguration(theme));
        table.addConfiguration(new AlternatingRowsConfiguration(theme));
        table.addConfiguration(selectionStyle);
        table.addConfiguration(new AddingElementConfiguration(theme));
    }

    private void createContextMenu() {
        final String menuId = "org.robotframework.ide.eclipse.editor.page.variables.contextMenu";

        final MenuManager manager = new MenuManager("Robot suite editor variables page context menu", menuId);
        final Menu menu = manager.createContextMenu(table);
        table.setMenu(menu);

        site.registerContextMenu(menuId, manager, selectionProvider, false);
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
            // viewer.update(variable, null);
            table.update();
            setDirty();
        }
    }

    @Inject
    @Optional
    private void whenVariableIsAddedOrRemoved(
            @UIEventTopic(RobotModelEvents.ROBOT_VARIABLE_STRUCTURAL_ALL) final RobotSuiteFileSection section) {
        if (section.getSuiteFile() == fileModel) {
            // viewer.refresh();

            setDirty();
        }
    }

    @Inject
    @Optional
    private void whenVariableIsAdded(
            @UIEventTopic(RobotModelEvents.ROBOT_VARIABLE_ADDED) final RobotSuiteFileSection variablesSection) {
        if (variablesSection.getSuiteFile() == fileModel) {
            // viewer.getTable().setSortColumn(null);
            // viewer.setComparator(null);
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

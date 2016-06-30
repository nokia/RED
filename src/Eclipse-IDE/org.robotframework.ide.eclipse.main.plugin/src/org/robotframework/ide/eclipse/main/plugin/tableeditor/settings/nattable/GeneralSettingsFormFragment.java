/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.nattable;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.tools.services.IDirtyProviderService;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Stylers;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractUiBindingConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
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
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.layer.NatGridLayerPainter;
import org.eclipse.nebula.widgets.nattable.selection.EditTraversalStrategy;
import org.eclipse.nebula.widgets.nattable.selection.ITraversalStrategy;
import org.eclipse.nebula.widgets.nattable.selection.MoveCellSelectionCommandHandler;
import org.eclipse.nebula.widgets.nattable.selection.RowSelectionProvider;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.nebula.widgets.nattable.sort.ISortModel;
import org.eclipse.nebula.widgets.nattable.sort.SortHeaderLayer;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.tooltip.NatTableContentTooltip;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuAction;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Section;
import org.rf.ide.core.testdata.model.IDocumentationHolder;
import org.rf.ide.core.testdata.model.presenter.DocumentationServiceHandler;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
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
import org.robotframework.ide.eclipse.main.plugin.tableeditor.FilterSwitchRequest;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.HeaderFilterMatchesCollection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionFormFragment;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorSources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotSuiteEditorEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.SelectionLayerAccessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableThemes;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableThemes.TableTheme;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.GeneralSettingsModel;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.SettingsMatchesCollection;
import org.robotframework.red.forms.RedFormToolkit;
import org.robotframework.red.forms.Sections;
import org.robotframework.red.graphics.ColorsManager;
import org.robotframework.red.graphics.ImagesManager;
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
import com.google.common.collect.Range;

import ca.odell.glazedlists.SortedList;

public class GeneralSettingsFormFragment implements ISectionFormFragment, ISettingsFormFragment {

    public static final String GENERAL_SETTINGS_CONTEXT_ID = "org.robotframework.ide.eclipse.tableeditor.settings.general.context";

    @Inject
    private IEventBroker eventBroker;

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

    private HeaderFilterMatchesCollection matches;

    private Section generalSettingsSection;

    private com.google.common.base.Optional<NatTable> table = com.google.common.base.Optional.absent();

    private StyledText documentation;

    private boolean hasFocusOnDocumentation;

    private boolean isDocumentationModified;

    private boolean hasEditDocRepresentation;

    private Job documenationChangeJob;

    private GeneralSettingsDataProvider dataProvider;

    private ISortModel sortModel;

    private RowSelectionProvider<Entry<String, RobotElement>> selectionProvider;

    private SelectionLayerAccessor selectionLayerAccessor;

    private final Object DOCUMENTATION_LOCK = new Object();

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
        return table.orNull();
    }

    @Override
    public void initialize(final Composite parent) {
        generalSettingsSection = toolkit.createSection(parent,
                ExpandableComposite.TWISTIE | ExpandableComposite.TITLE_BAR | Section.DESCRIPTION);
        generalSettingsSection.setExpanded(true);
        generalSettingsSection.setText("General");
        generalSettingsSection.setDescription("Provide test suite documentation and general settings");
        GridDataFactory.fillDefaults().grab(true, true).minSize(1, 22).indent(0, 5).applyTo(generalSettingsSection);
        Sections.switchGridCellGrabbingOnExpansion(generalSettingsSection);
        Sections.installMaximazingPossibility(generalSettingsSection);

        final Composite panel = createPanel(generalSettingsSection);
        createDocumentationControl(panel);
        if (!fileModel.isResourceFile()) {
            setupNatTable(panel);
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
        documentation = new StyledText(panel, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
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
                hasFocusOnDocumentation = true;
            }

            @Override
            public void focusLost(final FocusEvent e) {
                hasFocusOnDocumentation = false;
            }
        });
        toolkit.adapt(documentation, true, false);
        toolkit.paintBordersFor(documentation);
        if (fileModel.isEditable()) {
            documentation.addModifyListener(new ModifyListener() {

                @Override
                public void modifyText(final ModifyEvent e) {
                    if (!hasFocusOnDocumentation || (hasFocusOnDocumentation
                            && (documentation.getText().equals(getDocumentation(getSection(), true))
                                    || documentation.getText().equals(getDocumentation(getSection(), false))))) {
                        return;
                    }
                    setDirty();
                    isDocumentationModified = true;

                    if (documenationChangeJob != null && documenationChangeJob.getState() == Job.SLEEPING) {
                        documenationChangeJob.cancel();
                    }

                    documenationChangeJob = createDocumentationChangeJob(documentation.getText());
                    documenationChangeJob.schedule(300);
                }
            });
        }

        documentation.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(final KeyEvent e) {
                if (e.stateMask == SWT.CTRL) {
                    final int C_KEY = 0x3;
                    final int X_KEY = 0x18;

                    if (e.character == C_KEY) {
                        documentation.copy();
                    } else if (e.character == X_KEY) {
                        documentation.cut();
                    }
                }
            }
        });

        createPopupMenu();
        GridDataFactory.fillDefaults().grab(true, true).hint(SWT.DEFAULT, 30).applyTo(documentation);
    }

    protected void createPopupMenu() {
        final Menu docMenu = new Menu(documentation);
        documentation.setMenu(docMenu);

        final MenuItem modeItem = new MenuItem(docMenu, SWT.NONE);
        modeItem.setText("&View mode");
        modeItem.setImage(ImagesManager.getImage(RedImages.getResourceImage()));
        if (hasEditDocRepresentation) {
            modeItem.setText("&Edit mode");
            modeItem.setImage(ImagesManager.getImage(RedImages.getEditImage()));
            documentation.setEditable(false);

        }
        documentation.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseUp(final MouseEvent e) {
                if (!hasEditDocRepresentation && e.button == 1) {
                    hasEditDocRepresentation = true;
                    documentation.setText(getDocumentation(getSection(), true));
                    documentation.setEditable(true);
                    modeItem.setText("&View mode");
                    modeItem.setImage(ImagesManager.getImage(RedImages.getResourceImage()));
                }
            }
        });
        modeItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                if (!hasEditDocRepresentation) {
                    hasEditDocRepresentation = true;
                    modeItem.setText("&View mode");
                    modeItem.setImage(ImagesManager.getImage(RedImages.getResourceImage()));
                } else {
                    hasEditDocRepresentation = false;
                    modeItem.setImage(ImagesManager.getImage(RedImages.getEditImage()));
                    modeItem.setText("&Edit mode");
                }
                documentation.setText(getDocumentation(getSection(), hasEditDocRepresentation));
                documentation.setEditable(hasEditDocRepresentation);
            }
        });

        new MenuItem(docMenu, SWT.SEPARATOR);

        final MenuItem copyItem = new MenuItem(docMenu, SWT.NONE);
        copyItem.setText("&Copy\tCtrl+C");
        copyItem.setImage(ImagesManager
                .getImage(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_COPY)));
        copyItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                documentation.copy();
            }
        });

        final MenuItem cutItem = new MenuItem(docMenu, SWT.NONE);
        cutItem.setText("Cu&t\tCtrl+X");
        cutItem.setImage(ImagesManager
                .getImage(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_CUT)));
        cutItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                documentation.cut();
            }
        });

        final MenuItem pasteItem = new MenuItem(docMenu, SWT.NONE);
        pasteItem.setText("&Paste\tCtrl+V");
        pasteItem.setImage(ImagesManager.getImage(
                PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_PASTE)));
        pasteItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                documentation.paste();
            }
        });

    }

    private String getDocumentation(final RobotSettingsSection section, final boolean hasFocus) {
        synchronized (DOCUMENTATION_LOCK) {
            if (section != null) {
                final RobotSetting docSetting = section.getSetting("Documentation");
                if (docSetting != null && !docSetting.getArguments().isEmpty()) {
                    return hasFocus
                            ? DocumentationServiceHandler
                                    .toEditConsolidated((IDocumentationHolder) docSetting.getLinkedElement())
                            : DocumentationServiceHandler
                                    .toShowConsolidated((IDocumentationHolder) docSetting.getLinkedElement());
                }
            }
            return "";
        }
    }

    private Job createDocumentationChangeJob(final String docu) {
        return new Job("changing documentation") {

            @Override
            protected IStatus run(final IProgressMonitor monitor) {
                final String newDocumentation = docu.replaceAll("\t", "\\\\t");
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

    private void setupNatTable(final Composite parent) {

        final TableTheme theme = TableThemes.getTheme(parent.getBackground().getRGB());

        final ConfigRegistry configRegistry = new ConfigRegistry();

        final RedNattableDataProvidersFactory dataProvidersFactory = new RedNattableDataProvidersFactory();
        final RedNattableLayersFactory factory = new RedNattableLayersFactory();

        // data providers
        dataProvider = new GeneralSettingsDataProvider(commandsStack, getSection());
        final IDataProvider columnHeaderDataProvider = new GeneralSettingsColumnHeaderDataProvider();
        final IDataProvider rowHeaderDataProvider = dataProvidersFactory.createRowHeaderDataProvider(dataProvider);

        // body layers
        final DataLayer bodyDataLayer = factory.createDataLayer(dataProvider, 120, 150,
                new AlternatingRowConfigLabelAccumulator(), new EmptyGeneralSettingLabelAcumulator(dataProvider));
        final GlazedListsEventLayer<Entry<String, RobotElement>> bodyEventLayer = factory
                .createGlazedListEventsLayer(bodyDataLayer, dataProvider.getSortedList());
        final HoverLayer bodyHoverLayer = factory.createHoverLayer(bodyEventLayer);
        final SelectionLayer bodySelectionLayer = factory.createSelectionLayer(theme, bodyHoverLayer);
        final ViewportLayer bodyViewportLayer = factory.createViewportLayer(bodySelectionLayer);

        // column header layers
        final DataLayer columnHeaderDataLayer = factory.createColumnHeaderDataLayer(columnHeaderDataProvider,
                new SettingsDynamicTableColumnHeaderLabelAcumulator(dataProvider));
        final ColumnHeaderLayer columnHeaderLayer = factory.createColumnHeaderLayer(columnHeaderDataLayer,
                bodySelectionLayer, bodyViewportLayer);
        final SortHeaderLayer<Entry<String, RobotElement>> columnHeaderSortingLayer = factory
                .createSortingColumnHeaderLayer(columnHeaderDataLayer, columnHeaderLayer,
                        dataProvider.getPropertyAccessor(), configRegistry, dataProvider.getSortedList());

        // row header layers
        final RowHeaderLayer rowHeaderLayer = factory.createRowsHeaderLayer(bodySelectionLayer, bodyViewportLayer,
                rowHeaderDataProvider);

        // corner layer
        final ILayer cornerLayer = factory.createCornerLayer(columnHeaderDataProvider, columnHeaderSortingLayer,
                rowHeaderDataProvider, rowHeaderLayer);

        // combined grid layer
        final GridLayer gridLayer = factory.createGridLayer(bodyViewportLayer, columnHeaderSortingLayer, rowHeaderLayer,
                cornerLayer);
        gridLayer.addConfiguration(new RedTableEditConfiguration<>(fileModel, null,
                SettingsTableEditableRule.createEditableRule(fileModel)));

        addGeneralSettingsConfigAttributes(configRegistry);

        table = createTable(parent, theme, gridLayer, configRegistry);

        bodyViewportLayer.registerCommandHandler(new MoveCellSelectionCommandHandler(bodySelectionLayer,
                new EditTraversalStrategy(ITraversalStrategy.TABLE_CYCLE_TRAVERSAL_STRATEGY, table.get()),
                new EditTraversalStrategy(ITraversalStrategy.AXIS_CYCLE_TRAVERSAL_STRATEGY, table.get())));

        sortModel = columnHeaderSortingLayer.getSortModel();
        selectionProvider = new RowSelectionProvider<>(bodySelectionLayer, dataProvider, false);
        selectionLayerAccessor = new SelectionLayerAccessor(bodySelectionLayer);

        // tooltips support
        new GeneralSettingsTableContentTooltip(table.get());
    }

    public void addGeneralSettingsConfigAttributes(final ConfigRegistry configRegistry) {
        final Style style = new Style();
        style.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, GUIHelper.COLOR_GRAY);
        configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, style, DisplayMode.NORMAL,
                EmptyGeneralSettingLabelAcumulator.EMPTY_GENERAL_SETTING_LABEL);
    }

    private com.google.common.base.Optional<NatTable> createTable(final Composite parent, final TableTheme theme,
            final GridLayer gridLayer, final ConfigRegistry configRegistry) {
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
        table.addConfiguration(new SettingsDynamicTableSortingConfiguration());

        // popup menus
        table.addConfiguration(new GeneralSettingsTableMenuConfiguration(site, table, selectionProvider));

        table.configure();

        table.addFocusListener(new SettingsTableFocusListener(GENERAL_SETTINGS_CONTEXT_ID, site));
        GridDataFactory.fillDefaults().grab(true, true).applyTo(table);
        return com.google.common.base.Optional.of(table);
    }

    private void addCustomStyling(final NatTable table, final TableTheme theme) {
        final GeneralTableStyleConfiguration tableStyle = new GeneralTableStyleConfiguration(theme,
                new SearchMatchesTextPainter(new Supplier<HeaderFilterMatchesCollection>() {

                    @Override
                    public HeaderFilterMatchesCollection get() {
                        return matches;
                    }
                }, Stylers.Common.MATCH_STYLER));

        table.addConfiguration(tableStyle);
        table.addConfiguration(new HoveredCellStyleConfiguration(theme));
        table.addConfiguration(new ColumnHeaderStyleConfiguration(theme));
        table.addConfiguration(new RowHeaderStyleConfiguration(theme));
        table.addConfiguration(new AlternatingRowsStyleConfiguration(theme));
        table.addConfiguration(new SelectionStyleConfiguration(theme, table.getFont()));
        table.addConfiguration(new AddingElementStyleConfiguration(theme, fileModel.isEditable()));
    }

    @Override
    public void setFocus() {
        if (table.isPresent()) {
            table.get().setFocus();
        }
    }

    private void setDirty() {
        dirtyProviderService.setDirtyState(true);
    }

    private RobotSettingsSection getSection() {
        return fileModel.findSection(RobotSettingsSection.class).orNull();
    }

    public void revealSetting(final Entry<String, RobotElement> setting) {
        Sections.maximizeChosenSectionAndMinimalizeOthers(generalSettingsSection);
        if (table.isPresent()) {
            selectionProvider.setSelection(new StructuredSelection(new Object[] { setting }));
        }
        setFocus();
    }

    public void revealSetting(final RobotSetting setting) {
        Sections.maximizeChosenSectionAndMinimalizeOthers(generalSettingsSection);
        if ("Documentation".equals(setting.getName())) {
            documentation.forceFocus();
            documentation.selectAll();
            clearSettingsSelection();
        } else if (table.isPresent()) {
            final Object entry = getEntryForSetting(setting);
            if (entry != null) {
                if (dataProvider.isFilterSet() && !dataProvider.isPassingThroughFilter(setting)) {
                    final String topic = RobotSuiteEditorEvents.FORM_FILTER_SWITCH_REQUEST_TOPIC + "/"
                            + RobotSettingsSection.SECTION_NAME;
                    eventBroker.send(topic, new FilterSwitchRequest(RobotSettingsSection.SECTION_NAME, ""));
                }
                selectionProvider.setSelection(new StructuredSelection(new Object[] { entry }));
            }
            setFocus();
        }
    }

    private Object getEntryForSetting(final RobotSetting setting) {
        final SortedList<Entry<String, RobotElement>> list = dataProvider.getSortedList();
        for (final Entry<String, RobotElement> entry : list) {
            final RobotElement robotElement = entry.getValue();
            if (robotElement != null) {
                final RobotSetting entrySetting = (RobotSetting) robotElement;
                if (setting == entrySetting) {
                    return entry;
                }
            }
        }

        return null;
    }

    public void clearSettingsSelection() {
        if (selectionProvider != null) {
            selectionProvider.setSelection(StructuredSelection.EMPTY);
        }
    }

    class GeneralSettingsColumnHeaderDataProvider implements IDataProvider {

        @Override
        public Object getDataValue(final int columnIndex, final int rowIndex) {
            String columnName = "";
            if (columnIndex == 0) {
                columnName = "Setting";
            } else if (columnIndex == dataProvider.getColumnCount() - 1) {
                columnName = "Comment";
            }
            return columnName;
        }

        @Override
        public void setDataValue(final int columnIndex, final int rowIndex, final Object newValue) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getColumnCount() {
            return dataProvider.getColumnCount();
        }

        @Override
        public int getRowCount() {
            return 1;
        }

    }

    class EmptyGeneralSettingLabelAcumulator implements IConfigLabelAccumulator {

        public static final String EMPTY_GENERAL_SETTING_LABEL = "EMPTY_GENERAL_SETTING";

        private final GeneralSettingsDataProvider dataProvider;

        public EmptyGeneralSettingLabelAcumulator(final GeneralSettingsDataProvider dataProvider) {
            this.dataProvider = dataProvider;
        }

        @Override
        public void accumulateConfigLabels(final LabelStack configLabels, final int columnPosition,
                final int rowPosition) {
            final Entry<String, RobotElement> rowObject = dataProvider.getRowObject(rowPosition);
            if (rowObject != null && rowObject.getValue() == null && columnPosition == 0) {
                configLabels.addLabel(EMPTY_GENERAL_SETTING_LABEL);
            }
        }

    }

    class GeneralSettingsTableMenuConfiguration extends AbstractUiBindingConfiguration {

        private final Menu menu;

        public GeneralSettingsTableMenuConfiguration(final IEditorSite site, final NatTable table,
                final ISelectionProvider selectionProvider) {
            final String menuId = "org.robotframework.ide.eclipse.editor.page.settings.general.contextMenu";

            final MenuManager manager = new MenuManager("Robot suite editor general settings context menu", menuId);
            this.menu = manager.createContextMenu(table);
            table.setMenu(menu);

            site.registerContextMenu(menuId, manager, selectionProvider, false);
        }

        @Override
        public void configureUiBindings(final UiBindingRegistry uiBindingRegistry) {
            uiBindingRegistry.registerMouseDownBinding(new MouseEventMatcher(SWT.NONE, null, 3),
                    new PopupMenuAction(menu));
        }
    }

    private class GeneralSettingsTableContentTooltip extends NatTableContentTooltip {

        private final Map<String, String> tooltips = new HashMap<>();

        {
            tooltips.put("Suite Setup",
                    "The keyword %s is executed before executing any of the test cases or lower level suites");
            tooltips.put("Suite Teardown",
                    "The keyword %s is executed after all test cases and lower level suites have been executed");
            tooltips.put("Test Setup",
                    "The keyword %s is executed before every test cases in this suite unless test cases override it");
            tooltips.put("Test Teardown",
                    "The keyword %s is executed after every test cases in this suite unless test cases override it");
            tooltips.put("Test Template", "The keyword %s is used as default template keyword in this suite");
            tooltips.put("Test Timeout",
                    "Specifies default timeout for each test case in this suite, which can be overridden by test case settings.\n"
                            + "Numerical values are intepreted as seconds but special syntax like '1min 15s' or '2 hours' can be used.");
            tooltips.put("Force Tags", "Sets tags to all test cases in this suite. Inherited tags are not shown here.");
            tooltips.put("Default Tags",
                    "Sets tags to all tests cases in this suite, unless test case specifies own tags");
        }

        public GeneralSettingsTableContentTooltip(final NatTable natTable, final String... tooltipRegions) {
            super(natTable, tooltipRegions);
        }

        @Override
        protected String getText(final Event event) {
            String text = super.getText(event);
            final int col = this.natTable.getColumnPositionByX(event.x);
            if (col == 1 && text != null && tooltips.containsKey(text)) {
                final int row = this.natTable.getRowPositionByY(event.y);
                final ILayerCell cell = this.natTable.getCellByPosition(col + 1, row);
                final String keyword = cell != null && cell.getDataValue() != null
                        && !((String) cell.getDataValue()).isEmpty() ? (String) cell.getDataValue()
                                : "given in first argument";
                text = String.format(tooltips.get(text), keyword);
            }
            return text;
        }
    }

    private void setInput() {
        final RobotSettingsSection section = getSection();

        if (fileModel.isEditable() && section != null && hasEditDocRepresentation) {
            documentation.setEditable(true);
        } else {
            documentation.setEditable(false);
        }
        if (!hasFocusOnDocumentation) {
            documentation.setText(getDocumentation(section, hasEditDocRepresentation));
        }

        if (table.isPresent()) {
            dataProvider.setInput(section);
        }
    }

    @Override
    public HeaderFilterMatchesCollection collectMatches(final String filter) {
        final SettingsMatchesCollection settingsMatches = new SettingsMatchesCollection();
        final RobotSettingsSection settingsSection = getSection();
        final List<RobotElement> generalSettings = GeneralSettingsModel.findGeneralSettingsList(settingsSection);
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

        if (table.isPresent()) {
            dataProvider.setMatches(matches);
            table.get().refresh();
        }
    }

    private void setDocumentationMatches(final HeaderFilterMatchesCollection settingsMatches) {
        clearDocumentationMatches();
        final Collection<Range<Integer>> ranges = settingsMatches.getRanges(documentation.getText());
        for (final Range<Integer> range : ranges) {
            final Color bg = ColorsManager.getColor(255, 255, 175);
            documentation.setStyleRange(
                    new StyleRange(range.lowerEndpoint(), range.upperEndpoint() - range.lowerEndpoint(), null, bg));
        }
    }

    private void clearDocumentationMatches() {
        documentation.setStyleRange(null);
    }

    @Persist
    public void onSave() {
        isDocumentationModified = false;
        if (!table.isPresent()) {
            return;
        }
        final ICellEditor cellEditor = table.get().getActiveCellEditor();
        if (cellEditor != null && !cellEditor.isClosed()) {
            final boolean commited = cellEditor.commit(MoveDirectionEnum.NONE);
            if (!commited) {
                cellEditor.close();
            }
        }
        SwtThread.asyncExec(new Runnable() {

            @Override
            public void run() {
                setFocus();
            }
        });
    }

    protected void waitForDocumentationChangeJob() {
        // user could just typed something into documentation box, so the job was scheduled, we need
        // to wait for it to
        // end in order to proceed with saving
        if (documenationChangeJob != null && isDocumentationModified) {
            try {
                documenationChangeJob.join(3000, null);
            } catch (final InterruptedException e) {
                RedPlugin.logError("Documentation change job was interrupted", e);
            }
        }
    }

    @Inject
    @Optional
    private void whenSectionIsCreated(
            @UIEventTopic(RobotModelEvents.ROBOT_SUITE_SECTION_ADDED) final RobotSuiteFile file) {
        if (file == fileModel) {
            setInput();
            refreshTable();
            setDirty();
        }
    }

    @Inject
    @Optional
    private void whenSectionIsRemoved(
            @UIEventTopic(RobotModelEvents.ROBOT_SUITE_SECTION_REMOVED) final RobotSuiteFile file) {
        if (file == fileModel) {
            if (table.isPresent()) {
                final ICellEditor activeCellEditor = getTable().getActiveCellEditor();
                if (activeCellEditor != null && !activeCellEditor.isClosed()) {
                    activeCellEditor.close();
                }
            }

            setInput();
            documentation.setText("");
            selectionLayerAccessor.getSelectionLayer().clear();
            refreshTable();

            setDirty();
        }
    }

    @Inject
    @Optional
    private void whenSettingDetailsChanges(
            @UIEventTopic(RobotModelEvents.ROBOT_KEYWORD_CALL_DETAIL_CHANGE_ALL) final RobotSetting setting) {
        if (setting.getSuiteFile() == fileModel && setting.getGroup() == SettingsGroup.NO_GROUP) {
            if (table.isPresent()) {
                table.get().update();
            }
            refreshTable();
            setDirty();
        }
    }

    @Inject
    @Optional
    private void whenSettingIsAddedOrRemoved(
            @UIEventTopic(RobotModelEvents.ROBOT_SETTINGS_STRUCTURAL_ALL) final RobotSuiteFileSection section) {
        if (section.getSuiteFile() == fileModel) {
            if (sortModel != null) {
                sortModel.clear();
            }
            setInput();
            refreshTable();
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

    private void refreshTable() {
        if (table.isPresent()) {
            table.get().refresh();
        }
    }

    private void refreshEverything() {
        setInput();
        refreshTable();
    }
}

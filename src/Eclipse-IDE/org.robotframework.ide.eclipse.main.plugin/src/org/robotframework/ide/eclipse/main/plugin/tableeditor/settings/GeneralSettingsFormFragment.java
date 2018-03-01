/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import static com.google.common.collect.Lists.newArrayList;
import static org.robotframework.red.swt.Listeners.widgetSelectedAdapter;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

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
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Stylers;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
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
import org.eclipse.nebula.widgets.nattable.selection.EditTraversalStrategy;
import org.eclipse.nebula.widgets.nattable.selection.ITraversalStrategy;
import org.eclipse.nebula.widgets.nattable.selection.MoveCellSelectionCommandHandler;
import org.eclipse.nebula.widgets.nattable.selection.RowSelectionProvider;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.sort.ISortModel;
import org.eclipse.nebula.widgets.nattable.sort.SortHeaderLayer;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.Style;
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
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Section;
import org.rf.ide.core.testdata.model.IDocumentationHolder;
import org.rf.ide.core.testdata.model.presenter.DocumentationServiceHandler;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences.CellWrappingStrategy;
import org.robotframework.ide.eclipse.main.plugin.hyperlink.TableHyperlinksSupport;
import org.robotframework.ide.eclipse.main.plugin.hyperlink.detectors.ITableHyperlinksDetector;
import org.robotframework.ide.eclipse.main.plugin.hyperlink.detectors.TableHyperlinksToKeywordsDetector;
import org.robotframework.ide.eclipse.main.plugin.hyperlink.detectors.TableHyperlinksToVariablesDetector;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElementChange;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElementChange.Kind;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting.SettingsGroup;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.settings.CreateFreshSettingCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.settings.DeleteSettingCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.settings.SetDocumentationCommand;
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
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.nattable.AssistanceLabelAccumulator;
import org.robotframework.red.nattable.RedColumnHeaderDataProvider;
import org.robotframework.red.nattable.RedNattableDataProvidersFactory;
import org.robotframework.red.nattable.RedNattableLayersFactory;
import org.robotframework.red.nattable.TableCellsStrings;
import org.robotframework.red.nattable.configs.AddingElementStyleConfiguration;
import org.robotframework.red.nattable.configs.AlternatingRowsStyleConfiguration;
import org.robotframework.red.nattable.configs.ColumnHeaderStyleConfiguration;
import org.robotframework.red.nattable.configs.GeneralTableStyleConfiguration;
import org.robotframework.red.nattable.configs.HeaderSortConfiguration;
import org.robotframework.red.nattable.configs.HoveredCellStyleConfiguration;
import org.robotframework.red.nattable.configs.RedTableEditConfiguration;
import org.robotframework.red.nattable.configs.RedTableResizableRowsBindingsConfiguration;
import org.robotframework.red.nattable.configs.RowHeaderStyleConfiguration;
import org.robotframework.red.nattable.configs.SelectionStyleConfiguration;
import org.robotframework.red.nattable.configs.TableMatchesSupplierRegistryConfiguration;
import org.robotframework.red.nattable.configs.TableMenuConfiguration;
import org.robotframework.red.nattable.configs.TableStringsPositionsRegistryConfiguration;
import org.robotframework.red.nattable.edit.CellEditorCloser;
import org.robotframework.red.nattable.painter.RedNatGridLayerPainter;
import org.robotframework.red.nattable.painter.RedTableTextPainter;
import org.robotframework.services.event.Events;

import com.google.common.base.Predicates;
import com.google.common.collect.Range;
import com.google.common.collect.TreeRangeSet;

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
    private SuiteFileMarkersContainer markersContainer;

    @Inject
    private RobotEditorCommandsStack commandsStack;

    @Inject
    private IDirtyProviderService dirtyProviderService;

    @Inject
    private RedFormToolkit toolkit;

    private HeaderFilterMatchesCollection matches;

    private Section generalSettingsSection;

    private java.util.Optional<NatTable> table = java.util.Optional.empty();

    private StyledText documentation;

    private final AtomicBoolean hasFocusOnDocumentation = new AtomicBoolean(false);

    private final AtomicBoolean isDocumentationModified = new AtomicBoolean(false);

    private final AtomicBoolean hasEditDocRepresentation = new AtomicBoolean(false);

    private Job documentationChangeJob = null;

    private GeneralSettingsDataProvider dataProvider;

    private final Semaphore docSemaphore = new Semaphore(1, true);

    private ISortModel sortModel;

    private RowSelectionProvider<Entry<String, RobotElement>> selectionProvider;

    private SelectionLayerAccessor selectionLayerAccessor;

    private final Object DOCUMENTATION_LOCK = new Object();

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
        return table.orElse(null);
    }

    @Override
    public void initialize(final Composite parent) {
        generalSettingsSection = toolkit.createSection(parent,
                ExpandableComposite.TWISTIE | ExpandableComposite.TITLE_BAR | Section.DESCRIPTION);
        generalSettingsSection.setExpanded(true);
        generalSettingsSection.setText("General");
        generalSettingsSection.setDescription("Provide test suite documentation and general settings");
        generalSettingsSection.setBackground(parent.getBackground());
        GridDataFactory.fillDefaults().grab(true, true).minSize(1, 22).indent(0, 5).applyTo(generalSettingsSection);
        Sections.switchGridCellGrabbingOnExpansion(generalSettingsSection);
        Sections.installMaximazingPossibility(generalSettingsSection);

        final TableTheme theme = TableThemes.getTheme(parent.getBackground().getRGB());
        final Composite panel = createPanel(generalSettingsSection);
        createDocumentationControl(panel, theme);
        if (!fileModel.isResourceFile()) {
            setupNatTable(panel, theme);
        }
        setInput();
    }

    private Composite createPanel(final Section section) {
        final Composite panel = toolkit.createComposite(section);
        panel.setBackground(section.getBackground());
        GridDataFactory.fillDefaults().grab(true, true).indent(0, 0).applyTo(panel);
        GridLayoutFactory.fillDefaults().applyTo(panel);
        section.setClient(panel);
        return panel;
    }

    private void createDocumentationControl(final Composite panel, final TableTheme theme) {
        documentation = new StyledText(panel, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        documentation.setFont(theme.getFont());
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
                hasFocusOnDocumentation.set(true);
            }

            @Override
            public void focusLost(final FocusEvent e) {
                hasFocusOnDocumentation.set(false);
            }
        });
        toolkit.adapt(documentation, true, false);
        toolkit.paintBordersFor(documentation);
        if (fileModel.isEditable()) {
            documentation.addModifyListener(new ModifyListener() {

                @Override
                public void modifyText(final ModifyEvent e) {
                    if (!hasFocusOnDocumentation.get() || (hasFocusOnDocumentation.get()
                            && (documentation.getText().equals(getDocumentation(getSection(), true))
                                    || documentation.getText().equals(getDocumentation(getSection(), false))))) {
                        return;
                    }
                    isDocumentationModified.set(true);
                    setDirty();
                    if (documentationChangeJob != null && documentationChangeJob.getState() == Job.SLEEPING) {
                        documentationChangeJob.cancel();
                    }
                    documentationChangeJob = createDocumentationChangeJob(documentation.getText());
                    documentationChangeJob.schedule(300);
                }
            });
        }

        documentation.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(final KeyEvent e) {
                if (e.stateMask == SWT.CTRL) {
                    final int C_KEY = 0x3;
                    final int X_KEY = 0x18;
                    final int Z_KEY = 0x1A;
                    final int Y_KEY = 0x19;

                    if (e.character == C_KEY) {
                        documentation.copy();
                    } else if (e.character == X_KEY) {
                        documentation.cut();
                    } else if (e.character == Z_KEY) {
                        updateDocumentationWithPositionPresave(
                                getDocumentation(getSection(), hasEditDocRepresentation.get()));
                    } else if (e.character == Y_KEY) {
                        updateDocumentationWithPositionPresave(
                                getDocumentation(getSection(), hasEditDocRepresentation.get()));
                    }
                } else if (e.stateMask == SWT.NONE && e.character == SWT.TAB) {
                    updateDocumentationWithPositionPresave(documentation.getText().replaceAll("\\t", "\\\\t"));
                } else if (e.character == '#') {
                    updateDocumentationWithPositionPresave(escapeNotEscapedHashSigns(documentation.getText()));
                } else if (e.character == SWT.SPACE) {
                    updateDocumentationWithPositionPresave(documentation.getText().replaceAll("  ", " \\\\ "));
                }
            }
        });

        createPopupMenu();
        GridDataFactory.fillDefaults().grab(true, true).hint(SWT.DEFAULT, 30).applyTo(documentation);
    }

    private void updateDocumentationWithPositionPresave(final String newText) {
        if (documentation.isEnabled()) {
            final int caretOffset = documentation.getCaretOffset();
            final int lengthBefore = documentation.getCharCount();
            documentation.setText(newText);
            final int lengthAfter = documentation.getCharCount();
            documentation.setCaretOffset(caretOffset + (lengthAfter - lengthBefore));
        }
    }

    private String escapeNotEscapedHashSigns(final String text) {
        if (text != null) {
            final StringBuilder str = new StringBuilder("");
            boolean wasLastEscape = false;
            final char[] charArray = text.toCharArray();
            for (final char c : charArray) {
                if (c == '\\') {
                    wasLastEscape = true;
                } else if (c == '#') {
                    if (!wasLastEscape) {
                        str.append('\\');
                    }

                    wasLastEscape = false;
                } else {
                    wasLastEscape = false;
                }

                str.append(c);
            }

            return str.toString();
        }

        return text;
    }

    protected void createPopupMenu() {
        final Menu docMenu = new Menu(documentation);
        documentation.setMenu(docMenu);

        final MenuItem modeItem = new MenuItem(docMenu, SWT.NONE);
        modeItem.setText("&View mode");
        modeItem.setImage(ImagesManager.getImage(RedImages.getResourceImage()));
        if (hasEditDocRepresentation.get()) {
            modeItem.setText("&Edit mode");
            modeItem.setImage(ImagesManager.getImage(RedImages.getEditImage()));
            documentation.setEditable(false);

        }
        documentation.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseUp(final MouseEvent e) {
                if (!hasEditDocRepresentation.get() && e.button == 1) {
                    hasEditDocRepresentation.set(true);
                    final RobotSettingsSection section = getSection();
                    documentation.setText(getDocumentation(section, true));
                    if (section != null && section.getLinkedElement().isPresent()) {
                        documentation.setEditable(true);
                    }
                    documentation.setCaretOffset(
                            StyledTextCaretPositionProvider.getOffset(documentation, new Point(e.x, e.y)));
                    modeItem.setText("&View mode");
                    modeItem.setImage(ImagesManager.getImage(RedImages.getResourceImage()));
                }
            }
        });
        modeItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                if (!hasEditDocRepresentation.get()) {
                    hasEditDocRepresentation.set(true);
                    modeItem.setText("&View mode");
                    modeItem.setImage(ImagesManager.getImage(RedImages.getResourceImage()));
                } else {
                    hasEditDocRepresentation.set(false);
                    modeItem.setImage(ImagesManager.getImage(RedImages.getEditImage()));
                    modeItem.setText("&Edit mode");
                }
                final RobotSettingsSection section = getSection();
                documentation.setText(getDocumentation(section, hasEditDocRepresentation.get()));
                if (section != null && section.getLinkedElement().isPresent()) {
                    documentation.setEditable(hasEditDocRepresentation.get());
                }
            }
        });

        new MenuItem(docMenu, SWT.SEPARATOR);

        final MenuItem copyItem = new MenuItem(docMenu, SWT.NONE);
        copyItem.setText("&Copy\tCtrl+C");
        copyItem.setImage(ImagesManager.getImage(RedImages.getCopyImage()));
        copyItem.addSelectionListener(widgetSelectedAdapter(e -> documentation.copy()));

        final MenuItem cutItem = new MenuItem(docMenu, SWT.NONE);
        cutItem.setText("Cu&t\tCtrl+X");
        cutItem.setImage(ImagesManager.getImage(RedImages.getCutImage()));
        cutItem.addSelectionListener(widgetSelectedAdapter(e -> documentation.cut()));

        final MenuItem pasteItem = new MenuItem(docMenu, SWT.NONE);
        pasteItem.setText("&Paste\tCtrl+V");
        pasteItem.setImage(ImagesManager.getImage(RedImages.getPasteImage()));
        pasteItem.addSelectionListener(widgetSelectedAdapter(e -> documentation.paste()));
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

    private Job createDocumentationChangeJob(final String documentation) {
        return new Job("changing documentation") {

            @Override
            protected IStatus run(final IProgressMonitor monitor) {
                try {
                    docSemaphore.acquire();
                    final String newDocumentation = documentation.replaceAll("\t", "\\\\t");
                    final RobotSettingsSection settingsSection = getSection();
                    if (settingsSection == null) {
                        return Status.OK_STATUS;
                    }

                    final RobotSetting docSetting = settingsSection.getSetting("Documentation");

                    if (docSetting == null && !newDocumentation.isEmpty()) {
                        commandsStack.execute(new CreateFreshSettingCommand(settingsSection, "Documentation",
                                newArrayList(newDocumentation)));
                    } else if (docSetting != null && newDocumentation.isEmpty()) {
                        commandsStack.execute(new DeleteSettingCommand(newArrayList(docSetting)));
                    } else if (docSetting != null) {
                        commandsStack.execute(new SetDocumentationCommand(docSetting, newDocumentation));
                    }
                } catch (final InterruptedException e) {
                    RedPlugin.logError("Error during change job perform", e);
                } finally {
                    docSemaphore.release();
                }

                return Status.OK_STATUS;
            }
        };
    }

    private void setupNatTable(final Composite parent, final TableTheme theme) {
        final ConfigRegistry configRegistry = new ConfigRegistry();

        final RedNattableDataProvidersFactory dataProvidersFactory = new RedNattableDataProvidersFactory();
        final RedNattableLayersFactory factory = new RedNattableLayersFactory();

        // data providers
        dataProvider = new GeneralSettingsDataProvider(commandsStack, getSection());
        final IDataProvider columnHeaderDataProvider = new GeneralSettingsColumnHeaderDataProvider();
        final IDataProvider rowHeaderDataProvider = dataProvidersFactory.createRowHeaderDataProvider(dataProvider);

        // body layers
        final DataLayer bodyDataLayer = factory.createDataLayer(dataProvider,
                new AssistanceLabelAccumulator(dataProvider,
                        position -> position.getColumnPosition() < dataProvider.getColumnCount() - 1,
                        Predicates.alwaysTrue()),
                new AlternatingRowConfigLabelAccumulator(),
                new EmptyGeneralSettingLabelAccumulator(dataProvider));
        final GlazedListsEventLayer<Entry<String, RobotElement>> bodyEventLayer = factory
                .createGlazedListEventsLayer(bodyDataLayer, dataProvider.getSortedList());
        final HoverLayer bodyHoverLayer = factory.createHoverLayer(bodyEventLayer);
        final SelectionLayer bodySelectionLayer = factory.createSelectionLayer(theme, bodyHoverLayer);
        final ViewportLayer bodyViewportLayer = factory.createViewportLayer(bodySelectionLayer);

        // column header layers
        final DataLayer columnHeaderDataLayer = factory.createColumnHeaderDataLayer(columnHeaderDataProvider, theme,
                new SettingsDynamicTableColumnHeaderLabelAccumulator(dataProvider));
        final ColumnHeaderLayer columnHeaderLayer = factory.createColumnHeaderLayer(columnHeaderDataLayer,
                bodySelectionLayer, bodyViewportLayer);
        final SortHeaderLayer<Entry<String, RobotElement>> columnHeaderSortingLayer = factory
                .createSortingColumnHeaderLayer(columnHeaderDataLayer, columnHeaderLayer,
                        dataProvider.getPropertyAccessor(), configRegistry, dataProvider.getSortedList());

        // row header layers
        final RowHeaderLayer rowHeaderLayer = factory.createRowsHeaderLayer(bodySelectionLayer, bodyViewportLayer,
                rowHeaderDataProvider, new MarkersSelectionLayerPainter(theme.getHeadersGridColor()),
                new GeneralSettingsMarkersLabelAccumulator(markersContainer, dataProvider));

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
        gridLayer.addConfiguration(new RedTableEditConfiguration<>(null,
                GeneralSettingsTableEditableRule.createEditableRule(fileModel), wrapCellContent));
        gridLayer.addConfiguration(new GeneralSettingsEditConfiguration(fileModel, dataProvider, wrapCellContent));

        table = java.util.Optional.of(theme.configureScrollBars(parent, bodyViewportLayer,
                tableParent -> createTable(tableParent, theme, factory, gridLayer, bodyDataLayer, configRegistry)));

        bodyViewportLayer.registerCommandHandler(new MoveCellSelectionCommandHandler(bodySelectionLayer,
                new EditTraversalStrategy(ITraversalStrategy.TABLE_CYCLE_TRAVERSAL_STRATEGY, table.get()),
                new EditTraversalStrategy(ITraversalStrategy.AXIS_CYCLE_TRAVERSAL_STRATEGY, table.get())));

        sortModel = columnHeaderSortingLayer.getSortModel();
        selectionProvider = new RowSelectionProvider<>(bodySelectionLayer, dataProvider, false);
        selectionLayerAccessor = new SelectionLayerAccessor(dataProvider, bodySelectionLayer, selectionProvider);

        // tooltips support
        new GeneralSettingsTableContentTooltip(table.get(), markersContainer, dataProvider);
    }

    private NatTable createTable(final Composite parent, final TableTheme theme,
            final RedNattableLayersFactory factory, final GridLayer gridLayer, final DataLayer dataLayer,
            final ConfigRegistry configRegistry) {
        final int style = SWT.NO_BACKGROUND | SWT.NO_REDRAW_RESIZE | SWT.DOUBLE_BUFFERED | SWT.V_SCROLL | SWT.H_SCROLL;
        final NatTable table = new NatTable(parent, style, gridLayer, false);
        table.setFont(theme.getFont());
        table.setConfigRegistry(configRegistry);
        table.setLayerPainter(
                new RedNatGridLayerPainter(table, theme.getBodyGridColor(), theme.getHeadersBackground(),
                        theme.getHeadersUnderlineColor(), RedNattableLayersFactory.GRID_BORDER_WIDTH,
                        RedNattableLayersFactory.ROW_HEIGHT));
        table.setBackground(theme.getBodyOddRowBackground());
        table.setForeground(parent.getForeground());

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
                new TableHyperlinksToKeywordsDetector(dataProvider));

        // sorting
        table.addConfiguration(new HeaderSortConfiguration());
        table.addConfiguration(new SettingsDynamicTableSortingConfiguration());

        // popup menus
        table.addConfiguration(new GeneralSettingsTableMenuConfiguration(site, table, selectionProvider));

        table.configure();

        table.addFocusListener(new SettingsTableFocusListener(GENERAL_SETTINGS_CONTEXT_ID, site));
        GridDataFactory.fillDefaults().grab(true, true).applyTo(table);
        return table;
    }

    private void addCustomStyling(final NatTable table, final TableTheme theme) {
        table.addConfiguration(new GeneralTableStyleConfiguration(theme, new RedTableTextPainter(hasWrappedCells())));
        table.addConfiguration(new HoveredCellStyleConfiguration(theme));
        table.addConfiguration(new ColumnHeaderStyleConfiguration(theme));
        table.addConfiguration(new RowHeaderStyleConfiguration(theme));
        table.addConfiguration(new AlternatingRowsStyleConfiguration(theme));
        table.addConfiguration(new SelectionStyleConfiguration(theme, table.getFont()));
        table.addConfiguration(inactiveSettingsStyle(theme));
        table.addConfiguration(new AddingElementStyleConfiguration(theme, fileModel.isEditable()));
    }

    private boolean hasWrappedCells() {
        final RedPreferences preferences = RedPlugin.getDefault().getPreferences();
        return preferences.getCellWrappingStrategy() == CellWrappingStrategy.WRAP;
    }

    private DefaultNatTableStyleConfiguration inactiveSettingsStyle(final TableTheme theme) {
        return new DefaultNatTableStyleConfiguration() {

            @Override
            public void configureRegistry(final IConfigRegistry configRegistry) {
                final Style style = new Style();
                style.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, theme.getBodyInactiveCellForeground());
                configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, style, DisplayMode.NORMAL,
                        EmptyGeneralSettingLabelAccumulator.EMPTY_GENERAL_SETTING_LABEL);
            }
        };
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
        return fileModel.findSection(RobotSettingsSection.class).orElse(null);
    }

    public void revealSetting(final RobotSetting setting, final boolean focus) {
        Sections.maximizeChosenSectionAndMinimalizeOthers(generalSettingsSection);
        if ("Documentation".equals(setting.getName())) {
            if (focus) {
                documentation.forceFocus();
            }
            documentation.selectAll();
            clearSettingsSelection();
        } else if (table.isPresent()) {
            final Entry<String, RobotElement> entry = dataProvider.getEntryForSetting(setting);
            if (dataProvider.isFilterSet() && !dataProvider.isProvided(entry)) {
                final String topic = RobotSuiteEditorEvents.FORM_FILTER_SWITCH_REQUEST_TOPIC + "/"
                        + RobotSettingsSection.SECTION_NAME;
                eventBroker.send(topic, new FilterSwitchRequest(RobotSettingsSection.SECTION_NAME, ""));
            }
            CellEditorCloser.closeForcibly(table.get());
            if (focus) {
                table.get().setFocus();
            }
            selectionProvider.setSelection(new StructuredSelection(new Object[] { entry }));
        }
    }

    public void clearSettingsSelection() {
        if (selectionProvider != null) {
            selectionProvider.setSelection(StructuredSelection.EMPTY);
        }
    }

    private void setInput() {
        final RobotSettingsSection section = getSection();

        if (fileModel.isEditable() && section != null && hasEditDocRepresentation.get()) {
            documentation.setEditable(true);
        } else {
            documentation.setEditable(false);
        }
        if (!hasFocusOnDocumentation.get()) {
            documentation.setText(getDocumentation(section, hasEditDocRepresentation.get()));
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
    private void whenUserRequestedFilteringEnabled(@UIEventTopic(RobotSuiteEditorEvents.SECTION_FILTERING_ENABLED_TOPIC
            + "/" + RobotSettingsSection.SECTION_NAME) final HeaderFilterMatchesCollection matches) {
        if (matches.getCollectors().contains(this)) {
            this.matches = matches;
            setDocumentationMatches(matches);

            if (table.isPresent()) {
                dataProvider.setFilter(new SettingsMatchesFilter(matches));
                table.get().refresh();
            }
        }
    }

    @Inject
    @Optional
    private void whenUserRequestedFilteringDisabled(
            @UIEventTopic(RobotSuiteEditorEvents.SECTION_FILTERING_DISABLED_TOPIC + "/"
                    + RobotSettingsSection.SECTION_NAME) final Collection<HeaderFilterMatchesCollector> collectors) {
        if (collectors.contains(this)) {
            this.matches = null;
            clearDocumentationMatches();

            if (table.isPresent()) {
                dataProvider.setFilter(null);
                table.get().refresh();
            }
        }
    }

    private void setDocumentationMatches(final HeaderFilterMatchesCollection settingsMatches) {
        clearDocumentationMatches();
        final TreeRangeSet<Integer> ranges = settingsMatches.getRanges(documentation.getText());
        for (final Range<Integer> range : ranges.asRanges()) {
            final StyleRange styleRange = new StyleRange(range.lowerEndpoint(), range.upperEndpoint() - range.lowerEndpoint(), null, null);
            Stylers.Common.MATCH_STYLER.applyStyles(styleRange);
            documentation.setStyleRange(styleRange);
        }
    }

    private void clearDocumentationMatches() {
        documentation.setStyleRange(null);
    }

    @Override
    public void invokeSaveAction() {
        onSave();
    }

    public void aboutToChangeToOtherPage() {
        if (table.isPresent()) {
            CellEditorCloser.closeForcibly(table.get());
        }
    }

    @Persist
    public void onSave() {
        isDocumentationModified.set(false);
        if (table.isPresent()) {
            CellEditorCloser.closeForcibly(table.get());
        }
    }

    protected void waitForDocumentationChangeJob() {
        // user could just typed something into documentation box, so the job was
        // scheduled, we need to wait for it to end in order to proceed with saving
        try {
            if (isDocumentationModified.get() || (!documentation.getText().equals(getDocumentation(getSection(), true))
                    && !documentation.getText().equals(getDocumentation(getSection(), false)))) {
                if (documentationChangeJob != null) {
                    documentationChangeJob.cancel();
                }
                if (!hasFocusOnDocumentation.get() || (hasFocusOnDocumentation.get()
                        && (documentation.getText().equals(getDocumentation(getSection(), true))
                                || documentation.getText().equals(getDocumentation(getSection(), false))))) {
                    return;
                }
                documentationChangeJob = createDocumentationChangeJob(documentation.getText());
                documentationChangeJob.schedule();
                while (documentationChangeJob.getState() != Job.RUNNING) {
                }
                documentationChangeJob.join();
                documentationChangeJob = null;
            }
        } catch (final InterruptedException e) {
            RedPlugin.logError("Documentation change job was interrupted", e);
        }
    }

    @Override
    public List<ITableHyperlinksDetector> getDetectors() {
        return detector.getDetectors();
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
            if (table.isPresent()) {
                selectionLayerAccessor.clear();
            }
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
    private void whenKeywordCallIsConverted(
            @UIEventTopic(RobotModelEvents.ROBOT_KEYWORD_CALL_CONVERTED) final org.osgi.service.event.Event event) {

        final RobotSettingsSection settingsSection = Events.get(event, IEventBroker.DATA, RobotSettingsSection.class);
        final RobotSetting setting = Events.get(event, RobotModelEvents.ADDITIONAL_DATA, RobotSetting.class);

        if (settingsSection != null && settingsSection.getSuiteFile() == fileModel
                && RobotSetting.SettingsGroup.NO_GROUP.equals(setting.getGroup())) {
            sortModel.clear();
            selectionLayerAccessor.preserveSelectionWhen(() -> {
                refreshTable();
                setDirty();
            });
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
                    ? (RobotSuiteFile) change.getElement()
                    : null;
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
            commandsStack.clear();
            refreshEverything();
        }
    }

    @Inject
    @Optional
    private void whenMarkersContainerWasReloaded(
            @UIEventTopic(RobotModelEvents.MARKERS_CACHE_RELOADED) final RobotSuiteFile fileModel) {
        if (fileModel == this.fileModel) {
            refreshTable();
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

    private static java.util.Optional<RobotFileInternalElement> toOptionalModelElement(
            final Object rowObject) {
        if (rowObject instanceof Entry<?, ?>) {
            final Entry<?, ?> entry = (Entry<?, ?>) rowObject;
            return entry.getValue() instanceof RobotFileInternalElement
                    ? java.util.Optional.of((RobotFileInternalElement) entry.getValue())
                    : java.util.Optional.<RobotFileInternalElement>empty();
        }
        return java.util.Optional.<RobotFileInternalElement>empty();
    }

    private class GeneralSettingsColumnHeaderDataProvider extends RedColumnHeaderDataProvider {

        public GeneralSettingsColumnHeaderDataProvider() {
            super(dataProvider);
        }

        @Override
        public Object getDataValue(final int columnIndex, final int rowIndex) {
            if (columnIndex == 0) {
                return "Setting";
            } else if (isLastColumn(columnIndex)) {
                return "Comment";
            }
            return "";
        }
    }

    private static class EmptyGeneralSettingLabelAccumulator implements IConfigLabelAccumulator {

        public static final String EMPTY_GENERAL_SETTING_LABEL = "EMPTY_GENERAL_SETTING";

        private final GeneralSettingsDataProvider dataProvider;

        public EmptyGeneralSettingLabelAccumulator(final GeneralSettingsDataProvider dataProvider) {
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

    private static class GeneralSettingsTableMenuConfiguration extends TableMenuConfiguration {

        public GeneralSettingsTableMenuConfiguration(final IEditorSite site, final NatTable table,
                final ISelectionProvider selectionProvider) {
            super(site, table, selectionProvider,
                    "org.robotframework.ide.eclipse.editor.page.settings.general.contextMenu",
                    "Robot suite editor general settings context menu");
        }
    }

    private static class GeneralSettingsMarkersLabelAccumulator extends MarkersLabelAccumulator {

        public GeneralSettingsMarkersLabelAccumulator(final SuiteFileMarkersContainer markersContainer,
                final IRowDataProvider<?> dataProvider) {
            super(markersContainer, dataProvider);
        }

        @Override
        protected java.util.Optional<RobotFileInternalElement> getRowModelObject(final int rowPosition) {
            return toOptionalModelElement(dataProvider.getRowObject(rowPosition));
        }
    }

    private static class GeneralSettingsTableContentTooltip extends RedNatTableContentTooltip {

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
                            + "Numerical values are interpreted as seconds but special syntax like '1min 15s' or '2 hours' can be used.");
            tooltips.put("Force Tags", "Sets tags to all test cases in this suite. Inherited tags are not shown here.");
            tooltips.put("Default Tags",
                    "Sets tags to all tests cases in this suite, unless test case specifies own tags");
        }

        public GeneralSettingsTableContentTooltip(final NatTable natTable,
                final SuiteFileMarkersContainer markersContainer, final IRowDataProvider<?> dataProvider) {
            super(natTable, markersContainer, dataProvider);
        }

        @Override
        protected String getText(final Event event) {
            String text = super.getText(event);
            final int col = natTable.getColumnPositionByX(event.x);
            if (col == 1 && text != null && tooltips.containsKey(text)) {
                final int row = natTable.getRowPositionByY(event.y);
                final ILayerCell cell = natTable.getCellByPosition(col + 1, row);
                final String keyword = cell != null && cell.getDataValue() != null
                        && !((String) cell.getDataValue()).isEmpty() ? (String) cell.getDataValue()
                                : "given in first argument";
                text = String.format(tooltips.get(text), keyword);
            }
            return text;
        }

        @Override
        protected java.util.Optional<RobotFileInternalElement> getRowModelObject(final int rowPosition) {
            return toOptionalModelElement(dataProvider.getRowObject(rowPosition));
        }
    }
}

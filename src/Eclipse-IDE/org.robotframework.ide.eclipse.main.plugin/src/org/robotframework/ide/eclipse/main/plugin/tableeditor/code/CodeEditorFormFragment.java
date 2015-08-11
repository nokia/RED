package org.robotframework.ide.eclipse.main.plugin.tableeditor.code;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.tools.services.IDirtyProviderService;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.RowExposingTreeViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerEditor;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerColumnsFactory;
import org.eclipse.jface.viewers.ViewersConfigurator;
import org.eclipse.jface.viewers.ViewersConfigurator.MenuProvider;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Section;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElementChange;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElementChange.Kind;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.CellsAcivationStrategy;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.CellsAcivationStrategy.RowTabbingStrategy;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.FocusedViewerAccessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.FocusedViewerAccessor.ViewerColumnsManagingStrategy;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionFormFragment;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorSources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotElementEditingSupport.NewElementsCreator;
import org.robotframework.red.forms.RedFormToolkit;
import org.robotframework.red.viewers.Selections;

public abstract class CodeEditorFormFragment implements ISectionFormFragment {

    @Inject
    private IEditorSite site;

    @Inject
    private IDirtyProviderService dirtyProviderService;

    @Inject
    @Named(RobotEditorSources.SUITE_FILE_MODEL)
    protected RobotSuiteFile fileModel;

    @Inject
    protected RobotEditorCommandsStack commandsStack;

    @Inject
    protected RedFormToolkit toolkit;

    protected RowExposingTreeViewer viewer;

    private Menu viewerMenu;
    private Menu headerMenu;

    private Section section;

    private boolean isSaving = false;

    public TreeViewer getViewer() {
        return viewer;
    }

    @Override
    public void initialize(final Composite parent) {
        createViewer(parent);
        createDetailsPanel(parent);
        createViewerContextMenu();
        createHeaderContextMenu();

        createColumns();

        viewer.setInput(getSection());
        viewer.refresh();
        viewer.expandAll();

        parent.layout();
    }

    private void createViewer(final Composite parent) {
        viewer = new RowExposingTreeViewer(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
        CellsAcivationStrategy.addActivationStrategy(viewer, RowTabbingStrategy.MOVE_TO_NEXT);
        ColumnViewerToolTipSupport.enableFor(viewer, ToolTip.NO_RECREATE);

        GridDataFactory.fillDefaults().grab(true, true).applyTo(viewer.getTree());
        viewer.setUseHashlookup(true);
        viewer.getTree().setLinesVisible(true);
        viewer.getTree().setHeaderVisible(true);
        viewer.getTree().addListener(SWT.MeasureItem, new Listener() {
            @Override
            public void handleEvent(final Event event) {
                event.height = Double.valueOf(event.gc.getFontMetrics().getHeight() * 1.5).intValue();
            }
        });
        viewer.setContentProvider(createContentProvider());
        viewer.addSelectionChangedListener(createSelectionChangeListener());

        ViewersConfigurator.enableDeselectionPossibility(viewer);
        ViewersConfigurator.enableContextMenuOnHeader(viewer, 
            new MenuProvider() {
                @Override
                public Menu provide() {
                    return viewerMenu;
                }
            }, 
            new MenuProvider() {
                @Override
                public Menu provide() {
                    return headerMenu;
                }
            });
    }

    private ISelectionChangedListener createSelectionChangeListener() {
        final ISelectionChangedListener selectionChangeListener = new ISelectionChangedListener() {
            @Override
            public void selectionChanged(final SelectionChangedEvent event) {
                final RobotElement selectedElement = Selections.getOptionalFirstElement(
                        (IStructuredSelection) event.getSelection(), RobotElement.class).orNull();
                whenElementSelectionChanged(selectedElement);
            }
        };
        viewer.getTree().addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(final DisposeEvent e) {
                viewer.removeSelectionChangedListener(selectionChangeListener);
            }
        });
        return selectionChangeListener;
    }

    protected abstract void whenElementSelectionChanged(RobotElement selectedElement);

    protected abstract ITreeContentProvider createContentProvider();

    private void createDetailsPanel(final Composite parent) {
        section = toolkit.createSection(parent, ExpandableComposite.TWISTIE | ExpandableComposite.TITLE_BAR);
        section.setExpanded(false);
        section.setText("Settings");
        GridDataFactory.fillDefaults().grab(true, false).minSize(1, 22).applyTo(section);

        final Composite composite = toolkit.createComposite(section);
        GridLayoutFactory.fillDefaults().applyTo(composite);
        section.setClient(composite);

        createSettingsTable(composite);
    }

    protected abstract void createSettingsTable(final Composite parent);

    private void createViewerContextMenu() {
        final String menuId = getViewerMenuId();

        final MenuManager manager = new MenuManager("Robot suite editor code elements page context menu", menuId);
        final Control control = viewer.getControl();
        viewerMenu = manager.createContextMenu(control);
        control.setMenu(viewerMenu);
        site.registerContextMenu(menuId, manager, viewer, false);
    }

    protected abstract String getViewerMenuId();

    private void createHeaderContextMenu() {
        final String menuId = getHeaderMenuId();

        final MenuManager manager = new MenuManager("Robot suite editor code elements page header context menu", menuId);
        final Control control = viewer.getControl();
        headerMenu = manager.createContextMenu(control);
        control.setMenu(headerMenu);
        site.registerContextMenu(menuId, manager, viewer, false);
    }

    protected abstract String getHeaderMenuId();

    public FocusedViewerAccessor getFocusedViewerAccessor() {
        final ViewerColumnsManagingStrategy columnsManagingStrategy = new ViewerColumnsManagingStrategy() {
            @Override
            public void addColumn(final ColumnViewer viewer) {
                final RowExposingTreeViewer treeViewer = (RowExposingTreeViewer) viewer;
                final int index = treeViewer.getTree().getColumnCount() - 2;

                createArgumentColumn(index, provideNewElementsCreator());
                treeViewer.moveLastColumnTo(index + 1);
                treeViewer.reflowColumnsWidth();
            }

            @Override
            public void removeColumn(final ColumnViewer viewer) {
                final RowExposingTreeViewer treeViewer = (RowExposingTreeViewer) viewer;

                final int columnCount = treeViewer.getTree().getColumnCount();
                if (columnCount <= 2) {
                    return;
                }
                // always remove last columns which displays arguments
                final int position = columnCount - 2;
                final int orderIndexBeforeRemoving = treeViewer.getTree().getColumnOrder()[position];
                treeViewer.removeColumnAtPosition(position);
                treeViewer.reflowColumnsWidth();

                final TreeViewerEditor editor = (TreeViewerEditor) treeViewer.getColumnViewerEditor();
                final ViewerCell focusCell = editor.getFocusCell();
                if (focusCell.getColumnIndex() == orderIndexBeforeRemoving) {
                    treeViewer.setFocusCell(treeViewer.getTree().getColumnCount() - 2);
                }
            }
        };
        return new FocusedViewerAccessor(columnsManagingStrategy, viewer);
    }

    protected abstract RobotSuiteFileSection getSection();

    private void createColumns() {
        final NewElementsCreator creator = provideNewElementsCreator();

        createNameColumn(creator);
        final int maxLength = calculateLongestArgumentsList();
        for (int i = 0; i < maxLength; i++) {
            createArgumentColumn(i, creator);
        }
        createCommentColumn(maxLength + 1, creator);
    }

    protected abstract NewElementsCreator provideNewElementsCreator();

    protected int calculateLongestArgumentsList() {
        return RedPlugin.getDefault().getPreferences().getMimalNumberOfArgumentColumns();
    }

    private void createNameColumn(final NewElementsCreator creator) {
        ViewerColumnsFactory.newColumn("").withWidth(150)
            .equipWithThreeWaySorting(CodesViewerComparators.codeNamesAscendingComparator(),
                    CodesViewerComparators.codeNamesDescendingComparator())
            .labelsProvidedBy(new CodeNamesLabelProvider(getMatchesProvider()))
            .editingSupportedBy(new CodeNamesEditingSupport(viewer, commandsStack, creator))
            .editingEnabledOnlyWhen(fileModel.isEditable())
            .createFor(viewer);
    }

    private void createArgumentColumn(final int index, final NewElementsCreator creator) {
        ViewerColumnsFactory.newColumn("").withWidth(100)
            .labelsProvidedBy(new CodeArgumentLabelProvider(getMatchesProvider(), index))
            .editingSupportedBy(new CodeArgumentEditingSupport(viewer, index, commandsStack, creator))
            .editingEnabledOnlyWhen(fileModel.isEditable())
            .createFor(viewer);
    }

    private void createCommentColumn(final int index, final NewElementsCreator creator) {
        ViewerColumnsFactory.newColumn("Comment").withWidth(200)
            .shouldGrabAllTheSpaceLeft(true).withMinWidth(50)
            .labelsProvidedBy(new CodeCommentLabelProvider(getMatchesProvider()))
            .editingSupportedBy(new CodeCommentEditingSupport(viewer, index, commandsStack, creator))
            .editingEnabledOnlyWhen(fileModel.isEditable())
            .createFor(viewer);
    }

    protected abstract MatchesProvider getMatchesProvider();

    protected abstract boolean sectionIsDefined();

    @Override
    public void setFocus() {
        viewer.getTree().setFocus();
    }

    public void revealElement(final RobotElement robotElement) {
        viewer.setSelection(new StructuredSelection(new Object[] { robotElement }));
    }

    @Persist
    public void onSave() {
        isSaving = true;
        setFocus();
    }

    protected final void setDirty() {
        dirtyProviderService.setDirtyState(true);
    }

    @Inject
    @Optional
    private void whenSectionIsCreated(
            @UIEventTopic(RobotModelEvents.ROBOT_SUITE_SECTION_ADDED) final RobotSuiteFile file) {
        if (file == fileModel && viewer.getInput() == null) {
            viewer.setInput(getSection());
            viewer.refresh();

            dirtyProviderService.setDirtyState(true);
        }
    }

    @Inject
    @Optional
    private void whenSectionIsRemoved(
            @UIEventTopic(RobotModelEvents.ROBOT_SUITE_SECTION_REMOVED) final RobotSuiteFile file) {
        if (file == fileModel && viewer.getInput() != null) {
            viewer.setInput(getSection());
            viewer.refresh();

            dirtyProviderService.setDirtyState(true);
        }
    }

    @Inject
    @Optional
    private void whenFileChangedExternally(
            @UIEventTopic(RobotModelEvents.EXTERNAL_MODEL_CHANGE) final RobotElementChange change) {
        if (change.getKind() == Kind.CHANGED && change.getElement().getSuiteFile() == fileModel) {
            if (isSaving) {
                isSaving = false;
            }
            viewer.setInput(getSection());
            viewer.refresh();
        }
    }
}

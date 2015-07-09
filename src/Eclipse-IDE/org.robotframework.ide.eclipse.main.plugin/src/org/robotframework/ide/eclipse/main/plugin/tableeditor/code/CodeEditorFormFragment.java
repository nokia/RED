package org.robotframework.ide.eclipse.main.plugin.tableeditor.code;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.tools.services.IDirtyProviderService;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.RowExposingTreeViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerColumnsFactory;
import org.eclipse.jface.viewers.ViewersConfigurator;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IEditorSite;
import org.robotframework.forms.RedFormToolkit;
import org.robotframework.ide.eclipse.main.plugin.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.RobotElementChange;
import org.robotframework.ide.eclipse.main.plugin.RobotElementChange.Kind;
import org.robotframework.ide.eclipse.main.plugin.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.CellsAcivationStrategy;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.CellsAcivationStrategy.RowTabbingStrategy;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionFormFragment;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorSources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotElementEditingSupport.NewElementsCreator;

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
    private RedFormToolkit toolkit;

    protected RowExposingTreeViewer viewer;

    private boolean isSaving;

    public TreeViewer getViewer() {
        return viewer;
    }

    @Override
    public void initialize(final Composite parent) {
        createViewer(parent);
        createContextMenu();

        setInput();
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

        ViewersConfigurator.enableDeselectionPossibility(viewer);
        ViewersConfigurator.disableContextMenuOnHeader(viewer);
    }

    protected abstract ITreeContentProvider createContentProvider();

    private void createContextMenu() {
        final String menuId = getContextMenuId();

        final MenuManager manager = new MenuManager("Robot suite editor code page context menu", menuId);
        final Control control = viewer.getControl();
        final Menu menu = manager.createContextMenu(control);
        control.setMenu(menu);
        site.registerContextMenu(menuId, manager, viewer, false);
    }

    protected abstract String getContextMenuId();

    private void setInput() {
        viewer.setInput(getSection());

        viewer.removeAllColumns();
        createColumns();

        viewer.refresh();
        viewer.expandAll();
    }

    protected abstract RobotSuiteFileSection getSection();

    private void createColumns() {
        final NewElementsCreator creator = provideNewElementsCreator();

        createNameColumn(creator);
        if (sectionIsDefined()) {
            final int maxLength = calculateLongestArgumentsList();
            for (int i = 1; i <= maxLength; i++) {
                createArgumentColumn(i, creator);
            }
            createCommentColumn(maxLength + 1, creator);
        }
    }

    protected abstract NewElementsCreator provideNewElementsCreator();

    private void createNameColumn(final NewElementsCreator creator) {
        ViewerColumnsFactory.newColumn("").withWidth(150)
            .shouldGrabAllTheSpaceLeft(!sectionIsDefined()).withMinWidth(50)
            .labelsProvidedBy(new CodeNamesLabelProvider())
            .editingSupportedBy(new CodeNamesEditingSupport(viewer, commandsStack, creator))
            .editingEnabledOnlyWhen(fileModel.isEditable())
            .createFor(viewer);
    }

    private void createArgumentColumn(final int index, final NewElementsCreator creator) {
        ViewerColumnsFactory.newColumn("").withWidth(100)
            .labelsProvidedBy(new CodeArgumentLabelProvider(index))
            .editingSupportedBy(new CodeArgumentEditingSupport(viewer, index, commandsStack, creator))
            .editingEnabledOnlyWhen(fileModel.isEditable())
            .createFor(viewer);
    }

    private void createCommentColumn(final int index, final NewElementsCreator creator) {
        ViewerColumnsFactory.newColumn("Comment").withWidth(200)
            .shouldGrabAllTheSpaceLeft(true).withMinWidth(50)
            .labelsProvidedBy(new CodeCommentLabelProvider())
            .editingSupportedBy(new CodeCommentEditingSupport(viewer, index, commandsStack, creator))
            .editingEnabledOnlyWhen(fileModel.isEditable())
            .createFor(viewer);
    }

    private int calculateLongestArgumentsList() {
        return 5;
    }

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
    }

    protected final void setDirty() {
        dirtyProviderService.setDirtyState(true);
    }

    @Inject
    @Optional
    private void whenSectionIsCreated(
            @UIEventTopic(RobotModelEvents.ROBOT_SUITE_SECTION_ADDED) final RobotSuiteFile file) {
        if (file == fileModel && viewer.getInput() == null) {
            setInput();
            dirtyProviderService.setDirtyState(true);
        }
    }

    @Inject
    @Optional
    private void whenSectionIsRemoved(
            @UIEventTopic(RobotModelEvents.ROBOT_SUITE_SECTION_REMOVED) final RobotSuiteFile file) {
        if (file == fileModel && viewer.getInput() != null) {
            setInput();
            dirtyProviderService.setDirtyState(true);
        }
    }

    @Inject
    @Optional
    private void whenFileChangedExternally(
            @UIEventTopic(RobotModelEvents.EXTERNAL_MODEL_CHANGE) final RobotElementChange change) {
        if (change.getKind() == Kind.CHANGED && !isSaving) {
            isSaving = false;
            setInput();
        }
    }
}

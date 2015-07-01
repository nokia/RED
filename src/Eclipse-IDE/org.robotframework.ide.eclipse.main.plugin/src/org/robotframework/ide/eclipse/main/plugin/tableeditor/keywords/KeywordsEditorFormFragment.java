package org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.tools.services.IDirtyProviderService;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
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
import org.robotframework.ide.eclipse.main.plugin.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.CellsAcivationStrategy;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.CellsAcivationStrategy.RowTabbingStrategy;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionEditorPart;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionFormFragment;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorSources;

public class KeywordsEditorFormFragment implements ISectionFormFragment {

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

    private RowExposingTreeViewer viewer;

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
        viewer.setContentProvider(new UserKeywordsContentProvider());

        ViewersConfigurator.enableDeselectionPossibility(viewer);
        ViewersConfigurator.disableContextMenuOnHeader(viewer);
    }

    private void createContextMenu() {
        final String menuId = "org.robotframework.ide.eclipse.editor.page.keywords.contextMenu";

        final MenuManager manager = new MenuManager("Robot suite editor keywords page context menu", menuId);
        final Control control = viewer.getControl();
        final Menu menu = manager.createContextMenu(control);
        control.setMenu(menu);
        site.registerContextMenu(menuId, manager, viewer, false);
    }

    private void setInput() {
        viewer.setInput(getKeywordsSection());

        viewer.removeAllColumns();
        createColumns();

        viewer.refresh();
        viewer.expandAll();
    }

    private void createColumns() {
        createNameColumn();
        if (keywordsSectionIsDefined()) {
            for (int i = 0; i < calculateLongestArgumentsList(); i++) {
                createArgumentColumn(i);
            }
            createCommentColumn();
        }
    }

    private void createNameColumn() {
        ViewerColumnsFactory.newColumn("").withWidth(150)
            .shouldGrabAllTheSpaceLeft(!keywordsSectionIsDefined()).withMinWidth(50)
            .labelsProvidedBy(new UserKeywordNamesLabelProvider())            
//            .editingSupportedBy(new CasesNameEditingSupport(viewer, commandsStack, creator))
            .editingEnabledOnlyWhen(fileModel.isEditable())
            .createFor(viewer);
    }

    private void createArgumentColumn(final int index) {
        ViewerColumnsFactory.newColumn("").withWidth(100)
            .labelsProvidedBy(new UserKeywordArgumentLabelProvider(index))
    //        .editingSupportedBy(new KeywordCommentEditingSupport(viewer, commandsStack, null))
            .editingEnabledOnlyWhen(fileModel.isEditable())
            .createFor(viewer);
    }

    private void createCommentColumn() {
        ViewerColumnsFactory.newColumn("Comment").withWidth(200)
            .shouldGrabAllTheSpaceLeft(true).withMinWidth(50)
            .labelsProvidedBy(new UserKeywordCommentLabelProvider())
            .editingSupportedBy(new UserKeywordCommentEditingSupport(viewer, commandsStack, null))
            .editingEnabledOnlyWhen(fileModel.isEditable())
            .createFor(viewer);
    }

    private int calculateLongestArgumentsList() {
        return 5;
    }

    private boolean keywordsSectionIsDefined() {
        return fileModel.findSection(RobotKeywordsSection.class).isPresent();
    }

    private RobotElement getKeywordsSection() {
        return fileModel.findSection(RobotKeywordsSection.class).orNull();
    }

    @Override
    public void setFocus() {
        viewer.getTree().setFocus();
    }

    public void revealElement(final RobotElement robotElement) {
        viewer.setSelection(new StructuredSelection(new Object[] { robotElement }));
    }

    @Inject
    @Optional
    private void whenFileChangedExternally(@UIEventTopic(ISectionEditorPart.SECTION_FILTERING_TOPIC + "/"
            + RobotKeywordsSection.SECTION_NAME) final String filter) {
        System.out.println("Requested filtering of keywords section with '" + filter + "'");
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
}

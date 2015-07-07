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
import org.robotframework.ide.eclipse.main.plugin.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.cmd.CreateFreshKeywordCallCommand;
import org.robotframework.ide.eclipse.main.plugin.cmd.CreateFreshKeywordDefinitionCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.CellsAcivationStrategy;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.CellsAcivationStrategy.RowTabbingStrategy;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionFormFragment;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorSources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotElementEditingSupport.NewElementsCreator;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotSuiteEditorEvents;

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
        final NewElementsCreator creator = provideNewElementsCreator();

        createNameColumn(creator);
        if (keywordsSectionIsDefined()) {
            final int maxLength = calculateLongestArgumentsList();
            for (int i = 1; i <= maxLength; i++) {
                createArgumentColumn(i, creator);
            }
            createCommentColumn(maxLength + 1, creator);
        }
    }

    private NewElementsCreator provideNewElementsCreator() {
        return new NewElementsCreator() {

            @Override
            public RobotElement createNew(final Object parent) {
                if (parent instanceof RobotKeywordsSection) {
                    final RobotKeywordsSection section = (RobotKeywordsSection) parent;
                    commandsStack.execute(new CreateFreshKeywordDefinitionCommand(section, true));
                    return section.getChildren().get(section.getChildren().size() - 1);
                } else if (parent instanceof RobotKeywordDefinition) {
                    final RobotKeywordDefinition definition = (RobotKeywordDefinition) parent;
                    commandsStack.execute(new CreateFreshKeywordCallCommand(definition, true));
                    return definition.getChildren().get(definition.getChildren().size() - 1);
                }
                return null;
            }
        };
    }

    private void createNameColumn(final NewElementsCreator creator) {
        ViewerColumnsFactory.newColumn("").withWidth(150)
            .shouldGrabAllTheSpaceLeft(!keywordsSectionIsDefined()).withMinWidth(50)
            .labelsProvidedBy(new UserKeywordNamesLabelProvider())            
            .editingSupportedBy(new UserKeywordNamesEditingSupport(viewer, commandsStack, creator))
            .editingEnabledOnlyWhen(fileModel.isEditable())
            .createFor(viewer);
    }

    private void createArgumentColumn(final int index, final NewElementsCreator creator) {
        ViewerColumnsFactory.newColumn("").withWidth(100)
            .labelsProvidedBy(new UserKeywordArgumentLabelProvider(index))
            .editingSupportedBy(new UserKeywordArgumentEditingSupport(viewer, index, commandsStack, creator))
            .editingEnabledOnlyWhen(fileModel.isEditable())
            .createFor(viewer);
    }

    private void createCommentColumn(final int index, final NewElementsCreator creator) {
        ViewerColumnsFactory.newColumn("Comment").withWidth(200)
            .shouldGrabAllTheSpaceLeft(true).withMinWidth(50)
            .labelsProvidedBy(new UserKeywordCommentLabelProvider())
            .editingSupportedBy(new UserKeywordCommentEditingSupport(viewer, index, commandsStack, creator))
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
    private void whenUserRequestedFiltering(@UIEventTopic(RobotSuiteEditorEvents.SECTION_FILTERING_TOPIC + "/"
            + RobotKeywordsSection.SECTION_NAME) final String filter) {
        // nothing to do yet
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
    private void whenKeywordIsAddedOrRemoved(
            @UIEventTopic(RobotModelEvents.ROBOT_KEYWORD_DEFINITION_STRUCTURAL_ALL) final RobotSuiteFileSection section) {
        if (section.getSuiteFile() == fileModel) {
            viewer.refresh();
            dirtyProviderService.setDirtyState(true);
        }
    }

    @Inject
    @Optional
    private void whenKeywordCallIsAddedOrRemoved(
            @UIEventTopic(RobotModelEvents.ROBOT_KEYWORD_CALL_STRUCTURAL_ALL) final RobotKeywordDefinition definition) {
        if (definition.getSuiteFile() == fileModel) {
            viewer.refresh(definition);
            dirtyProviderService.setDirtyState(true);
        }
    }

    @Inject
    @Optional
    private void whenKeywordDetailIsChanged(
            @UIEventTopic(RobotModelEvents.ROBOT_KEYWORD_DEFINITION_CHANGE_ALL) final RobotKeywordDefinition definition) {
        if (definition.getSuiteFile() == fileModel) {
            viewer.update(definition, null);
            dirtyProviderService.setDirtyState(true);
        }
    }

    @Inject
    @Optional
    private void whenKeywordCallDetailIsChanged(
            @UIEventTopic(RobotModelEvents.ROBOT_KEYWORD_CALL_DETAIL_CHANGE_ALL) final RobotKeywordCall keywordCall) {
        if (keywordCall.getSuiteFile() == fileModel) {
            viewer.update(keywordCall, null);
            dirtyProviderService.setDirtyState(true);
        }
    }
}

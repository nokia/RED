package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.tools.services.IDirtyProviderService;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.RowExposingTableViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerColumnsFactory;
import org.eclipse.jface.viewers.ViewersConfigurator;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Section;
import org.robotframework.forms.RedFormToolkit;
import org.robotframework.forms.Sections;
import org.robotframework.ide.eclipse.main.plugin.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.RobotElementChange;
import org.robotframework.ide.eclipse.main.plugin.RobotElementChange.Kind;
import org.robotframework.ide.eclipse.main.plugin.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.cmd.CreateFreshCaseCommand;
import org.robotframework.ide.eclipse.main.plugin.cmd.CreateFreshKeywordCallCommand;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.KeywordProblem;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionFormFragment;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorSources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotElementEditingSupport.NewElementsCreator;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableCellsAcivationStrategy;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableCellsAcivationStrategy.RowTabbingStrategy;
import org.robotframework.viewers.Selections;

public class CasesEditorFormFragment implements ISectionFormFragment {

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

    private RowExposingTableViewer viewer;

    private RowExposingTableViewer caseViewer;

    TableViewer getViewer() {
        return viewer;
    }

    @Override
    public void initialize(final Composite parent) {
        final SashForm mainSash = new SashForm(parent, SWT.SMOOTH | SWT.HORIZONTAL);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(mainSash);
        toolkit.adapt(mainSash);
        toolkit.paintBordersFor(mainSash);

        viewer = new RowExposingTableViewer(mainSash, SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
        TableCellsAcivationStrategy.addActivationStrategy(viewer, RowTabbingStrategy.MOVE_TO_NEXT);

        GridDataFactory.fillDefaults().grab(true, true).applyTo(viewer.getTable());
        viewer.getTable().setLinesVisible(true);
        viewer.setUseHashlookup(true);
        viewer.getTable().addListener(SWT.MeasureItem, new Listener() {
            @Override
            public void handleEvent(final Event event) {
                event.height = Double.valueOf(event.gc.getFontMetrics().getHeight() * 1.5).intValue();
            }
        });
        final ISelectionChangedListener selectionListener = createSelectionListener();
        viewer.addSelectionChangedListener(selectionListener);
        viewer.getTable().addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(final DisposeEvent e) {
                viewer.removeSelectionChangedListener(selectionListener);
            }
        });
        viewer.setContentProvider(new CasesContentProvider());

        final NewElementsCreator creator = newCasesCreator();

        ViewerColumnsFactory.newColumn("").withWidth(250)
            .labelsProvidedBy(new CasesNameLabelProvider())
            .shouldGrabAllTheSpaceLeft(true).withMinWidth(100)
            .shouldShowLastVerticalSeparator(true)
            .editingSupportedBy(new CasesNameEditingSupport(viewer, commandsStack, creator))
            .editingEnabledOnlyWhen(fileModel.isEditable())
            .createFor(viewer);

        final Composite casesDetailComposite = createCasesDetailComposite(mainSash);

        createCaseSection(casesDetailComposite);
        createCaseSettingsSection(casesDetailComposite);
        mainSash.setWeights(new int[] { 20, 80 });

        ViewersConfigurator.disableContextMenuOnHeader(viewer);
        createContextMenu();

        setInput();
    }

    private ISelectionChangedListener createSelectionListener() {
        return new ISelectionChangedListener() {
            @Override
            public void selectionChanged(final SelectionChangedEvent event) {
                final List<RobotCase> robotCases = Selections.getElements((IStructuredSelection) event.getSelection(),
                        RobotCase.class);
                caseViewer.setInput(robotCases.isEmpty() ? null : robotCases.get(0));
            }
        };
    }

    private NewElementsCreator newCasesCreator() {
        return new NewElementsCreator() {
            @Override
            public RobotElement createNew() {
                final RobotCasesSection section = (RobotCasesSection) getViewer().getInput();
                commandsStack.execute(new CreateFreshCaseCommand(section));

                return section.getChildren().get(section.getChildren().size() - 1);
            }
        };
    }

    private NewElementsCreator newKeywordsCreator() {
        return new NewElementsCreator() {
            @Override
            public RobotElement createNew() {
                final RobotCase testCase = (RobotCase) caseViewer.getInput();
                commandsStack.execute(new CreateFreshKeywordCallCommand(testCase));

                return testCase.getChildren().get(testCase.getChildren().size() - 1);
            }
        };
    }

    private Composite createCasesDetailComposite(final Composite composite) {
        final Composite caseComposite = toolkit.createComposite(composite);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(caseComposite);
        GridLayoutFactory.fillDefaults().extendedMargins(10, 3, 0, 0).applyTo(caseComposite);
        return caseComposite;
    }

    private void createCaseSection(final Composite casesDetailComposite) {
        final Section caseSection = toolkit.createSection(casesDetailComposite, ExpandableComposite.TITLE_BAR);
        caseSection.setExpanded(true);
        caseSection.setText("Case");
        GridDataFactory.fillDefaults().grab(true, true).indent(0, 5).applyTo(caseSection);

        final Composite comp = toolkit.createComposite(caseSection);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(comp);
        caseSection.setClient(comp);

        caseViewer = new RowExposingTableViewer(caseSection, SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL
                | SWT.V_SCROLL);
        caseSection.setClient(caseViewer.getTable());
        caseViewer.setUseHashlookup(true);
        TableCellsAcivationStrategy.addActivationStrategy(caseViewer, RowTabbingStrategy.MOVE_TO_NEXT);
        ColumnViewerToolTipSupport.enableFor(caseViewer, ToolTip.NO_RECREATE);

        GridDataFactory.fillDefaults().grab(true, true).applyTo(caseViewer.getTable());
        caseViewer.getTable().setLinesVisible(true);
        caseViewer.getTable().setHeaderVisible(true);
        caseViewer.getTable().addListener(SWT.MeasureItem, new Listener() {
            @Override
            public void handleEvent(final Event event) {
                event.height = Double.valueOf(event.gc.getFontMetrics().getHeight() * 1.5).intValue();
            }
        });
        caseViewer.setContentProvider(new KeywordCallsContentProvider(fileModel.isEditable()));
        
        final NewElementsCreator creator = newKeywordsCreator();
        ViewerColumnsFactory.newColumn("").withWidth(140)
            .labelsProvidedBy(new KeywordCallNameLabelProvider())
            .editingSupportedBy(new KeywordCallNameEditingSupport(caseViewer, commandsStack, creator))
            .editingEnabledOnlyWhen(fileModel.isEditable())
            .createFor(caseViewer);

    }

    private void createCaseSettingsSection(final Composite casesDetailComposite) {
        final Section caseSection = toolkit.createSection(casesDetailComposite, ExpandableComposite.TWISTIE
                | ExpandableComposite.TITLE_BAR);
        caseSection.setExpanded(false);
        caseSection.setText("Case Settings");
        GridDataFactory.fillDefaults().grab(true, false).applyTo(caseSection);
        Sections.switchGridCellGrabbingOnExpansion(caseSection);

        final Composite comp = toolkit.createComposite(caseSection);
        caseSection.setClient(comp);

    }

    private void createContextMenu() {
        final String menuId = "org.robotframework.ide.eclipse.editor.page.cases.contextMenu";

        final MenuManager manager = new MenuManager("Robot suite editor cases page context menu", menuId);
        final Table control = viewer.getTable();
        final Menu menu = manager.createContextMenu(control);
        control.setMenu(menu);
        site.registerContextMenu(menuId, manager, viewer, false);
    }

    private void setInput() {
        final com.google.common.base.Optional<RobotElement> casesSection = fileModel
                .findSection(RobotCasesSection.class);
        if (casesSection.isPresent()) {
            viewer.setInput(casesSection.get());
        } else {
            viewer.setInput(null);
            viewer.refresh();
        }
    }

    public void revealCase(final RobotCase robotCase) {
        viewer.setSelection(new StructuredSelection(new RobotCase[] { robotCase }));
    }

    @Override
    public void setFocus() {
        viewer.getTable().setFocus();
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
    private void whenCaseDetailChanges(
            @UIEventTopic(RobotModelEvents.ROBOT_CASE_DETAIL_CHANGE_ALL) final RobotCase testCase) {
        if (testCase.getSuiteFile() == fileModel) {
            viewer.refresh();
            dirtyProviderService.setDirtyState(true);
        }
    }

    @Inject
    @Optional
    private void whenCaseIsAddedOrRemoved(
            @UIEventTopic(RobotModelEvents.ROBOT_CASE_STRUCTURAL_ALL) final RobotSuiteFileSection section) {
        if (section.getSuiteFile() == fileModel) {
            viewer.refresh();
            dirtyProviderService.setDirtyState(true);
        }
    }

    @Inject
    @Optional
    private void whenKeywordCallDetailChanges(
            @UIEventTopic(RobotModelEvents.ROBOT_KEYWORD_CALL_DETAIL_CHANGE_ALL) final RobotKeywordCall keywordCall) {
        // FIXME : check if this keyword call is shown in viewer
        caseViewer.update(keywordCall, null);
        dirtyProviderService.setDirtyState(true);

        RobotProblem.causedBy(KeywordProblem.UNKNOWN_KEYWORD).createMarker(fileModel.getFile(), 5);
    }

    @Inject
    @Optional
    private void whenFileChangedExternally(
            @UIEventTopic(RobotModelEvents.EXTERNAL_MODEL_CHANGE) final RobotElementChange change) {
        if (change.getKind() == Kind.CHANGED) {
            setInput();
        }
    }
}

package org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.tools.services.IDirtyProviderService;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.RowExposingTableViewer;
import org.eclipse.jface.viewers.ViewerColumnsFactory;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewersConfigurator;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Section;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.IRobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.CreateFreshKeywordSettingCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.DeleteKeywordCallCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallArgumentCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.CellsAcivationStrategy;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.CellsAcivationStrategy.RowTabbingStrategy;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ElementAddingToken;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionFormFragment;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorSources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotSuiteEditorEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.CodeEditorFormFragment;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.SettingsMatchesCollection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.SettingsMatchesFilter;
import org.robotframework.red.forms.RedFormToolkit;
import org.robotframework.red.graphics.ColorsManager;
import org.robotframework.red.viewers.Selections;

import com.google.common.collect.Range;

public class KeywordSettingsFormFragment implements ISectionFormFragment {

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

    private StyledText documentation;

    private Job documentionChangeJob;

    private RowExposingTableViewer viewer;

    protected MatchesCollection matches;

    @Override
    public void initialize(final Composite parent) {
        final Composite detailsPanel = createDetailsPanel(parent);
        createViewer(detailsPanel);
        createColumns();

        setInput(com.google.common.base.Optional.<RobotKeywordDefinition> absent());
    }

    private Composite createDetailsPanel(final Composite parent) {
        final Section section = toolkit.createSection(parent,
                ExpandableComposite.TWISTIE | ExpandableComposite.TITLE_BAR);
        section.setExpanded(false);
        section.setText("Settings");
        GridDataFactory.fillDefaults().grab(true, false).minSize(1, 22).applyTo(section);

        final Composite composite = toolkit.createComposite(section);
        GridLayoutFactory.fillDefaults().applyTo(composite);
        section.setClient(composite);

        return composite;
    }

    private void createViewer(final Composite parent) {
        createDocumentationControl(parent);

        viewer = new RowExposingTableViewer(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
        CellsAcivationStrategy.addActivationStrategy(viewer, RowTabbingStrategy.MOVE_TO_NEXT);
        ColumnViewerToolTipSupport.enableFor(viewer, ToolTip.NO_RECREATE);

        GridDataFactory.fillDefaults().grab(true, true).applyTo(viewer.getTable());
        viewer.setUseHashlookup(true);
        viewer.getTable().setLinesVisible(true);
        viewer.getTable().setHeaderVisible(true);
        viewer.getTable().addListener(SWT.MeasureItem, new Listener() {
            @Override
            public void handleEvent(final Event event) {
                event.height = Double.valueOf(event.gc.getFontMetrics().getHeight() * 1.5).intValue();
            }
        });
        viewer.setContentProvider(new KeywordSettingsContentProvider());

        ViewersConfigurator.enableDeselectionPossibility(viewer);
        ViewersConfigurator.disableContextMenuOnHeader(viewer);
    }

    private void createDocumentationControl(final Composite panel) {
        documentation = new StyledText(panel, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
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
                documentation.redraw();
            }

            @Override
            public void focusLost(final FocusEvent e) {
                documentation.redraw();
            }
        });
        toolkit.adapt(documentation, true, false);
        toolkit.paintBordersFor(documentation);
        if (fileModel.isEditable()) {
            documentation.addModifyListener(new ModifyListener() {

                @Override
                public void modifyText(final ModifyEvent e) {
                    final RobotKeywordDefinition definition = getCurrentKeywordDefinition();
                    if (documentation.getText()
                            .equals(getDocumentation(com.google.common.base.Optional.fromNullable(definition)))) {
                        return;
                    }
                    dirtyProviderService.setDirtyState(true);

                    if (documentionChangeJob != null && documentionChangeJob.getState() == Job.SLEEPING) {
                        documentionChangeJob.cancel();
                    }
                    documentionChangeJob = createDocumentationChangeJob(definition, documentation.getText());
                    documentionChangeJob.schedule(300);
                }
            });
        }

        GridDataFactory.fillDefaults()
                .grab(true, true)
                .minSize(SWT.DEFAULT, 60)
                .hint(SWT.DEFAULT, 60)
                .applyTo(documentation);
    }

    private Job createDocumentationChangeJob(final RobotKeywordDefinition definition, final String docu) {
        return new Job("changing documentation") {

            @Override
            protected IStatus run(final IProgressMonitor monitor) {
                if (definition == null) {
                    return Status.OK_STATUS;
                }

                final String newDocumentation = docu.replaceAll("\t", " ").replaceAll("  +", " ");
                final RobotDefinitionSetting docSetting = definition.getDocumentationSetting();

                if (docSetting == null && !newDocumentation.isEmpty()) {
                    commandsStack.execute(new CreateFreshKeywordSettingCommand(definition, "Documentation",
                            newArrayList(newDocumentation)));
                } else if (docSetting != null && newDocumentation.isEmpty()) {
                    commandsStack.execute(new DeleteKeywordCallCommand(newArrayList(docSetting)));
                } else if (docSetting != null) {
                    commandsStack.execute(new SetKeywordCallArgumentCommand(docSetting, 0, newDocumentation));
                }
                return Status.OK_STATUS;
            }
        };
    }

    private void createColumns() {
        final MatchesProvider matcherProvider = getMatchesProvider();
        createNameColumn(matcherProvider);

        for (int i = 0; i < calculateLongestArgumentsLength(); i++) {
            createArgumentColumn(i, matcherProvider);
        }
        createCommentColumn(matcherProvider);
    }

    private MatchesProvider getMatchesProvider() {
        return new MatchesProvider() {
            @Override
            public MatchesCollection getMatches() {
                return matches;
            }
        };
    }

    private int calculateLongestArgumentsLength() {
        return RedPlugin.getDefault().getPreferences().getMimalNumberOfArgumentColumns();
    }

    private void createNameColumn(final MatchesProvider matcherProvider) {
        ViewerColumnsFactory.newColumn("Setting")
                .withMinWidth(50)
                .withWidth(150)
                .labelsProvidedBy(new KeywordSettingsNamesLabelProvider(matcherProvider))
                .createFor(viewer);
    }

    private void createArgumentColumn(final int index, final MatchesProvider matcherProvider) {
        ViewerColumnsFactory.newColumn("")
                .withWidth(120)
                .labelsProvidedBy(new KeywordSettingsArgumentsLabelProvider(matcherProvider, index))
                .createFor(viewer);
    }

    private void createCommentColumn(final MatchesProvider matcherProvider) {
        ViewerColumnsFactory.newColumn("Comment")
                .withMinWidth(50).withWidth(200)
                .shouldGrabAllTheSpaceLeft(true)
                .labelsProvidedBy(new KeywordSettingsCommentsLabelProvider(matcherProvider))
                .createFor(viewer);
    }

    private void setInput(final com.google.common.base.Optional<RobotKeywordDefinition> definition) {
        documentation.setEditable(fileModel.isEditable() && definition.isPresent());
        viewer.getTable().setEnabled(fileModel.isEditable() && definition.isPresent());

        viewer.setInput(definition);
        documentation.setText(getDocumentation(definition));

        viewer.getTable().getParent().layout();
    }

    private RobotKeywordDefinition getCurrentKeywordDefinition() {
        return (RobotKeywordDefinition) ((com.google.common.base.Optional<?>) viewer.getInput()).orNull();
    }

    private String getDocumentation(final com.google.common.base.Optional<RobotKeywordDefinition> definition) {
        if (!definition.isPresent()) {
            return "";
        } else {
            final RobotDefinitionSetting docSetting = definition.get().getDocumentationSetting();
            return docSetting != null && !docSetting.getArguments().isEmpty() ? docSetting.getArguments().get(0) : "";
        }
    }

    @Override
    public void setFocus() {
        viewer.getTable().setFocus();
    }

    @Persist
    public void onSave() {
        if (documentionChangeJob != null) {
            try {
                documentionChangeJob.join();
            } catch (final InterruptedException e) {
                RedPlugin.logError("Documentation change job was interrupted", e);
            }
        }
    }

    @Override
    public MatchesCollection collectMatches(final String filter) {
        if (filter.isEmpty()) {
            return null;
        } else {
            final SettingsMatchesCollection settingsMatches = new SettingsMatchesCollection();
            final RobotKeywordDefinition keyword = getCurrentKeywordDefinition();
            final List<RobotElement> settings = KeywordSettingsModel.findKeywordSettingsList(keyword);
            if (keyword != null) {
                final RobotDefinitionSetting docSetting = keyword.getDocumentationSetting();
                if (docSetting != null) {
                    settings.add(docSetting);
                }
            }

            settingsMatches.collect(settings, filter);
            return settingsMatches;
        }
    }

    @Inject
    @Optional
    private void whenUserRequestedFiltering(@UIEventTopic(RobotSuiteEditorEvents.SECTION_FILTERING_TOPIC + "/"
            + RobotKeywordsSection.SECTION_NAME) final MatchesCollection matches) {
        this.matches = matches;

        try {
            viewer.getTable().setRedraw(false);
            if (matches == null) {
                clearDocumentationMatches();
                viewer.setFilters(new ViewerFilter[0]);
            } else {
                setDocumentationMatches(matches);
                viewer.setFilters(new ViewerFilter[] { new SettingsMatchesFilter(matches) });
            }
        } finally {
            viewer.getTable().setRedraw(true);
        }
    }

    private void setDocumentationMatches(final MatchesCollection settingsMatches) {
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

    @Inject
    @Optional
    private void whenKeywordSelectionChanged(
            @UIEventTopic(CodeEditorFormFragment.MAIN_PART_SELECTION_CHANGED_TOPIC) final IStructuredSelection selection) {
        final Object selectedElement = Selections.getOptionalFirstElement(selection, Object.class).orNull();

        if (selectedElement instanceof RobotKeywordDefinition) {
            newDefinitionWasSelected((RobotKeywordDefinition) selectedElement);
        } else if (selectedElement instanceof RobotKeywordCall) {
            final IRobotCodeHoldingElement parent = ((RobotKeywordCall) selectedElement).getParent();
            if (parent instanceof RobotKeywordDefinition) {
                newDefinitionWasSelected((RobotKeywordDefinition) parent);
            }
        } else if (selectedElement instanceof ElementAddingToken) {
            final Object parent = ((ElementAddingToken) selectedElement).getParent();
            if (parent instanceof RobotKeywordDefinition) {
                newDefinitionWasSelected((RobotKeywordDefinition) parent);
            }
        } else {
            setInput(com.google.common.base.Optional.<RobotKeywordDefinition> absent());
        }
    }

    private void newDefinitionWasSelected(final RobotKeywordDefinition newlySelectedDefinition) {
        final RobotKeywordDefinition currentDefinition = getCurrentKeywordDefinition();
        if (newlySelectedDefinition != currentDefinition) {
            setInput(com.google.common.base.Optional.of(newlySelectedDefinition));
        }
    }
}

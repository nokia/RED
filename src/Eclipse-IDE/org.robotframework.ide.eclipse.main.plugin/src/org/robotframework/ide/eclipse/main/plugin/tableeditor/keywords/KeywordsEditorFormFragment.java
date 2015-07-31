package org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.RowExposingTableViewer;
import org.eclipse.jface.viewers.ViewerColumnsFactory;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewersConfigurator;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.CreateFreshKeywordCallCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.CreateFreshKeywordDefinitionCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.CellsAcivationStrategy;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.CellsAcivationStrategy.RowTabbingStrategy;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotElementEditingSupport.NewElementsCreator;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotSuiteEditorEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.CodeEditorFormFragment;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.MatchesFilter;

public class KeywordsEditorFormFragment extends CodeEditorFormFragment {

    private RowExposingTableViewer settingsViewer;
    private Text documentation;
    private MatchesCollection matches;

    @Override
    protected void createSettingsTable(final Composite parent) {
        createDocumentationControl(parent);

        settingsViewer = new RowExposingTableViewer(parent, SWT.MULTI | SWT.FULL_SELECTION
                | SWT.H_SCROLL | SWT.V_SCROLL);
        CellsAcivationStrategy.addActivationStrategy(settingsViewer, RowTabbingStrategy.MOVE_TO_NEXT);
        ColumnViewerToolTipSupport.enableFor(settingsViewer, ToolTip.NO_RECREATE);

        GridDataFactory.fillDefaults().grab(true, true).applyTo(settingsViewer.getTable());
        settingsViewer.setUseHashlookup(true);
        settingsViewer.getTable().setLinesVisible(true);
        settingsViewer.getTable().setHeaderVisible(true);
        settingsViewer.getTable().addListener(SWT.MeasureItem, new Listener() {
            @Override
            public void handleEvent(final Event event) {
                event.height = Double.valueOf(event.gc.getFontMetrics().getHeight() * 1.5).intValue();
            }
        });
        settingsViewer.setContentProvider(new KeywordSettingsContentProvider());

        createColumns(settingsViewer);
        settingsViewer.setInput(new KeywordSettingsModel(null));

        ViewersConfigurator.enableDeselectionPossibility(settingsViewer);
    }

    private void createColumns(final RowExposingTableViewer settingsViewer) {
        ViewerColumnsFactory.newColumn("Setting").withMinWidth(50).withWidth(150)
            .labelsProvidedBy(new KeywordSettingsNamesLabelProvider())
            .createFor(settingsViewer);

    }

    private void createDocumentationControl(final Composite panel) {
        documentation = toolkit.createText(panel, "", SWT.MULTI);
        documentation.setEditable(fileModel.isEditable());
        documentation.addPaintListener(new PaintListener() {
            @Override
            public void paintControl(final PaintEvent e) {
                final Text text = (Text) e.widget;
                if (text.getText().isEmpty() && !text.isFocusControl()) {
                    final Color current = e.gc.getForeground();
                    e.gc.setForeground(e.display.getSystemColor(SWT.COLOR_GRAY));
                    e.gc.drawString("Documentation", 3, 3);
                    e.gc.setForeground(current);
                }
            }
        });
        if (fileModel.isEditable()) {
            // documentation.addModifyListener(new ModifyListener() {
            // @Override
            // public void modifyText(final ModifyEvent e) {
            // if (!((Text)
            // e.getSource()).getText().equals(model.getDocumentation())) {
            // dirtyProviderService.setDirtyState(true);
            // }
            // }
            // });
        }

        GridDataFactory.fillDefaults().grab(true, true).minSize(SWT.DEFAULT, 60).hint(SWT.DEFAULT, 60)
                .applyTo(documentation);
    }

    @Override
    protected void whenElementSelectionChanged(final RobotElement selectedElement) {
        RobotKeywordDefinition def = null;
        if (selectedElement instanceof RobotKeywordDefinition) {
            def = (RobotKeywordDefinition) selectedElement;
        } else if (selectedElement instanceof RobotKeywordCall) {
            def = (RobotKeywordDefinition) ((RobotKeywordCall) selectedElement).getParent();
        }
        newDefinitionWasSelected(def);
    }

    private void newDefinitionWasSelected(final RobotKeywordDefinition def) {
        final KeywordSettingsModel model = new KeywordSettingsModel(def);
        final KeywordSettingsModel currentModel = (KeywordSettingsModel) settingsViewer.getInput();

        if (model.equals(currentModel)) {
            return;
        }

        settingsViewer.setInput(model);

        if (def == null) {
            documentation.setEnabled(false);
            documentation.setText("");
        } else {
            documentation.setEnabled(true);
            documentation.setText(model.getDocumentation());
        }
    }

    @Override
    protected ITreeContentProvider createContentProvider() {
        return new UserKeywordsContentProvider();
    }

    @Override
    protected String getViewerMenuId() {
        return "org.robotframework.ide.eclipse.editor.page.keywords.contextMenu";
    }

    @Override
    protected String getHeaderMenuId() {
        return "org.robotframework.ide.eclipse.editor.page.keywords.header.contextMenu";
    }

    @Override
    protected boolean sectionIsDefined() {
        return fileModel.findSection(RobotKeywordsSection.class).isPresent();
    }

    @Override
    protected RobotSuiteFileSection getSection() {
        return (RobotSuiteFileSection) fileModel.findSection(RobotKeywordsSection.class).orNull();
    }

    @Override
    protected NewElementsCreator provideNewElementsCreator() {
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

    @Override
    protected int calculateLongestArgumentsList() {
        final RobotSuiteFileSection section = getSection();
        int max = RedPlugin.getDefault().getPreferences().getMimalNumberOfArgumentColumns();
        if (section != null) {
            for (final RobotElement element : section.getChildren()) {
                final RobotKeywordDefinition keyword = (RobotKeywordDefinition) element;
                if (keyword.hasArguments()) {
                    max = Math.max(max, keyword.getArgumentsSetting().getArguments().size());
                }

                for (final RobotElement nestedElement : element.getChildren()) {
                    final RobotKeywordCall call = (RobotKeywordCall) nestedElement;
                    max = Math.max(max, call.getArguments().size());
                }
            }
        }
        return max;
    }

    @Override
    protected MatcherProvider getMatchesProvider() {
        return new MatcherProvider() {
            @Override
            public MatchesCollection getMatches() {
                return matches;
            }
        };
    }

    @Override
    public MatchesCollection collectMatches(final String filter) {
        if (filter.isEmpty()) {
            return null;
        } else {
            final KeywordsMatchesCollection keywordMatches = new KeywordsMatchesCollection();
            keywordMatches.collect((RobotElement) viewer.getInput(), filter);
            return keywordMatches;
        }
    }

    @Inject
    @Optional
    private void whenUserRequestedFiltering(@UIEventTopic(RobotSuiteEditorEvents.SECTION_FILTERING_TOPIC + "/"
            + RobotKeywordsSection.SECTION_NAME) final MatchesCollection matches) {
        this.matches = matches;
        try {
            viewer.getTree().setRedraw(false);
            if (matches == null) {
                viewer.collapseAll();
                viewer.setFilters(new ViewerFilter[0]);
            } else {
                viewer.expandAll();
                viewer.setFilters(new ViewerFilter[] { new MatchesFilter(matches) });
            }
        } finally {
            viewer.getTree().setRedraw(true);
        }
    }

    @Inject
    @Optional
    private void whenKeywordIsAddedOrRemoved(
            @UIEventTopic(RobotModelEvents.ROBOT_KEYWORD_DEFINITION_STRUCTURAL_ALL) final RobotSuiteFileSection section) {
        if (section.getSuiteFile() == fileModel) {
            viewer.refresh();
            setDirty();
        }
    }

    @Inject
    @Optional
    private void whenKeywordIsAdded(
            @UIEventTopic(RobotModelEvents.ROBOT_KEYWORD_DEFINITION_ADDED) final RobotSuiteFileSection section) {
        if (section.getSuiteFile() == fileModel) {
            viewer.setComparator(null);
            viewer.getTree().setSortColumn(null);
        }
    }

    @Inject
    @Optional
    private void whenKeywordCallIsAddedOrRemoved(
            @UIEventTopic(RobotModelEvents.ROBOT_KEYWORD_CALL_STRUCTURAL_ALL) final RobotKeywordDefinition definition) {
        if (definition.getSuiteFile() == fileModel) {
            viewer.refresh(definition);
            viewer.update(definition, null);
            setDirty();
        }
    }

    @Inject
    @Optional
    private void whenKeywordDetailIsChanged(
            @UIEventTopic(RobotModelEvents.ROBOT_KEYWORD_DEFINITION_CHANGE_ALL) final RobotKeywordDefinition definition) {
        if (definition.getSuiteFile() == fileModel) {
            viewer.update(definition, null);
            setDirty();
        }
    }

    @Inject
    @Optional
    private void whenKeywordCallDetailIsChanged(
            @UIEventTopic(RobotModelEvents.ROBOT_KEYWORD_CALL_DETAIL_CHANGE_ALL) final RobotKeywordCall keywordCall) {
        if (keywordCall.getParent() instanceof RobotKeywordDefinition && keywordCall.getSuiteFile() == fileModel) {
            viewer.update(keywordCall, null);
            setDirty();
        }
    }
}

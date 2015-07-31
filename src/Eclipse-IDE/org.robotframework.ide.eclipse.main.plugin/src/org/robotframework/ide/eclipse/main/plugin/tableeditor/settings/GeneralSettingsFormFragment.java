package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Collection;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.tools.services.IDirtyProviderService;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.RowExposingTableViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerColumnsFactory;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewersConfigurator;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Section;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElementChange;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElementChange.Kind;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.CreateSettingKeywordCallCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.DeleteSettingKeywordCallCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallArgumentCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.CellsAcivationStrategy;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.CellsAcivationStrategy.RowTabbingStrategy;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionFormFragment;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorSources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotSuiteEditorEvents;
import org.robotframework.red.forms.RedFormToolkit;
import org.robotframework.red.forms.Sections;
import org.robotframework.red.graphics.ColorsManager;
import org.robotframework.red.viewers.Viewers;

import com.google.common.collect.Range;

public class GeneralSettingsFormFragment implements ISectionFormFragment {

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

    private StyledText documentation;

    private final GeneralSettingsModel model = new GeneralSettingsModel();

    private Section section;

    private MatchesCollection matches;

    TableViewer getViewer() {
        return viewer;
    }

    @Override
    public void initialize(final Composite parent) {
        section = toolkit.createSection(parent, ExpandableComposite.TWISTIE
                | ExpandableComposite.TITLE_BAR | Section.DESCRIPTION);
        section.setExpanded(true);
        section.setText("General");
        section.setDescription("Provide test suite documentation and general settings");
        GridDataFactory.fillDefaults().grab(true, true).minSize(1, 22).indent(0, 5).applyTo(section);
        Sections.switchGridCellGrabbingOnExpansion(section);
        Sections.installMaximazingPossibility(section);

        final Composite panel = createPanel(section);
        createDocumentationControl(panel);
        createViewer(panel);

        ViewersConfigurator.disableContextMenuOnHeader(viewer);
        createContextMenu();

        setInput(true);
    }

    private Composite createPanel(final Section section) {
        final Composite panel = toolkit.createComposite(section);
        GridDataFactory.fillDefaults().grab(true, true).indent(0, 0).applyTo(panel);
        GridLayoutFactory.fillDefaults().applyTo(panel);
        section.setClient(panel);
        return panel;
    }

    private void createDocumentationControl(final Composite panel) {
        documentation = new StyledText(panel, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        documentation.setEditable(fileModel.isEditable());
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
        toolkit.adapt(documentation, true, false);
        toolkit.paintBordersFor(documentation);
        if (fileModel.isEditable()) {
            documentation.addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(final ModifyEvent e) {
                    if (!((StyledText) e.getSource()).getText().equals(model.getDocumentation())) {
                        dirtyProviderService.setDirtyState(true);
                    }
                }
            });
        }

        GridDataFactory.fillDefaults().grab(true, true).minSize(SWT.DEFAULT, 60).hint(SWT.DEFAULT, 100)
                .applyTo(documentation);
    }

    private void createViewer(final Composite panel) {
        viewer = new RowExposingTableViewer(panel, SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
        viewer.getTable().setLinesVisible(true);
        viewer.getTable().setHeaderVisible(true);
        viewer.getTable().addListener(SWT.MeasureItem, new Listener() {
            @Override
            public void handleEvent(final Event event) {
                event.height = Double.valueOf(event.gc.getFontMetrics().getHeight() * 1.5).intValue();
            }
        });
        viewer.setContentProvider(new GeneralSettingsContentProvider());
        GridDataFactory.fillDefaults().grab(true, true).applyTo(viewer.getTable());
        Viewers.boundViewerWithContext(viewer, site,
                "org.robotframework.ide.eclipse.tableeditor.settings.general.context");
        CellsAcivationStrategy.addActivationStrategy(viewer, RowTabbingStrategy.MOVE_IN_CYCLE);
        ColumnViewerToolTipSupport.enableFor(viewer, ToolTip.NO_RECREATE);

        createColumns(true);
    }

    private void createColumns(final boolean createFirst) {
        final MatcherProvider matcherProvider = new MatcherProvider() {
            @Override
            public MatchesCollection getMatches() {
                return matches;
            }
        };

        if (createFirst) {
            ViewerColumnsFactory.newColumn("Setting").withWidth(100)
                .shouldGrabAllTheSpaceLeft(!fileModel.findSection(RobotSettingsSection.class).isPresent()).withMinWidth(100)
                .labelsProvidedBy(new GeneralSettingsNamesLabelProvider(matcherProvider))
                .createFor(viewer);
        }
        if (!model.areSettingsExist()) {
            return;
        }

        final int max = calcualateLongestArgumentsLength();
        for (int i = 0; i < max; i++) {
            ViewerColumnsFactory.newColumn("").withWidth(120)
                .labelsProvidedBy(new GeneralSettingsArgsLabelProvider(matcherProvider, i))
                .editingSupportedBy(new GeneralSettingsArgsEditingSupport(viewer, i, commandsStack))
                .editingEnabledOnlyWhen(fileModel.isEditable())
                .createFor(viewer);
        }
        ViewerColumnsFactory.newColumn("Comment").withWidth(200)
            .shouldGrabAllTheSpaceLeft(true).withMinWidth(100)
            .labelsProvidedBy(new GeneralSettingsCommentsLabelProvider(matcherProvider))
            .editingSupportedBy(new GeneralSettingsCommentsEditingSupport(viewer, commandsStack))
            .editingEnabledOnlyWhen(fileModel.isEditable())
            .createFor(viewer);
    }

    private int calcualateLongestArgumentsLength() {
        int max = RedPlugin.getDefault().getPreferences().getMimalNumberOfArgumentColumns();
        for (final Entry<String, RobotElement> entry : model.getEntries()) {
            final RobotSetting setting = (RobotSetting) entry.getValue();
            if (setting != null) {
                max = Math.max(max, setting.getArguments().size());
            }
        }
        return max;
    }

    private void createContextMenu() {
        final String menuId = "org.robotframework.ide.eclipse.editor.page.settings.general.contextMenu";

        final MenuManager manager = new MenuManager("Robot suite editor general settings context menu", menuId);
        final Table control = viewer.getTable();
        final Menu menu = manager.createContextMenu(control);
        control.setMenu(menu);
        site.registerContextMenu(menuId, manager, site.getSelectionProvider(), false);
    }

    private void setInput(final boolean setDocumentation) {
        final com.google.common.base.Optional<RobotElement> settingsSection = fileModel
                .findSection(RobotSettingsSection.class);
        model.update(settingsSection);

        documentation.setEnabled(settingsSection.isPresent());
        if (setDocumentation) {
            documentation.setText(model.getDocumentation());
        }

        viewer.setInput(model);
        viewer.removeColumns(1);
        createColumns(false);
        viewer.packFirstColumn();

        viewer.refresh();
    }

    @Override
    public void setFocus() {
        viewer.getTable().setFocus();

        // there is some problem with measuring table row height sometimes, so
        // the table needs to be laid out in its parent
        viewer.getTable().getParent().layout();
    }

    public void revealSetting(final RobotSetting setting) {
        Sections.maximizeChosenSectionAndMinimalizeOthers(section);
        if ("Documentation".equals(setting.getName())) {
            documentation.forceFocus();
            documentation.selectAll();
            viewer.setSelection(StructuredSelection.EMPTY);
        } else {
            for (final Entry<String, RobotElement> entry : model.getEntries()) {
                if (entry.getValue() == setting) {
                    viewer.setSelection(new StructuredSelection(entry));
                }
            }
            setFocus();
        }
    }

    public void clearSettingsSelection() {
        viewer.setSelection(StructuredSelection.EMPTY);
    }

    @Override
    public MatchesCollection collectMatches(final String filter) {
        if (filter.isEmpty()) {
            return null;
        } else {
            final SettingsMatchesCollection settingsMatches = new SettingsMatchesCollection();
            final GeneralSettingsModel model = (GeneralSettingsModel) viewer.getInput();
            settingsMatches.collect(model.getSettings(), filter);
            return settingsMatches;
        }
    }

    @Inject
    @Optional
    private void whenUserRequestedFiltering(@UIEventTopic(RobotSuiteEditorEvents.SECTION_FILTERING_TOPIC + "/"
            + RobotSettingsSection.SECTION_NAME) final MatchesCollection matches) {
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
            documentation.setStyleRange(new StyleRange(range.lowerEndpoint(), range.upperEndpoint()
                    - range.lowerEndpoint(), null, bg));
        }
    }

    private void clearDocumentationMatches() {
        documentation.setStyleRange(null);
    }

    @Persist
    public void whenSaving() {
        final String newDocumentation = documentation.getText().replaceAll("\t", " ").replaceAll("  +", " ");
        final RobotSetting currentSetting = model.getDocumentationSetting();

        if (model.getSection() == null) {
            return;
        }

        if (currentSetting == null && !newDocumentation.isEmpty()) {
            commandsStack.execute(new CreateSettingKeywordCallCommand(model.getSection(), "Documentation",
                    newArrayList(newDocumentation)));
        } else if (currentSetting != null && newDocumentation.isEmpty()) {
            commandsStack.execute(new DeleteSettingKeywordCallCommand(newArrayList(currentSetting)));
        } else if (currentSetting != null) {
            commandsStack.execute(new SetKeywordCallArgumentCommand(currentSetting, 0, newDocumentation));
        }
    }

    @Inject
    @Optional
    private void whenSectionIsCreated(
            @UIEventTopic(RobotModelEvents.ROBOT_SUITE_SECTION_ADDED) final RobotSuiteFile file) {
        if (file == fileModel) {
            setInput(false);
            dirtyProviderService.setDirtyState(true);
        }
    }

    @Inject
    @Optional
    private void whenSectionIsRemoved(
            @UIEventTopic(RobotModelEvents.ROBOT_SUITE_SECTION_REMOVED) final RobotSuiteFile file) {
        if (file == fileModel) {
            documentation.setText("");
            setInput(false);
            dirtyProviderService.setDirtyState(true);
        }
    }

    @Inject
    @Optional
    private void whenSettingDetailsChanges(
            @UIEventTopic(RobotModelEvents.ROBOT_KEYWORD_CALL_DETAIL_CHANGE_ALL) final RobotSetting setting) {
        if (setting.getSuiteFile() == fileModel && model.contains(setting)) {
            setInput(false);
            dirtyProviderService.setDirtyState(true);
        }
    }

    @Inject
    @Optional
    private void whenSettingIsAddedOrRemoved(
            @UIEventTopic(RobotModelEvents.ROBOT_SETTINGS_STRUCTURAL_ALL) final RobotSuiteFileSection section) {
        if (section == model.getSection()) {
            setInput(false);
            dirtyProviderService.setDirtyState(true);
        }
    }

    @Inject
    @Optional
    private void whenFileChangedExternally(
            @UIEventTopic(RobotModelEvents.EXTERNAL_MODEL_CHANGE) final RobotElementChange change) {
        if (change.getKind() == Kind.CHANGED && change.getElement().getSuiteFile() == fileModel) {
            setInput(false);
        }
    }
}

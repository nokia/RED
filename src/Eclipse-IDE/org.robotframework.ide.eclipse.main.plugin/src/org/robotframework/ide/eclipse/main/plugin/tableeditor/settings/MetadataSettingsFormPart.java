package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.RowExposingTableViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerColumnsFactory;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.robotframework.ide.eclipse.main.plugin.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.cmd.CreateSettingKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorSources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotElementEditingSupport.NewElementsCreator;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableCellsAcivationStrategy;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableCellsAcivationStrategy.RowTabbingStrategy;

class MetadataSettingsFormPart extends AbstractFormPart {

    @Inject
    @Named(RobotEditorSources.SUITE_FILE_MODEL)
    private RobotSuiteFile fileModel;

    @Inject
    private RobotEditorCommandsStack commandsStack;

    private final IEditorSite site;

    private RowExposingTableViewer viewer;

    public MetadataSettingsFormPart(final IEditorSite site) {
        this.site = site;
    }

    @Override
    public final void initialize(final IManagedForm managedForm) {
        super.initialize(managedForm);
        final SashForm sash = (SashForm) managedForm.getForm().getBody().getChildren()[0];
        final SashForm rightPanelSash = (SashForm) sash.getChildren()[1];
        final Composite rightPanelTopComposite = (Composite) rightPanelSash.getChildren()[0];

        createContent(rightPanelTopComposite);
    }

    private void createContent(final Composite parent) {
        final ExpandableComposite section = getManagedForm().getToolkit().createSection(parent,
                ExpandableComposite.TWISTIE | ExpandableComposite.TITLE_BAR);
        section.setText("Metadata");
        section.setExpanded(true);
        GridDataFactory.fillDefaults().grab(true, true).indent(0, 10).applyTo(section);

        viewer = new RowExposingTableViewer(section, SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(viewer.getTable());
        section.setClient(viewer.getTable());
        viewer.getTable().setLinesVisible(true);
        viewer.getTable().setHeaderVisible(true);
        viewer.getTable().addListener(SWT.MeasureItem, new Listener() {

            @Override
            public void handleEvent(final Event event) {
                event.height = event.gc.getFontMetrics().getHeight() * 2;
            }
        });
        viewer.setContentProvider(new MetadataSettingsContentProvider(fileModel.isEditable()));
        TableCellsAcivationStrategy.addActivationStrategy(viewer, RowTabbingStrategy.MOVE_TO_NEXT);
        ColumnViewerToolTipSupport.enableFor(viewer, ToolTip.NO_RECREATE);

        final NewElementsCreator creator = newElementsCreator();
        ViewerColumnsFactory.newColumn("Metadata").withWidth(140)
                .labelsProvidedBy(new SettingsArgsLabelProvider(0, true))
                .editingSupportedBy(new SettingsArgsEditingSupport(viewer, 0, commandsStack, creator))
                .editingEnabledOnlyWhen(fileModel.isEditable())
                .createFor(viewer);
        ViewerColumnsFactory.newColumn("Value").withWidth(140)
                .labelsProvidedBy(new SettingsArgsLabelProvider(1))
                .editingSupportedBy(new SettingsArgsEditingSupport(viewer, 1, commandsStack, creator))
                .editingEnabledOnlyWhen(fileModel.isEditable())
                .createFor(viewer);
        ViewerColumnsFactory.newColumn("Comment").withWidth(140)
                .labelsProvidedBy(new SettingsCommentsLabelProvider())
                .editingSupportedBy(new SettingsCommentsEditingSupport(viewer, commandsStack, creator))
                .editingEnabledOnlyWhen(fileModel.isEditable())
                .createFor(viewer);

        setInput();
    }

    private NewElementsCreator newElementsCreator() {
        return new NewElementsCreator() {
            @Override
            public RobotElement createNew() {
                final RobotSuiteSettingsSection settingsSection = getSection();
                commandsStack.execute(new CreateSettingKeywordCall(settingsSection, "Metadata", newArrayList("data")));
                return settingsSection.getChildren().get(settingsSection.getChildren().size() - 1);
            }
        };
    }

    private void setInput() {
        viewer.setInput(getMetadataElements());
    }

    private List<RobotElement> getMetadataElements() {
        final RobotSuiteSettingsSection section = getSection();
        return section != null ? section.getMetadataSettings() : null;
    }

    private RobotSuiteSettingsSection getSection() {
        final com.google.common.base.Optional<RobotElement> settingsSection = fileModel
                .findSection(RobotSuiteSettingsSection.class);
        return (RobotSuiteSettingsSection) settingsSection.orNull();
    }

    @Override
    public void setFocus() {
        viewer.getTable().setFocus();
    }

    public void revealSetting(final RobotSetting setting) {
        viewer.setSelection(new StructuredSelection(setting));
    }

    public void clearSettingsSelection() {
        viewer.setSelection(StructuredSelection.EMPTY);
    }

    @Inject
    @Optional
    private void whenSectionIsCreated(
            @UIEventTopic(RobotModelEvents.ROBOT_SUITE_SECTION_ADDED) final RobotSuiteFile file) {
        if (file == fileModel) {
            setInput();
            markDirty();
        }
    }

    @Inject
    @Optional
    private void whenSectionIsRemoved(
            @UIEventTopic(RobotModelEvents.ROBOT_SUITE_SECTION_REMOVED) final RobotSuiteFile file) {
        if (file == fileModel) {
            setInput();
            markDirty();
        }
    }

    @Inject
    @Optional
    private void whenSettingIsAddedOrRemoved(
            @UIEventTopic(RobotModelEvents.ROBOT_SETTINGS_STRUCTURAL_ALL) final RobotSuiteFileSection section) {
        if (section.getSuiteFile() == fileModel) {
            setInput();
            markDirty();
        }
    }

    @Inject
    @Optional
    private void whenSettingDetailsChanges(
            @UIEventTopic(RobotModelEvents.ROBOT_SETTING_DETAIL_CHANGE_ALL) final RobotSetting setting) {
        final List<?> input = (List<?>) viewer.getInput();
        if (setting.getSuiteFile() == fileModel && input != null && input.contains(setting)) {
            setInput();
            markDirty();
        }
    }
}

package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ColumnAddingEditingSupport;
import org.eclipse.jface.viewers.ColumnAddingEditingSupport.ColumnProviders;
import org.eclipse.jface.viewers.ColumnAddingLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.RowExposingTableViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
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
import org.robotframework.ide.eclipse.main.plugin.RobotImages;
import org.robotframework.ide.eclipse.main.plugin.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorSources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableCellsAcivationStrategy;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableCellsAcivationStrategy.RowTabbingStrategy;

public class ImportsSettingsFormPart extends AbstractFormPart {

    @Inject
    @Named(RobotEditorSources.SUITE_FILE_MODEL)
    private RobotSuiteFile fileModel;

    @Inject
    private RobotEditorCommandsStack commandsStack;

    private final IEditorSite site;

    private RowExposingTableViewer viewer;

    public ImportsSettingsFormPart(final IEditorSite site) {
        this.site = site;
    }

    TableViewer getViewer() {
        return viewer;
    }

    @Override
    public final void initialize(final IManagedForm managedForm) {
        super.initialize(managedForm);
        super.initialize(managedForm);
        final SashForm sash = (SashForm) managedForm.getForm().getBody().getChildren()[0];
        final SashForm rightPanelSash = (SashForm) sash.getChildren()[1];
        final Composite rightPanelBottomComposite = (Composite) rightPanelSash.getChildren()[1];

        createContent(rightPanelBottomComposite);
    }

    private void createContent(final Composite parent) {
        final ExpandableComposite section = getManagedForm().getToolkit().createSection(parent,
                ExpandableComposite.TWISTIE | ExpandableComposite.TITLE_BAR);
        section.setText("Imports");
        section.setExpanded(true);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(section);

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
        viewer.setContentProvider(new ImportsSettingsContentProvider(fileModel.isEditable()));
        TableCellsAcivationStrategy.addActivationStrategy(viewer, RowTabbingStrategy.MOVE_TO_NEXT);
        ColumnViewerToolTipSupport.enableFor(viewer, ToolTip.NO_RECREATE);

        setInput();
    }

    private int calcualateLongestArgumentsLength() {
        int max = 1;
        final List<?> elements = (List<?>) viewer.getInput();
        if (elements != null) {
            for (final Object element : elements) {
                final RobotSetting setting = (RobotSetting) element;
                if (setting != null) {
                    max = Math.max(max, setting.getArguments().size());
                }
            }
        }
        return max;
    }

    private void setInput() {
        viewer.setInput(getImportElements());
        viewer.removeColumns(1);
        createColumns(!viewer.hasAtLeastOneColumn());
        viewer.packFirstColumn();
    }

    private List<RobotElement> getImportElements() {
        final com.google.common.base.Optional<RobotElement> settingsSection = fileModel
                .findSection(RobotSuiteSettingsSection.class);

        if (settingsSection.isPresent()) {
            return ((RobotSuiteSettingsSection) settingsSection.get()).getMetadataSettings();
        }
        return null;
    }

    private void createColumns(final boolean createFirst) {
        if (createFirst) {
            ViewerColumnsFactory.newColumn("Import").withWidth(100)
                    .labelsProvidedBy(new SettingsCallNameLabelProvider())
                    .createFor(viewer);
        }
        if (getViewer().getInput() == null) {
            return;
        }

        ViewerColumnsFactory.newColumn("Name / Path").withWidth(80)
                .labelsProvidedBy(new SettingsArgsLabelProvider(0))
                .createFor(viewer);

        final int max = calcualateLongestArgumentsLength();
        for (int i = 1; i < max; i++) {
            ViewerColumnsFactory.newColumn("").withWidth(80)
                    .labelsProvidedBy(new SettingsArgsLabelProvider(i))
                    .createFor(viewer);
        }
        ViewerColumnsFactory.newColumn("Comment").withWidth(100)
                .labelsProvidedBy(new SettingsCommentsLabelProvider())
                .editingSupportedBy(new SettingsCommentsEditingSupport(viewer))
                .createFor(viewer);

        final int newColumnsStartingPosition = max + 1;
        
        ViewerColumnsFactory.newColumn("").withWidth(28).resizable(false)
                .withTooltip("Activate cell in this column to add new arguments columns")
                .withImage(RobotImages.getAddImage().createImage())
                .labelsProvidedBy(new ColumnAddingLabelProvider())
                .editingSupportedBy(
                        new ColumnAddingEditingSupport(viewer, newColumnsStartingPosition, new ColumnProviders() {
                            @Override
                            public void createColumn(final int index) {
                                ViewerColumnsFactory.newColumn("").withWidth(80)
                                        .labelsProvidedBy(new SettingsArgsLabelProvider(index - 1))
                                        .createFor(viewer);
                            }
                        }))
                .createFor(viewer);
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
}

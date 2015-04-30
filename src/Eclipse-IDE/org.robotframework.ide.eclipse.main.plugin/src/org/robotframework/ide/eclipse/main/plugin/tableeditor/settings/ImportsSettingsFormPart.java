package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnAddingEditingSupport;
import org.eclipse.jface.viewers.ColumnAddingEditingSupport.ColumnProviders;
import org.eclipse.jface.viewers.ColumnAddingLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.RowExposingTableViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TooltipsEnablingDelegatingStyledCellLabelProvider;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.robotframework.ide.eclipse.main.plugin.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.RobotImages;
import org.robotframework.ide.eclipse.main.plugin.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.RobotSetting.SettingsGroup;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorSources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableCellsAcivationStrategy;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableCellsAcivationStrategy.RowTabbingStrategy;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

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

        createColumns(true);

        setInput();
        packFirstColumn();
    }

    private void createColumns(final boolean createFirst) {
        if (createFirst) {
            createColumn(100, "Import", new SettingsCallNameLabelProvider(), null);
        }
        if (getViewer().getInput() == null) {
            return;
        }

        createColumn(80, "Name / Path", new SettingsArgsLabelProvider(0), null);

        final int max = calcualateLongestArgumentsLength();
        for (int i = 1; i < max; i++) {
            createColumn(80, "", new SettingsArgsLabelProvider(i), null);
        }
        createColumn(100, "Comment", new SettingsCommentsLabelProvider(), new SettingsCommentsEditingSupport(viewer));

        final int newColumnsStartingPosition = max + 1;
        final TableViewerColumn addingColumn = createColumn(28, "", new ColumnAddingLabelProvider(),
                new ColumnAddingEditingSupport(viewer, newColumnsStartingPosition, new ColumnProviders() {
                    @Override
                    public void createColumn(final int index) {
                        ImportsSettingsFormPart.this.createColumn(80, "", new SettingsArgsLabelProvider(
                                index - 1), null);
                    }
                }));
        addingColumn.getColumn().setResizable(false);
        addingColumn.getColumn().setToolTipText("Activate cell in this column to add new arguments columns");
        final Image addingColumnImage = RobotImages.getAddImage().createImage();
        addingColumn.getColumn().setImage(addingColumnImage);
        addingColumn.getColumn().addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(final DisposeEvent e) {
                addingColumnImage.dispose();
            }
        });
    }

    private TableViewerColumn createColumn(final int width, final String name, final CellLabelProvider labelProvider,
            final EditingSupport editingSupport) {
        final TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
        column.getColumn().setWidth(width);
        column.getColumn().setText(name);
        if (labelProvider instanceof IStyledLabelProvider) {
            column.setLabelProvider(new TooltipsEnablingDelegatingStyledCellLabelProvider(
                    (IStyledLabelProvider) labelProvider));
        } else {
            column.setLabelProvider(labelProvider);
        }
        if (fileModel.isEditable()) {
            column.setEditingSupport(editingSupport);
        }
        return column;
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

    private void packFirstColumn() {
        viewer.getTable().getColumn(0).pack();
    }

    private void setInput() {
        viewer.setInput(getImportElements());
        reloadColumns();
    }

    private void reloadColumns() {
        int i = 0;
        for (final TableColumn column : viewer.getTable().getColumns()) {
            if (i > 0) { // no need to remove first column
                column.dispose();
            }
            i++;
        }
        createColumns(false);
    }

    private List<RobotElement> getImportElements() {
        final com.google.common.base.Optional<RobotElement> settingsSection = fileModel
                .findSection(RobotSuiteSettingsSection.class);

        if (settingsSection.isPresent()) {
            final List<RobotElement> settings = ((RobotSuiteSettingsSection) settingsSection.get()).getChildren();
            return newArrayList(Iterables.filter(settings, new Predicate<RobotElement>() {
                @Override
                public boolean apply(final RobotElement element) {
                    return element instanceof RobotSetting
                            && SettingsGroup.getImportsGroupsSet().contains(((RobotSetting) element).getGroup());
                }
            }));
        }
        return null;
    }

    @Override
    public void setFocus() {
        viewer.getTable().setFocus();
    }

    @Override
    public void refresh() {
        setInput();
        super.refresh();
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

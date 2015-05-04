package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
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
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.robotframework.ide.eclipse.main.plugin.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.RobotImages;
import org.robotframework.ide.eclipse.main.plugin.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.cmd.CreateSettingKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.cmd.DeleteSettingKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.cmd.SetArgumentOfSettingKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorSources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableCellsAcivationStrategy;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableCellsAcivationStrategy.RowTabbingStrategy;

public class GeneralSettingsFormPart extends AbstractFormPart {

    @Inject
    @Named(RobotEditorSources.SUITE_FILE_MODEL)
    private RobotSuiteFile fileModel;

    @Inject
    private RobotEditorCommandsStack commandsStack;

    private final IEditorSite site;
    private RowExposingTableViewer viewer;

    private Text documentation;

    private final GeneralSettingsModel model = new GeneralSettingsModel();

    public GeneralSettingsFormPart(final IEditorSite site) {
        this.site = site;
    }

    TableViewer getViewer() {
        return viewer;
    }

    @Override
    public final void initialize(final IManagedForm managedForm) {
        super.initialize(managedForm);
        final SashForm sash = (SashForm) managedForm.getForm().getBody().getChildren()[0];
        final Composite leftPanel = (Composite) sash.getChildren()[0];
        createContent(leftPanel);
    }

    private void createContent(final Composite parent) {
        final FormToolkit toolkit = getManagedForm().getToolkit();
        final Section section = toolkit.createSection(parent,
                Section.EXPANDED | Section.TITLE_BAR | Section.DESCRIPTION);
        section.setText("General");
        section.setDescription("Provide general suite settings");
        GridDataFactory.fillDefaults().grab(true, true).span(1, 2).indent(0, 10).applyTo(section);
        
        final Composite panel = createPanel(section);

        createDocumentationControl(panel);
        createViewer(panel);

        setInput();
        viewer.packFirstColumn();
    }

    private Composite createPanel(final Section section) {
        final Composite panel = getManagedForm().getToolkit().createComposite(section);
        GridDataFactory.fillDefaults().indent(0, 0).applyTo(panel);
        GridLayoutFactory.fillDefaults().applyTo(panel);
        section.setClient(panel);
        return panel;
    }

    private void createDocumentationControl(final Composite panel) {
        documentation = getManagedForm().getToolkit().createText(panel, "", SWT.MULTI);
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
        documentation.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(final FocusEvent e) {
                final String newDocumentation = documentation.getText();
                final RobotSetting currentSetting = model.getDocumentationSetting();

                if (currentSetting == null && !newDocumentation.isEmpty()) {
                    commandsStack.execute(new CreateSettingKeywordCall(model.getSection(), "Documentation", newDocumentation));
                } else if (currentSetting != null && newDocumentation.isEmpty()) {
                    commandsStack.execute(new DeleteSettingKeywordCall(newArrayList(currentSetting)));
                } else if (currentSetting != null) {
                    commandsStack.execute(new SetArgumentOfSettingKeywordCall(currentSetting, 0, newDocumentation));
                }
            }
        });

        GridDataFactory.fillDefaults().grab(true, false).hint(SWT.DEFAULT, 120).applyTo(documentation);
    }

    private void createViewer(final Composite panel) {
        viewer = new RowExposingTableViewer(panel, SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(viewer.getTable());
        viewer.getTable().setLinesVisible(true);
        viewer.getTable().setHeaderVisible(true);
        viewer.getTable().addListener(SWT.MeasureItem, new Listener() {

            @Override
            public void handleEvent(final Event event) {
                event.height = event.gc.getFontMetrics().getHeight() * 2;
            }
        });
        viewer.setContentProvider(new GeneralSettingsContentProvider());
        TableCellsAcivationStrategy.addActivationStrategy(viewer, RowTabbingStrategy.MOVE_IN_CYCLE);
        ColumnViewerToolTipSupport.enableFor(viewer, ToolTip.NO_RECREATE);

        createColumns(true);
    }

    private void createColumns(final boolean createFirst) {
        if (createFirst) {
            createColumn(100, "Setting", new GeneralSettingsNamesLabelProvider(), null);
        }
        if (!model.areSettingsExist()) {
            return;
        }

        final int max = calcualateLongestArgumentsLength();
        for (int i = 0; i < max; i++) {
            createColumn(80, "", new GeneralSettingsArgsLabelProvider(i),
                    new GeneralSettingsArgsEditingSupport(viewer, i, commandsStack));
        }
        createColumn(100, "Comment", new SettingsCommentsLabelProvider(),
                new SettingsCommentsEditingSupport(viewer));

        final int newColumnsStartingPosition = max + 1;
        final TableViewerColumn addingColumn = createColumn(28, "", new ColumnAddingLabelProvider(),
                new ColumnAddingEditingSupport(viewer,
                newColumnsStartingPosition,
                new ColumnProviders() {
                    @Override
                    public void createColumn(final int index) {
                        GeneralSettingsFormPart.this.createColumn(80, "",
                                new GeneralSettingsArgsLabelProvider(index - 1), 
                                new GeneralSettingsArgsEditingSupport(viewer, index - 1, commandsStack));
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
        for (final Entry<String, RobotElement> entry : model.getEntries()) {
            final RobotSetting setting = (RobotSetting) entry.getValue();
            if (setting != null) {
                max = Math.max(max, setting.getArguments().size());
            }
        }
        return max;
    }

    private void setInput() {
        final com.google.common.base.Optional<RobotElement> settingsSection = fileModel
                .findSection(RobotSuiteSettingsSection.class);
        model.update(settingsSection);

        documentation.setEnabled(settingsSection.isPresent());
        documentation.setText(model.getDocumentation());

        viewer.setInput(model);
        viewer.removeColumns(1);
        createColumns(false);
    }

    @Override
    public void setFocus() {
        viewer.getTable().setFocus();
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
        if (section == model.getSection()) {
            setInput();
            markDirty();
        }
    }
    
    @Inject
    @Optional
    private void whenSettingDetailsChanges(
            @UIEventTopic(RobotModelEvents.ROBOT_SETTING_DETAIL_CHANGE_ALL) final RobotSetting setting) {
        if (setting.getSuiteFile() == fileModel) {
            setInput();
            markDirty();
        }
    }

    public void revealSetting(final RobotSetting setting) {
        if ("Documentation".equals(setting.getName())) {
            documentation.forceFocus();
            documentation.selectAll();
            viewer.setSelection(StructuredSelection.EMPTY);
        }
        for (final Entry<String, RobotElement> entry : model.getEntries()) {
            if (entry.getValue() == setting) {
                viewer.setSelection(new StructuredSelection(entry));
            }
        }
    }

    public void clearSettingsSelection() {
        viewer.setSelection(StructuredSelection.EMPTY);
    }
}

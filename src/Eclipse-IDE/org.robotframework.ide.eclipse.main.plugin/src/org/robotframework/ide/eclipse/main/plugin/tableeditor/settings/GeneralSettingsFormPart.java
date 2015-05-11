package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
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
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
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
import org.robotframework.ide.eclipse.main.plugin.cmd.SetSettingKeywordCallArgument;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorSources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableCellsAcivationStrategy;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableCellsAcivationStrategy.RowTabbingStrategy;

class GeneralSettingsFormPart extends AbstractFormPart {

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
        createContextMenu();

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
            documentation.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(final FocusEvent e) {
                    final String newDocumentation = documentation.getText();
                    final RobotSetting currentSetting = model.getDocumentationSetting();

                    if (currentSetting == null && !newDocumentation.isEmpty()) {
                        commandsStack.execute(new CreateSettingKeywordCall(model.getSection(), "Documentation",
                                newArrayList(newDocumentation)));
                    } else if (currentSetting != null && newDocumentation.isEmpty()) {
                        commandsStack.execute(new DeleteSettingKeywordCall(newArrayList(currentSetting)));
                    } else if (currentSetting != null) {
                        commandsStack.execute(new SetSettingKeywordCallArgument(currentSetting, 0, newDocumentation));
                    }
                }
            });
        }

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
            ViewerColumnsFactory.newColumn("Setting").withWidth(100)
                    .labelsProvidedBy(new GeneralSettingsNamesLabelProvider())
                    .createFor(viewer);
        }
        if (!model.areSettingsExist()) {
            return;
        }

        final int max = calcualateLongestArgumentsLength();
        for (int i = 0; i < max; i++) {
            ViewerColumnsFactory.newColumn("").withWidth(80)
                    .labelsProvidedBy(new GeneralSettingsArgsLabelProvider(i))
                    .editingSupportedBy(new GeneralSettingsArgsEditingSupport(viewer, i, commandsStack))
                    .editingEnabledOnlyWhen(fileModel.isEditable())
                    .createFor(viewer);
        }
        ViewerColumnsFactory.newColumn("Comment").withWidth(100)
                .labelsProvidedBy(new GeneralSettingsCommentsLabelProvider())
                .editingSupportedBy(new GeneralSettingsCommentsEditingSupport(viewer, commandsStack))
                .editingEnabledOnlyWhen(fileModel.isEditable())
                .createFor(viewer);

        final int newColumnsStartingPosition = max + 1;
        
        final ImageDescriptor addImage = fileModel.isEditable() ? RobotImages.getAddImage() : RobotImages
                .getGreyedImage(RobotImages.getAddImage());
        ViewerColumnsFactory.newColumn("").withWidth(28).resizable(false)
                .withTooltip("Activate cell in this column to add new arguments columns")
                .withImage(addImage.createImage())
                .labelsProvidedBy(new ColumnAddingLabelProvider())
                .editingSupportedBy(
                        new ColumnAddingEditingSupport(viewer, newColumnsStartingPosition, new ColumnProviders() {
                            @Override
                            public void createColumn(final int index) {
                                ViewerColumnsFactory.newColumn("").withWidth(80)
                                        .labelsProvidedBy(new GeneralSettingsArgsLabelProvider(index - 1))
                                        .editingSupportedBy(
                                                new GeneralSettingsArgsEditingSupport(viewer, index - 1, commandsStack))
                                        .editingEnabledOnlyWhen(fileModel.isEditable())
                                        .createFor(viewer);
                            }
                        }))
                .editingEnabledOnlyWhen(fileModel.isEditable())
                .createFor(viewer);
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

    private void createContextMenu() {
        final String menuId = "org.robotframework.ide.eclipse.editor.page.settings.general.contextMenu";

        final MenuManager manager = new MenuManager("Robot suite editor general settings context menu", menuId);
        final Table control = viewer.getTable();
        control.addMenuDetectListener(new MenuDetectListener() {

            @Override
            public void menuDetected(final MenuDetectEvent e) {
                e.doit = !isClickedOnHeader(e);
            }

            private boolean isClickedOnHeader(final MenuDetectEvent e) {
                return control.toControl(e.x, e.y).y <= control.getHeaderHeight();
            }
        });
        final Menu menu = manager.createContextMenu(control);
        control.setMenu(menu);
        site.registerContextMenu(menuId, manager, site.getSelectionProvider(), false);
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

        viewer.refresh();
    }

    @Override
    public void setFocus() {
        viewer.getTable().setFocus();
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
        if (setting.getSuiteFile() == fileModel && model.contains(setting)) {
            setInput();
            markDirty();
        }
    }
}

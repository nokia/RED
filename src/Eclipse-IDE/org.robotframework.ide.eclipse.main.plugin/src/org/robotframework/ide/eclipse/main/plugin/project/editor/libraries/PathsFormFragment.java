/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.tools.services.IDirtyProviderService;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.RowExposingTableViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerColumnsFactory;
import org.eclipse.jface.viewers.ViewersConfigurator;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Section;
import org.rf.ide.core.environment.IRuntimeEnvironment;
import org.rf.ide.core.environment.SuiteExecutor;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.RelativeTo;
import org.rf.ide.core.project.RobotProjectConfig.RelativityPoint;
import org.rf.ide.core.project.RobotProjectConfig.SearchPath;
import org.robotframework.ide.eclipse.main.plugin.project.RedProjectConfigEventData;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigEvents;
import org.robotframework.ide.eclipse.main.plugin.project.editor.Environments;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.PathsEditingSupport.SearchPathCreator;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.CellsActivationStrategy;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.CellsActivationStrategy.RowTabbingStrategy;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.HeaderFilterMatchesCollection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionFormFragment;
import org.robotframework.red.forms.RedFormToolkit;
import org.robotframework.red.viewers.Viewers;

/**
 * @author Michal Anglart
 */
class PathsFormFragment implements ISectionFormFragment {

    @Inject
    private IEditorSite site;

    @Inject
    private IEventBroker eventBroker;

    @Inject
    private RedFormToolkit toolkit;

    @Inject
    private IDirtyProviderService dirtyProviderService;

    @Inject
    private RedProjectEditorInput editorInput;

    private Combo relativityCombo;

    private RowExposingTableViewer pythonPathViewer;

    private RowExposingTableViewer classPathViewer;

    private ControlDecoration decoration;

    TableViewer getPythonPathViewer() {
        return pythonPathViewer;
    }

    TableViewer getClassPathViewer() {
        return classPathViewer;
    }

    @Override
    public void initialize(final Composite parent) {
        final Section section = createSection(parent);

        final ScrolledComposite scrolledParent = new ScrolledComposite(section, SWT.H_SCROLL | SWT.V_SCROLL);
        toolkit.adapt(scrolledParent);
        section.setClient(scrolledParent);

        final Composite internalComposite = toolkit.createComposite(scrolledParent);
        scrolledParent.setContent(internalComposite);
        scrolledParent.setExpandVertical(true);
        scrolledParent.setExpandHorizontal(true);

        GridDataFactory.fillDefaults().grab(true, true).applyTo(internalComposite);
        GridLayoutFactory.fillDefaults().extendedMargins(0, 0, 0, 5).applyTo(internalComposite);

        createRelativityCombo(internalComposite);

        pythonPathViewer = createPathViewer(internalComposite, new PythonPathViewerConfiguration());
        classPathViewer = createPathViewer(internalComposite, new ClassPathViewerConfiguration());
        scrolledParent.setMinSize(internalComposite.computeSize(-1, -1));

        setInput();
    }

    private Section createSection(final Composite parent) {
        final Section section = toolkit.createSection(parent,
                ExpandableComposite.EXPANDED | ExpandableComposite.TITLE_BAR | Section.DESCRIPTION);
        section.setText("Paths");
        section.setDescription(
                "Specify additional paths which will be added to respective "
                        + "environment variables when communicating with Robot Framework");
        GridDataFactory.fillDefaults().grab(true, true).applyTo(section);
        return section;
    }

    private void createRelativityCombo(final Composite sectionInternal) {
        final Label label = toolkit.createLabel(sectionInternal,
                "Relative paths defined below are relative to:", SWT.WRAP);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(label);
        relativityCombo = new Combo(sectionInternal, SWT.DROP_DOWN | SWT.READ_ONLY);
        relativityCombo.setItems(new String[] { RelativeTo.WORKSPACE.toString(), RelativeTo.PROJECT.toString() });
        GridDataFactory.fillDefaults().applyTo(relativityCombo);

        relativityCombo.addModifyListener(e -> {
            final RobotProjectConfig config = editorInput.getProjectConfiguration();

            final RelativeTo oldRelativeTo = config.getRelativityPoint().getRelativeTo();
            final RelativeTo newRelativeTo = RelativeTo
                    .valueOf(relativityCombo.getItem(relativityCombo.getSelectionIndex()));

            if (oldRelativeTo != newRelativeTo) {
                setDirty(true);
                config.setRelativityPoint(RelativityPoint.create(newRelativeTo));
            }
        });
    }

    private RowExposingTableViewer createPathViewer(final Composite parent, final ViewerConfiguration config) {
        final RowExposingTableViewer viewer = createViewer(parent, config);
        createColumns(viewer, config);
        createContextMenu(viewer, config);
        return viewer;
    }

    private RowExposingTableViewer createViewer(final Composite parent, final ViewerConfiguration config) {
        final RowExposingTableViewer viewer = new RowExposingTableViewer(parent,
                SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        CellsActivationStrategy.addActivationStrategy(viewer, RowTabbingStrategy.MOVE_TO_NEXT);
        ColumnViewerToolTipSupport.enableFor(viewer, ToolTip.NO_RECREATE);

        GridDataFactory.fillDefaults().grab(true, true).minSize(100, 100).applyTo(viewer.getTable());
        viewer.setUseHashlookup(true);
        viewer.getTable().setEnabled(false);
        viewer.getTable().setLinesVisible(true);
        viewer.getTable().setHeaderVisible(true);

        viewer.setContentProvider(new PathsContentProvider(config.getVariableName(), editorInput.isEditable()));

        ViewersConfigurator.enableDeselectionPossibility(viewer);
        ViewersConfigurator.disableContextMenuOnHeader(viewer);
        Viewers.boundViewerWithContext(viewer, site, config.getContextId());
        return viewer;
    }

    private void createColumns(final RowExposingTableViewer viewer, final ViewerConfiguration config) {
        ViewerColumnsFactory.newColumn(config.getVariableName())
                .withWidth(300)
                .labelsProvidedBy(new PathsLabelProvider(config.getVariableName(), editorInput))
                .editingEnabledOnlyWhen(editorInput.isEditable())
                .editingSupportedBy(config.getEditingSupport(viewer))
                .createFor(viewer);
    }

    private void createContextMenu(final RowExposingTableViewer viewer, final ViewerConfiguration config) {
        final String menuId = config.getMenuId();

        final MenuManager manager = new MenuManager(config.getMenuText(), menuId);
        final Table control = viewer.getTable();
        final Menu menu = manager.createContextMenu(control);
        control.setMenu(menu);
        site.registerContextMenu(menuId, manager, viewer, false);
    }

    @Override
    public void setFocus() {
        pythonPathViewer.getControl().setFocus();
    }

    private void setDirty(final boolean isDirty) {
        dirtyProviderService.setDirtyState(isDirty);
    }

    @Override
    public HeaderFilterMatchesCollection collectMatches(final String filter) {
        return null;
    }

    private void setInput() {
        final RobotProjectConfig configuration = editorInput.getProjectConfiguration();
        pythonPathViewer.setInput(configuration.getPythonPaths());
        classPathViewer.setInput(configuration.getClassPaths());

        final int indexToSelect = newArrayList(relativityCombo.getItems())
                .indexOf(configuration.getRelativityPoint().getRelativeTo().toString());
        relativityCombo.select(indexToSelect);

        adjustColumnWidth(pythonPathViewer.getTable());
        adjustColumnWidth(classPathViewer.getTable());
    }

    private void adjustColumnWidth(final Table table) {
        final int totalTableWidth = table.getSize().x;

        final TableColumn column = table.getColumn(0);
        column.pack();
        final int columnWidth = column.getWidth();
        column.setWidth(columnWidth + 10);

        final int scrollbarWidth = getScrollBarWidth(table);
        final int borderWidth = table.getBorderWidth();
        final int widthToOccupy = totalTableWidth - (scrollbarWidth + 2 * borderWidth);
        column.setWidth(Math.max(columnWidth + 10, widthToOccupy));
    }

    private int getScrollBarWidth(final Table table) {
        final ScrollBar verticalBar = table.getVerticalBar();
        return verticalBar == null || !verticalBar.isVisible() ? 0 : verticalBar.getSize().x;
    }

    @Inject
    @Optional
    private void whenMarkerChanged(
            @UIEventTopic(RobotProjectConfigEvents.ROBOT_CONFIG_MARKER_CHANGED) final RedProjectConfigEventData<RobotProjectConfig> eventData) {
        if (eventData.isApplicable(editorInput.getRobotProject())) {
            setInput();
        }
    }

    @Inject
    @Optional
    private void whenEnvironmentLoadingStarted(
            @UIEventTopic(RobotProjectConfigEvents.ROBOT_CONFIG_ENV_LOADING_STARTED) final RedProjectConfigEventData<RobotProjectConfig> eventData) {
        if (eventData.isApplicable(editorInput.getRobotProject())) {
            relativityCombo.setEnabled(false);
            pythonPathViewer.getTable().setEnabled(false);
            classPathViewer.getTable().setEnabled(false);
        }
    }

    @Inject
    @Optional
    private void whenEnvironmentsWereLoaded(
            @UIEventTopic(RobotProjectConfigEvents.ROBOT_CONFIG_ENV_LOADED) final RedProjectConfigEventData<Environments> eventData) {
        if (!eventData.isApplicable(editorInput.getRobotProject())) {
            return;
        }

        final boolean isEditable = editorInput.isEditable();
        final IRuntimeEnvironment environment = eventData.getChangedElement().getActiveEnvironment();
        final boolean projectMayBeInterpretedByJython = environment.isNullEnvironment()
                || environment.getInterpreter() == SuiteExecutor.Jython;

        relativityCombo.setEnabled(isEditable);
        pythonPathViewer.getTable().setEnabled(isEditable);
        classPathViewer.getTable().setEnabled(isEditable && projectMayBeInterpretedByJython);

        if (!projectMayBeInterpretedByJython) {
            final String interpreter = environment.isValidPythonInstallation() ? environment.getInterpreter().name()
                    : environment.getVersion();
            decoration = new ControlDecoration(classPathViewer.getTable(), SWT.LEFT | SWT.TOP);
            decoration.setDescriptionText("Project is configured to use " + interpreter
                    + " interpreter, but Jython is needed for CLASSPATH entries.");
            decoration.setImage(FieldDecorationRegistry.getDefault()
                    .getFieldDecoration(FieldDecorationRegistry.DEC_INFORMATION)
                    .getImage());
        } else if (decoration != null) {
            decoration.dispose();
            decoration = null;
        }
    }

    @Inject
    @Optional
    private void whenPythonPathChanged(
            @UIEventTopic(RobotProjectConfigEvents.ROBOT_CONFIG_PYTHONPATH_CHANGED) final RedProjectConfigEventData<SearchPath> eventData) {
        if (eventData.isApplicable(editorInput.getRobotProject())) {
            setDirty(true);
            pythonPathViewer.refresh();
            adjustColumnWidth(pythonPathViewer.getTable());
        }
    }

    @Inject
    @Optional
    private void whenPythonPathStructureChanged(
            @UIEventTopic(RobotProjectConfigEvents.ROBOT_CONFIG_PYTHONPATH_STRUCTURE_CHANGED) final RedProjectConfigEventData<List<SearchPath>> eventData) {
        if (eventData.isApplicable(editorInput.getRobotProject())) {
            setInput();
            setDirty(true);
            pythonPathViewer.refresh();
            adjustColumnWidth(pythonPathViewer.getTable());
        }
    }

    @Inject
    @Optional
    private void whenClassPathChanged(
            @UIEventTopic(RobotProjectConfigEvents.ROBOT_CONFIG_CLASSPATH_CHANGED) final RedProjectConfigEventData<SearchPath> eventData) {
        if (eventData.isApplicable(editorInput.getRobotProject())) {
            setDirty(true);
            classPathViewer.refresh();
            adjustColumnWidth(classPathViewer.getTable());
        }
    }

    @Inject
    @Optional
    private void whenClassPathStructureChanged(
            @UIEventTopic(RobotProjectConfigEvents.ROBOT_CONFIG_CLASSPATH_STRUCTURE_CHANGED) final RedProjectConfigEventData<List<SearchPath>> eventData) {
        if (eventData.isApplicable(editorInput.getRobotProject())) {
            setInput();
            setDirty(true);
            classPathViewer.refresh();
            adjustColumnWidth(classPathViewer.getTable());
        }
    }

    private interface ViewerConfiguration {

        String getContextId();

        String getVariableName();

        String getMenuText();

        String getMenuId();

        EditingSupport getEditingSupport(RowExposingTableViewer viewer);
    }

    private class PythonPathViewerConfiguration implements ViewerConfiguration {

        @Override
        public String getContextId() {
            return "org.robotframework.ide.eclipse.redxmleditor.pythonpath.context";
        }

        @Override
        public String getVariableName() {
            return "PYTHONPATH";
        }

        @Override
        public String getMenuText() {
            return "Red.xml file editor pythonpath context menu";
        }

        @Override
        public String getMenuId() {
            return "org.robotframework.ide.eclipse.redxmleditor.pythonpath.contextMenu";
        }

        @Override
        public EditingSupport getEditingSupport(final RowExposingTableViewer viewer) {
            final SearchPathCreator elementsCreator = new SearchPathCreator(viewer.getTable().getShell(),
                    path -> editorInput.getProjectConfiguration().addPythonPath(path),
                    paths -> eventBroker.send(RobotProjectConfigEvents.ROBOT_CONFIG_PYTHONPATH_STRUCTURE_CHANGED,
                            new RedProjectConfigEventData<>(editorInput.getFile(), paths)));
            return new PathsEditingSupport(viewer, elementsCreator,
                    path -> eventBroker.send(RobotProjectConfigEvents.ROBOT_CONFIG_PYTHONPATH_CHANGED,
                            new RedProjectConfigEventData<>(editorInput.getFile(), path)));
        }
    }

    private class ClassPathViewerConfiguration implements ViewerConfiguration {

        @Override
        public String getContextId() {
            return "org.robotframework.ide.eclipse.redxmleditor.classpath.context";
        }

        @Override
        public String getVariableName() {
            return "CLASSPATH";
        }

        @Override
        public String getMenuText() {
            return "Red.xml file editor classpath context menu";
        }

        @Override
        public String getMenuId() {
            return "org.robotframework.ide.eclipse.redxmleditor.classpath.contextMenu";
        }

        @Override
        public EditingSupport getEditingSupport(final RowExposingTableViewer viewer) {
            final SearchPathCreator elementsCreator = new SearchPathCreator(viewer.getTable().getShell(),
                    path -> editorInput.getProjectConfiguration().addClassPath(path),
                    paths -> eventBroker.send(RobotProjectConfigEvents.ROBOT_CONFIG_CLASSPATH_STRUCTURE_CHANGED,
                            new RedProjectConfigEventData<>(editorInput.getFile(), paths)));
            return new PathsEditingSupport(viewer, elementsCreator,
                    path -> eventBroker.send(RobotProjectConfigEvents.ROBOT_CONFIG_CLASSPATH_CHANGED,
                            new RedProjectConfigEventData<>(editorInput.getFile(), path)));
        }
    }
}

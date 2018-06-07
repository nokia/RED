/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;
import java.util.function.Supplier;

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
import org.eclipse.jface.viewers.RowExposingTableViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerColumnsFactory;
import org.eclipse.jface.viewers.ViewersConfigurator;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.executor.SuiteExecutor;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.RelativeTo;
import org.rf.ide.core.project.RobotProjectConfig.RelativityPoint;
import org.rf.ide.core.project.RobotProjectConfig.SearchPath;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigEvents;
import org.robotframework.ide.eclipse.main.plugin.project.editor.Environments;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.CellsActivationStrategy;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.CellsActivationStrategy.RowTabbingStrategy;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.HeaderFilterMatchesCollection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionFormFragment;
import org.robotframework.red.forms.RedFormToolkit;
import org.robotframework.red.viewers.Viewers;


/**
 * @author Michal Anglart
 *
 */
class PathsFormFragment implements ISectionFormFragment {

    private static final String PYTHONPATH_CONTEXT_ID = "org.robotframework.ide.eclipse.redxmleditor.pythonpath.context";

    private static final String CLASSPATH_CONTEXT_ID = "org.robotframework.ide.eclipse.redxmleditor.classpath.context";

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
        GridLayoutFactory.fillDefaults().extendedMargins(0, 10, 0, 5).applyTo(internalComposite);

        createRelativityCombo(internalComposite);

        pythonPathViewer = createPathViewer(internalComposite, new PythonPathViewerConfiguration());
        classPathViewer = createPathViewer(internalComposite, new ClassPathViewerConfiguration());
        scrolledParent.setMinSize(internalComposite.computeSize(-1, -1));
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
        GridDataFactory.fillDefaults().grab(true, false).indent(0, 0).applyTo(label);
        relativityCombo = new Combo(sectionInternal, SWT.DROP_DOWN | SWT.READ_ONLY);
        relativityCombo.setItems(new String[] { RelativeTo.WORKSPACE.toString(), RelativeTo.PROJECT.toString() });
        GridDataFactory.fillDefaults().indent(10, 0).applyTo(relativityCombo);

        relativityCombo.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                final RobotProjectConfig config = editorInput.getProjectConfiguration();

                final RelativeTo oldRelativeTo = config.getRelativityPoint().getRelativeTo();
                final RelativeTo newRelativeTo = RelativeTo
                        .valueOf(relativityCombo.getItem(relativityCombo.getSelectionIndex()));

                if (oldRelativeTo != newRelativeTo) {
                    setDirty(true);
                    config.setRelativityPoint(RelativityPoint.create(newRelativeTo));
                }
            }
        });
    }

    private RowExposingTableViewer createPathViewer(final Composite parent, final ViewerConfiguration config) {
        toolkit.createFormText(parent, false)
                .setText("<form><li>" + config.getVariableName() + "</li></form>", true, false);
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

        GridDataFactory.fillDefaults()
                .grab(true, true)
                .indent(10, 0)
                .minSize(100, 100)
                .applyTo(viewer.getTable());
        viewer.setUseHashlookup(true);
        viewer.getTable().setEnabled(false);
        viewer.getTable().setLinesVisible(false);
        viewer.getTable().setHeaderVisible(false);

        viewer.setContentProvider(new PathsContentProvider(config.getVariableName(), editorInput.isEditable()));

        ViewersConfigurator.enableDeselectionPossibility(viewer);
        ViewersConfigurator.disableContextMenuOnHeader(viewer);
        Viewers.boundViewerWithContext(viewer, site, config.getContextId());
        return viewer;
    }

    private void createColumns(final TableViewer viewer, final ViewerConfiguration config) {
        ViewerColumnsFactory.newColumn("")
                .withWidth(300)
                .labelsProvidedBy(new PathsLabelProvider(config.getVariableName(), editorInput))
                .editingEnabledOnlyWhen(editorInput.isEditable())
                .editingSupportedBy(new PathsEditingSupport(viewer, elementsCreator(config.getPathAddingStrategy()),
                        eventBroker, config.getPathModificationTopic()))
                .createFor(viewer);
    }

    private Supplier<SearchPath> elementsCreator(final PathAdder adder) {
        return () -> {
            final PathEntryDialog dialog = new PathEntryDialog(site.getShell());
            if (dialog.open() == Window.OK) {
                final List<SearchPath> paths = dialog.getSearchPath();
                if (paths.isEmpty()) {
                    return null;
                }
                boolean added = false;
                for (final SearchPath path : paths) {
                    added |= adder.addPath(path);
                }
                if (added) {
                    setDirty(true);
                }
                return paths.get(paths.size() - 1);
            }
            return null;
        };
    }

    private void createContextMenu(final TableViewer viewer, final ViewerConfiguration config) {
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
        pythonPathViewer.setInput(configuration.getPythonPath());
        classPathViewer.setInput(configuration.getClassPath());

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
    private void whenEnvironmentLoadingStarted(
            @UIEventTopic(RobotProjectConfigEvents.ROBOT_CONFIG_ENV_LOADING_STARTED) final RobotProjectConfig config) {
        setInput();

        relativityCombo.setEnabled(false);
        pythonPathViewer.getTable().setEnabled(false);
        classPathViewer.getTable().setEnabled(false);
    }

    @Inject
    @Optional
    private void whenEnvironmentsWereLoaded(
            @UIEventTopic(RobotProjectConfigEvents.ROBOT_CONFIG_ENV_LOADED) final Environments envs) {
        final boolean isEditable = editorInput.isEditable();
        final boolean projectMayBeInterpretedByJython = envs.getActiveEnvironment() == null
                || envs.getActiveEnvironment().getInterpreter() == SuiteExecutor.Jython;

        relativityCombo.setEnabled(isEditable);
        pythonPathViewer.getTable().setEnabled(isEditable);
        classPathViewer.getTable().setEnabled(isEditable && projectMayBeInterpretedByJython);

        if (!projectMayBeInterpretedByJython) {
            final String envName = java.util.Optional.ofNullable(envs.getActiveEnvironment())
                    .map(RobotRuntimeEnvironment::getInterpreter)
                    .map(SuiteExecutor::toString)
                    .orElse("<unknown>");
            decoration = new ControlDecoration(classPathViewer.getTable(), SWT.LEFT | SWT.TOP);
            decoration.setDescriptionText("Project is configured to use " + envName
                    + " interpreter, but Jython is needed to use CLASSPATH entries.");
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
            @UIEventTopic(RobotProjectConfigEvents.ROBOT_CONFIG_PYTHONPATH_CHANGED) final SearchPath newPath) {
        if (editorInput.getProjectConfiguration().getPythonPath().contains(newPath)) {
            setDirty(true);
            pythonPathViewer.refresh();
            adjustColumnWidth(pythonPathViewer.getTable());
        }
    }

    @Inject
    @Optional
    private void whenPythonPathStructureChanged(
            @UIEventTopic(RobotProjectConfigEvents.ROBOT_CONFIG_PYTHONPATH_STRUCTURE_CHANGED) final List<SearchPath> affectedPaths) {
        if (editorInput.getProjectConfiguration().getPythonPath() == affectedPaths) {
            setDirty(true);
            pythonPathViewer.refresh();
            adjustColumnWidth(pythonPathViewer.getTable());
        }
    }

    @Inject
    @Optional
    private void whenClassPathChanged(
            @UIEventTopic(RobotProjectConfigEvents.ROBOT_CONFIG_CLASSPATH_CHANGED) final SearchPath newPath) {
        if (editorInput.getProjectConfiguration().getClassPath().contains(newPath)) {
            setDirty(true);
            classPathViewer.refresh();
            adjustColumnWidth(classPathViewer.getTable());
        }
    }

    @Inject
    @Optional
    private void whenClassPathStructureChanged(
            @UIEventTopic(RobotProjectConfigEvents.ROBOT_CONFIG_CLASSPATH_STRUCTURE_CHANGED) final List<SearchPath> affectedPaths) {
        if (editorInput.getProjectConfiguration().getClassPath() == affectedPaths) {
            setDirty(true);
            classPathViewer.refresh();
            adjustColumnWidth(classPathViewer.getTable());
        }
    }

    private interface ViewerConfiguration {

        String getContextId();

        String getMenuText();

        String getMenuId();

        String getVariableName();

        String getPathModificationTopic();

        PathAdder getPathAddingStrategy();
    }

    private class PythonPathViewerConfiguration implements ViewerConfiguration {

        @Override
        public String getContextId() {
            return PYTHONPATH_CONTEXT_ID;
        }

        @Override
        public String getVariableName() {
            return "PYTHONPATH";
        }

        @Override
        public String getPathModificationTopic() {
            return RobotProjectConfigEvents.ROBOT_CONFIG_PYTHONPATH_CHANGED;
        }

        @Override
        public PathAdder getPathAddingStrategy() {
            return path -> editorInput.getProjectConfiguration().addPythonPath(path);
        }

        @Override
        public String getMenuText() {
            return "Red.xml file editor pythonpath context menu";
        }

        @Override
        public String getMenuId() {
            return "org.robotframework.ide.eclipse.redxmleditor.pythonpath.contextMenu";
        }
    }

    private class ClassPathViewerConfiguration implements ViewerConfiguration {

        @Override
        public String getContextId() {
            return CLASSPATH_CONTEXT_ID;
        }

        @Override
        public String getVariableName() {
            return "CLASSPATH";
        }

        @Override
        public String getPathModificationTopic() {
            return RobotProjectConfigEvents.ROBOT_CONFIG_CLASSPATH_CHANGED;
        }

        @Override
        public PathAdder getPathAddingStrategy() {
            return path -> editorInput.getProjectConfiguration().addClassPath(path);
        }

        @Override
        public String getMenuText() {
            return "Red.xml file editor classpath context menu";
        }

        @Override
        public String getMenuId() {
            return "org.robotframework.ide.eclipse.redxmleditor.pythonpath.contextMenu";
        }
    }

    private interface PathAdder {

        boolean addPath(SearchPath path);
    }
}

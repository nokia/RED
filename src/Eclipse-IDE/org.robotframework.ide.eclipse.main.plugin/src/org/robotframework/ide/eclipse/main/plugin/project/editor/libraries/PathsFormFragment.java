/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

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
import org.eclipse.jface.viewers.RowExposingTableViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerColumnsFactory;
import org.eclipse.jface.viewers.ViewersConfigurator;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Section;
import org.rf.ide.core.executor.SuiteExecutor;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.SearchPath;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigEvents;
import org.robotframework.ide.eclipse.main.plugin.project.editor.Environments;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.CellsActivationStrategy;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.CellsActivationStrategy.RowTabbingStrategy;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionFormFragment;
import org.robotframework.red.forms.RedFormToolkit;
import org.robotframework.red.viewers.ElementsAddingEditingSupport.NewElementsCreator;
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

        final Composite internalComposite = toolkit.createComposite(section);
        section.setClient(internalComposite);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(internalComposite);
        GridLayoutFactory.fillDefaults().margins(0, 5).applyTo(internalComposite);

        final ViewerConfiguration pythonConfig = new PythonPathViewerConfiguration();
        toolkit.createFormText(internalComposite, false)
                .setText("<form><li>" + pythonConfig.getVariableName() + "</li></form>", true, false);
        pythonPathViewer = createViewer(internalComposite, pythonConfig);
        createColumns(pythonPathViewer, pythonConfig);
        createContextMenu(pythonPathViewer, pythonConfig);

        final ViewerConfiguration classConfig = new ClassPathViewerConfiguration();
        toolkit.createFormText(internalComposite, false)
                .setText("<form><li>" + classConfig.getVariableName() + "</li></form>", true, false);
        classPathViewer = createViewer(internalComposite, classConfig);
        createColumns(classPathViewer, classConfig);
        createContextMenu(classPathViewer, classConfig);
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

    private RowExposingTableViewer createViewer(final Composite parent, final ViewerConfiguration config) {
        final RowExposingTableViewer viewer = new RowExposingTableViewer(parent,
                SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        CellsActivationStrategy.addActivationStrategy(viewer, RowTabbingStrategy.MOVE_TO_NEXT);
        ColumnViewerToolTipSupport.enableFor(viewer, ToolTip.NO_RECREATE);

        GridDataFactory.fillDefaults().grab(true, true).indent(10, 0).applyTo(viewer.getTable());
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
                .withWidth(200)
                .withMinWidth(200)
                .shouldGrabAllTheSpaceLeft(true)
                .labelsProvidedBy(new PathsLabelProvider(config.getVariableName()))
                .editingEnabledOnlyWhen(editorInput.isEditable())
                .editingSupportedBy(new PathsEditingSupport(viewer, elementsCreator(config.getPathAddingStrategy()),
                        eventBroker, config.getPathModifcationTopic()))
                .createFor(viewer);
    }

    private NewElementsCreator<SearchPath> elementsCreator(final PathAdder adder) {
        return new NewElementsCreator<RobotProjectConfig.SearchPath>() {

            @Override
            public SearchPath createNew() {
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
            }
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
    public MatchesCollection collectMatches(final String filter) {
        return null;
    }

    private void setInput(final RobotProjectConfig config) {
        pythonPathViewer.setInput(config.getPythonPath());
        classPathViewer.setInput(config.getClassPath());
    }

    @Inject
    @Optional
    private void whenEnvironmentLoadingStarted(
            @UIEventTopic(RobotProjectConfigEvents.ROBOT_CONFIG_ENV_LOADING_STARTED) final RobotProjectConfig config) {
        setInput(config);

        pythonPathViewer.getTable().setEnabled(false);
        classPathViewer.getTable().setEnabled(false);
    }

    @Inject
    @Optional
    private void whenEnvironmentsWereLoaded(
            @UIEventTopic(RobotProjectConfigEvents.ROBOT_CONFIG_ENV_LOADED) final Environments envs) {
        final boolean isEditable = editorInput.isEditable();
        final boolean projectIsInterpretedByJython = envs.getActiveEnvironment()
                .getInterpreter() == SuiteExecutor.Jython;

        pythonPathViewer.getTable().setEnabled(isEditable);
        classPathViewer.getTable().setEnabled(isEditable && projectIsInterpretedByJython);

        if (!projectIsInterpretedByJython) {
            decoration = new ControlDecoration(classPathViewer.getTable(), SWT.LEFT | SWT.TOP);
            decoration.setDescriptionText(
                    "Project is configured to use " + envs.getActiveEnvironment().getInterpreter().toString()
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
        }
    }

    @Inject
    @Optional
    private void whenPythonPathStructureChanged(
            @UIEventTopic(RobotProjectConfigEvents.ROBOT_CONFIG_PYTHONPATH_STRUCTURE_CHANGED) final List<SearchPath> affectedPaths) {
        if (editorInput.getProjectConfiguration().getPythonPath() == affectedPaths) {
            setDirty(true);
            pythonPathViewer.refresh();
        }
    }

    @Inject
    @Optional
    private void whenClassPathChanged(
            @UIEventTopic(RobotProjectConfigEvents.ROBOT_CONFIG_CLASSPATH_CHANGED) final SearchPath newPath) {
        if (editorInput.getProjectConfiguration().getClassPath().contains(newPath)) {
            setDirty(true);
            classPathViewer.refresh();
        }
    }

    @Inject
    @Optional
    private void whenClassPathStructureChanged(
            @UIEventTopic(RobotProjectConfigEvents.ROBOT_CONFIG_CLASSPATH_STRUCTURE_CHANGED) final List<SearchPath> affectedPaths) {
        if (editorInput.getProjectConfiguration().getClassPath() == affectedPaths) {
            setDirty(true);
            classPathViewer.refresh();
        }
    }

    private interface ViewerConfiguration {

        String getContextId();

        String getMenuText();

        String getMenuId();

        String getVariableName();

        String getPathModifcationTopic();

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
        public String getPathModifcationTopic() {
            return RobotProjectConfigEvents.ROBOT_CONFIG_PYTHONPATH_CHANGED;
        }

        @Override
        public PathAdder getPathAddingStrategy() {
            return new PathAdder() {
                @Override
                public boolean addPath(final SearchPath path) {
                    return editorInput.getProjectConfiguration().addPythonPath(path);
                }
            };
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
        public String getPathModifcationTopic() {
            return RobotProjectConfigEvents.ROBOT_CONFIG_CLASSPATH_CHANGED;
        }

        @Override
        public PathAdder getPathAddingStrategy() {
            return new PathAdder() {
                @Override
                public boolean addPath(final SearchPath path) {
                    return editorInput.getProjectConfiguration().addClassPath(path);
                }
            };
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

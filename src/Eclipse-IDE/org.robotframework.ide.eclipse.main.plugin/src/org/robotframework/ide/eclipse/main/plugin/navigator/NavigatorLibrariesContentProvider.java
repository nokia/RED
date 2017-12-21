/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.navigator;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.rf.ide.core.project.RobotProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.red.viewers.TreeContentProvider;

public class NavigatorLibrariesContentProvider extends TreeContentProvider {

    protected TreeViewer viewer;

    private final IResourceChangeListener listener;

    public NavigatorLibrariesContentProvider() {
        final IEclipseContext activeContext = getContext().getActiveLeaf();
        ContextInjectionFactory.inject(this, activeContext);
        
        listener = new IResourceChangeListener() {
            @Override
            public void resourceChanged(final IResourceChangeEvent event) {
                if (event.getType() == IResourceChangeEvent.POST_BUILD) {
                    final AtomicBoolean shouldRefresh = new AtomicBoolean(false);
                    try {
                        event.getDelta().accept(new IResourceDeltaVisitor() {

                            @Override
                            public boolean visit(final IResourceDelta delta) throws CoreException {
                                if (delta.getFlags() != 0 && delta.getFlags() != IResourceDelta.MARKERS) {
                                    shouldRefresh.set(true);
                                }
                                return true;
                            }
                        });
                    } catch (final CoreException e) {
                        // nothing to do
                    }
                    if (shouldRefresh.get()) {
                        refreshViewer();
                    }
                } else if (event.getType() == IResourceChangeEvent.POST_CHANGE && event.getDelta() != null) {
                    try {
                        event.getDelta().accept(new IResourceDeltaVisitor() {
                            @Override
                            public boolean visit(final IResourceDelta delta) throws CoreException {
                                if (delta.getResource().getName().equals(RobotProjectConfig.FILENAME)) {
                                    refreshViewer();
                                    return false;
                                }
                                return true;
                            }
                        });
                    } catch (final CoreException e) {
                        // nothing to do
                    }
                }
            }

            private void refreshViewer() {
                Display.getDefault().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        if (viewer != null && !viewer.getControl().isDisposed()) {
                            viewer.refresh();
                        }
                    }
                });
            }
        };
        ResourcesPlugin.getWorkspace().addResourceChangeListener(listener,
                IResourceChangeEvent.POST_BUILD | IResourceChangeEvent.POST_CHANGE);
    }
    
    private IEclipseContext getContext() {
        return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getService(IEclipseContext.class);
    }

    @Override
    public void dispose() {
        final IEclipseContext activeContext = getContext().getActiveLeaf();
        ContextInjectionFactory.uninject(this, activeContext);
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(listener);
    }

    @Override
    public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
        this.viewer = (TreeViewer) viewer;
    }

    @Override
    public Object[] getElements(final Object inputElement) {
        return null;
    }

    @Override
    public Object[] getChildren(final Object parentElement) {
        final IProject project = RedPlugin.getAdapter(parentElement, IProject.class);

        if (project != null) {
            final RobotProject robotProject = RedPlugin.getModelManager().getModel().createRobotProject(project);
            final List<RobotProjectDependencies> dependencies = newArrayList(
                    new RobotProjectDependencies(robotProject));
            if (robotProject.hasReferencedLibraries()) {
                dependencies.add(new RobotProjectExternalDependencies(robotProject));
            }
            return dependencies.toArray();
        } else if (parentElement instanceof RobotProjectDependencies) {
            return ((RobotProjectDependencies) parentElement).getLibraries().toArray();
        }
        return new Object[0];
    }

    @Override
    public Object getParent(final Object element) {
        return null;
    }

    @Override
    public boolean hasChildren(final Object element) {
        return element instanceof RobotProjectDependencies;
    }

    @Inject
    @Optional
    private void whenStructuralChangeWasMade(
            @UIEventTopic(RobotModelEvents.ROBOT_LIBRARY_SPECIFICATION_CHANGE) final IProject project) {
        if (viewer != null) {
            viewer.refresh(project);
        }
    }
}

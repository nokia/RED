/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigEvents;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigReader;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigWriter;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedXmlFileChangeListener.OnRedConfigFileChange;
import org.robotframework.ide.eclipse.main.plugin.project.editor.general.GeneralProjectConfigurationEditorPart;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.ReferencedLibrariesProjectConfigurationEditorPart;
import org.robotframework.ide.eclipse.main.plugin.project.editor.variables.VariablesProjectConfigurationEditorPart;

public class RedProjectEditor extends MultiPageEditorPart {

    public static final String ID = "org.robotframework.ide.project.editor";

    @Inject
    private IEventBroker eventBroker;

    private final List<IEditorPart> parts = new ArrayList<>();

    private RedProjectEditorInput editorInput;

    private IResourceChangeListener resourceListener;

    public RedProjectEditorInput getRedProjectEditorInput() {
        return editorInput;
    }

    @Override
    protected void setInput(final IEditorInput input) {
        if (input instanceof FileEditorInput) {
            final IFile file = ((FileEditorInput) input).getFile();
            setPartName(file.getProject().getName() + "/" + input.getName());

            editorInput = new RedProjectEditorInput(!file.isReadOnly(),
                    new RobotProjectConfigReader().readConfiguration(file), file.getProject());
            installResourceListener();
        } else {
            final IStorage storage = input.getAdapter(IStorage.class);
            if (storage != null) {
                setPartName(storage.getName() + " [" + storage.getFullPath() + "]");

                try {
                    editorInput = new RedProjectEditorInput(!storage.isReadOnly(),
                            new RobotProjectConfigReader().readConfiguration(storage.getContents()), null);
                } catch (final CoreException e) {
                    throw new IllegalProjectConfigurationEditorInputException(
                            "Unable to open editor: unrecognized input of class: " + input.getClass().getName(), e);
                }
            } else {
                throw new IllegalProjectConfigurationEditorInputException(
                        "Unable to open editor: unrecognized input of class: " + input.getClass().getName());
            }
        }
        super.setInput(input);
    }

    @Override
    protected void createPages() {
        try {
            final IEclipseContext context = prepareContext();

            addEditorPart(new GeneralProjectConfigurationEditorPart(), "General", context);
            addEditorPart(new ReferencedLibrariesProjectConfigurationEditorPart(), "Referenced libraries", context);
            addEditorPart(new VariablesProjectConfigurationEditorPart(), "Variable files", context);

            setupEnvironmentLoadingJob();
        } catch (final PartInitException e) {
            throw new EditorInitalizationException("Unable to initialize editor", e);
        }
    }

    private IEclipseContext prepareContext() {
        final IEclipseContext context = getEditorSite().getService(IEclipseContext.class).getActiveLeaf();
        context.set(RedProjectEditorInput.class, editorInput);
        context.set(IEditorSite.class, getEditorSite());
        context.set(RedProjectEditor.class, this);
        ContextInjectionFactory.inject(this, context);
        return context;
    }

    private void addEditorPart(final IEditorPart part, final String title, final IEclipseContext context)
            throws PartInitException {
        parts.add(part);
        ContextInjectionFactory.inject(part, context);
        setPageText(addPage(part, getEditorInput()), title);
    }

    private void installResourceListener() {
        final IProject project = editorInput.getRobotProject().getProject();
        resourceListener = new RedXmlFileChangeListener(project, new OnRedConfigFileChange() {

            @Override
            public void whenFileWasRemoved() {
                getSite().getPage().closeEditor(RedProjectEditor.this, false);
            }

            @Override
            public void whenFileChanged() {
                editorInput.refreshProjectConfiguration();
                setupEnvironmentLoadingJob();
            }
        });
        ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceListener, IResourceChangeEvent.POST_CHANGE);
    }

    private void setupEnvironmentLoadingJob() {
        final RobotProject project = editorInput.getRobotProject();
        final String activeEnv = "activeEnv";
        final String allEnvs = "allEnvs";

        final Job envLoadingJob = new Job("Reading available frameworks") {

            @Override
            protected IStatus run(final IProgressMonitor monitor) {
                eventBroker.send(RobotProjectConfigEvents.ROBOT_CONFIG_ENV_LOADING_STARTED,
                        editorInput.getProjectConfiguration());

                final RobotRuntimeEnvironment activeEnvironment = project == null ? null
                        : project.getRuntimeEnvironment();
                if (monitor.isCanceled()) {
                    return Status.CANCEL_STATUS;
                }
                final List<RobotRuntimeEnvironment> allRuntimeEnvironments = RedPlugin.getDefault()
                        .getAllRuntimeEnvironments();
                if (monitor.isCanceled()) {
                    return Status.CANCEL_STATUS;
                }
                setProperty(createKey(activeEnv), activeEnvironment);
                setProperty(createKey(allEnvs), allRuntimeEnvironments);
                return Status.OK_STATUS;
            }
        };
        envLoadingJob.addJobChangeListener(new JobChangeAdapter() {

            @SuppressWarnings("unchecked")
            @Override
            public void done(final IJobChangeEvent event) {
                final RobotRuntimeEnvironment env = (RobotRuntimeEnvironment) envLoadingJob
                        .getProperty(createKey(activeEnv));
                final List<RobotRuntimeEnvironment> allEnvironments = (List<RobotRuntimeEnvironment>) envLoadingJob
                        .getProperty(createKey(allEnvs));

                eventBroker.send(RobotProjectConfigEvents.ROBOT_CONFIG_ENV_LOADED,
                        new Environments(allEnvironments, env));
            }
        });
        envLoadingJob.schedule();
    }

    private QualifiedName createKey(final String localName) {
        return new QualifiedName(RedPlugin.PLUGIN_ID, localName);
    }

    @Override
    public void doSave(final IProgressMonitor monitor) {
        for (final IEditorPart dirtyEditor : getDirtyEditors()) {
            dirtyEditor.doSave(monitor);
        }
        final RobotProject project = editorInput.getRobotProject();
        project.clearAll();
        new RobotProjectConfigWriter().writeConfiguration(editorInput.getProjectConfiguration(), project);
    }

    private List<? extends IEditorPart> getDirtyEditors() {
        final List<IEditorPart> dirtyEditors = newArrayList();
        for (int i = 0; i < getPageCount(); i++) {
            final IEditorPart editorPart = getEditor(i);
            if (editorPart != null && editorPart.isDirty()) {
                dirtyEditors.add(editorPart);
            }
        }
        return dirtyEditors;
    }

    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    @Override
    public void doSaveAs() {
        // save as is not allowed
    }

    @Override
    public void dispose() {
        final IEclipseContext context = getEditorSite().getService(IEclipseContext.class).getActiveLeaf();
        for (final IEditorPart part : parts) {
            ContextInjectionFactory.uninject(part, context);
        }

        if (resourceListener != null) {
            ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceListener);
        }
        super.dispose();
    }

    private static class IllegalProjectConfigurationEditorInputException extends RuntimeException {
        public IllegalProjectConfigurationEditorInputException(final String message) {
            super(message);
        }

        public IllegalProjectConfigurationEditorInputException(final String message, final CoreException cause) {
            super(message, cause);
        }
    }

    private static class EditorInitalizationException extends RuntimeException {

        public EditorInitalizationException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }
}

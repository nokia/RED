/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.tools.compat.parts.DIEditorPart;
import org.eclipse.e4.tools.services.IDirtyProviderService;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.IMessageManager;
import org.eclipse.ui.forms.widgets.Form;
import org.robotframework.ide.core.executor.RobotRuntimeEnvironment;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectConfigurationEditorPart.ProjectConfigurationEditor;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectConfigurationFileChangeListener.OnRedConfigFileChange;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionFormFragment;
import org.robotframework.red.forms.RedFormToolkit;

class RedProjectConfigurationEditorPart extends DIEditorPart<ProjectConfigurationEditor> {

    public RedProjectConfigurationEditorPart() {
        super(ProjectConfigurationEditor.class);
    }

    static class ProjectConfigurationEditor {

        private IEclipseContext context;

        private RedFormToolkit toolkit;

        private List<? extends ISectionFormFragment> formFragments;

        private FrameworksSectionFormFragment frameworksFragment;

        private ReferencedLibrariesFormFragment referencedFragment;

        private RemoteLibraryLocationsFormFragment remoteFragment;
        
        private VariableFilesFormFragment variablesFragment;

        private Form form;

        @Inject
        private RedProjectEditorInput editorInput;

        @Inject
        private IEditorSite editorSite;

        @Inject
        private RedProjectEditor editor;

        @PostConstruct
        public final void postConstruct(final Composite parent, final IEditorPart editorPart) {
            adjustParentLayout(parent);
            toolkit = createToolkit(parent);

            final IEditorSite site = editorPart.getEditorSite();
            context = site.getService(IEclipseContext.class).getActiveLeaf();
            context.set(RedFormToolkit.class, toolkit);
            context.set(IDirtyProviderService.class, context.get(IDirtyProviderService.class));

            form = createForm(parent, editorPart.getTitleImage());
            context.set(IMessageManager.class, form.getMessageManager());

            formFragments = createFormFragments();
            injectToFormParts(context, formFragments);
            installResourceListener();
            for (final ISectionFormFragment part : formFragments) {
                part.initialize(form.getBody());
            }
            site.setSelectionProvider(getSelectionProvider());

            setupEnvironmentLoadingJob();
        }

        private void adjustParentLayout(final Composite parent) {
            final FillLayout parentLayout = (FillLayout) parent.getLayout();
            parentLayout.marginHeight = 0;
            parentLayout.marginWidth = 0;
        }

        private RedFormToolkit createToolkit(final Composite parent) {
            final RedFormToolkit toolkit = new RedFormToolkit(parent.getDisplay());
            parent.addDisposeListener(new DisposeListener() {
                @Override
                public void widgetDisposed(final DisposeEvent e) {
                    toolkit.dispose();
                }
            });
            return toolkit;
        }

        private List<? extends ISectionFormFragment> createFormFragments() {
            frameworksFragment = new FrameworksSectionFormFragment();
            referencedFragment = new ReferencedLibrariesFormFragment();
            remoteFragment = new RemoteLibraryLocationsFormFragment();
            variablesFragment = new VariableFilesFormFragment();
            return newArrayList(frameworksFragment, referencedFragment, remoteFragment, variablesFragment);
        }

        private void injectToFormParts(final IEclipseContext context,
                final List<? extends ISectionFormFragment> sectionForms) {
            for (final ISectionFormFragment part : sectionForms) {
                ContextInjectionFactory.inject(part, context);
            }
        }

        private Form createForm(final Composite parent, final Image image) {
            final Form form = toolkit.createForm(parent);
            form.setImage(image);
            form.setText("RED Project");
            toolkit.decorateFormHeading(form);

            GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(true).margins(3, 3).applyTo(form.getBody());
            return form;
        }

        private void installResourceListener() {
            final IProject project = editorInput.getRobotProject().getProject();
            final IResourceChangeListener listener = new RedProjectConfigurationFileChangeListener(project,
                    new OnRedConfigFileChange() {
                        @Override
                        public void whenFileWasRemoved() {
                            editorSite.getPage().closeEditor(editor, false);
                        }

                        @Override
                        public void whenFileChanged() {
                            whenConfigurationFiledChanged();
                            setupEnvironmentLoadingJob();
                        }
                    });
            ResourcesPlugin.getWorkspace().addResourceChangeListener(listener, IResourceChangeEvent.POST_CHANGE);
            form.addDisposeListener(new DisposeListener() {
                @Override
                public void widgetDisposed(final DisposeEvent e) {
                    ResourcesPlugin.getWorkspace().removeResourceChangeListener(listener);
                }
            });
        }

        protected ISelectionProvider getSelectionProvider() {
            return frameworksFragment.getViewer();
        }

        private void setupEnvironmentLoadingJob() {
            final RobotProject project = editorInput.getRobotProject();
            final String activeEnv = "activeEnv";
            final String allEnvs = "allEnvs";

            final Job job = new Job("Reading available frameworks") {
                @Override
                protected IStatus run(final IProgressMonitor monitor) {
                    final RobotRuntimeEnvironment activeEnvironment = project == null ? null : project
                            .getRuntimeEnvironment();
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
            job.addJobChangeListener(new JobChangeAdapter() {
                @Override
                public void done(final IJobChangeEvent event) {
                    final RobotRuntimeEnvironment env = (RobotRuntimeEnvironment) job.getProperty(createKey(activeEnv));
                    final List<?> allEnvironments = (List<?>) job.getProperty(createKey(allEnvs));

                    if (form != null && !form.isDisposed()) {
                        whenEnvironmentWasLoaded(env, allEnvironments);
                        form.setBusy(false);
                    }
                }
            });
            form.setBusy(true);
            form.addDisposeListener(new DisposeListener() {
                @Override
                public void widgetDisposed(final DisposeEvent e) {
                    job.cancel();
                }
            });
            job.schedule();
        }

        private QualifiedName createKey(final String localName) {
            return new QualifiedName(RedPlugin.PLUGIN_ID, localName);
        }

        private void whenConfigurationFiledChanged() {
            form.getDisplay().syncExec(new Runnable() {
                @Override
                public void run() {
                    frameworksFragment.whenConfigurationFiledChanged();
                    referencedFragment.whenConfigurationFiledChanged();
                    remoteFragment.whenConfigurationFiledChanged();
                    variablesFragment.whenConfigurationFiledChanged();
                }
            });
        }

        private void whenEnvironmentWasLoaded(final RobotRuntimeEnvironment env, final List<?> allEnvironments) {
            form.getDisplay().syncExec(new Runnable() {
                @Override
                public void run() {
                    frameworksFragment.whenEnvironmentWasLoaded(env, allEnvironments);
                    referencedFragment.whenEnvironmentWasLoaded(env);
                    remoteFragment.whenEnvironmentWasLoaded();
                    variablesFragment.whenEnvironmentWasLoaded();
                }
            });
        }

        @Focus
        public void setFocus() {
            if (!formFragments.isEmpty()) {
                formFragments.get(0).setFocus();
            }
        }

        @Persist
        public void onSave() {
            final IDirtyProviderService dirtyProviderService = context.getActive(IDirtyProviderService.class);
            dirtyProviderService.setDirtyState(false);

            for (final ISectionFormFragment fragment : formFragments) {
                ContextInjectionFactory.invoke(fragment, Persist.class, context, context, null);
            }
        }

        @PreDestroy
        public final void preDestroy() {
            for (final ISectionFormFragment fragment : formFragments) {
                ContextInjectionFactory.uninject(fragment, context);
            }
        }
    }
}

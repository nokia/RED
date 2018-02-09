/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.tools.services.IDirtyProviderService;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.IMessageManager;
import org.eclipse.ui.forms.widgets.Form;
import org.rf.ide.core.project.RobotProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionFormFragment;
import org.robotframework.red.forms.RedFormToolkit;
import org.robotframework.red.graphics.ImagesManager;

/**
 * @author Michal Anglart
 *
 */
public abstract class RedProjectEditorPage {

    private IEclipseContext context;

    private RedFormToolkit toolkit;

    private List<? extends ISectionFormFragment> formFragments;

    private Form form;

    @PostConstruct
    public final void postConstruct(final Composite parent, final IEditorPart editorPart) {
        adjustParentLayout(parent);
        toolkit = createToolkit(parent);

        final IEditorSite site = editorPart.getEditorSite();
        context = site.getService(IEclipseContext.class).getActiveLeaf();
        context.set(RedFormToolkit.class, toolkit);
        context.set(IDirtyProviderService.class, context.get(IDirtyProviderService.class));
        context.set(IEditorSite.class, editorPart.getEditorSite());

        form = createForm(parent, ImagesManager.getImage(RedImages.getRobotProjectConfigFile()));
        context.set(IMessageManager.class, form.getMessageManager());

        formFragments = createFormFragments();
        injectToFormParts(context, formFragments);
        for (final ISectionFormFragment part : formFragments) {
            part.initialize(form.getBody());
        }
        site.setSelectionProvider(getSelectionProvider());
    }

    private void adjustParentLayout(final Composite parent) {
        final FillLayout parentLayout = (FillLayout) parent.getLayout();
        parentLayout.marginHeight = 0;
        parentLayout.marginWidth = 0;
    }

    private RedFormToolkit createToolkit(final Composite parent) {
        final RedFormToolkit toolkit = new RedFormToolkit(parent.getDisplay());
        parent.addDisposeListener(e -> toolkit.dispose());
        return toolkit;
    }

    protected abstract List<? extends ISectionFormFragment> createFormFragments();

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

        GridLayoutFactory.fillDefaults().numColumns(getNumberOfColumnsInForm()).equalWidth(true).margins(3, 3).applyTo(form.getBody());
        return form;
    }

    protected int getNumberOfColumnsInForm() {
        return 2;
    }

    protected abstract ISelectionProvider getSelectionProvider();

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

    @Inject
    @Optional
    private void whenEnvironmentLoadingStarted(
            @UIEventTopic(RobotProjectConfigEvents.ROBOT_CONFIG_ENV_LOADING_STARTED) final RobotProjectConfig config) {
        form.setBusy(true);
    }

    @Inject
    @Optional
    private void whenEnvironmentsWereLoaded(
            @UIEventTopic(RobotProjectConfigEvents.ROBOT_CONFIG_ENV_LOADED) final Environments envs) {
        form.setBusy(false);
    }
}

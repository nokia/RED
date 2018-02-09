/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.tools.services.IDirtyProviderService;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.services.IServiceLocator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.CreateFreshSectionCommand;
import org.robotframework.red.forms.RedFormToolkit;

public abstract class SectionEditorPart implements ISectionEditorPart {

    private static final String SECTION_EDITOR_PART_CONTEXT_ID = "org.robotframework.ide.eclipse.tableeditor.tables.context";

    private final RobotEditorCommandsStack commandsStack = new RobotEditorCommandsStack();

    protected RedFormToolkit toolkit;

    @Inject
    protected IEventBroker eventBroker;

    @Inject
    @Named(RobotEditorSources.SUITE_FILE_MODEL)
    private RobotSuiteFile fileModel;

    private final List<CommandContributionItem> updateNeededContributions = new ArrayList<>();

    private Form form;

    private List<? extends ISectionFormFragment> formFragments;

    private IHyperlinkListener createSectionLinkListener;

    private IEclipseContext context;

    private HeaderFilterSupport filterSupport;

    protected final IEclipseContext getContext() {
        return context;
    }

    @PostConstruct
    public final void postConstruct(final Composite parent, final IEditorPart editorPart) {
        adjustParentLayout(parent);
        toolkit = createToolkit(parent);

        final IEditorSite site = editorPart.getEditorSite();
        final IEclipseContext parentContext = site.getService(IEclipseContext.class);
        context = parentContext.getActiveLeaf();
        context.set(RobotEditorCommandsStack.class, commandsStack);
        context.set(RedFormToolkit.class, toolkit);
        context.set(IEditorSite.class, site);
        context.set(IDirtyProviderService.class, context.get(IDirtyProviderService.class));

        formFragments = createFormFragments();
        injectToFormParts(context, formFragments);

        final Form form = createForm(parent, editorPart.getTitleImage());
        for (final ISectionFormFragment part : formFragments) {
            part.initialize(form.getBody());
        }

        site.setSelectionProvider(getSelectionProvider());
        prepareCommandsContext(site);
        createToolbarActions(site, form);
        addFormMessages();
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

    private Form createForm(final Composite parent, final Image image) {
        form = toolkit.createForm(parent);
        form.setImage(image);
        form.setText(getTitle());
        toolkit.decorateFormHeading(form);

        addFilteringSupport();

        GridLayoutFactory.fillDefaults().applyTo(form.getBody());
        form.getBody().setBackground(parent.getBackground());
        form.getBody().setForeground(parent.getForeground());
        return form;
    }

    private void addFilteringSupport() {
        final boolean isFilteringEnabled = new RobotSuiteEditorDialogSettings().isHeaderFilteringEnabled();

        filterSupport = new HeaderFilterSupport(form, toolkit, eventBroker, getSectionName().replaceAll(" ", "_"),
                formFragments);
        ContextInjectionFactory.inject(filterSupport, context);

        final HeaderFilterSwitchAction filterSwitchAction = new HeaderFilterSwitchAction(filterSupport, isFilteringEnabled, eventBroker);
        ContextInjectionFactory.inject(filterSwitchAction, context);
        form.addDisposeListener(e -> ContextInjectionFactory.uninject(filterSwitchAction, context));

        form.getMenuManager().add(filterSwitchAction);

        if (isFilteringEnabled) {
            filterSupport.enableFilter();
        }
    }

    private void injectToFormParts(final IEclipseContext context, final List<? extends ISectionFormFragment> sectionForms) {
        for (final ISectionFormFragment part : sectionForms) {
            ContextInjectionFactory.inject(part, context);
        }
    }

    protected abstract String getTitle();

    protected abstract String getSectionName();

    protected abstract List<? extends ISectionFormFragment> createFormFragments();

    protected abstract ISelectionProvider getSelectionProvider();

    private void prepareCommandsContext(final IWorkbenchPartSite site) {
        final IContextService service = site.getService(IContextService.class);
        service.activateContext(SECTION_EDITOR_PART_CONTEXT_ID);
        service.activateContext(getContextId());
    }

    protected abstract String getContextId();

    private void createToolbarActions(final IServiceLocator serviceLocator, final Form form) {
        final ToolBarManager toolBarManager = (ToolBarManager) form.getToolBarManager();

        final CommandContributionItem deleteSection = ToolbarContributions.createDeleteSectionContributionItem(
                serviceLocator, getSectionName());
        updateNeededContributions.add(deleteSection);
        toolBarManager.add(deleteSection);
        toolBarManager.update(true);
    }

    private void addFormMessages() {
        if (!fileModel.isEditable()) {
            form.setMessage("The file is read-only!", IMessageProvider.WARNING);
            return;
        }

        final Optional<? extends RobotSuiteFileSection> section = provideSection(fileModel);
        form.removeMessageHyperlinkListener(createSectionLinkListener);
        if (section.isPresent()) {
            form.setMessage(null, 0);
        } else {
            createSectionLinkListener = createHyperlinkListener();
            form.addMessageHyperlinkListener(createSectionLinkListener);
            form.setMessage("Section is not yet defined, do you want to create it?", IMessageProvider.ERROR);
        }
        filterSupport.addFormMessageIfNeeded();
    }

    private HyperlinkAdapter createHyperlinkListener() {
        return new HyperlinkAdapter() {
            @Override
            public void linkEntered(final HyperlinkEvent e) {
                ((Hyperlink) e.getSource()).setToolTipText("Click to create section");
            }

            @Override
            public void linkActivated(final HyperlinkEvent e) {
                commandsStack.execute(new CreateFreshSectionCommand(fileModel, getSectionName()));
            }
        };
    }

    @Override
    @Focus
    public void setFocus() {
        if (!formFragments.isEmpty()) {
            formFragments.get(0).setFocus();
        }
    }

    @Persist
    public void onSave() {
        for (final ISectionFormFragment fragment : formFragments) {
            ContextInjectionFactory.invoke(fragment, Persist.class, context, context, null);
        }

        final IDirtyProviderService dirtyProviderService = context.getActive(IDirtyProviderService.class);
        dirtyProviderService.setDirtyState(false);
    }

    @Override
    public void updateOnActivation() {
        addFormMessages();
        for (final CommandContributionItem item : updateNeededContributions) {
            item.update();
        }
        form.getToolBarManager().update(true);
    }

    @PreDestroy
    public final void preDestroy() {
        updateNeededContributions.clear();
        if (commandsStack != null) {
            commandsStack.clear();
        }
        for (final ISectionFormFragment fragment : formFragments) {
            ContextInjectionFactory.uninject(fragment, context);
        }
    }
}

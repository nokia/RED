/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.documentation;

import java.util.Objects;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.statushandlers.StatusManager;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.DocumentationInputGenerationException;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.DocumentationInputOpenException;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.DocumentationViewInput;
import org.robotframework.red.swt.SwtThread;

public class DocumentationView {

    public static final String ID = "org.robotframework.ide.DocumentationView";

    private Browser browser;

    private DocumentationViewInput currentInput;
    private Job documentationJob;

    private LinkWithSelectionAction linkSelectionAction;
    private OpenInputAction openInputAction;

    @PostConstruct
    public void postConstruct(final Composite parent, final IWorkbenchPage page, final IViewPart part,
            final IPartService partService) {
        parent.setLayout(new FillLayout());

        final IWorkbenchBrowserSupport browserSupport = PlatformUI.getWorkbench().getBrowserSupport();
        browser = new Browser(parent, SWT.NONE);
        browser.addLocationListener(new DocumentationViewLinksListener(page, browserSupport, this));

        createToolbarActions(partService, part.getViewSite().getActionBars().getToolBarManager(), page);
    }

    Browser getBrowser() {
        return browser;
    }

    private void createToolbarActions(final IPartService partService, final IToolBarManager manager,
            final IWorkbenchPage page) {
        linkSelectionAction = new LinkWithSelectionAction(partService);
        openInputAction = new OpenInputAction(page);

        manager.add(linkSelectionAction);
        manager.add(openInputAction);
    }

    @Focus
    public void onFocus() {
        browser.setFocus();
    }

    @PreDestroy
    public void dispose() {
        if (linkSelectionAction != null) {
            linkSelectionAction.dispose();
        }
    }

    void markSynced() {
        linkSelectionAction.switchToSynced();
    }

    void markSyncBroken() {
        linkSelectionAction.switchToBrokenSync();
    }

    public synchronized void displayDocumentation(final DocumentationViewInput input) {
        if (Objects.equals(currentInput, input)) {
            markSynced();
            return;
        }
        scheduleInputLoadingJob(input);
    }

    private void scheduleInputLoadingJob(final DocumentationViewInput input) {
        if (documentationJob != null && documentationJob.getState() == Job.SLEEPING) {
            documentationJob.cancel();
        }
        documentationJob = createDocumentationJob(input);
        documentationJob.schedule(100);
    }

    private Job createDocumentationJob(final DocumentationViewInput input) {
        return Job.create("Generating documentation", monitor -> {
            try {
                input.prepare();
                final String html = input.provideHtml();

                currentInput = input;
                openInputAction.setEnabled(true);
                SwtThread.asyncExec(() -> {
                    browser.setText(html);
                    markSynced();
                });

            } catch (final DocumentationInputGenerationException e) {
                // nothing to do yet

            } catch (final RuntimeException e) {
                StatusManager.getManager().handle(
                        new Status(IStatus.ERROR, RedPlugin.PLUGIN_ID, IStatus.OK, "Error displaying documentation", e),
                        StatusManager.SHOW);
            }
            return Status.OK_STATUS;
        });
    }

    @Inject
    @org.eclipse.e4.core.di.annotations.Optional
    private void whenKeywordCallDetailIsChanged(
            @UIEventTopic(RobotModelEvents.ROBOT_KEYWORD_CALL_DETAIL_CHANGE_ALL) final RobotKeywordCall keywordCall) {
        if (currentInput == null) {
            return;
        }
        if (keywordCall instanceof RobotDefinitionSetting && ((RobotDefinitionSetting) keywordCall).isDocumentation()
                && currentInput.contains(keywordCall)) {
            scheduleInputLoadingJob(currentInput);
        } else if (keywordCall instanceof RobotSetting && ((RobotSetting) keywordCall).isDocumentation()
                && currentInput.contains(keywordCall)) {
            scheduleInputLoadingJob(currentInput);
        }
    }

    private class LinkWithSelectionAction extends Action implements IWorkbenchAction {

        private static final String ID = "org.robotframework.action.views.documentation.LinkWithSelection";

        private final IPartService partService;

        private DocumentationViewPartListener partsListener;

        public LinkWithSelectionAction(final IPartService partService) {
            this.partService = partService;
            setId(ID);
            setText("Link with Selection");
            setImageDescriptor(RedImages.getSyncedImage());

            setChecked(false);
        }

        private void switchToSynced() {
            if (isChecked()) {
                setText("Link with Selection");
                setImageDescriptor(RedImages.getSyncedImage());
            }
        }

        private void switchToBrokenSync() {
            if (isChecked()) {
                setText("Link with Selection (showing last valid input)");
                setImageDescriptor(RedImages.getSyncBrokenImage());
            }
        }

        @Override
        public void run() {
            if (isChecked()) {
                createPartsListener();
            } else {
                removePartsListener();
            }
        }

        @Override
        public void dispose() {
            removePartsListener();
        }

        private void createPartsListener() {
            partsListener = new DocumentationViewPartListener();
            partService.addPartListener(partsListener);
        }

        private void removePartsListener() {
            if (partsListener != null) {
                partService.removePartListener(partsListener);
                partsListener.dispose();
                partsListener = null;
            }
        }
    }

    private class OpenInputAction extends Action {

        private static final String ID = "org.robotframework.action.views.documentation.OpenInput";

        private final IWorkbenchPage page;

        public OpenInputAction(final IWorkbenchPage page) {
            this.page = page;
            setId(ID);
            setText("Open Input");
            setImageDescriptor(RedImages.getGoToImage());

            setEnabled(false);
        }

        @Override
        public void run() {
            if (currentInput != null) {
                try {
                    currentInput.showInput(page);
                } catch (final DocumentationInputOpenException e) {
                    StatusManager.getManager().handle(
                            new Status(IStatus.ERROR, RedPlugin.PLUGIN_ID, IStatus.OK, "Error opening input", e),
                            StatusManager.SHOW);
                }
            }
        }
    }
}

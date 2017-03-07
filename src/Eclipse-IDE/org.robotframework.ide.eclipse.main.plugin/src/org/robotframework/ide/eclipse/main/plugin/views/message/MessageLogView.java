/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.message;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotTestExecutionService;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotTestExecutionService.RobotTestExecutionListener;
import org.robotframework.ide.eclipse.main.plugin.views.message.ExecutionMessagesStore.ExecutionMessagesStoreListener;
import org.robotframework.red.swt.SwtThread;


/**
 * @author mmarzec
 *
 */
public class MessageLogView {
    
    public static final String ID = "org.robotframework.ide.MessageLogView";
    
    private final RobotTestExecutionService executionService;

    private StyledText styledText;

    private RobotTestExecutionListener executionListener;
    
    public MessageLogView() {
        executionService = RedPlugin.getTestExecutionService();
    }

    @PostConstruct
    public void postConstruct(final Composite parent) {
        final FillLayout layout = new FillLayout();
        layout.marginHeight=2;
        layout.marginWidth=2;
        parent.setLayout(layout);
        
        styledText = new StyledText(parent, SWT.H_SCROLL | SWT.V_SCROLL);
        styledText.setFont(JFaceResources.getTextFont());
        styledText.setEditable(false);
        styledText.setText(getMessageFromLastLaunch());

        // clear log view always when new tests are launched
        final ExecutionMessagesStoreListener storeListener = (store, msg) -> SwtThread.syncExec(() -> append(msg));
        executionListener = launch -> SwtThread.syncExec(() -> {
            final ExecutionMessagesStore store = launch.getExecutionData(ExecutionMessagesStore.class, () -> new ExecutionMessagesStore());
            store.addStoreListener(storeListener);

            styledText.setText("");
        });
        executionService.addExecutionListener(executionListener);
    }

    private void append(final String msg) {
        styledText.append(msg);
        styledText.setTopIndex(styledText.getLineCount() - 1);
    }

    private String getMessageFromLastLaunch() {
        return executionService.getLastLaunch()
                .flatMap(launch -> launch.getExecutionData(ExecutionMessagesStore.class))
                .map(store -> store.getMessage())
                .orElse("");
    }

    @Focus
    public void onFocus() {
        styledText.setFocus();
    }

    @PreDestroy
    public void dispose() {
        executionService.removeExecutionListner(executionListener);
    }
}

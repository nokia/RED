/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.message;

import java.util.Optional;

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
import org.robotframework.ide.eclipse.main.plugin.launch.RobotTestExecutionService.RobotTestsLaunch;
import org.robotframework.ide.eclipse.main.plugin.views.message.ExecutionMessagesStore.ExecutionMessagesStoreListener;
import org.robotframework.red.swt.SwtThread;

import com.google.common.annotations.VisibleForTesting;

/**
 * @author mmarzec
 *
 */
public class MessageLogView {
    
    public static final String ID = "org.robotframework.ide.MessageLogView";
    
    private final RobotTestExecutionService executionService;

    private StyledText styledText;

    private RobotTestExecutionListener executionListener;

    private final ExecutionMessagesStoreListener storeListener = this::append;
    
    public MessageLogView() {
        this(RedPlugin.getTestExecutionService());
    }

    @VisibleForTesting
    MessageLogView(final RobotTestExecutionService executionService) {
        this.executionService = executionService;
    }

    @VisibleForTesting
    StyledText getTextControl() {
        return styledText;
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

        setInput();
    }

    private void setInput() {

        // synchronize on service, so that any thread which would like to start another launch
        // will have to wait
        synchronized (executionService) {
            executionListener = new ExecutionListener(storeListener);
            executionService.addExecutionListener(executionListener);

            final Optional<RobotTestsLaunch> lastLaunch = executionService.getLastLaunch();
            if (lastLaunch.isPresent()) {
                final RobotTestsLaunch launch = lastLaunch.get();

                // this launch may be currently running, so we have to synchronize in order
                // to get proper state of messages, as other threads may change it in the meantime
                synchronized (launch) {
                    final ExecutionMessagesStore messagesStore = launch.getExecutionData(ExecutionMessagesStore.class,
                            ExecutionMessagesStore::new);
                    messagesStore.addStoreListener(storeListener);

                    append(messagesStore.getMessage());
                }
            }
        }
    }

    private void append(final String msg) {
        SwtThread.asyncExec(() -> {
            // it could have been queued earlier in main thread...
            if (styledText == null || styledText.isDisposed()) {
                return;
            }
            styledText.append(msg);
            styledText.setTopIndex(styledText.getLineCount() - 1);
        });
    }

    @Focus
    public void onFocus() {
        styledText.setFocus();
    }

    protected void toggleWordsWrapping() {
        styledText.setWordWrap(!styledText.getWordWrap());
    }

    @PreDestroy
    public void dispose() {
        synchronized (executionService) {
            executionService.removeExecutionListener(executionListener);
            executionService.forEachLaunch(launch -> launch.getExecutionData(ExecutionMessagesStore.class)
                    .ifPresent(store -> store.removeStoreListener(storeListener)));
        }
    }

    private class ExecutionListener implements RobotTestExecutionListener {

        private final ExecutionMessagesStoreListener storeListener;

        ExecutionListener(final ExecutionMessagesStoreListener storeListener) {
            this.storeListener = storeListener;
        }

        @Override
        public void executionStarting(final RobotTestsLaunch launch) {
            SwtThread.asyncExec(() -> styledText.setText(""));

            launch.getExecutionData(ExecutionMessagesStore.class, ExecutionMessagesStore::new)
                    .addStoreListener(storeListener);
        }

        @Override
        public void executionEnded(final RobotTestsLaunch launch) {
            // nothing to do
        }
    }
}

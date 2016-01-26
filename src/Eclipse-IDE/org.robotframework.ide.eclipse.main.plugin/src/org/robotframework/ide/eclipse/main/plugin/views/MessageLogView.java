/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotEventBroker;


/**
 * @author mmarzec
 *
 */
public class MessageLogView {
    
    public static final String ID = "org.robotframework.ide.MessageLogView";
    
    private StyledText styledText;
    
    @PostConstruct
    public void postConstruct(final Composite parent) {
        
        final FillLayout layout = new FillLayout();
        layout.marginHeight=2;
        layout.marginWidth=2;
        parent.setLayout(layout);
        
        styledText = new StyledText(parent, SWT.H_SCROLL | SWT.V_SCROLL);
        styledText.setFont(JFaceResources.getTextFont());
        styledText.setEditable(false);
        
        styledText.setText(RobotEventBroker.getMessageLogViewContent());
    }
    
    @Focus
    public void onFocus() {
        styledText.setFocus();
    }
    
    public void appendLine(final String line) {
        styledText.append(line);
        styledText.setTopIndex(styledText.getLineCount() - 1);
    }
    
    @Inject
    @Optional
    private void lineEvent(@UIEventTopic("MessageLogView/AppendLine") final String line) {
        appendLine(line);
    }
    
    @Inject
    @Optional
    private void clearEvent(@UIEventTopic("MessageLogView/Clear") final String s) {
        styledText.setText("");
    }
}

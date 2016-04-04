/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.tabs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotLaunchConfiguration;
import org.robotframework.red.graphics.ImagesManager;

import com.google.common.base.Optional;
import com.google.common.collect.Range;
import com.google.common.primitives.Ints;

/**
 * @author mmarzec
 *
 */
public class RobotLaunchConfigurationRemoteTab extends AbstractLaunchConfigurationTab implements
        ILaunchConfigurationTab {

    private Text hostTxt;

    private Text portTxt;
    
    private Text timeoutTxt;
    
    @Override
    public void createControl(final Composite parent) {
        final Composite composite = new Composite(parent, SWT.NONE);
        GridLayoutFactory.fillDefaults().margins(3, 3).applyTo(composite);

        final Group remoteGroup = new Group(composite, SWT.NONE);
        remoteGroup.setText("Remote Debugging");
        GridDataFactory.fillDefaults().grab(true, false).applyTo(remoteGroup);
        GridLayoutFactory.fillDefaults().numColumns(2).margins(3, 3).applyTo(remoteGroup);

        final Label hostLbl = new Label(remoteGroup, SWT.NONE);
        hostLbl.setText("Local IP:");

        hostTxt = new Text(remoteGroup, SWT.BORDER);
        GridDataFactory.fillDefaults().hint(100, SWT.DEFAULT).applyTo(hostTxt);
        hostTxt.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                updateLaunchConfigurationDialog();
            }
        });

        final Label portLbl = new Label(remoteGroup, SWT.NONE);
        portLbl.setText("Local port:");

        portTxt = new Text(remoteGroup, SWT.BORDER);
        GridDataFactory.fillDefaults().hint(100, SWT.DEFAULT).applyTo(portTxt);
        portTxt.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                updateLaunchConfigurationDialog();
            }
        });
        
        final Label timeoutLbl = new Label(remoteGroup, SWT.NONE);
        timeoutLbl.setText("Connection timeout [s]:");

        timeoutTxt = new Text(remoteGroup, SWT.BORDER);
        GridDataFactory.fillDefaults().hint(100, SWT.DEFAULT).applyTo(timeoutTxt);
        timeoutTxt.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                updateLaunchConfigurationDialog();
            }
        });
        
        final Button exportBtn = new Button(remoteGroup, SWT.PUSH);
        GridDataFactory.fillDefaults().span(2, 1).applyTo(exportBtn);
        exportBtn.setText("Export Debug Script");
        exportBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                final DirectoryDialog dirDialog = new DirectoryDialog(parent.getShell());
                final String fileName = "TestRunnerAgent.py";
                dirDialog.setMessage("Choose \"" + fileName + "\" export destination.");
                final String dir = dirDialog.open();
                if (dir != null) {
                    final File scriptFile = new File(dir + File.separator + fileName);
                    try {
                        Files.copy(RobotRuntimeEnvironment.class.getResourceAsStream(fileName), scriptFile.toPath(),
                                StandardCopyOption.REPLACE_EXISTING);
                    } catch (final IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
        
        setControl(composite);
    }

    @Override
    public void setDefaults(final ILaunchConfigurationWorkingCopy configuration) {
        RobotLaunchConfiguration.fillDefaults(configuration);
    }

    @Override
    public void initializeFrom(final ILaunchConfiguration configuration) {
        try {
            final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(configuration);
            final Optional<Integer> port = robotConfig.getRemoteDebugPort();
            final Optional<Integer> timeout = robotConfig.getRemoteDebugTimeout();

            hostTxt.setText(robotConfig.getRemoteDebugHost());
            portTxt.setText(port.isPresent() ? port.get().toString() : "");
            timeoutTxt.setText(timeout.isPresent() ? timeout.get().toString() : "");
        } catch (final CoreException e) {
            setErrorMessage("Invalid launch configuration: " + e.getMessage());
        }
    }
    
    @Override
    public boolean isValid(final ILaunchConfiguration configuration) {
        setErrorMessage(null);
        setWarningMessage(null);
        if(!isPortValid()) {
            setErrorMessage("Invalid port specified");
            return false;
        }
        if(!isConnectionTimeoutValid()) {
            setErrorMessage("Invalid connection timeout specified");
            return false;
        }
        return true;
    }
    
    @Override
    public boolean canSave() {
        return isDirty() && isPortValid() && isConnectionTimeoutValid();
    }

    private boolean isPortValid() {
        if (!portTxt.getText().isEmpty()) {
            final Integer port = Ints.tryParse(portTxt.getText());
            return port != null && Range.closed(1, 65535).contains(port);
        }
        return true;
    }

    private boolean isConnectionTimeoutValid() {
        if (!timeoutTxt.getText().isEmpty()) {
            final Integer timeout = Ints.tryParse(timeoutTxt.getText());
            return timeout != null && Range.atLeast(1).contains(timeout);
        }
        return true;
    }

    @Override
    public void performApply(final ILaunchConfigurationWorkingCopy configuration) {
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(configuration);
        robotConfig.setRemoteDebugHost(hostTxt.getText().trim());
        robotConfig.setRemoteDebugPort(portTxt.getText().trim());
        robotConfig.setRemoteDebugTimeout(timeoutTxt.getText().trim());
    }
    
    @Override
    public String getName() {
        return "Remote";
    }

    @Override
    public String getMessage() {
        return "Create or edit a configuration to launch Robot Framework tests";
    }

    @Override
    public Image getImage() {
        return ImagesManager.getImage(RedImages.getRobotImage());
    }
}

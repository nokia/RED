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

/**
 * @author mmarzec
 *
 */
public class RobotLaunchConfigurationRemoteTab extends AbstractLaunchConfigurationTab implements
        ILaunchConfigurationTab {

    private Text hostTxt;

    private Text portTxt;

    @Override
    public void createControl(final Composite parent) {
        final Composite composite = new Composite(parent, SWT.NONE);
        GridLayoutFactory.fillDefaults().margins(3, 3).applyTo(composite);

        final Group remoteGroup = new Group(composite, SWT.NONE);
        remoteGroup.setText("Remote Debugging");
        GridDataFactory.fillDefaults().grab(true, false).applyTo(remoteGroup);
        GridLayoutFactory.fillDefaults().numColumns(2).margins(3, 3).applyTo(remoteGroup);

        final Label hostLbl = new Label(remoteGroup, SWT.NONE);
        hostLbl.setText("Host:");

        hostTxt = new Text(remoteGroup, SWT.BORDER);
        GridDataFactory.fillDefaults().hint(100, SWT.DEFAULT).applyTo(hostTxt);
        hostTxt.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                updateLaunchConfigurationDialog();
            }
        });

        final Label portLbl = new Label(remoteGroup, SWT.NONE);
        portLbl.setText("Port:");

        portTxt = new Text(remoteGroup, SWT.BORDER);
        GridDataFactory.fillDefaults().hint(100, SWT.DEFAULT).applyTo(portTxt);
        portTxt.addModifyListener(new ModifyListener() {

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
            public void widgetSelected(SelectionEvent e) {
                final DirectoryDialog dirDialog = new DirectoryDialog(parent.getShell());
                dirDialog.setMessage("Choose export destination.");
                final String dir = dirDialog.open();
                if (dir != null) {
                    final String fileName = "TestRunnerAgent.py";
                    final File scriptFile = new File(dir + File.separator + fileName);
                    try {
                        Files.copy(RobotRuntimeEnvironment.class.getResourceAsStream(fileName), scriptFile.toPath(),
                                StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
        
        setControl(composite);
    }

    @Override
    public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
        RobotLaunchConfiguration.fillDefaults(configuration);
    }

    @Override
    public void initializeFrom(ILaunchConfiguration configuration) {
        try {
            final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(configuration);
            hostTxt.setText(robotConfig.getRemoteDebugHost());
            portTxt.setText(robotConfig.getRemoteDebugPort());
        } catch (final CoreException e) {
            setErrorMessage("Invalid launch configuration: " + e.getMessage());
        }
    }

    @Override
    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(configuration);
        robotConfig.setRemoteDebugHost(hostTxt.getText().trim());
        robotConfig.setRemoteDebugPort(portTxt.getText().trim());
    }

    @Override
    public String getName() {
        return "Remote";
    }

    @Override
    public Image getImage() {
        return ImagesManager.getImage(RedImages.getRobotImage());
    }
}

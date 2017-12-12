/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.popup;

import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.red.jface.dialogs.InputLoadingFormComposite;
import org.robotframework.red.jface.dialogs.RobotPopupDialog;

@SuppressWarnings("restriction")
public class ImportSettingsPopup extends RobotPopupDialog {

    private InputLoadingFormComposite composite;
    private final RobotEditorCommandsStack commandsStack;
    private final RobotSuiteFile fileModel;
    private final RobotSetting initialSetting;

    public ImportSettingsPopup(final Shell parent, final IThemeEngine engine,
            final RobotEditorCommandsStack commandsStack, final RobotSuiteFile fileModel,
            final RobotSetting initialSetting) {
        super(parent, engine);
        setShellStyle(PopupDialog.INFOPOPUPRESIZE_SHELLSTYLE | SWT.ON_TOP | SWT.APPLICATION_MODAL);
        this.commandsStack = commandsStack;
        this.fileModel = fileModel;
        this.initialSetting = initialSetting;
    }

    @Override
    protected Control createDialogControls(final Composite parent) {
        composite = new ImportSettingsComposite(parent, commandsStack, fileModel, initialSetting);
        return composite;
    }

    @Override
    protected Control getFocusControl() {
        return composite.getFocusControl();
    }
}

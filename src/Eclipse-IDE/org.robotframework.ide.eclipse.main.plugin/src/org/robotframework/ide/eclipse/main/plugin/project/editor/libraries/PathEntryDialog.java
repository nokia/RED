/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.SearchPath;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.base.Splitter;


/**
 * @author Michal Anglart
 *
 */
class PathEntryDialog extends Dialog {

    private List<SearchPath> searchPath;

    private Text pathText;

    PathEntryDialog(final Shell parentShell) {
        super(parentShell);
    }

    @Override
    public void create() {
        super.create();
        getShell().setText("Add new search path");
    }

    @Override
    protected Control createDialogArea(final Composite parent) {
        final Composite dialogComposite = (Composite) super.createDialogArea(parent);
        GridLayoutFactory.fillDefaults().margins(10, 10).applyTo(dialogComposite);

        final Label infoLabel = new Label(dialogComposite, SWT.WRAP);
        infoLabel.setText("Provide search paths to be added. Each path should be specified in separate line.");
        GridDataFactory.fillDefaults().hint(350, SWT.DEFAULT).applyTo(infoLabel);

        pathText = new Text(dialogComposite, SWT.BORDER | SWT.MULTI);
        GridDataFactory.fillDefaults().hint(350, 100).applyTo(pathText);

        return dialogComposite;
    }

    @Override
    protected void okPressed() {
        searchPath = newArrayList(
                filter(transform(Splitter.on('\n').splitToList(pathText.getText()), new Function<String, SearchPath>() {

                    @Override
                    public SearchPath apply(final String singlePath) {
                        final SearchPath path = new SearchPath();
                        path.setLocation(singlePath.trim());
                        return path.getLocation().isEmpty() ? null : path;
                    }
                }), Predicates.notNull()));
        super.okPressed();
    }

    @VisibleForTesting
    Button getOkButton() {
        return getButton(IDialogConstants.OK_ID);
    }

    @VisibleForTesting
    Text getSearchPathsText() {
        return pathText;
    }

    List<SearchPath> getSearchPath() {
        return searchPath;
    }
}

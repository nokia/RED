/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.forms;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.Section;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.red.graphics.ImagesManager;

public class Sections {

    public static void installMaximazingPossibility(final Section section) {
        final ToolBar bar = new ToolBar(section, SWT.FLAT | SWT.HORIZONTAL);
        final ToolItem item = new ToolItem(bar, SWT.PUSH);
        item.setToolTipText("Maximize section");
        item.setImage(ImagesManager.getImage(RedImages.getFocusSectionImage()));
        item.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                maximizeChosenSectionAndMinimalizeOthers(section);
            }
        });
        section.setTextClient(bar);
    }

    public static void maximizeChosenSectionAndMinimalizeOthers(final Section section) {
        final Composite parent = section.getParent();
        final Control[] children = parent.getChildren();
        for (final Control child : children) {
            if (child instanceof Section) {
                final Section childSection = (Section) child;
                final GridData layoutData = (GridData) childSection.getLayoutData();

                childSection.setExpanded(child == section);
                layoutData.grabExcessVerticalSpace = child == section;
            }
        }
        parent.layout();
    }

    public static void switchGridCellGrabbingOnExpansion(final Section section) {
        final ExpansionAdapter expansionListener = new ExpansionAdapter() {
            @Override
            public void expansionStateChanged(final ExpansionEvent e) {
                final GridData layoutData = (GridData) section.getLayoutData();
                layoutData.grabExcessVerticalSpace = e.getState();
                section.getParent().layout();
                section.layout();
            }
        };
        section.addExpansionListener(expansionListener);
        section.addDisposeListener(e -> section.removeExpansionListener(expansionListener));
    }

}

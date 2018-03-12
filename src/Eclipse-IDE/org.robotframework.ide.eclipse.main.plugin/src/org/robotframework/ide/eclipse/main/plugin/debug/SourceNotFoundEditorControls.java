/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.debug;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.robotframework.ide.eclipse.main.plugin.debug.RedDebuggerAssistantEditorWrapper.RedDebuggerAssistantEditorControls;
import org.robotframework.ide.eclipse.main.plugin.debug.RedDebuggerAssistantEditorWrapper.RedDebuggerAssistantEditorInput;
import org.robotframework.red.graphics.ColorsManager;
import org.robotframework.red.graphics.ImagesManager;

class SourceNotFoundEditorControls implements RedDebuggerAssistantEditorControls {

    private Composite innerParent;

    private CLabel titleLabel;

    private StyledText text;

    @Override
    public void construct(final Composite parent) {
        final FillLayout parentLayout = (FillLayout) parent.getLayout();
        parentLayout.marginHeight = 0;
        parentLayout.marginWidth = 0;

        innerParent = new Composite(parent, SWT.NONE);
        innerParent.setBackground(ColorsManager.getColor(SWT.COLOR_WHITE));
        GridLayoutFactory.fillDefaults().applyTo(innerParent);

        titleLabel = new CLabel(innerParent, SWT.NONE);
        titleLabel.setFont(JFaceResources.getBannerFont());
        titleLabel.setBackground((Color) null);
        titleLabel.setForeground(ColorsManager.getColor(SWT.COLOR_DARK_GRAY));
        titleLabel.addPaintListener(e -> {
            final int oldThickness = e.gc.getLineWidth();
            try {
                e.gc.setLineWidth(2);
                e.gc.drawLine(0, e.y + e.height - 1, e.x + e.width, e.y + e.height - 1);
            } finally {
                e.gc.setLineWidth(oldThickness);
            }

        });
        GridDataFactory.fillDefaults().grab(true, false).applyTo(titleLabel);

        text = new StyledText(innerParent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        text.setEditable(false);
        text.setWordWrap(true);
        text.setBackground(innerParent.getBackground());
        text.setFont(JFaceResources.getTextFont());
        GridDataFactory.fillDefaults().grab(true, true).applyTo(text);
    }

    @Override
    public void setFocus() {
        text.setFocus();
    }

    @Override
    public void setInput(final RedDebuggerAssistantEditorInput input) {
        titleLabel.setImage(ImagesManager.getImage(input.getTitleImageDescriptor()));
        titleLabel.setText(input.getTitle());

        text.setText(input.getDetailedInformation());
    }

    @Override
    public Composite getParent() {
        return innerParent.getParent();
    }

    @Override
    public void dispose() {
        innerParent.dispose();
    }
}

/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.navigator.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.forms.widgets.ScrolledFormText;
import org.robotframework.ide.eclipse.main.plugin.RedImages;

class WrapFormAction extends Action {

    private static final String ACTION_TOOLTIP_NAME = "Wrap";

    private final ScrolledFormText scrolledFormText;

    WrapFormAction(final ScrolledFormText scrolledFormText) {
        super(ACTION_TOOLTIP_NAME, AS_CHECK_BOX);
        setImageDescriptor(RedImages.getWordwrapImage());
        this.scrolledFormText = scrolledFormText;
    }

    @Override
    public void run() {
        scrolledFormText.setExpandHorizontal(isChecked());
        scrolledFormText.setExpandVertical(isChecked());

        scrolledFormText.reflow(true);
    }
}

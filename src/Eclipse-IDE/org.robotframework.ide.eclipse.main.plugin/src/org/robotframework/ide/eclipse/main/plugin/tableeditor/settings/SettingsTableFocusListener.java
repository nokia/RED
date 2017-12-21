/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;

public class SettingsTableFocusListener implements FocusListener {

    private final String contextId;

    private final IWorkbenchSite site;

    private IContextActivation activationToken = null;

    public SettingsTableFocusListener(final String contextId, final IWorkbenchSite site) {
        this.contextId = contextId;
        this.site = site;
    }

    @Override
    public void focusLost(final FocusEvent e) {
        getContextService(site).deactivateContext(activationToken);
    }

    @Override
    public void focusGained(final FocusEvent e) {
        activationToken = getContextService(site).activateContext(contextId);
    }

    private IContextService getContextService(final IWorkbenchSite site) {
        return site.getService(IContextService.class);
    }
}

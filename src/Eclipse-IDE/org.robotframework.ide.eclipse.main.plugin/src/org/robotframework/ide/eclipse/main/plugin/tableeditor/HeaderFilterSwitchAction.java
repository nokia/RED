/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.action.Action;


/**
 * @author Michal Anglart
 *
 */
class HeaderFilterSwitchAction extends Action {

    private final HeaderFilterSupport filterSupport;

    private final IEventBroker eventBroker;

    HeaderFilterSwitchAction(final HeaderFilterSupport filterSupport, final boolean isFilteringEnabled,
            final IEventBroker eventBroker) {
        super("Show filter", AS_CHECK_BOX);
        this.filterSupport = filterSupport;
        this.eventBroker = eventBroker;
        setChecked(isFilteringEnabled);
    }

    @Override
    public void run() {
        final boolean isChecked = isChecked();
        // simple send the request to all the switch actions; resulting in enabling/disabling all
        // the filters in all currently opened editors on all form pages
        if (isChecked) {
            eventBroker.send(RobotSuiteEditorEvents.FORM_FILTER_ENABLED, Boolean.valueOf(isChecked));
        } else {
            eventBroker.send(RobotSuiteEditorEvents.FORM_FILTER_DISABLED, Boolean.valueOf(isChecked));
        }

    }

    @Inject
    @Optional
    private void whenFilterEnablementChanSomewhere(
            @UIEventTopic(RobotSuiteEditorEvents.FORM_FILTER_ENABLAMENT_CHANGED) final Boolean isEnabled) {
        if (isEnabled) {
            filterSupport.enableFilter();
        } else {
            filterSupport.disableFilter();
        }
        setChecked(isEnabled);
        new RobotSuiteEditorDialogSettings().setHeaderFilteringEnabled(isEnabled);
    }
}

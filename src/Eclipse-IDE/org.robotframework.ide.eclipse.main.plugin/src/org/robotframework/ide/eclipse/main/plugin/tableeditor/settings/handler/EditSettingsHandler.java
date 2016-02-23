/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.handler;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.handler.EditSettingsHandler.E4EditSettingsHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class EditSettingsHandler extends DIParameterizedHandler<E4EditSettingsHandler> {

    public EditSettingsHandler() {
        super(E4EditSettingsHandler.class);
    }

    public static class E4EditSettingsHandler {

        @Inject
        protected IEventBroker eventBroker;

        @Execute
        public Object editSettings(@Named(Selections.SELECTION) final IStructuredSelection selection) {
            final RobotSetting setting = Selections.getSingleElement(selection, RobotSetting.class);
            
            eventBroker.send(RobotModelEvents.ROBOT_SETTING_IMPORTS_EDIT, setting);
            
            return null;
        }
    }
}

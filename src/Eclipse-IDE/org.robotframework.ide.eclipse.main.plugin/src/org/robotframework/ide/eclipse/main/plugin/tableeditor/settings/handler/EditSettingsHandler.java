package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.handler;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.tools.compat.parts.DIHandler;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.handler.EditSettingsHandler.E4EditSettingsHandler;
import org.robotframework.red.viewers.Selections;

public class EditSettingsHandler extends DIHandler<E4EditSettingsHandler> {

    public EditSettingsHandler() {
        super(E4EditSettingsHandler.class);
    }

    public static class E4EditSettingsHandler {

        @Inject
        protected IEventBroker eventBroker;

        @Execute
        public Object deleteSettings(@Named(Selections.SELECTION) final IStructuredSelection selection) {
            final RobotSetting setting = Selections.getSingleElement(selection, RobotSetting.class);
            
            eventBroker.send(RobotModelEvents.ROBOT_SETTING_IMPORTS_EDIT, setting);
            
            return null;
        }
    }
}

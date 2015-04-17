package org.robotframework.ide.eclipse.main.plugin;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.ui.PlatformUI;

public final class RobotModelManager {

    private static class ModelSynchronizer implements IResourceChangeListener {

        @Override
        public void resourceChanged(final IResourceChangeEvent event) {
            List<RobotElementChange> changes = null;
            if (event.getType() == IResourceChangeEvent.PRE_CLOSE) {
                changes = InstanceHolder.INSTANCE.getModel().removeProject((IProject) event.getResource());

                notifyAboutChanges(changes);
            } else if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
                changes = InstanceHolder.INSTANCE.getModel().synchronizeChanges(event.getDelta());

                notifyAboutChanges(changes);
            }
        }

        private void notifyAboutChanges(final List<RobotElementChange> changes) {
            final IEventBroker eventBroker = (IEventBroker) PlatformUI.getWorkbench().getService(IEventBroker.class);
            
            for (final RobotElementChange change : changes) {
                eventBroker.post(RobotModelEvents.EXTERNAL_MODEL_CHANGE, change);
            }
        }
    }

    private static class InstanceHolder {
        private static RobotModelManager INSTANCE = new RobotModelManager();
    }

    private RobotModel model = new RobotModel();
    private final IResourceChangeListener resourceListener;

    private RobotModelManager() {
        resourceListener = new ModelSynchronizer();
        ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceListener);
    }

    public static RobotModelManager getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public RobotSuiteFile createSuiteFile(final IFile file) {
        return model.createSuiteFile(file);
    }

    public RobotModel getModel() {
        return model;
    }

    public void dispose() {
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceListener);
        model = new RobotModel();
    }

}

package org.robotframework.ide.eclipse.main.plugin;

import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchPage;

public interface RobotElement {

    String getName();

    RobotElement getParent();

    List<RobotElement> getChildren();

    boolean contains(RobotElement element);

    ImageDescriptor getImage();

    OpenStrategy getOpenRobotEditorStrategy(IWorkbenchPage page);

    public class OpenStrategy {
        public void run() {

        }
    }
}

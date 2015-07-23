package org.robotframework.ide.eclipse.main.plugin.model;

import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchPage;

/**
 * An interface which all Robot model objects has to implement
 * 
 * @{author Michal Anglart
 */
public interface RobotElement {

    /**
     * Gets the name of the element
     * 
     * @return Element name
     */
    String getName();

    /**
     * Gets the comment of the element
     * 
     * TODO : this has no sense for elements like projects, folders or files.
     * Consider moving to some other place
     * 
     * @return Comment of the element
     */
    String getComment();

    /**
     * Gets parent of this element
     * 
     * @return
     */
    RobotElement getParent();

    /**
     * Gets the suite file in which this element is contained or null if it is
     * not inside the suite file.
     * 
     * @return Model object representing containg file.
     */
    RobotSuiteFile getSuiteFile();

    /**
     * Gets children elements
     * 
     * @return List of children elements
     */
    List<RobotElement> getChildren();

    /**
     * Gets image descriptor of this element
     * 
     * @return image descriptor
     */
    ImageDescriptor getImage();

    /**
     * Returns open strategy capable of opening and selecting this element in
     * editor.
     * 
     * @param page
     * @return
     */
    OpenStrategy getOpenRobotEditorStrategy(IWorkbenchPage page);

    /**
     * The strategy for opening given this element in editor.
     */
    public class OpenStrategy {
        public void run() {

        }
    }
}

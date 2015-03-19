package org.robotframework.ide.eclipse.main.plugin.perspectives;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class RobotPerspective implements IPerspectiveFactory {
	 
	@Override
	public void createInitialLayout(IPageLayout layout) {
	
	    String editorArea = layout.getEditorArea();
        layout.setEditorAreaVisible(true); 
        
        IFolderLayout bottom= layout.createFolder("bottom", IPageLayout.BOTTOM, 0.65f ,editorArea);
        bottom.addView("org.eclipse.ui.console.ConsoleView"); 
        bottom.addView(IPageLayout.ID_PROBLEM_VIEW);
        
        IFolderLayout topLeft = layout.createFolder("topLeft", IPageLayout.LEFT, 0.25f,editorArea);
        topLeft.addView(IPageLayout.ID_PROJECT_EXPLORER); 	
		
	}

}
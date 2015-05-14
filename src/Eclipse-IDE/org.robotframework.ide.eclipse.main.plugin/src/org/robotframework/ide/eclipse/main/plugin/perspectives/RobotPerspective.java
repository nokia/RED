package org.robotframework.ide.eclipse.main.plugin.perspectives;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class RobotPerspective implements IPerspectiveFactory {
	 
    private static final String MESSAGE_LOG_VIEW = "org.robotframework.ide.MessageLogView";

    @Override
	public void createInitialLayout(final IPageLayout layout) {
	    final String editorArea = layout.getEditorArea();
        layout.setEditorAreaVisible(true); 

        final IFolderLayout topLeft = layout.createFolder("topLeft", IPageLayout.LEFT, 0.25f, editorArea);
        topLeft.addView(IPageLayout.ID_PROJECT_EXPLORER);
        
        final IFolderLayout bottom = layout.createFolder("bottom", IPageLayout.BOTTOM, 0.65f ,editorArea);
        bottom.addView("org.eclipse.ui.console.ConsoleView"); 
        bottom.addView(IPageLayout.ID_PROBLEM_VIEW);
        
        final IFolderLayout bottomLog = layout.createFolder("bottom", IPageLayout.RIGHT, 0.50f, "bottom");
        bottomLog.addView(MESSAGE_LOG_VIEW);

        final IFolderLayout topRight = layout.createFolder("topRight", IPageLayout.RIGHT, 0.75f, editorArea);
        topRight.addView(IPageLayout.ID_OUTLINE);

        layout.addPerspectiveShortcut("org.eclipse.debug.ui.DebugPerspective");
        layout.addShowViewShortcut(MESSAGE_LOG_VIEW);

        layout.addShowInPart("org.eclipse.ui.navigator.ProjectExplorer");
	}
}
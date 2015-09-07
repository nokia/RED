package org.robotframework.ide.eclipse.main.plugin.perspectives;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.robotframework.ide.eclipse.main.plugin.views.ExecutionView;
import org.robotframework.ide.eclipse.main.plugin.views.MessageLogView;

public class RobotPerspective implements IPerspectiveFactory {
	 
    @Override
	public void createInitialLayout(final IPageLayout layout) {
	    final String editorArea = layout.getEditorArea();
        layout.setEditorAreaVisible(true); 

        final IFolderLayout topLeft = layout.createFolder("topLeft", IPageLayout.LEFT, 0.25f, editorArea);
        topLeft.addView(IPageLayout.ID_PROJECT_EXPLORER);
        
        final IFolderLayout bottom = layout.createFolder("bottom", IPageLayout.BOTTOM, 0.65f ,editorArea);
        bottom.addView("org.eclipse.ui.console.ConsoleView"); 
        bottom.addView(IPageLayout.ID_PROBLEM_VIEW);
        
        final IFolderLayout bottomLog = layout.createFolder("bottomRight", IPageLayout.RIGHT, 0.50f, "bottom");
        bottomLog.addView(MessageLogView.ID);
        bottomLog.addView(ExecutionView.ID);

        final IFolderLayout topRight = layout.createFolder("topRight", IPageLayout.RIGHT, 0.75f, editorArea);
        topRight.addView(IPageLayout.ID_OUTLINE);

        layout.addPerspectiveShortcut("org.eclipse.debug.ui.DebugPerspective");
        layout.addShowViewShortcut(MessageLogView.ID);
        layout.addShowViewShortcut(ExecutionView.ID);

        layout.addShowInPart("org.eclipse.ui.navigator.ProjectExplorer");
	}
}
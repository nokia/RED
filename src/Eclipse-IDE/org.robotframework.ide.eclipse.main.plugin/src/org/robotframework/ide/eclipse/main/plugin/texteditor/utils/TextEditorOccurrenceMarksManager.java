package org.robotframework.ide.eclipse.main.plugin.texteditor.utils;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.source.SourceViewer;

public class TextEditorOccurrenceMarksManager {

    public static final String MARKER_ID = "org.robotframework.ide.texteditor.occurrencesMark";

    private IFile editedFile;

    private SourceViewer viewer;

    private FindReplaceDocumentAdapter findAdapter;

    private boolean isMarkVisible;

    public TextEditorOccurrenceMarksManager(SourceViewer viewer, IFile editedFile) {
        this.viewer = viewer;
        this.editedFile = editedFile;
    }

    public void showOccurrenceMarks(int offset) {

        removeOldOccurrenceMarks();

        try {
            IRegion selectedRegion = new TextEditorHoverManager().findHoveredText(viewer, offset);
            String selectedText = viewer.getDocument().get(selectedRegion.getOffset(), selectedRegion.getLength());
            findAdapter = new FindReplaceDocumentAdapter(viewer.getDocument());
            IRegion foundedRegion = null;
            int startFindOffset = 0;
            List<IRegion> foundedRegions = new ArrayList<IRegion>();
            int variableBegin = selectedText.indexOf("{");
            int variableEnd = selectedText.indexOf("}");
            if (variableBegin >= 0 && variableEnd >= 0) {
                String variableName = selectedText.substring(variableBegin + 1, variableEnd);
                while ((foundedRegion = findAdapter.find(startFindOffset, "(.[{]" + variableName + "[}])", true, true,
                        false, true)) != null) {
                    startFindOffset = foundedRegion.getOffset() + foundedRegion.getLength();
                    foundedRegions.add(foundedRegion);
                }
            } else {
                while ((foundedRegion = findAdapter.find(startFindOffset, selectedText, true, true, true, false)) != null) {
                    startFindOffset = foundedRegion.getOffset() + foundedRegion.getLength();
                    foundedRegions.add(foundedRegion);
                }
            }
            for (IRegion region : foundedRegions) {
                createMarker(region, selectedText);
            }
            isMarkVisible = true;
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    public void removeOldOccurrenceMarks() {
        if (isMarkVisible) {
            try {
                final IMarker[] markers = editedFile.findMarkers(MARKER_ID, true, IResource.DEPTH_ONE);
                for (int i = 0; i < markers.length; i++) {
                    markers[i].delete();
                }
            } catch (final CoreException e) {
                e.printStackTrace();
            }
            isMarkVisible = false;
        }
    }

    private void createMarker(IRegion region, String selectedText) {
        try {
            final IMarker marker = editedFile.createMarker(MARKER_ID);
            marker.setAttribute(IMarker.TRANSIENT, true);
            marker.setAttribute(IMarker.MESSAGE, "Occurrence of '" + selectedText + "'");
            marker.setAttribute(IMarker.CHAR_START, region.getOffset());
            marker.setAttribute(IMarker.CHAR_END, region.getOffset() + region.getLength());
        } catch (CoreException e) {
            e.printStackTrace();
        }
    }
}

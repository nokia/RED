package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import org.eclipse.jface.viewers.CellsHighlighter;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.RowExposingTableViewer;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.TableViewerFocusCellManager;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerEditor;
import org.eclipse.jface.viewers.TreeViewerFocusCellManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;

public class CellsAcivationStrategy {

    public static void addActivationStrategy(final RowExposingTableViewer viewer, final RowTabbingStrategy rowTabbing) {
        final CellsHighlighter highlighter = new CellsHighlighter(viewer);
        final TableViewerFocusCellManager fcm = new TableViewerFocusCellManager(viewer, highlighter);
        final ColumnViewerEditorActivationStrategy activationSupport = createActivationSupport(viewer);
        TableViewerEditor.create(viewer, fcm, activationSupport, createStyle(rowTabbing));
    }

    public static void addActivationStrategy(final TreeViewer viewer, final RowTabbingStrategy rowTabbing) {
        final CellsHighlighter highlighter = new CellsHighlighter(viewer);
        final TreeViewerFocusCellManager fcm = new TreeViewerFocusCellManager(viewer, highlighter);
        final ColumnViewerEditorActivationStrategy activationSupport = createActivationSupport(viewer);
        TreeViewerEditor.create(viewer, fcm, activationSupport, createStyle(rowTabbing));
    }

    private static int createStyle(final RowTabbingStrategy rowTabbing) {
        return ColumnViewerEditor.KEYBOARD_ACTIVATION | ColumnViewerEditor.TABBING_HORIZONTAL
                | ColumnViewerEditor.TABBING_VERTICAL | rowTabbing.getStyle();
    }

    private static ColumnViewerEditorActivationStrategy createActivationSupport(final ColumnViewer viewer) {
        final ColumnViewerEditorActivationStrategy activationSupport = new ColumnViewerEditorActivationStrategy(viewer) {

            @Override
            protected boolean isEditorActivationEvent(final ColumnViewerEditorActivationEvent event) {
                if (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED) {
                    if (event.character == SWT.CR || isPrintableChar(event.character)) {
                        ((KeyEvent) event.sourceEvent).doit = false;
                        return true;
                    }
                } else if (event.eventType == ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION) {
                    if (event.sourceEvent instanceof MouseEvent) {
                        return true;
                    }
                } else if (event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL) {
                    return true;
                } else if (event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC) {
                    return true;
                }
                return false;
            }

            private boolean isPrintableChar(final char character) {
                return ' ' <= character && character <= '~';
            }
        };
        activationSupport.setEnableEditorActivationWithKeyboard(true);
        return activationSupport;
    }

    public enum RowTabbingStrategy {
        MOVE_IN_CYCLE {
            @Override
            int getStyle() {
                return ColumnViewerEditor.TABBING_CYCLE_IN_ROW;
            }
        },
        MOVE_TO_NEXT {
            @Override
            int getStyle() {
                return ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR;
            }
        };

        abstract int getStyle();

    }
}

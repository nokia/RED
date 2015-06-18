package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.viewers.ActivationCharPreservingTextCellEditor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.robotframework.ide.eclipse.main.plugin.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.assist.KeywordsContentProposingSupport;
import org.robotframework.ide.eclipse.main.plugin.cmd.SetKeywordCallNameCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotElementEditingSupport;

public class KeywordCallNameEditingSupport extends RobotElementEditingSupport {

    public KeywordCallNameEditingSupport(final ColumnViewer viewer, final RobotEditorCommandsStack commandsStack,
            final NewElementsCreator creator) {
        super(viewer, 0, commandsStack, creator);
    }

    @Override
    protected CellEditor getCellEditor(final Object element) {
        final Composite parent = (Composite) getViewer().getControl();
        if (element instanceof RobotKeywordCall) {
            final RobotKeywordCall keywordCall = (RobotKeywordCall) element;
            final ActivationCharPreservingTextCellEditor editor = new ActivationCharPreservingTextCellEditor(
                    getViewer().getColumnViewerEditor(), parent, DETAILS_EDITING_CONTEXT_ID);
            editor.addContentProposalsSupport(new KeywordsContentProposingSupport(keywordCall.getSuiteFile()));
            final ControlDecoration decoration = new ControlDecoration(editor.getControl(), SWT.RIGHT | SWT.TOP);
            decoration.setDescriptionText("Press Ctrl+Space for content assist");
            decoration.setImage(FieldDecorationRegistry.getDefault()
                    .getFieldDecoration(FieldDecorationRegistry.DEC_CONTENT_PROPOSAL).getImage());

            return editor;
        }
        return super.getCellEditor(element);
    }

    @Override
    protected Object getValue(final Object element) {
        if (element instanceof RobotKeywordCall) {
            final RobotKeywordCall keywordCall = (RobotKeywordCall) element;
            return keywordCall.getName();
        }
        return "";
    }

    @Override
    protected void setValue(final Object element, final Object value) {
        if (element instanceof RobotKeywordCall) {
            final String name = (String) value;
            commandsStack.execute(new SetKeywordCallNameCommand((RobotKeywordCall) element, name));
        } else {
            super.setValue(element, value);
        }
    }
}

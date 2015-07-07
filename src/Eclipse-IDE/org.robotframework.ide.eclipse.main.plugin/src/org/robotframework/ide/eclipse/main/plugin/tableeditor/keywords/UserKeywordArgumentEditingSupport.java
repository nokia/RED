package org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords;

import java.util.List;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.viewers.ActivationCharPreservingTextCellEditor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.RowExposingTreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.robotframework.ide.eclipse.main.plugin.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.assist.KeywordsContentProposingSupport;
import org.robotframework.ide.eclipse.main.plugin.cmd.SetKeywordCallArgumentCommand;
import org.robotframework.ide.eclipse.main.plugin.cmd.SetKeywordDefinitionArgumentCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotElementEditingSupport;

class UserKeywordArgumentEditingSupport extends RobotElementEditingSupport {

    private KeywordsContentProposingSupport contentAssistSupport;

    UserKeywordArgumentEditingSupport(final RowExposingTreeViewer viewer, final int index,
            final RobotEditorCommandsStack commandsStack, final NewElementsCreator creator) {
        super(viewer, index, commandsStack, creator);
    }

    @Override
    protected CellEditor getCellEditor(final Object element) {
        final Composite parent = (Composite) getViewer().getControl();
        if (element instanceof RobotKeywordDefinition) {
            return new ActivationCharPreservingTextCellEditor(getViewer().getColumnViewerEditor(), parent,
                    DETAILS_EDITING_CONTEXT_ID);
        } else if (element instanceof RobotKeywordCall) {
            final ActivationCharPreservingTextCellEditor editor = new ActivationCharPreservingTextCellEditor(
                    getViewer().getColumnViewerEditor(), parent, DETAILS_EDITING_CONTEXT_ID);
            if (contentAssistSupport == null) {
                final RobotKeywordCall keywordCall = (RobotKeywordCall) element;
                contentAssistSupport = new KeywordsContentProposingSupport(keywordCall.getSuiteFile());
            }
            editor.addContentProposalsSupport(contentAssistSupport);
            final ControlDecoration decoration = new ControlDecoration(editor.getControl(), SWT.RIGHT | SWT.TOP);
            decoration.setDescriptionText("Press Ctrl+Space for content assist");
            decoration.setImage(FieldDecorationRegistry.getDefault()
                    .getFieldDecoration(FieldDecorationRegistry.DEC_CONTENT_PROPOSAL).getImage());
            editor.getControl().addDisposeListener(new DisposeListener() {
                @Override
                public void widgetDisposed(final DisposeEvent e) {
                    decoration.dispose();
                }
            });

            return editor;
        }
        return super.getCellEditor(element);
    }

    @Override
    protected Object getValue(final Object element) {
        List<String> arguments = null;
        if (element instanceof RobotKeywordDefinition) {
            arguments = ((RobotKeywordDefinition) element).getArguments();
        } else if (element instanceof RobotKeywordCall) {
            arguments = ((RobotKeywordCall) element).getArguments();
        }
        return arguments != null && index < arguments.size() ? arguments.get(index) : "";
    }

    @Override
    protected void setValue(final Object element, final Object value) {
        if (element instanceof RobotKeywordDefinition) {
            final String argument = (String) value;
            commandsStack.execute(new SetKeywordDefinitionArgumentCommand((RobotKeywordDefinition) element, index,
                    argument));
        } else if (element instanceof RobotKeywordCall) {
            final String argument = (String) value;
            commandsStack.execute(new SetKeywordCallArgumentCommand((RobotKeywordCall) element, index, argument));
        } else {
            super.setValue(element, value);
        }
    }

}

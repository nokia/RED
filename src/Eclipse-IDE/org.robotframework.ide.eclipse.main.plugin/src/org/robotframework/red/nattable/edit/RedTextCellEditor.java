/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.edit;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.edit.editor.TextCellEditor;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectCellCommand;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.widget.EditModeEnum;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.rf.ide.core.testdata.model.table.variables.AVariable;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences.CellCommitBehavior;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.assist.CombinedProposalsProvider;
import org.robotframework.red.jface.assist.AssistantContext;
import org.robotframework.red.jface.assist.RedContentProposal;
import org.robotframework.red.jface.assist.RedContentProposal.ModificationStrategy;
import org.robotframework.red.jface.assist.RedContentProposalAdapter;
import org.robotframework.red.jface.assist.RedContentProposalAdapter.RedContentProposalListener;
import org.robotframework.red.jface.assist.RedContentProposalProvider;
import org.robotframework.red.nattable.edit.AssistanceSupport.NatTableAssistantContext;
import org.robotframework.red.swt.SwtThread;

import com.google.common.annotations.VisibleForTesting;

/**
 * Modified version of
 * {@link org.eclipse.nebula.widgets.nattable.edit.editor.TextCellEditor} which
 * will move left/right after commits and validate entries asynchronously.
 *
 * @author Michal Anglart
 */
public class RedTextCellEditor extends TextCellEditor {

    private final int selectionStartShift;

    private final int selectionEndShift;

    private final CellEditorValueValidationJobScheduler<String> validationJobScheduler;

    private final AssistanceSupport support;

    private final boolean wrapCellContent;

    private IContextActivation contextActivation;

    static MoveDirectionEnum getMoveDirection(final EditModeEnum editMode, final KeyEvent event) {
        if (RedPlugin.getDefault().getPreferences().getCellCommitBehavior() == CellCommitBehavior.STAY_IN_SAME_CELL) {
            return MoveDirectionEnum.NONE;

        } else if (editMode == EditModeEnum.INLINE) {
            if (event.stateMask == 0) {
                return MoveDirectionEnum.RIGHT;
            } else if (event.stateMask == SWT.SHIFT) {
                return MoveDirectionEnum.LEFT;
            }
        }
        return MoveDirectionEnum.NONE;
    }

    public RedTextCellEditor(final boolean wrapCellContent, final IRowDataProvider<?> dataProvider) {
        this(0, 0, new DefaultRedCellEditorValueValidator(dataProvider), null, wrapCellContent);
    }

    public RedTextCellEditor(final RedContentProposalProvider proposalProvider, final boolean wrapCellContent,
            final IRowDataProvider<?> dataProvider) {
        this(0, 0, new DefaultRedCellEditorValueValidator(dataProvider), proposalProvider, wrapCellContent);
    }

    public RedTextCellEditor(final CellEditorValueValidator<String> validator, final boolean wrapCellContent) {
        this(0, 0, validator, null, wrapCellContent);
    }

    public RedTextCellEditor(final int selectionStartShift, final int selectionEndShift,
            final boolean wrapCellContent, final IRowDataProvider<?> dataProvider) {
        this(selectionStartShift, selectionEndShift, new DefaultRedCellEditorValueValidator(dataProvider), null,
                wrapCellContent);
    }

    public RedTextCellEditor(final int selectionStartShift, final int selectionEndShift,
            final CellEditorValueValidator<String> validator, final boolean wrapCellContent) {
        this(selectionStartShift, selectionEndShift, validator, null, wrapCellContent);
    }

    public RedTextCellEditor(final CombinedProposalsProvider proposalProvider, final boolean wrapCellContent,
            final IRowDataProvider<?> dataProvider) {
        this(0, 0, new DefaultRedCellEditorValueValidator(dataProvider), proposalProvider, wrapCellContent);
    }

    public RedTextCellEditor(final int selectionStartShift, final int selectionEndShift,
            final CellEditorValueValidator<String> validator, final RedContentProposalProvider proposalProvider,
            final boolean wrapCellContent) {
        super(true, true);
        this.selectionStartShift = selectionStartShift;
        this.selectionEndShift = selectionEndShift;
        this.wrapCellContent = wrapCellContent;
        this.support = new AssistanceSupport(proposalProvider);
        this.validationJobScheduler = new CellEditorValueValidationJobScheduler<>(validator);
    }

    @Override
    public boolean supportMultiEdit(final IConfigRegistry configRegistry, final List<String> configLabels) {
        return false;
    }

    @Override
    protected Text createEditorControl(final Composite parent, final int style) {
        final int finalStyle = wrapCellContent ? style | SWT.WRAP | SWT.MULTI : style;
        final Text textControl = new Text(parent, finalStyle);

        textControl.setBackground(this.cellStyle.getAttributeValue(CellStyleAttributes.BACKGROUND_COLOR));
        textControl.setForeground(parent.getForeground());
        textControl.setFont(parent.getFont());
        textControl.setCursor(new Cursor(Display.getDefault(), SWT.CURSOR_IBEAM));

        textControl.addKeyListener(new TextKeyListener(parent));
        textControl.addVerifyListener(new TextVerifyListener(textControl, RedPlugin.getDefault().getPreferences()));
        validationJobScheduler.armRevalidationOn(textControl, getRowIndex());

        return textControl;
    }

    @Override
    protected Control activateCell(final Composite parent, final Object originalCanonicalValue) {
        // workaround: switching off the focus listener during activation; this was
        // causing some inconsistent state where newly created cell editor was not
        // responsive
        ((InlineFocusListener) focusListener).handleFocusChanges = false;

        final Text text = (Text) super.activateCell(parent, originalCanonicalValue);

        final RedContentProposalListener assistListener = new ContentProposalsListener(
                (InlineFocusListener) focusListener);

        final AssistantContext context = new NatTableAssistantContext(getColumnIndex(), getRowIndex());
        support.install(text, context, assistListener);
        parent.redraw();

        if ((selectionStartShift > 0 || selectionEndShift > 0) && !text.isDisposed()) {
            if (text.getText().length() >= selectionStartShift + selectionEndShift) {
                text.setSelection(selectionStartShift, text.getText().length() - selectionEndShift);
            }
        }
        ((InlineFocusListener) focusListener).handleFocusChanges = true;

        final IContextService service = PlatformUI.getWorkbench().getService(IContextService.class);
        contextActivation = service.activateContext(RedPlugin.DETAILS_EDITING_CONTEXT_ID);

        return text;
    }

    @Override
    public boolean commit(final MoveDirectionEnum direction, final boolean closeAfterCommit,
            final boolean skipValidation) {
        if (validationJobScheduler.canCloseCellEditor()) {
            removeEditorControlListeners();
            final boolean committed = super.commit(direction, closeAfterCommit, skipValidation);
            if (direction == MoveDirectionEnum.NONE) {
                layerCell.getLayer().doCommand(new SelectCellCommand(layerCell.getLayer(),
                        layerCell.getColumnPosition(), layerCell.getRowPosition(), false, false));
            }
            return committed;
        } else {
            return false;
        }
    }

    @Override
    public void close() {
        super.close();

        final IContextService service = PlatformUI.getWorkbench().getService(IContextService.class);
        service.deactivateContext(contextActivation);
    }

    @VisibleForTesting
    public CellEditorValueValidationJobScheduler<String> getValidationJobScheduler() {
        return this.validationJobScheduler;
    }

    private class TextKeyListener extends KeyAdapter {

        private final Composite parent;

        private TextKeyListener(final Composite parent) {
            this.parent = parent;
        }

        @Override
        public void keyPressed(final KeyEvent event) {
            if (support.areContentProposalsShown()) {
                return;
            }

            if (commitOnEnter && (event.keyCode == SWT.CR || event.keyCode == SWT.KEYPAD_CR)) {
                final boolean commit = event.stateMask != SWT.ALT;
                if (commit) {
                    final boolean committed = commit(getMoveDirection(editMode, event));
                    if (!committed && wrapCellContent) {
                        // when there is multiline Text control we don't want to
                        // have new lines there, so cannot deliver this event to
                        // control
                        event.doit = false;
                    }
                }
                if (editMode == EditModeEnum.DIALOG) {
                    parent.forceFocus();
                }
            } else if (event.keyCode == SWT.ESC && event.stateMask == 0) {
                close();
            } else if (editMode == EditModeEnum.INLINE && !wrapCellContent) {
                if (event.keyCode == SWT.ARROW_UP) {
                    commit(MoveDirectionEnum.UP);
                } else if (event.keyCode == SWT.ARROW_DOWN) {
                    commit(MoveDirectionEnum.DOWN);
                }
            }
        }

        @Override
        public void keyReleased(final KeyEvent e) {
            try {
                final Object canonicalValue = getCanonicalValue(getInputConversionErrorHandler());
                validateCanonicalValue(canonicalValue, getInputValidationErrorHandler());
            } catch (final Exception ex) {
                // do nothing
            }
        }
    }

    private class TextVerifyListener implements VerifyListener {

        private final Text control;

        private final boolean isInsertionEnabled;

        private final boolean isWrappingEnabled;

        private final String wrappingPattern;

        private TextVerifyListener(final Text control, final RedPreferences preferences) {
            this.control = control;
            this.isInsertionEnabled = preferences.isVariablesBracketsInsertionEnabled();
            this.isWrappingEnabled = preferences.isVariablesBracketsInsertionWrappingEnabled();
            this.wrappingPattern = preferences.getVariablesBracketsInsertionWrappingPattern();
        }

        @Override
        public void verifyText(final VerifyEvent event) {
            if (support.areContentProposalsShown() || !isInsertionEnabled) {
                return;
            }

            if (AVariable.ROBOT_VAR_IDENTIFICATORS.contains(event.text)
                    && !"{".equals(control.getText(event.start, event.end))) {
                final String selectedText = control.getText(event.start, event.end - 1);
                if (selectedText.isEmpty() || isWrappingEnabled && selectedText.matches(wrappingPattern)) {
                    addVariableBrackets(event);
                }
            } else if ((event.keyCode == SWT.BS || event.keyCode == SWT.DEL) && event.start == event.end - 1) {
                final String bracketsCandidate = control.getText(event.start, event.end);
                final String varTypeCandidate = control.getText(event.start - 1, event.end - 2);
                if ("{}".equals(bracketsCandidate) && AVariable.ROBOT_VAR_IDENTIFICATORS.contains(varTypeCandidate)) {
                    deleteVariableBrackets(event);
                }
            }
        }

        private void addVariableBrackets(final VerifyEvent event) {
            final String wrappedText = "{" + control.getText(event.start, event.end - 1) + "}";
            event.text += wrappedText;

            SwtThread.asyncExec(event.display, () -> control.setSelection(event.start + wrappedText.length()));
        }

        private void deleteVariableBrackets(final VerifyEvent event) {
            final String prefix = control.getText(0, event.start);
            final String suffix = control.getText().substring(event.end + 1);

            // when empty brackets are deleted we want to set control text and selection ourself,
            // so cannot deliver this event to control
            event.doit = false;

            SwtThread.asyncExec(event.display, () -> {
                control.setText(prefix + suffix);
                control.setSelection(event.start);
            });
        }
    }

    private class ContentProposalsListener implements RedContentProposalListener {

        private final InlineFocusListener focusListener;

        public ContentProposalsListener(final InlineFocusListener focusListener) {
            this.focusListener = focusListener;
        }

        @Override
        public void proposalPopupOpened(final RedContentProposalAdapter adapter) {
            // under GTK2 when user double-clicks proposal the focus is lost which
            // results in editor closing
            focusListener.handleFocusChanges = false;
            removeEditorControlListeners();
        }

        @Override
        public void proposalPopupClosed(final RedContentProposalAdapter adapter) {
            // due to GTK2 issue we're queuing new runnable to be executed after all
            // currently waiting operations in order to regain focus to text control
            // end enable focus changes handling once again
            SwtThread.asyncExec(() -> {
                final Text editorControl = getEditorControl();
                if (editorControl != null && !editorControl.isDisposed()) {
                    editorControl.forceFocus();
                }
                focusListener.handleFocusChanges = true;
            });
            addEditorControlListeners();
        }

        @Override
        public void proposalAccepted(final RedContentProposal proposal) {
            for (final Runnable operation : proposal.getOperationsToPerformAfterAccepting()) {
                operation.run();
            }
            final ModificationStrategy modificationStrategy = proposal.getModificationStrategy();
            if (modificationStrategy.shouldSelectAllAfterInsert()) {
                RedTextCellEditor.this.getEditorControl().selectAll();
            }
            if (modificationStrategy.shouldCommitAfterInsert()) {
                commit(getCommitMoveDirection());
            }
        }

        private MoveDirectionEnum getCommitMoveDirection() {
            return RedPlugin.getDefault()
                    .getPreferences()
                    .getCellCommitBehavior() == CellCommitBehavior.STAY_IN_SAME_CELL ? MoveDirectionEnum.NONE
                            : MoveDirectionEnum.RIGHT;
        }
    }
}

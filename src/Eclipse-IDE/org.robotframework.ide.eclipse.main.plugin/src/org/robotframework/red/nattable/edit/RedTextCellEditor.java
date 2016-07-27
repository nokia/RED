/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.edit;

import java.util.List;

import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.viewers.IContentProposingSupport;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.edit.editor.TextCellEditor;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.widget.EditModeEnum;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences.CellCommitBehavior;
import org.robotframework.red.jface.assist.RedContentProposalAdapter;
import org.robotframework.red.jface.assist.RedContentProposalAdapter.RedContentProposalListener;

import com.google.common.base.Optional;

/**
 * Modified version of {@link org.eclipse.nebula.widgets.nattable.edit.editor.TextCellEditor} which
 * will move left/right after commits and validate entries asynchronously.
 *
 * @author Michal Anglart
 */
public class RedTextCellEditor extends TextCellEditor {

    public static final String DETAILS_EDITING_CONTEXT_ID = "org.robotframework.ide.eclipse.details.context";

    private final int selectionStartShift;

    private final int selectionEndShift;

    private final CellEditorValueValidationJobScheduler<String> validationJobScheduler;

    private final AssistanceSupport support;

    private IContextActivation contextActivation;

    public RedTextCellEditor() {
        this(0, 0, new DefaultRedCellEditorValueValidator(), null);
    }

    public RedTextCellEditor(final IContentProposingSupport support) {
        this(0, 0, new DefaultRedCellEditorValueValidator(), support);
    }

    public RedTextCellEditor(final CellEditorValueValidator<String> validator) {
        this(0, 0, validator, null);
    }

    public RedTextCellEditor(final int selectionStartShift, final int selectionEndShift) {
        this(selectionStartShift, selectionEndShift, new DefaultRedCellEditorValueValidator(), null);
    }

    public RedTextCellEditor(final int selectionStartShift, final int selectionEndShift,
            final CellEditorValueValidator<String> validator) {
        this(selectionStartShift, selectionEndShift, validator, null);
    }

    public RedTextCellEditor(final int selectionStartShift, final int selectionEndShift,
            final CellEditorValueValidator<String> validator, final IContentProposingSupport support) {
        super(true, true);
        this.selectionStartShift = selectionStartShift;
        this.selectionEndShift = selectionEndShift;
        this.support = new AssistanceSupport(support);
        this.validationJobScheduler = new CellEditorValueValidationJobScheduler<>(validator);
    }

    @Override
    public boolean supportMultiEdit(final IConfigRegistry configRegistry, final List<String> configLabels) {
        return false;
    }

    @Override
    protected Text createEditorControl(final Composite parent, final int style) {
        final Text textControl = new Text(parent, style);

        textControl.setBackground(this.cellStyle.getAttributeValue(CellStyleAttributes.BACKGROUND_COLOR));
        textControl.setForeground(this.cellStyle.getAttributeValue(CellStyleAttributes.FOREGROUND_COLOR));
        textControl.setFont(this.cellStyle.getAttributeValue(CellStyleAttributes.FONT));
        textControl.setCursor(new Cursor(Display.getDefault(), SWT.CURSOR_IBEAM));

        textControl.addKeyListener(new TextKeyListener(parent));

        return textControl;
    }

    @Override
    protected Control activateCell(final Composite parent, final Object originalCanonicalValue) {
        // workaround: switching off the focus listener during activation; this was causing
        // some incosistent state where newly created cell editor was not responsive
        ((InlineFocusListener) focusListener).handleFocusChanges = false;

        final Text text = (Text) super.activateCell(parent, originalCanonicalValue);

        validationJobScheduler.armRevalidationOn(text);

        final RedContentProposalListener assistListener = new ContentProposalsListener();
        support.install(text, Optional.of(assistListener), RedContentProposalAdapter.PROPOSAL_SHOULD_INSERT);
        parent.redraw();

        if ((selectionStartShift > 0 || selectionEndShift > 0) && !text.isDisposed()) {
            if (text.getText().length() >= selectionStartShift + selectionEndShift) {
                text.setSelection(selectionStartShift, text.getText().length() - selectionEndShift);
            }
        }
        ((InlineFocusListener) focusListener).handleFocusChanges = true;

        final IContextService service = (IContextService) PlatformUI.getWorkbench().getService(IContextService.class);
        contextActivation = service.activateContext(RedPlugin.DETAILS_EDITING_CONTEXT_ID);

        return text;
    }

    @Override
    public boolean commit(final MoveDirectionEnum direction, final boolean closeAfterCommit,
            final boolean skipValidation) {
        if (validationJobScheduler.canCloseCellEditor()) {
            return super.commit(direction, closeAfterCommit, skipValidation);
        } else {
            return false;
        }
    }

    @Override
    public void close() {
        super.close();

        final IContextService service = (IContextService) PlatformUI.getWorkbench().getService(IContextService.class);
        service.deactivateContext(contextActivation);
    }

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
                    commit(getMoveDirection(event));
                }
                if (RedTextCellEditor.this.editMode == EditModeEnum.DIALOG) {
                    parent.forceFocus();
                }
            } else if (event.keyCode == SWT.ESC && event.stateMask == 0) {
                close();
            } else if (RedTextCellEditor.this.editMode == EditModeEnum.INLINE) {
                if (event.keyCode == SWT.ARROW_UP) {
                    commit(MoveDirectionEnum.UP);
                } else if (event.keyCode == SWT.ARROW_DOWN) {
                    commit(MoveDirectionEnum.DOWN);
                }
            }
        }

        private MoveDirectionEnum getMoveDirection(final KeyEvent event) {
            if (RedPlugin.getDefault()
                    .getPreferences()
                    .getCellCommitBehavior() == CellCommitBehavior.STAY_IN_SAME_CELL) {
                return MoveDirectionEnum.NONE;
            }

            if (RedTextCellEditor.this.editMode == EditModeEnum.INLINE) {
                if (event.stateMask == 0) {
                    return MoveDirectionEnum.RIGHT;
                } else if (event.stateMask == SWT.SHIFT) {
                    return MoveDirectionEnum.LEFT;
                }
            }
            return MoveDirectionEnum.NONE;
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

    private class ContentProposalsListener implements RedContentProposalListener {

        @Override
        public void proposalPopupOpened(final RedContentProposalAdapter adapter) {
            RedTextCellEditor.this.removeEditorControlListeners();
        }

        @Override
        public void proposalPopupClosed(final RedContentProposalAdapter adapter) {
            RedTextCellEditor.this.addEditorControlListeners();
        }

        @Override
        public void proposalAccepted(final IContentProposal proposal) {
            // nothing to do
        }
    }
}

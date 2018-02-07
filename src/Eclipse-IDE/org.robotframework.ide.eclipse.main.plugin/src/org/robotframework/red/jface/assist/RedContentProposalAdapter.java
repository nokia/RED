/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.jface.assist;

import java.util.ArrayList;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TooltipsEnablingDelegatingStyledCellLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.viewers.RedCommonLabelProvider;
import org.robotframework.red.viewers.StructuredContentProvider;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

/**
 * This is highly customized version of org.eclipse.jface.fieldassist.ContentProposalAdapter for our
 * needs. Most of the code comes from mentioned class.
 */
public class RedContentProposalAdapter {

    public static final int PROPOSAL_SHOULD_INSERT = 1;
    public static final int PROPOSAL_SHOULD_REPLACE = 2;

    /** See explanation for {@link org.eclipse.jface.fieldassist.ContentProposalAdapter.USE_VIRTUAL} */
    private static final boolean USE_VIRTUAL = !Util.isMotif();

    private static final int SECONDARY_POPUP_DELAY = 750;

    private static final int POPUP_CHAR_HEIGHT = 10;
    private static final int POPUP_MINIMUM_WIDTH = 300;
    private static final int POPUP_OFFSET = 3;

    private final ListenerList<Object> proposalListeners = new ListenerList<>();

    private final RedContentProposalProvider proposalProvider;
    private final ILabelProvider labelProvider;

    private final Control control;
    private final AssistantContext context;
    private final RedControlContentAdapter controlContentAdapter;

    private ContentProposalPopup popup;

    private final KeySequence activationTrigger;

    private final KeyStroke triggerKeyStroke;

    private final String autoActivateString;

    private final int proposalAcceptanceStyle;

    private Listener controlListener;

    private final int autoActivationDelay;

    private boolean receivedKeyDown;

    private Point popupSize;

    private int insertionPos = -1;

    private Point selectionRange = new Point(-1, -1);

    private boolean watchModify = false;

    public static RedContentProposalAdapter install(final Text text, final AssistantContext context,
            final RedContentProposalProvider proposalsProvider, final RedContentProposalListener listener) {

        final RedContentProposalAdapter adapter = install(text, context, proposalsProvider);
        if (adapter != null) {
            adapter.addContentProposalListener(listener);
        }
        return adapter;
    }

    public static RedContentProposalAdapter install(final Text text, final AssistantContext context,
            final RedContentProposalProvider proposalsProvider) {

        final RedPreferences preferences = RedPlugin.getDefault().getPreferences();

        final RedControlContentAdapter controlAdapter = new RedTextContentAdapter();
        final char[] activationChars = preferences.getAssistantAutoActivationChars();
        final int autoActivationDelay = preferences.getAssistantAutoActivationDelay();

        final KeySequence activationTrigger = getContentAssistActivationTrigger();
        if (activationTrigger != null) {
            final RedContentProposalAdapter adapter = new RedContentProposalAdapter(text, context, controlAdapter,
                    proposalsProvider, activationTrigger, activationChars, autoActivationDelay,
                    RedContentProposalAdapter.PROPOSAL_SHOULD_INSERT);
            adapter.install();
            return adapter;
        }
        return null;
    }

    public static RedContentProposalAdapter install(final Combo combo,
            final RedContentProposalProvider proposalsProvider) {

        final RedPreferences preferences = RedPlugin.getDefault().getPreferences();

        final RedControlContentAdapter controlAdapter = new RedComboContentAdapter();
        final char[] activationChars = preferences.getAssistantAutoActivationChars();
        final int autoActivationDelay = preferences.getAssistantAutoActivationDelay();

        final KeySequence activationTrigger = getContentAssistActivationTrigger();
        if (activationTrigger != null) {
            final RedContentProposalAdapter adapter = new RedContentProposalAdapter(combo, null, controlAdapter,
                    proposalsProvider, activationTrigger, activationChars, autoActivationDelay,
                    RedContentProposalAdapter.PROPOSAL_SHOULD_INSERT);
            adapter.install();
            return adapter;
        }
        return null;
    }

    private static KeySequence getContentAssistActivationTrigger() {
        final IBindingService service = PlatformUI.getWorkbench().getAdapter(IBindingService.class);
        return (KeySequence) service.getBestActiveBindingFor(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
    }

    public static void markControlWithDecoration(final RedContentProposalAdapter adapter) {
        final Control control = adapter.control;
        final ControlDecoration decoration = new ControlDecoration(control, SWT.RIGHT | SWT.TOP);
        decoration.setDescriptionText(String.format("Press %s for content assist", adapter.activationTrigger.format()));
        decoration.setImage(FieldDecorationRegistry.getDefault()
                .getFieldDecoration(FieldDecorationRegistry.DEC_CONTENT_PROPOSAL)
                .getImage());
        control.addDisposeListener(e -> decoration.dispose());
    }

    private RedContentProposalAdapter(final Control control, final AssistantContext context,
            final RedControlContentAdapter controlContentAdapter, final RedContentProposalProvider proposalProvider,
            final KeySequence activationTrigger, final char[] autoActivationCharacters, final int autoActivationDelay,
            final int acceptanceStyle) {
        this.control = Preconditions.checkNotNull(control);
        this.context = context;
        this.controlContentAdapter = Preconditions.checkNotNull(controlContentAdapter);
        this.labelProvider = new TooltipsEnablingDelegatingStyledCellLabelProvider(new ProposalsLabelProvider());

        this.proposalProvider = proposalProvider;
        this.activationTrigger = activationTrigger;
        this.triggerKeyStroke = determineTriggerKeyStroke(activationTrigger);
        this.autoActivateString = new String(autoActivationCharacters).intern();
        this.autoActivationDelay = autoActivationDelay;
        this.proposalAcceptanceStyle = acceptanceStyle;
    }

    private KeyStroke determineTriggerKeyStroke(final KeySequence activationTrigger) {
        if (activationTrigger.getKeyStrokes().length > 0) {
            final KeyStroke firstKeyStroke = activationTrigger.getKeyStrokes()[0];
            final int modifierKeys = firstKeyStroke.getModifierKeys();
            final int naturalKey = Character.toLowerCase(firstKeyStroke.getNaturalKey());
            return KeyStroke.getInstance(modifierKeys, naturalKey);
        }
        return null;
    }

    private void addContentProposalListener(final RedContentProposalListener listener) {
        proposalListeners.add(listener);
    }

    private void install() {
        addControlListener(control);
    }

    public void uninstall() {
        removeControlListener(control);
    }

    /*
     * Add our listener to the control. Debug information to be left in until
     * this support is stable on all platforms.
     */
    private void addControlListener(final Control control) {
        if (controlListener != null) {
            return;
        }
        controlListener = new Listener() {

            @Override
            public void handleEvent(final Event e) {
                switch (e.type) {
                    case SWT.Traverse:
                    case SWT.KeyDown:
                        // If the popup is open, it gets first shot at the
                        // keystroke and should set the doit flags appropriately.
                        if (popup != null) {
                            if (triggerKeyStroke.getNaturalKey() == e.keyCode
                                    && (triggerKeyStroke.getModifierKeys() & e.stateMask) == triggerKeyStroke
                                            .getModifierKeys()) {
                                // do not propagate when triggers with modifiers are used
                                e.doit = false;
                            } else {
                                popup.getTargetControlListener().handleEvent(e);
                            }

                            // See
                            // https://bugs.eclipse.org/bugs/show_bug.cgi?id=192633
                            // If the popup is open and this is a valid character,
                            // we
                            // want to watch for the modified text.
                            if (e.character != 0) {
                                watchModify = true;
                            }
                            return;
                        }

                        // We were only listening to traverse events for the popup
                        if (e.type == SWT.Traverse) {
                            return;
                        }

                        // The popup is not open. We are looking at keydown events
                        // for a trigger to open the popup.
                        if (triggerKeyStroke != null) {
                            // Either there are no modifiers for the trigger and we
                            // check the character field...
                            if ((triggerKeyStroke.getModifierKeys() == KeyStroke.NO_KEY
                                    && triggerKeyStroke.getNaturalKey() == e.character) ||
                                    // ...or there are modifiers, in which case the
                                    // keycode and state must match
                            (triggerKeyStroke.getNaturalKey() == e.keyCode
                                    && ((triggerKeyStroke.getModifierKeys() & e.stateMask) == triggerKeyStroke
                                            .getModifierKeys()))) {
                                // We never propagate the keystroke for an explicit
                                // keystroke invocation of the popup
                                e.doit = false;
                                openProposalPopup(false);
                                return;
                            }
                        }
                        /*
                         * The triggering keystroke was not invoked. If a character
                         * was typed, compare it to the autoactivation characters.
                         */
                        if (e.character != 0) {
                            if (autoActivateString != null) {
                                if (autoActivateString.indexOf(e.character) >= 0) {
                                    autoActivate();
                                } else {
                                    // No autoactivation occurred, so record the key
                                    // down as a means to interrupt any
                                    // autoactivation that is pending due to
                                    // autoactivation delay.
                                    receivedKeyDown = true;
                                    // watch the modify so we can close the popup in
                                    // cases where there is no longer a trigger
                                    // character in the content
                                    watchModify = true;
                                }
                            } else {
                                // The autoactivate string is null. If the trigger
                                // is also null, we want to act on any modification
                                // to the content. Set a flag so we'll catch this
                                // in the modify event.
                                if (triggerKeyStroke == null) {
                                    watchModify = true;
                                }
                            }
                        } else {
                            // A non-character key has been pressed. Interrupt any
                            // autoactivation that is pending due to autoactivation
                            // delay.
                            receivedKeyDown = true;
                        }
                        break;

                    // There are times when we want to monitor content changes
                    // rather than individual keystrokes to determine whether
                    // the popup should be closed or opened based on the entire
                    // content of the control.
                    // The watchModify flag ensures that we don't autoactivate if
                    // the content change was caused by something other than typing.
                    // See https://bugs.eclipse.org/bugs/show_bug.cgi?id=183650
                    case SWT.Modify:
                        if (allowsAutoActivate() && watchModify) {
                            watchModify = false;
                            // We are in autoactivation mode, either for specific
                            // characters or for all characters. In either case,
                            // we should close the proposal popup when there is no
                            // content in the control.
                            if (isControlContentEmpty()) {
                                // see
                                // https://bugs.eclipse.org/bugs/show_bug.cgi?id=192633
                                closeProposalPopup();
                            } else {
                                // See
                                // https://bugs.eclipse.org/bugs/show_bug.cgi?id=147377
                                // Given that we will close the popup when there are
                                // no valid proposals, we must consider reopening it
                                // on any
                                // content change when there are no particular
                                // autoActivation
                                // characters
                                if (autoActivateString == null) {
                                    autoActivate();
                                } else {
                                    // Autoactivation characters are defined, but
                                    // this
                                    // modify event does not involve one of them.
                                    // See
                                    // if any of the autoactivation characters are
                                    // left
                                    // in the content and close the popup if none
                                    // remain.
                                    if (!shouldPopupRemainOpen()) {
                                        closeProposalPopup();
                                    }
                                }
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
        };
        if (!control.isDisposed()) {
            control.addListener(SWT.KeyDown, controlListener);
            control.addListener(SWT.Traverse, controlListener);
            control.addListener(SWT.Modify, controlListener);
        }
    }

    private void removeControlListener(final Control control) {
        if (!control.isDisposed()) {
            control.removeListener(SWT.KeyDown, controlListener);
            control.removeListener(SWT.Traverse, controlListener);
            control.removeListener(SWT.Modify, controlListener);
        }
    }

    /**
     * Open the proposal popup and display the proposals provided by the
     * proposal provider. If there are no proposals to be shown, do not show the
     * popup. This method returns immediately. That is, it does not wait for the
     * popup to open or a proposal to be selected.
     *
     * @param autoActivated
     *            a boolean indicating whether the popup was autoactivated. If
     *            false, a beep will sound when no proposals can be shown.
     */
    private void openProposalPopup(final boolean autoActivated) {
        if (isValid()) {
            if (popup == null) {
                // Check whether there are any proposals to be shown.
                recordCursorPosition(); // must be done before getting proposals
                final RedContentProposal[] proposals = getProposals();
                if (proposals == null)
                    return;
                if (proposals.length > 0) {
                    recordCursorPosition();
                    popup = new ContentProposalPopup(null, proposals);
                    popup.open();
                    popup.getShell().addDisposeListener(event -> popup = null);
                    internalPopupOpened();
                    notifyPopupOpened();
                } else if (!autoActivated) {
                    control.getDisplay().beep();
                }
            }
        }
    }

    private void closeProposalPopup() {
        if (popup != null) {
            popup.close();
        }
    }

    /*
     * A content proposal has been accepted. Update the control contents
     * accordingly and notify any listeners.
     * @param proposal the accepted proposal
     */
    private void proposalAccepted(final RedContentProposal proposal) {
        switch (proposalAcceptanceStyle) {
            case PROPOSAL_SHOULD_REPLACE:
                setControlContent(proposal);
                break;
            case PROPOSAL_SHOULD_INSERT:
                insertControlContent(proposal);
                break;
            default:
                // do nothing. Typically a listener is installed to handle this in
                // a custom way.
                break;
        }
        notifyProposalAccepted(proposal);
    }

    /*
     * Set the text content of the control to the specified text, setting the
     * cursorPosition at the desired location within the new contents.
     */
    private void setControlContent(final RedContentProposal proposal) {
        if (isValid()) {
            // should already be false, but just in case.
            watchModify = false;
            controlContentAdapter.setControlContents(control, proposal);
        }
    }

    /*
     * Insert the specified text into the control content, setting the
     * cursorPosition at the desired location within the new contents.
     */
    private void insertControlContent(final RedContentProposal proposal) {
        if (isValid()) {
            // should already be false, but just in case.
            watchModify = false;
            // Not all controls preserve their selection index when they lose
            // focus, so we must set it explicitly here to what it was before
            // the popup opened.
            // See https://bugs.eclipse.org/bugs/show_bug.cgi?id=127108
            // See https://bugs.eclipse.org/bugs/show_bug.cgi?id=139063
            if (selectionRange.x != -1) {
                controlContentAdapter.setSelection(control, selectionRange);
            } else if (insertionPos != -1) {
                controlContentAdapter.setCursorPosition(control, insertionPos);
            }
            controlContentAdapter.insertControlContents(control, proposal);
        }
    }

    /*
     * Check that the control and content adapter are valid.
     */
    private boolean isValid() {
        return control != null && !control.isDisposed() && controlContentAdapter != null;
    }

    /*
     * Record the control's cursor position.
     */
    private void recordCursorPosition() {
        if (isValid()) {
            insertionPos = controlContentAdapter.getCursorPosition(control);
            // see https://bugs.eclipse.org/bugs/show_bug.cgi?id=139063
            selectionRange = controlContentAdapter.getSelection(control);
        }
    }

    /*
     * Get the proposals from the proposal provider. Gets all of the proposals
     * without doing any filtering.
     */
    private RedContentProposal[] getProposals() {
        if (proposalProvider == null || !isValid()) {
            return null;
        }
        int position = insertionPos;
        if (position == -1) {
            position = controlContentAdapter.getCursorPosition(control);
        }
        final String contents = controlContentAdapter.getControlContents(control);
        return proposalProvider.getProposals(contents, position, context);
    }

    /**
     * Autoactivation has been triggered. Open the popup using any specified
     * delay.
     */
    private void autoActivate() {
        if (autoActivationDelay > 0) {
            final Runnable runnable = () -> {
                receivedKeyDown = false;
                try {
                    Thread.sleep(autoActivationDelay);
                } catch (final InterruptedException e) {
                }
                if (!isValid() || receivedKeyDown) {
                    return;
                }
                control.getDisplay().syncExec(() -> openProposalPopup(true));
            };
            final Thread t = new Thread(runnable);
            t.start();
        } else {
            // Since we do not sleep, we must open the popup
            // in an async exec. This is necessary because
            // this method may be called in the middle of handling
            // some event that will cause the cursor position or
            // other important info to change as a result of this
            // event occurring.
            control.getDisplay().asyncExec(() -> {
                if (isValid()) {
                    openProposalPopup(true);
                }
            });
        }
    }

    /*
     * Return whether the control content is empty
     */
    private boolean isControlContentEmpty() {
        return controlContentAdapter.getControlContents(control).length() == 0;
    }

    /*
     * The popup has just opened, but listeners have not yet been notified.
     * Perform any cleanup that is needed.
     */
    private void internalPopupOpened() {
        // see https://bugs.eclipse.org/bugs/show_bug.cgi?id=243612
        if (control instanceof Combo) {
            ((Combo) control).setListVisible(false);
        }
    }

    /*
     * Return whether a proposal popup should remain open. If it was
     * autoactivated by specific characters, and none of those characters
     * remain, then it should not remain open. This method should not be used to
     * determine whether autoactivation has occurred or should occur, only
     * whether the circumstances would dictate that a popup remain open.
     */
    private boolean shouldPopupRemainOpen() {
        // If we always autoactivate or never autoactivate, it should remain
        // open
        if (autoActivateString == null || autoActivateString.length() == 0) {
            return true;
        }
        final String content = controlContentAdapter.getControlContents(control);
        for (int i = 0; i < autoActivateString.length(); i++) {
            if (content.indexOf(autoActivateString.charAt(i)) >= 0) {
                return true;
            }
        }
        return false;
    }

    /*
     * Return whether this adapter is configured for autoactivation, by specific
     * characters or by any characters.
     */
    private boolean allowsAutoActivate() {
        // there are specific autoactivation chars supplied
        // or we autoactivate on everything
        return (autoActivateString != null && autoActivateString.length() > 0)
                || (autoActivateString == null && triggerKeyStroke == null);
    }

    /**
     * Answers a boolean indicating whether the main proposal popup is open.
     *
     * @return <code>true</code> if the proposal popup is open, and
     *         <code>false</code> if it is not.
     * @since 3.6
     */
    public boolean isProposalPopupOpen() {
        return isValid() && popup != null;
    }

    /*
     * The lightweight popup used to show content proposals for a text field. If
     * additional information exists for a proposal, then selecting that
     * proposal will result in the information being displayed in a secondary
     * popup.
     */
    class ContentProposalPopup extends PopupDialog {

        /*
         * The listener we install on the popup and related controls to
         * determine when to close the popup. Some events (move, resize, close,
         * deactivate) trigger closure as soon as they are received, simply
         * because one of the registered listeners received them. Other events
         * depend on additional circumstances.
         */
        private final class PopupCloserListener implements Listener {
            private boolean scrollbarClicked = false;

            @Override
            public void handleEvent(final Event e) {

                // If focus is leaving an important widget or the field's
                // shell is deactivating
                if (e.type == SWT.FocusOut) {
                    scrollbarClicked = false;
                    /*
                     * Ignore this event if it's only happening because focus is
                     * moving between the popup shells, their controls, or a
                     * scrollbar. Do this in an async since the focus is not
                     * actually switched when this event is received.
                     */
                    e.display.asyncExec(() -> {
                        if (popupExists()) {
                            if (scrollbarClicked || hasFocus()) {
                                return;
                            }
                            // Workaround a problem on X and Mac, whereby at
                            // this point, the focus control is not known.
                            // This can happen, for example, when resizing
                            // the popup shell on the Mac.
                            // Check the active shell.
                            final Shell activeShell = e.display.getActiveShell();
                            if (activeShell == getShell()
                                    || (infoPopup != null && infoPopup.getShell() == activeShell)) {
                                return;
                            }
                            close();
                        }
                    });
                    return;
                }

                // Scroll bar has been clicked. Remember this for focus event
                // processing.
                if (e.type == SWT.Selection) {
                    scrollbarClicked = true;
                    return;
                }
                // For all other events, merely getting them dictates closure.
                close();
            }

            // Install the listeners for events that need to be monitored for
            // popup closure.
            void installListeners() {
                // Listeners on this popup's table and scroll bar
                proposalTableViewer.getTable().addListener(SWT.FocusOut, this);
                final ScrollBar scrollbar = proposalTableViewer.getTable().getVerticalBar();
                if (scrollbar != null) {
                    scrollbar.addListener(SWT.Selection, this);
                }

                // Listeners on this popup's shell
                getShell().addListener(SWT.Deactivate, this);
                getShell().addListener(SWT.Close, this);

                // Listeners on the target control
                control.addListener(SWT.MouseDoubleClick, this);
                control.addListener(SWT.MouseDown, this);
                control.addListener(SWT.Dispose, this);
                control.addListener(SWT.FocusOut, this);
                // Listeners on the target control's shell
                final Shell controlShell = control.getShell();
                controlShell.addListener(SWT.Move, this);
                controlShell.addListener(SWT.Resize, this);

            }

            // Remove installed listeners
            void removeListeners() {
                if (popupExists()) {
                    proposalTableViewer.getTable().removeListener(SWT.FocusOut, this);
                    final ScrollBar scrollbar = proposalTableViewer.getTable().getVerticalBar();
                    if (scrollbar != null) {
                        scrollbar.removeListener(SWT.Selection, this);
                    }

                    getShell().removeListener(SWT.Deactivate, this);
                    getShell().removeListener(SWT.Close, this);
                }

                if (control != null && !control.isDisposed()) {

                    control.removeListener(SWT.MouseDoubleClick, this);
                    control.removeListener(SWT.MouseDown, this);
                    control.removeListener(SWT.Dispose, this);
                    control.removeListener(SWT.FocusOut, this);

                    final Shell controlShell = control.getShell();
                    controlShell.removeListener(SWT.Move, this);
                    controlShell.removeListener(SWT.Resize, this);
                }
            }
        }

        /*
         * The listener we will install on the target control.
         */
        private final class TargetControlListener implements Listener {
            // Key events from the control
            @Override
            public void handleEvent(final Event e) {
                if (!popupExists()) {
                    return;
                }

                final char key = e.character;

                // Traverse events are handled depending on whether the
                // event has a character.
                if (e.type == SWT.Traverse) {
                    // If the traverse event contains a legitimate character,
                    // then we must set doit false so that the widget will
                    // receive the key event. We return immediately so that
                    // the character is handled only in the key event.
                    // See https://bugs.eclipse.org/bugs/show_bug.cgi?id=132101
                    if (key != 0) {
                        e.doit = false;
                        return;
                    }
                    // Traversal does not contain a character. Set doit true
                    // to indicate TRAVERSE_NONE will occur and that no key
                    // event will be triggered. We will check for navigation
                    // keys below.
                    e.detail = SWT.TRAVERSE_NONE;
                    e.doit = true;
                } else {
                    // Default is to only propagate when configured that way.
                    // Some keys will always set doit to false anyway.
                    e.doit = true;
                }

                // No character. Check for navigation keys.

                if (key == 0) {
                    int newSelection = proposalTableViewer.getTable().getSelectionIndex();
                    final int visibleRows = (proposalTableViewer.getTable().getSize().y
                            / proposalTableViewer.getTable().getItemHeight()) - 1;
                    switch (e.keyCode) {
                        case SWT.ARROW_UP:
                            newSelection -= 1;
                            if (newSelection < 0) {
                                newSelection = proposalTableViewer.getTable().getItemCount() - 1;
                            }
                            // Not typical - usually we get this as a Traverse and
                            // therefore it never propagates. Added for consistency.
                            e.doit = e.doit && e.type != SWT.KeyDown;
                            break;

                        case SWT.ARROW_DOWN:
                            newSelection += 1;
                            if (newSelection > proposalTableViewer.getTable().getItemCount() - 1) {
                                newSelection = 0;
                            }

                            // Not typical - usually we get this as a Traverse and
                            // therefore it never propagates. Added for consistency.
                            e.doit = e.doit && e.type != SWT.KeyDown;

                            break;

                        case SWT.PAGE_DOWN:
                            newSelection += visibleRows;
                            if (newSelection >= proposalTableViewer.getTable().getItemCount()) {
                                newSelection = proposalTableViewer.getTable().getItemCount() - 1;
                            }
                            e.doit = e.doit && e.type != SWT.KeyDown;
                            break;

                        case SWT.PAGE_UP:
                            newSelection -= visibleRows;
                            if (newSelection < 0) {
                                newSelection = 0;
                            }
                            e.doit = e.doit && e.type != SWT.KeyDown;
                            break;

                        case SWT.HOME:
                            newSelection = 0;
                            e.doit = e.doit && e.type != SWT.KeyDown;
                            break;

                        case SWT.END:
                            newSelection = proposalTableViewer.getTable().getItemCount() - 1;
                            e.doit = e.doit && e.type != SWT.KeyDown;
                            break;

                        // If received as a Traverse, these should propagate
                        // to the control as keydown. If received as a keydown,
                        // proposals should be recomputed since the cursor
                        // position has changed.
                        case SWT.ARROW_LEFT:
                        case SWT.ARROW_RIGHT:
                            if (e.type == SWT.Traverse) {
                                e.doit = false;
                            } else {
                                e.doit = true;
                                final String contents = controlContentAdapter.getControlContents(control);
                                // If there are no contents, changes in cursor
                                // position have no effect. Note also that we do
                                // not affect the filter text on ARROW_LEFT as
                                // we would with BS.
                                if (contents.length() > 0) {
                                    asyncRecomputeProposals(filterText);
                                }
                            }
                            break;

                        // Any unknown keycodes will cause the popup to close.
                        // Modifier keys are explicitly checked and ignored because
                        // they are not complete yet (no character).
                        default:
                            if (e.keyCode != SWT.CAPS_LOCK && e.keyCode != SWT.NUM_LOCK && e.keyCode != SWT.MOD1
                                    && e.keyCode != SWT.MOD2 && e.keyCode != SWT.MOD3 && e.keyCode != SWT.MOD4) {
                                close();
                            }
                            return;
                    }

                    // If any of these navigation events caused a new selection,
                    // then handle that now and return.
                    if (newSelection >= 0) {
                        selectProposal(newSelection);
                    }
                    return;
                }

                // key != 0
                // Check for special keys involved in cancelling, accepting, or
                // filtering the proposals.
                switch (key) {
                    case SWT.ESC:
                        e.doit = false;
                        close();
                        break;

                    case SWT.LF:
                    case SWT.CR:
                        e.doit = false;
                        final Object p = getSelectedProposal();
                        if (p != null) {
                            acceptCurrentProposal();
                        } else {
                            close();
                        }
                        break;

                    case SWT.TAB:
                        e.doit = false;
                        getShell().setFocus();
                        return;

                    case SWT.BS:
                        // There is no filtering provided by us, but some
                        // clients provide their own filtering based on content.
                        // Recompute the proposals if the cursor position
                        // will change (is not at 0).
                        final int pos = controlContentAdapter.getCursorPosition(control);
                        // We rely on the fact that the contents and pos do not yet
                        // reflect the result of the BS. If the contents were
                        // already empty, then BS should not cause
                        // a recompute.
                        if (pos > 0) {
                            asyncRecomputeProposals(filterText);
                        }
                        break;

                    default:
                        // If the key is a defined unicode character, and not one of
                        // the special cases processed above, update the filter text
                        // and filter the proposals.
                        if (Character.isDefined(key)) {
                            // Recompute proposals after processing this event.
                            asyncRecomputeProposals(filterText);
                        }
                        break;
                }
            }
        }

        /*
         * Internal class used to implement the secondary popup.
         */
        private class InfoPopupDialog extends PopupDialog {

            /*
             * The text control that displays the text.
             */
            private StyledText text;

            /*
             * The String shown in the popup.
             */
            private String contents = "";

            /*
             * Construct an info-popup with the specified parent.
             */
            InfoPopupDialog(final Shell parent) {
                super(parent, PopupDialog.INFOPOPUPRESIZE_SHELLSTYLE | PopupDialog.HOVER_SHELLSTYLE, false, false,
                        false, false, false, null, null);
            }

            /*
             * Create a text control for showing the info about a proposal.
             */
            @Override
            protected Control createDialogArea(final Composite parent) {
                text = new StyledText(parent, SWT.NONE);

                // Use the compact margins employed by PopupDialog.
                final GridData gd = new GridData(GridData.BEGINNING | GridData.FILL_BOTH);
                gd.horizontalIndent = PopupDialog.POPUP_HORIZONTALSPACING;
                gd.verticalIndent = PopupDialog.POPUP_VERTICALSPACING;
                text.setLayoutData(gd);

                // since SWT.NO_FOCUS is only a hint...
                text.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusGained(final FocusEvent event) {
                        // ContentProposalPopup.this.close();
                    }
                });
                return text;
            }

            /*
             * Adjust the bounds so that we appear adjacent to our parent shell
             */
            @Override
            protected void adjustBounds() {
                final Rectangle parentBounds = getParentShell().getBounds();
                Rectangle proposedBounds;
                // Try placing the info popup to the right
                Rectangle rightProposedBounds = new Rectangle(parentBounds.x + parentBounds.width
                        + PopupDialog.POPUP_HORIZONTALSPACING, parentBounds.y + PopupDialog.POPUP_VERTICALSPACING,
                        parentBounds.width, parentBounds.height);
                rightProposedBounds = getConstrainedShellBounds(rightProposedBounds);
                // If it won't fit on the right, try the left
                if (rightProposedBounds.intersects(parentBounds)) {
                    Rectangle leftProposedBounds = new Rectangle(parentBounds.x - parentBounds.width
                            - POPUP_HORIZONTALSPACING - 1, parentBounds.y, parentBounds.width, parentBounds.height);
                    leftProposedBounds = getConstrainedShellBounds(leftProposedBounds);
                    // If it won't fit on the left, choose the proposed bounds
                    // that fits the best
                    if (leftProposedBounds.intersects(parentBounds)) {
                        if (rightProposedBounds.x - parentBounds.x >= parentBounds.x - leftProposedBounds.x) {
                            rightProposedBounds.x = parentBounds.x + parentBounds.width
                                    + PopupDialog.POPUP_HORIZONTALSPACING;
                            proposedBounds = rightProposedBounds;
                        } else {
                            leftProposedBounds.width = parentBounds.x - POPUP_HORIZONTALSPACING - leftProposedBounds.x;
                            proposedBounds = leftProposedBounds;
                        }
                    } else {
                        // use the proposed bounds on the left
                        proposedBounds = leftProposedBounds;
                    }
                } else {
                    // use the proposed bounds on the right
                    proposedBounds = rightProposedBounds;
                }
                getShell().setBounds(proposedBounds);
            }

            @Override
            protected Color getForeground() {
                return control.getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND);
            }

            @Override
            protected Color getBackground() {
                return control.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND);
            }

            /*
             * Set the text contents of the popup.
             */
            void setContents(final String newContents) {
                this.contents = Strings.nullToEmpty(newContents);
                if (text != null && !text.isDisposed()) {
                    text.setText(contents);
                }
            }

            /*
             * Return whether the popup has focus.
             */
            boolean hasFocus() {
                if (text == null || text.isDisposed()) {
                    return false;
                }
                return text.getShell().isFocusControl() || text.isFocusControl();
            }
        }

        /*
         * The listener installed on the target control.
         */
        private Listener targetControlListener;

        /*
         * The listener installed in order to close the popup.
         */
        private PopupCloserListener popupCloser;

        private TableViewer proposalTableViewer;

        /*
         * The proposals to be shown (cached to avoid repeated requests).
         */
        private RedContentProposal[] proposals;

        /*
         * Secondary popup used to show detailed information about the selected
         * proposal..
         */
        private InfoPopupDialog infoPopup;

        /*
         * Flag indicating whether there is a pending secondary popup update.
         */
        private boolean pendingDescriptionUpdate = false;

        /*
         * Filter text - tracked while popup is open, only if we are told to
         * filter
         */
        private final String filterText = "";

        /**
         * Constructs a new instance of this popup, specifying the control for
         * which this popup is showing content, and how the proposals should be
         * obtained and displayed.
         *
         * @param infoText
         *            Text to be shown in a lower info area, or
         *            <code>null</code> if there is no info area.
         */
        ContentProposalPopup(final String infoText, final RedContentProposal[] proposals) {
            // IMPORTANT: Use of SWT.ON_TOP is critical here for ensuring
            // that the target control retains focus on Mac and Linux. Without
            // it, the focus will disappear, keystrokes will not go to the
            // popup, and the popup closer will wrongly close the popup.
            // On platforms where SWT.ON_TOP overrides SWT.RESIZE, we will live
            // with this.
            // See https://bugs.eclipse.org/bugs/show_bug.cgi?id=126138
            super(control.getShell(), SWT.RESIZE | SWT.ON_TOP, false, false, false, false, false, null, infoText);
            this.proposals = proposals;
        }

        @Override
        protected Color getForeground() {
            return JFaceResources.getColorRegistry().get(JFacePreferences.CONTENT_ASSIST_FOREGROUND_COLOR);
        }

        @Override
        protected Color getBackground() {
            return JFaceResources.getColorRegistry().get(JFacePreferences.CONTENT_ASSIST_BACKGROUND_COLOR);
        }

        /*
         * Creates the content area for the proposal popup. This creates a table
         * and places it inside the composite. The table will contain a list of
         * all the proposals.
         *
         * @param parent The parent composite to contain the dialog area; must
         * not be <code>null</code>.
         */
        @Override
        protected final Control createDialogArea(final Composite parent) {
            final int style = USE_VIRTUAL ? SWT.H_SCROLL | SWT.V_SCROLL | SWT.VIRTUAL : SWT.H_SCROLL | SWT.V_SCROLL;
            proposalTableViewer = new TableViewer(parent, style);
            proposalTableViewer.setContentProvider(new StructuredContentProvider() {

                @Override
                public Object[] getElements(final Object inputElement) {
                    return (Object[]) inputElement;
                }
            });
            proposalTableViewer.setLabelProvider(labelProvider);
            setProposals(filterProposals(proposals, filterText));

            proposalTableViewer.getTable().setHeaderVisible(false);
            proposalTableViewer.getTable().addSelectionListener(new SelectionListener() {

                @Override
                public void widgetSelected(final SelectionEvent e) {
                    if (e.item == null) {
                        if (infoPopup != null) {
                            infoPopup.close();
                        }
                    } else {
                        showProposalDescription();
                    }
                }

                // Default selection was made. Accept the current proposal.
                @Override
                public void widgetDefaultSelected(final SelectionEvent e) {
                    acceptCurrentProposal();
                }
            });

            return proposalTableViewer.getTable();
        }

        @Override
        protected void adjustBounds() {
            // Get our control's location in display coordinates.
            final Point location = control.getDisplay().map(control.getParent(), null, control.getLocation());
            int initialX = location.x + POPUP_OFFSET;
            int initialY = location.y + control.getSize().y + POPUP_OFFSET;
            // If we are inserting content, use the cursor position to
            // position the control.
            if (proposalAcceptanceStyle == PROPOSAL_SHOULD_INSERT) {
                final Rectangle insertionBounds = controlContentAdapter.getInsertionBounds(control);
                initialX = initialX + insertionBounds.x;
                initialY = location.y + insertionBounds.y + insertionBounds.height;
            }

            // If there is no specified size, force it by setting
            // up a layout on the table.
            if (popupSize == null) {
                final GridData data = new GridData(GridData.FILL_BOTH);
                data.heightHint = proposalTableViewer.getTable().getItemHeight() * POPUP_CHAR_HEIGHT;
                data.widthHint = Math.max(control.getSize().x, POPUP_MINIMUM_WIDTH);
                proposalTableViewer.getTable().setLayoutData(data);
                getShell().pack();
                popupSize = getShell().getSize();
            }

            // Constrain to the display
            final Rectangle constrainedBounds = getConstrainedShellBounds(new Rectangle(initialX, initialY,
                    popupSize.x, popupSize.y));

            // If there has been an adjustment causing the popup to overlap
            // with the control, then put the popup above the control.
            if (constrainedBounds.y < initialY) {
                getShell().setBounds(initialX, location.y - popupSize.y, popupSize.x, popupSize.y);
            } else {
                getShell().setBounds(initialX, initialY, popupSize.x, popupSize.y);
            }

            // Now set up a listener to monitor any changes in size.
            getShell().addListener(SWT.Resize, new Listener() {
                @Override
                public void handleEvent(final Event e) {
                    popupSize = getShell().getSize();
                    if (infoPopup != null) {
                        infoPopup.adjustBounds();
                    }
                }
            });
        }

        /*
         * Caches the specified proposals and repopulates the table if it has
         * been created.
         */
        private void setProposals(final RedContentProposal[] newProposals) {
            this.proposals = newProposals == null || newProposals.length == 0 ? new RedContentProposal[0]
                    : newProposals;

            // If there is a table
            if (popupExists()) {
                proposalTableViewer.setInput(proposals);
                final int newSize = newProposals.length;
                if (USE_VIRTUAL) {
                    // Set and clear the virtual table. Data will be
                    // provided in the SWT.SetData event handler.
                    proposalTableViewer.getTable().setItemCount(newSize);
                    proposalTableViewer.getTable().clearAll();
                } else {
                    // Populate the table manually
                    proposalTableViewer.getTable().setRedraw(false);
                    proposalTableViewer.getTable().setItemCount(newSize);
                    final TableItem[] items = proposalTableViewer.getTable().getItems();
                    for (int i = 0; i < items.length; i++) {
                        final TableItem item = items[i];
                        final RedContentProposal proposal = newProposals[i];
                        item.setText(getString(proposal));
                        item.setImage(getImage(proposal));
                        item.setData(proposal);
                    }
                    proposalTableViewer.getTable().setRedraw(true);
                }
                // Default to the first selection if there is content.
                if (newProposals.length > 0) {
                    selectProposal(0);
                } else {
                    // No selection, close the secondary popup if it was open
                    if (infoPopup != null) {
                        infoPopup.close();
                    }

                }
            }
        }

        /*
         * Get the string for the specified proposal. Always return a String of
         * some kind.
         */
        private String getString(final RedContentProposal proposal) {
            if (proposal == null) {
                return "";
            }
            if (labelProvider == null) {
                return proposal.getLabel() == null ? proposal.getContent() : proposal.getLabel();
            }
            return labelProvider.getText(proposal);
        }

        /*
         * Get the image for the specified proposal. If there is no image
         * available, return null.
         */
        private Image getImage(final RedContentProposal proposal) {
            if (proposal == null || labelProvider == null) {
                return null;
            }
            return labelProvider.getImage(proposal);
        }

        /*
         * Answer true if the popup is valid, which means the table has been
         * created and not disposed.
         */
        private boolean popupExists() {
            return proposalTableViewer.getTable() != null && !proposalTableViewer.getTable().isDisposed();
        }

        /*
         * Return whether the receiver has focus. Since 3.4, this includes a
         * check for whether the info popup has focus.
         */
        private boolean hasFocus() {
            if (!popupExists()) {
                return false;
            }
            if (getShell().isFocusControl() || proposalTableViewer.getTable().isFocusControl()) {
                return true;
            }
            if (infoPopup != null && infoPopup.hasFocus()) {
                return true;
            }
            return false;
        }

        /*
         * Return the current selected proposal.
         */
        private RedContentProposal getSelectedProposal() {
            if (popupExists()) {
                final int i = proposalTableViewer.getTable().getSelectionIndex();
                if (proposals == null || i < 0 || i >= proposals.length) {
                    return null;
                }
                return proposals[i];
            }
            return null;
        }

        /*
         * Select the proposal at the given index.
         */
        private void selectProposal(final int index) {
            Assert.isTrue(index >= 0, "Proposal index should never be negative"); //$NON-NLS-1$
            if (!popupExists() || proposals == null || index >= proposals.length) {
                return;
            }
            proposalTableViewer.getTable().setSelection(index);
            proposalTableViewer.getTable().showSelection();

            showProposalDescription();
        }

        /**
         * Opens this ContentProposalPopup. This method is extended in order to
         * add the control listener when the popup is opened and to invoke the
         * secondary popup if applicable.
         *
         * @return the return code
         *
         * @see org.eclipse.jface.window.Window#open()
         */
        @Override
        public int open() {
            final int value = super.open();
            if (popupCloser == null) {
                popupCloser = new PopupCloserListener();
            }
            popupCloser.installListeners();
            final RedContentProposal p = getSelectedProposal();
            if (p != null) {
                showProposalDescription();
            }
            return value;
        }

        /**
         * Closes this popup. This method is extended to remove the control
         * listener.
         *
         * @return <code>true</code> if the window is (or was already) closed,
         *         and <code>false</code> if it is still open
         */
        @Override
        public boolean close() {
            popupCloser.removeListeners();
            if (infoPopup != null) {
                infoPopup.close();
            }
            final boolean closed = super.close();
            notifyPopupClosed();
            return closed;
        }

        /*
         * Show the currently selected proposal's description in a secondary
         * popup.
         */
        private void showProposalDescription() {
            // If we do not already have a pending update, then
            // create a thread now that will show the proposal description
            if (!pendingDescriptionUpdate) {
                // Create a thread that will sleep for the specified delay
                // before creating the popup. We do not use Jobs since this
                // code must be able to run independently of the Eclipse
                // runtime.
                final Runnable runnable = () -> {
                    pendingDescriptionUpdate = true;
                    try {
                        Thread.sleep(SECONDARY_POPUP_DELAY);
                    } catch (final InterruptedException e) {
                    }
                    if (!popupExists()) {
                        return;
                    }
                    getShell().getDisplay().syncExec(() -> {
                        // Query the current selection since we have
                        // been delayed
                        final RedContentProposal p = getSelectedProposal();
                        if (p == null) {
                            return;
                        }
                        if (p.hasDescription()) {
                            if (infoPopup == null) {
                                infoPopup = new InfoPopupDialog(getShell());
                                infoPopup.open();
                                infoPopup.getShell().addDisposeListener(event -> infoPopup = null);
                            }
                            infoPopup.setContents(p.getDescription());
                        } else if (infoPopup != null) {
                            infoPopup.close();
                        }
                        pendingDescriptionUpdate = false;
                    });
                };
                final Thread t = new Thread(runnable);
                t.start();
            }
        }

        /*
         * Accept the current proposal.
         */
        private void acceptCurrentProposal() {
            // Close before accepting the proposal. This is important
            // so that the cursor position can be properly restored at
            // acceptance, which does not work without focus on some controls.
            // See https://bugs.eclipse.org/bugs/show_bug.cgi?id=127108
            final RedContentProposal proposal = getSelectedProposal();
            close();
            proposalAccepted(proposal);
        }

        /*
         * Request the proposals from the proposal provider, and recompute any
         * caches. Repopulate the popup if it is open.
         */
        private void recomputeProposals(final String filterText) {
            RedContentProposal[] allProposals = getProposals();
            if (allProposals == null) {
                allProposals = new RedContentProposal[0];
            }
            setProposals(filterProposals(allProposals, filterText));
        }

        /*
         * In an async block, request the proposals. This is used when clients
         * are in the middle of processing an event that affects the widget
         * content. By using an async, we ensure that the widget content is up
         * to date with the event.
         */
        private void asyncRecomputeProposals(final String filterText) {
            if (popupExists()) {
                control.getDisplay().asyncExec(() -> {
                    recordCursorPosition();
                    recomputeProposals(filterText);
                });
            } else {
                recomputeProposals(filterText);
            }
        }

        /*
         * Filter the provided list of content proposals according to the filter
         * text.
         */
        private RedContentProposal[] filterProposals(final RedContentProposal[] proposals, final String filterString) {
            if (filterString.length() == 0) {
                return proposals;
            }

            // Check each string for a match. Use the string displayed to the
            // user, not the proposal content.
            final ArrayList<RedContentProposal> list = new ArrayList<>();
            for (int i = 0; i < proposals.length; i++) {
                final String string = getString(proposals[i]);
                if (string.length() >= filterString.length()
                        && string.substring(0, filterString.length()).equalsIgnoreCase(filterString)) {
                    list.add(proposals[i]);
                }

            }
            return list.toArray(new RedContentProposal[list.size()]);
        }

        Listener getTargetControlListener() {
            if (targetControlListener == null) {
                targetControlListener = new TargetControlListener();
            }
            return targetControlListener;
        }
    }

    private void notifyProposalAccepted(final RedContentProposal proposal) {
        for (final Object listener : proposalListeners.getListeners()) {
            ((RedContentProposalListener) listener).proposalAccepted(proposal);
        }
    }

    private void notifyPopupOpened() {
        for (final Object listener : proposalListeners.getListeners()) {
            ((RedContentProposalListener) listener).proposalPopupOpened(this);
        }
    }

    private void notifyPopupClosed() {
        for (final Object listener : proposalListeners.getListeners()) {
            ((RedContentProposalListener) listener).proposalPopupClosed(this);
        }
    }

    public static interface RedContentProposalListener {

        void proposalAccepted(final RedContentProposal proposal);

        void proposalPopupClosed(final RedContentProposalAdapter adapter);

        void proposalPopupOpened(final RedContentProposalAdapter adapter);
    }

    private static class ProposalsLabelProvider extends RedCommonLabelProvider {

        @Override
        public StyledString getStyledText(final Object element) {
            return ((RedContentProposal) element).getStyledLabel();
        }

        @Override
        public Image getImage(final Object element) {
            final ImageDescriptor descriptor = ((RedContentProposal) element).getImage();
            if (descriptor == null) {
                return null;
            }
            return ImagesManager.getImage(descriptor);
        }
    }
}

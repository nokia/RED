/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.jface.preferences;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.google.common.base.Strings;

// TODO: replace this class with StringFieldEditor when platform is updated somewhere in future
/**
 * This is copy org.eclipse.jface.preference.StringFieldEditor with support of multiline text field
 * added in 3.17.
 */
public class MultiLineStringFieldEditor extends FieldEditor {

    /**
     * Validation strategy constant (value <code>0</code>) indicating that the
     * editor should perform validation after every key stroke.
     *
     * @see #setValidateStrategy
     */
    public static final int VALIDATE_ON_KEY_STROKE = 0;

    /**
     * Validation strategy constant (value <code>1</code>) indicating that
     * the editor should perform validation only when the text widget
     * loses focus.
     *
     * @see #setValidateStrategy
     */
    public static final int VALIDATE_ON_FOCUS_LOST = 1;

    /**
     * Text limit constant (value <code>-1</code>) indicating unlimited
     * text limit and width.
     */
    public static int UNLIMITED = -1;

    /**
     * Cached valid state.
     */
    private boolean isValid;

    /**
     * Old text value.
     *
     * @since 3.4 this field is protected.
     */
    protected String oldValue;

    /**
     * The text field, or <code>null</code> if none.
     */
    Text textField;

    /**
     * Height of text field in characters; initially 1.
     */
    private int heigthInChars = 1;

    /**
     * Width of text field in characters; initially unlimited.
     */
    private int widthInChars = UNLIMITED;

    /**
     * Text limit of text field in characters; initially unlimited.
     */
    private int textLimit = UNLIMITED;

    /**
     * The error message, or <code>null</code> if none.
     */
    private String errorMessage;

    /**
     * Indicates whether the empty string is legal;
     * <code>true</code> by default.
     */
    private boolean emptyStringAllowed = true;

    /**
     * The validation strategy;
     * <code>VALIDATE_ON_KEY_STROKE</code> by default.
     */
    private int validateStrategy = VALIDATE_ON_KEY_STROKE;

    /**
     * Creates a new string field editor
     */
    protected MultiLineStringFieldEditor() {
    }

    /**
     * Creates a string field editor. Use the method <code>setTextLimit</code> to
     * limit the text.
     *
     * @param name
     *            the name of the preference this field editor works on
     * @param labelText
     *            the label text of the field editor
     * @param widthInChars
     *            the width of the text input field in characters, or
     *            <code>UNLIMITED</code> for no limit
     * @param heigthInChars
     *            the height of the text input field in characters.
     * @param strategy
     *            either <code>VALIDATE_ON_KEY_STROKE</code> to perform on
     *            the fly checking (the default), or
     *            <code>VALIDATE_ON_FOCUS_LOST</code> to perform
     *            validation only after the text has been typed in
     * @param parent
     *            the parent of the field editor's control
     * @since 3.17
     */
    public MultiLineStringFieldEditor(final String name, final String labelText, final int widthInChars,
            final int heigthInChars,
            final int strategy, final Composite parent) {
        init(name, labelText);
        this.widthInChars = widthInChars;
        this.heigthInChars = heigthInChars;
        setValidateStrategy(strategy);
        isValid = false;
        errorMessage = JFaceResources.getString("StringFieldEditor.errorMessage");//$NON-NLS-1$
        createControl(parent);
    }

    /**
     * Creates a string field editor. Use the method <code>setTextLimit</code> to
     * limit the text.
     *
     * @param name
     *            the name of the preference this field editor works on
     * @param labelText
     *            the label text of the field editor
     * @param width
     *            the width of the text input field in characters, or
     *            <code>UNLIMITED</code> for no limit
     * @param strategy
     *            either <code>VALIDATE_ON_KEY_STROKE</code> to perform on the
     *            fly checking (the default), or
     *            <code>VALIDATE_ON_FOCUS_LOST</code> to perform validation
     *            only after the text has been typed in
     * @param parent
     *            the parent of the field editor's control
     * @since 2.0
     */
    public MultiLineStringFieldEditor(final String name, final String labelText, final int width, final int strategy,
            final Composite parent) {
        this(name, labelText, width, 1, strategy, parent);
    }

    /**
     * Creates a string field editor.
     * Use the method <code>setTextLimit</code> to limit the text.
     *
     * @param name
     *            the name of the preference this field editor works on
     * @param labelText
     *            the label text of the field editor
     * @param width
     *            the width of the text input field in characters,
     *            or <code>UNLIMITED</code> for no limit
     * @param parent
     *            the parent of the field editor's control
     */
    public MultiLineStringFieldEditor(final String name, final String labelText, final int width,
            final Composite parent) {
        this(name, labelText, width, VALIDATE_ON_KEY_STROKE, parent);
    }

    /**
     * Creates a string field editor of unlimited width.
     * Use the method <code>setTextLimit</code> to limit the text.
     *
     * @param name
     *            the name of the preference this field editor works on
     * @param labelText
     *            the label text of the field editor
     * @param parent
     *            the parent of the field editor's control
     */
    public MultiLineStringFieldEditor(final String name, final String labelText, final Composite parent) {
        this(name, labelText, UNLIMITED, parent);
    }

    @Override
    protected void adjustForNumColumns(final int numColumns) {
        final GridData gd = (GridData) textField.getLayoutData();
        gd.horizontalSpan = numColumns - 1;
        // We only grab excess space if we have to
        // If another field editor has more columns then
        // we assume it is setting the width.
        gd.grabExcessHorizontalSpace = gd.horizontalSpan == 1;
    }

    /**
     * Checks whether the text input field contains a valid value or not.
     *
     * @return <code>true</code> if the field value is valid,
     *         and <code>false</code> if invalid
     */
    protected boolean checkState() {
        boolean result = false;
        if (emptyStringAllowed) {
            result = true;
        }

        if (textField == null) {
            result = false;
        } else {
            final String txt = textField.getText();
            result = (txt.trim().length() > 0) || emptyStringAllowed;
        }

        // call hook for subclasses
        result = result && doCheckState();

        if (result) {
            clearErrorMessage();
        } else {
            showErrorMessage(errorMessage);
        }

        return result;
    }

    /**
     * Hook for subclasses to do specific state checks.
     * <p>
     * The default implementation of this framework method does
     * nothing and returns <code>true</code>. Subclasses should
     * override this method to specific state checks.
     * </p>
     *
     * @return <code>true</code> if the field value is valid,
     *         and <code>false</code> if invalid
     */
    protected boolean doCheckState() {
        return true;
    }

    /**
     * Fills this field editor's basic controls into the given parent.
     * <p>
     * The string field implementation of this <code>FieldEditor</code>
     * framework method contributes the text field. Subclasses may override
     * but must call <code>super.doFillIntoGrid</code>.
     * </p>
     */
    @Override
    protected void doFillIntoGrid(final Composite parent, final int numColumns) {
        getLabelControl(parent);

        textField = getTextControl(parent);
        final GridData gd = new GridData();
        gd.horizontalSpan = numColumns - 1;
        if (widthInChars != UNLIMITED || heigthInChars > 1) {
            final GC gc = new GC(textField);
            try {
                if (widthInChars != UNLIMITED) {
                    final Point extent = gc.textExtent("X");//$NON-NLS-1$
                    gd.widthHint = widthInChars * extent.x;
                } else {
                    gd.horizontalAlignment = GridData.FILL;
                    gd.grabExcessHorizontalSpace = true;
                }
                if (heigthInChars > 1) {
                    gd.heightHint = heigthInChars * gc.getFontMetrics().getHeight();
                }
            } finally {
                gc.dispose();
            }
        } else {
            gd.horizontalAlignment = GridData.FILL;
            gd.grabExcessHorizontalSpace = true;
        }
        textField.setLayoutData(gd);
    }

    @Override
    protected void doLoad() {
        if (textField != null) {
            final String value = getPreferenceStore().getString(getPreferenceName());
            textField.setText(value);
            oldValue = value;
        }
    }

    @Override
    protected void doLoadDefault() {
        if (textField != null) {
            final String value = getPreferenceStore().getDefaultString(getPreferenceName());
            textField.setText(value);
        }
        valueChanged();
    }

    @Override
    protected void doStore() {
        getPreferenceStore().setValue(getPreferenceName(), textField.getText());
    }

    /**
     * Returns the error message that will be displayed when and if
     * an error occurs.
     *
     * @return the error message, or <code>null</code> if none
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public int getNumberOfControls() {
        return 2;
    }

    /**
     * Returns the field editor's value.
     *
     * @return the current value
     */
    public String getStringValue() {
        if (textField != null) {
            return textField.getText();
        }

        return getPreferenceStore().getString(getPreferenceName());
    }

    /**
     * Returns this field editor's text control.
     *
     * @return the text control, or <code>null</code> if no
     *         text field is created yet
     */
    protected Text getTextControl() {
        return textField;
    }

    /**
     * Returns this field editor's text control.
     * <p>
     * The control is created if it does not yet exist
     * </p>
     *
     * @param parent
     *            the parent
     * @return the text control
     */
    public Text getTextControl(final Composite parent) {
        if (textField == null) {
            textField = createTextWidget(parent);
            textField.setFont(parent.getFont());
            switch (validateStrategy) {
                case VALIDATE_ON_KEY_STROKE:
                    textField.addKeyListener(new KeyAdapter() {

                        @Override
                        public void keyReleased(final KeyEvent e) {
                            valueChanged();
                        }
                    });
                    textField.addFocusListener(new FocusAdapter() {

                        // Ensure that the value is checked on focus loss in case we
                        // missed a keyRelease or user hasn't released key.
                        // See https://bugs.eclipse.org/bugs/show_bug.cgi?id=214716
                        @Override
                        public void focusLost(final FocusEvent e) {
                            valueChanged();
                        }
                    });

                    break;
                case VALIDATE_ON_FOCUS_LOST:
                    textField.addKeyListener(new KeyAdapter() {

                        @Override
                        public void keyPressed(final KeyEvent e) {
                            clearErrorMessage();
                        }
                    });
                    textField.addFocusListener(new FocusAdapter() {

                        @Override
                        public void focusGained(final FocusEvent e) {
                            refreshValidState();
                        }

                        @Override
                        public void focusLost(final FocusEvent e) {
                            valueChanged();
                        }
                    });
                    break;
                default:
                    Assert.isTrue(false, "Unknown validate strategy");//$NON-NLS-1$
            }
            textField.addDisposeListener(event -> textField = null);
            if (textLimit > 0) {// Only set limits above 0 - see SWT spec
                textField.setTextLimit(textLimit);
            }
        } else {
            checkParent(textField, parent);
        }
        return textField;
    }

    /**
     * Create the text widget.
     *
     * @param parent
     * @return The widget
     * @since 3.17
     */
    protected Text createTextWidget(final Composite parent) {
        if (heigthInChars > 1) {
            return new Text(parent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
        }
        return new Text(parent, SWT.SINGLE | SWT.BORDER);
    }

    /**
     * Returns whether an empty string is a valid value.
     *
     * @return <code>true</code> if an empty string is a valid value, and
     *         <code>false</code> if an empty string is invalid
     * @see #setEmptyStringAllowed
     */
    public boolean isEmptyStringAllowed() {
        return emptyStringAllowed;
    }

    @Override
    public boolean isValid() {
        return isValid;
    }

    @Override
    protected void refreshValidState() {
        isValid = checkState();
    }

    /**
     * Sets whether the empty string is a valid value or not.
     *
     * @param b
     *            <code>true</code> if the empty string is allowed,
     *            and <code>false</code> if it is considered invalid
     */
    public void setEmptyStringAllowed(final boolean b) {
        emptyStringAllowed = b;
    }

    /**
     * Sets the error message that will be displayed when and if
     * an error occurs.
     *
     * @param message
     *            the error message
     */
    public void setErrorMessage(final String message) {
        errorMessage = message;
    }

    @Override
    public void setFocus() {
        if (textField != null) {
            textField.setFocus();
        }
    }

    /**
     * Sets this field editor's value.
     *
     * @param value
     *            the new value, or <code>null</code> meaning the empty string
     */
    public void setStringValue(final String value) {
        if (textField != null) {
            final String newValue = Strings.nullToEmpty(value);
            oldValue = textField.getText();
            if (!oldValue.equals(newValue)) {
                textField.setText(newValue);
                valueChanged();
            }
        }
    }

    /**
     * Sets this text field's text limit.
     *
     * @param limit
     *            the limit on the number of character in the text
     *            input field, or <code>UNLIMITED</code> for no limit
     */
    public void setTextLimit(final int limit) {
        textLimit = limit;
        if (textField != null) {
            textField.setTextLimit(limit);
        }
    }

    /**
     * Sets the strategy for validating the text.
     * <p>
     * Calling this method has no effect after <code>createPartControl</code>
     * is called. Thus this method is really only useful for subclasses to call
     * in their constructor. However, it has public visibility for backward
     * compatibility.
     * </p>
     *
     * @param value
     *            either <code>VALIDATE_ON_KEY_STROKE</code> to perform
     *            on the fly checking (the default), or <code>VALIDATE_ON_FOCUS_LOST</code> to
     *            perform validation only after the text has been typed in
     */
    public void setValidateStrategy(final int value) {
        Assert.isTrue(value == VALIDATE_ON_FOCUS_LOST || value == VALIDATE_ON_KEY_STROKE);
        validateStrategy = value;
    }

    /**
     * Shows the error message set via <code>setErrorMessage</code>.
     */
    public void showErrorMessage() {
        showErrorMessage(errorMessage);
    }

    /**
     * Informs this field editor's listener, if it has one, about a change
     * to the value (<code>VALUE</code> property) provided that the old and
     * new values are different.
     * <p>
     * This hook is <em>not</em> called when the text is initialized
     * (or reset to the default value) from the preference store.
     * </p>
     */
    protected void valueChanged() {
        setPresentsDefaultValue(false);
        final boolean oldState = isValid;
        refreshValidState();

        if (isValid != oldState) {
            fireStateChanged(IS_VALID, oldState, isValid);
        }

        final String newValue = textField.getText();
        if (!newValue.equals(oldValue)) {
            fireValueChanged(VALUE, oldValue, newValue);
            oldValue = newValue;
        }
    }

    /*
     * @see FieldEditor.setEnabled(boolean,Composite).
     */
    @Override
    public void setEnabled(final boolean enabled, final Composite parent) {
        super.setEnabled(enabled, parent);
        getTextControl(parent).setEnabled(enabled);
    }

}

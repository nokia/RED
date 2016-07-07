/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.propertytester;

import org.eclipse.core.expressions.PropertyTester;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditor;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;


public class RedXmlEditorPropertyTester extends PropertyTester {

    @VisibleForTesting static final String RED_XML_IS_EDITABLE = "redXmlIsEditable";

    @Override
    public boolean test(final Object receiver, final String property, final Object[] args, final Object expectedValue) {
        Preconditions.checkArgument(receiver instanceof RedProjectEditor,
                "Property tester is unable to test properties of " + receiver.getClass().getName()
                        + ". It should be used with " + RedProjectEditor.class.getName());

        if (expectedValue instanceof Boolean) {
            return testProperty((RedProjectEditor) receiver, property, ((Boolean) expectedValue).booleanValue());
        }
        return false;
    }

    private boolean testProperty(final RedProjectEditor editor, final String property, final boolean expected) {
        if (RED_XML_IS_EDITABLE.equals(property)) {
            return editor.getRedProjectEditorInput().isEditable() == expected;
        }
        return false;
    }

}

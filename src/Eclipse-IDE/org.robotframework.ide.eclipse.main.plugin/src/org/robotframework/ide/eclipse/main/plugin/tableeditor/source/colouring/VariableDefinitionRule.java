/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring;

import java.util.EnumSet;

import org.eclipse.jface.text.rules.IToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;


public class VariableDefinitionRule extends TokenTypeBasedRule {

    private static EnumSet<RobotTokenType> types = EnumSet.of(RobotTokenType.VARIABLES_DICTIONARY_DECLARATION,
            RobotTokenType.VARIABLES_LIST_DECLARATION, RobotTokenType.VARIABLES_SCALAR_AS_LIST_DECLARATION,
            RobotTokenType.VARIABLES_SCALAR_DECLARATION, RobotTokenType.VARIABLES_UNKNOWN_DECLARATION);

    public VariableDefinitionRule(final IToken textToken) {
        super(textToken, types);
    }
}

/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.variables;

import java.util.List;

import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableScope;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;


public interface IVariableHolder {

    VariableType getType();


    VariableScope getScope();


    String getName();


    List<RobotToken> getComment();


    void addCommentPart(final RobotToken rt);


    RobotToken getDeclaration();
    
    IVariableHolder copy();
}

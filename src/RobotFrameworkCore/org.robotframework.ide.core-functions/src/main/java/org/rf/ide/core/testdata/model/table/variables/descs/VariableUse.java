/*
 * Copyright 2020 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.variables.descs;

import java.util.Set;

import org.rf.ide.core.testdata.model.FileRegion;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

/**
 * Interface representing a single variable usage inside a token
 * 
 * @author anglart
 */
public interface VariableUse {

    /**
     * Returns type identifier of this variable use
     * 
     * @return Type of variable
     */
    VariableType getType();

    /**
     * Returns name of this variable used. When variable is using extended variables syntax only
     * used name is returned.
     * 
     * @return Name of variable
     */
    String getName();

    /**
     * Return this variable use as RobotToken
     * 
     * @return Variable use as token
     */
    RobotToken asToken();

    /**
     * Region in file where this variable use is placed
     * 
     * @return File region of this variable use
     */
    FileRegion getRegion();

    /**
     * Checks if this usage is defined in given variable names set
     * 
     * @param variableDefinitions
     *            Set of defined variables names
     * @return True if this usage is defined in given set
     */
    boolean isDefinedIn(Set<String> variableDefinitions);

    /**
     * Returns true when name of this variable depends on other variables used inside this one
     * 
     * @return True when this variable is dynamic
     */
    boolean isDynamic();

    /**
     * Returns true when this variables is accessed with indexing e.g. ${var}[0]
     * 
     * @return True when this variable is indexed
     */
    boolean isIndexed();

    /**
     * Returns true if this variable usage is the only one in whole analyzed token e.g. ${x} is
     * plain in "${x}" token but is not in "${x}${y}", "${x}y", "${a${x}}"
     * 
     * @return True if this variable is a plain usage
     */
    boolean isPlainVariable();

    /**
     * Returns true if this variable usage is the only one in whole analyzed token optionally
     * followed by trailing "=" character. Notice that if {@link #isPlainVariable()} returns true
     * then this method will also return true.
     * 
     * @return True if this variable is plain assign usage
     */
    boolean isPlainVariableAssign();

}

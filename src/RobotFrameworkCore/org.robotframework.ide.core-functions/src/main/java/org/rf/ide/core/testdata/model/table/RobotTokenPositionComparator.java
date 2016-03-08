/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table;

import java.util.Comparator;

import org.rf.ide.core.testdata.text.read.IRobotLineElement;

public class RobotTokenPositionComparator implements Comparator<IRobotLineElement> {

    @Override
    public int compare(final IRobotLineElement o1, final IRobotLineElement o2) {
        return o1.getFilePosition().compare(o2.getFilePosition());
    }
}

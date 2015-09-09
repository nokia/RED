/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table;

import java.util.Comparator;

import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public class RobotTokenPositionComparator implements Comparator<RobotToken> {

    @Override
    public int compare(RobotToken o1, RobotToken o2) {
        return o1.getFilePosition().compare(o2.getFilePosition());
    }
}

/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class RobotExecutableRowTest {

    @Test
    public void compactGetElementsToken_lastArgumentTokenIsEmpty() {
        // prepare
        final RobotToken action = new RobotToken();
        final RobotToken arg1 = new RobotToken();
        arg1.setText("foo");
        final RobotToken arg2 = new RobotToken();
        final RobotToken arg3 = new RobotToken();
        arg3.setText("bar");
        final RobotToken arg4 = new RobotToken();

        final RobotExecutableRow<UserKeyword> row = new RobotExecutableRow<>();
        row.setAction(action);
        row.addArgument(arg1);
        row.addArgument(arg2);
        row.addArgument(arg3);
        row.addArgument(arg4);

        // execute
        List<RobotToken> allElements = row.getElementTokens();

        // verify
        assertThat(allElements).containsExactly(action, arg1, arg2, arg3);
    }

    @Test
    public void compactGetElementsToken_lastArgumentTokenIsNotEmpty() {
        // prepare
        final RobotToken action = new RobotToken();
        final RobotToken arg1 = new RobotToken();
        arg1.setText("foo");
        final RobotToken arg2 = new RobotToken();
        final RobotToken arg3 = new RobotToken();
        arg3.setText("bar");
        final RobotToken arg4 = new RobotToken();
        arg4.setText("foobar");

        final RobotExecutableRow<UserKeyword> row = new RobotExecutableRow<>();
        row.setAction(action);
        row.addArgument(arg1);
        row.addArgument(arg2);
        row.addArgument(arg3);
        row.addArgument(arg4);

        // execute
        List<RobotToken> allElements = row.getElementTokens();

        // verify
        assertThat(allElements).containsExactly(action, arg1, arg2, arg3, arg4);
    }
}

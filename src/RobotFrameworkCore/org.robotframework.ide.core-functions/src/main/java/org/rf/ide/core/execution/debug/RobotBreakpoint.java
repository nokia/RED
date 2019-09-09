/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.debug;

import java.util.Arrays;
import java.util.List;

public interface RobotBreakpoint {

    boolean isHitCountEnabled();

    void setHitCountEnabled(boolean enabled);

    int getHitCount();

    void setHitCount(int count);

    default boolean evaluateHitCount() {
        return true;
    }

    default boolean isConditionEnabled() {
        return false;
    }

    default String getConditionExpression() {
        return "";
    }

    default List<String> getCondition() {
        return Arrays.asList(getConditionExpression().split("(\\s{2,}|\t)"));
    }
}

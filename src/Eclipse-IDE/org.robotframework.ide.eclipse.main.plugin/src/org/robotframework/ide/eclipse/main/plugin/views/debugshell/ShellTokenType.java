/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.debugshell;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.VersionAvailabilityInfo;

public enum ShellTokenType implements IRobotTokenType {

    CALL_KW,
    CALL_ARG,
    MODE_FLAG,
    MODE_CONTINUATION,
    FAIL,
    PASS;

    @Override
    public List<String> getRepresentation() {
        return new ArrayList<>();
    }

    @Override
    public List<VersionAvailabilityInfo> getVersionAvailabilityInfos() {
        return new ArrayList<>();
    }

    @Override
    public VersionAvailabilityInfo findVersionAvailabilityInfo(final String text) {
        return null;
    }
}

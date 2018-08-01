/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.setting.suite;

import java.util.List;

import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.model.table.setting.SuiteTeardown;

public class SuiteTeardownKeywordMapperOld extends SuiteTeardownKeywordMapper {

    @Override
    public boolean isApplicableFor(final RobotVersion robotVersion) {
        return robotVersion.isOlderThan(new RobotVersion(3, 0));
    }

    @Override
    protected boolean canBeMappedTo(final List<SuiteTeardown> suiteTeardowns) {
        return !utility.checkIfFirstHasKeywordNameAlready(suiteTeardowns);
    }
}

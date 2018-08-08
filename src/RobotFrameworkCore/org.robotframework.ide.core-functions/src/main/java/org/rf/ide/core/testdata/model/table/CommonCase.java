/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table;

import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ExecutableSetting;

public abstract class CommonCase<T, C extends AModelElement<? extends ARobotSectionTable>> extends AModelElement<T>
        implements IExecutableStepsHolder<C> {

    public abstract List<? extends ExecutableSetting> getSetupExecutables();

    public abstract List<? extends ExecutableSetting> getTeardownExecutables();
}

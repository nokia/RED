/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table;

import org.rf.ide.core.testdata.model.AModelElement;

public abstract class CommonStep<T> extends AModelElement<T> {

    public abstract void createToken(final int index);

    public abstract void updateToken(final int index, final String newValue);

    public abstract void deleteToken(final int index);

    public abstract void rewriteFrom(final CommonStep<?> other);
}

/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter.update;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;

public interface IKeywordTableElementOperation {

    boolean isApplicable(final ModelType elementType);

    //AModelElement<?> create(final KeywordTable settingsTable, final int tableIndex, final List<String> args, final String comment);

    void update(final AModelElement<?> modelElement, final int index, final String value);

    //void remove(final KeywordTable settingsTable, final AModelElement<?> modelElements);
    
}

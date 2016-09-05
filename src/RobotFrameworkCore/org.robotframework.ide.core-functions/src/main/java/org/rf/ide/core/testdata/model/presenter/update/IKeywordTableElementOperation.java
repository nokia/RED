/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter.update;

import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;

public interface IKeywordTableElementOperation {

    boolean isApplicable(final ModelType elementType);
    
    boolean isApplicable(final IRobotTokenType elementType);

    AModelElement<?> create(final UserKeyword userKeyword, String actionName, final List<String> args,
            final String comment);

    AModelElement<?> insert(final UserKeyword userKeyword, int index, final AModelElement<?> modelElement);

    void update(final AModelElement<?> modelElement, final int index, final String value);

    void remove(UserKeyword userKeyword, AModelElement<?> modelElement);
    
}

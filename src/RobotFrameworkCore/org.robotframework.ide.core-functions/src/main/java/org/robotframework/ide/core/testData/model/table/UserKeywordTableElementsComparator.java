/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table;

import java.util.LinkedHashMap;
import java.util.Map;

import org.robotframework.ide.core.testData.model.AModelElement;
import org.robotframework.ide.core.testData.model.ModelType;
import org.robotframework.ide.core.testData.model.table.userKeywords.UserKeyword;


public class UserKeywordTableElementsComparator extends
        AModelTypeComparator<AModelElement<UserKeyword>> {

    private final static Map<ModelType, Integer> position = new LinkedHashMap<>();
    static {
        int startPosition = 1;
        position.put(ModelType.USER_KEYWORD, startPosition);
        position.put(ModelType.USER_KEYWORD_ARGUMENTS, ++startPosition);
        position.put(ModelType.USER_KEYWORD_DOCUMENTATION, ++startPosition);
        position.put(ModelType.USER_KEYWORD_TAGS, ++startPosition);
        position.put(ModelType.USER_KEYWORD_TIMEOUT, ++startPosition);
        position.put(ModelType.USER_KEYWORD_EXECUTABLE_ROW, ++startPosition);
        position.put(ModelType.USER_KEYWORD_TEARDOWN, ++startPosition);
        position.put(ModelType.USER_KEYWORD_RETURN, ++startPosition);
    }


    public UserKeywordTableElementsComparator() {
        super(position);
    }
}

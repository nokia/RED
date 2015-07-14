package org.robotframework.ide.core.testData.model.table.keywords;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.table.keywords.doc.Documentation;


public class UserKeyword {

    private final UserKeywordName keywordName;
    private final List<Documentation> documentation = new LinkedList<>();
    private final List<Arguments> arguments = new LinkedList<>();
    private final List<Return> returned = new LinkedList<>();
    private final List<Teardown> teardowns = new LinkedList<>();
    private final List<Timeout> timeouts = new LinkedList<>();


    public UserKeyword(final UserKeywordName keywordName) {
        this.keywordName = keywordName;
    }
}

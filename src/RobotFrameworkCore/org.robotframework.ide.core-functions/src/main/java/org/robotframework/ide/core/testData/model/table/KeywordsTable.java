package org.robotframework.ide.core.testData.model.table;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.table.keywords.UserKeyword;


public class KeywordsTable extends ATableModel {

    private List<UserKeyword> userHighLevelKeyword = new LinkedList<>();


    public KeywordsTable(final TableHeader header) {
        super(header, "Keyword");
    }
}

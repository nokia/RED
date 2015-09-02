package org.robotframework.ide.core.testData.model.table;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.table.userKeywords.UserKeyword;


public class KeywordTable extends ARobotSectionTable {

    public KeywordTable(String uuid) {
        super(uuid);
    }

    private final List<UserKeyword> userKeywords = new LinkedList<>();


    public void addKeyword(final UserKeyword keyword) {
        keyword.setFileUUID(getUUID());
        userKeywords.add(keyword);
    }


    public List<UserKeyword> getKeywords() {
        return Collections.unmodifiableList(userKeywords);
    }


    public boolean isEmpty() {
        return (userKeywords.isEmpty());
    }
}

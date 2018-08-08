/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.presenter.MoveElementHelper;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class KeywordTable extends ARobotSectionTable {

    private final List<UserKeyword> userKeywords = new ArrayList<>();

    public KeywordTable(final RobotFile parent) {
        super(parent);
    }
    
    public UserKeyword createUserKeyword(final String keywordName) {
        return createUserKeyword(keywordName, userKeywords.size());
    }

    public UserKeyword createUserKeyword(final String keywordName, final int position) {
        final UserKeyword keyword = new UserKeyword(RobotToken.create(keywordName));
        addKeyword(keyword, position);
        return keyword;
    }

    public void addKeyword(final UserKeyword keyword) {
        addKeyword(keyword, userKeywords.size());
    }
    
    public void addKeyword(final UserKeyword keyword, final int position) {
        keyword.setParent(this);
        userKeywords.add(position, keyword);
    }

    public void removeKeyword(final UserKeyword keyword) {
        userKeywords.remove(keyword);
    }

    @Override
    public boolean moveUpElement(final AModelElement<? extends ARobotSectionTable> element) {
        return MoveElementHelper.moveUp(userKeywords, (UserKeyword) element);
    }

    @Override
    public boolean moveDownElement(final AModelElement<? extends ARobotSectionTable> element) {
        return MoveElementHelper.moveDown(userKeywords, (UserKeyword) element);
    }

    public List<UserKeyword> getKeywords() {
        return Collections.unmodifiableList(userKeywords);
    }

    public boolean isEmpty() {
        return (userKeywords.isEmpty());
    }
}

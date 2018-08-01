/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.keywords;

import java.util.List;
import java.util.Stack;

import org.rf.ide.core.testdata.mapping.table.IParsingMapper;
import org.rf.ide.core.testdata.mapping.table.ParsingStateHelper;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.model.table.KeywordTable;
import org.rf.ide.core.testdata.model.table.keywords.KeywordTags;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class KeywordTagsTagNameMapper implements IParsingMapper {

    private final ParsingStateHelper utility = new ParsingStateHelper();

    @Override
    public boolean isApplicableFor(final RobotVersion robotVersion) {
        return robotVersion.isNewerOrEqualTo(new RobotVersion(2, 9));
    }

    @Override
    public boolean checkIfCanBeMapped(final RobotFileOutput robotFileOutput, final RobotLine currentLine,
            final RobotToken rt, final String text, final Stack<ParsingState> processingState) {

        final ParsingState state = utility.getCurrentStatus(processingState);
        return state == ParsingState.KEYWORD_SETTING_TAGS || state == ParsingState.KEYWORD_SETTING_TAGS_TAG_NAME;
    }

    @Override
    public RobotToken map(final RobotLine currentLine, final Stack<ParsingState> processingState,
            final RobotFileOutput robotFileOutput, final RobotToken rt, final FilePosition fp, final String text) {

        rt.getTypes().add(0, RobotTokenType.KEYWORD_SETTING_TAGS_TAG_NAME);
        rt.setText(text);

        final KeywordTable keywordTable = robotFileOutput.getFileModel().getKeywordTable();
        final List<UserKeyword> keywords = keywordTable.getKeywords();
        final UserKeyword keyword = keywords.get(keywords.size() - 1);
        final List<KeywordTags> tags = keyword.getTags();
        final KeywordTags keywordTags = tags.get(tags.size() - 1);
        keywordTags.addTag(rt);

        processingState.push(ParsingState.KEYWORD_SETTING_TAGS_TAG_NAME);
        return rt;
    }

}

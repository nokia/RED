/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.table;

import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.setting.Metadata;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class MetadataOldSyntaxUtility {

    private final Pattern METADATA = Pattern.compile("(([M|m])([E]|[e])([T]|[t])([A]|[a])[:])(\\s+)((?!\\s+).+)");

    public void fixSettingMetadata(final RobotFileOutput out, final RobotLine line, final RobotToken token,
            final Stack<ParsingState> processingState) {
        if (token.getTypes().contains(RobotTokenType.SETTING_METADATA_DECLARATION)) {
            final String metadataSettingText = token.getText();
            final Matcher matcher = METADATA.matcher(metadataSettingText);
            if (matcher.find()) {
                final int prettyAlignGroup = matcher.groupCount() - 1;
                final int prettyStart = matcher.start(prettyAlignGroup);
                final int prettyEnd = matcher.end(prettyAlignGroup);

                final String metadataSetting = metadataSettingText.substring(0, prettyStart);
                token.setText(metadataSetting);

                final String prettyAlignText = metadataSettingText.substring(prettyStart, prettyEnd);
                final RobotToken prettyToken = new RobotToken();
                prettyToken.setLineNumber(token.getLineNumber());
                prettyToken.setStartColumn(token.getEndColumn());
                prettyToken.setStartOffset(token.getStartOffset() + token.getText().length());
                prettyToken.setText(prettyAlignText);
                prettyToken.setType(RobotTokenType.PRETTY_ALIGN_SPACE);
                line.addLineElement(prettyToken);

                final String metadataKey = metadataSettingText.substring(prettyEnd);
                final RobotToken metadataKeyToken = new RobotToken();
                metadataKeyToken.setLineNumber(token.getLineNumber());
                metadataKeyToken.setStartColumn(prettyToken.getEndColumn());
                metadataKeyToken.setStartOffset(prettyToken.getStartOffset() + prettyToken.getText().length());
                metadataKeyToken.setText(metadataKey);
                metadataKeyToken.setType(RobotTokenType.SETTING_METADATA_KEY);
                line.addLineElement(metadataKeyToken);

                final List<Metadata> metadatas = out.getFileModel().getSettingTable().getMetadatas();
                final Metadata lastMetadata = metadatas.get(metadatas.size() - 1);
                lastMetadata.setKey(metadataKeyToken);

                processingState.push(ParsingState.SETTING_METADATA_KEY);
            }
        }
    }
}

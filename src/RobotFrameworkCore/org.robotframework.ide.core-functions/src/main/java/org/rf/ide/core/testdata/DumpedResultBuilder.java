/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.separators.Separator;
import org.rf.ide.core.testdata.text.write.ILineDumpTokenListener;

/**
 * @author wypych
 */
public class DumpedResultBuilder implements ILineDumpTokenListener {

    private List<RobotLine> lines;

    private final Map<RobotToken, RobotToken> oldToNewTokenMappingTmp = new IdentityHashMap<>();

    public DumpedResultBuilder producedLines(final List<RobotLine> lines) {
        this.lines = lines;

        return this;
    }

    public DumpedResultBuilder addMapNewToOld(final RobotToken oldModelToken, final RobotToken newRecalculatedToken) {
        oldToNewTokenMappingTmp.put(oldModelToken, newRecalculatedToken);

        return this;
    }

    @Override
    public void tokenDumped(final RobotToken oldToken, final RobotToken newToken) {
        this.addMapNewToOld(oldToken, newToken);
    }

    public static class DumpedResult {

        private String dumpedContent;

        private List<RobotLine> lines;

        private final Map<RobotToken, RobotToken> oldToNewTokenMapping = new IdentityHashMap<>();

        private void setDumpedContent(final String dumpedContent) {
            this.dumpedContent = dumpedContent;
        }

        public String newContent() {
            return this.dumpedContent;
        }

        private void setLines(final List<RobotLine> lines) {
            this.lines = lines;
        }

        public List<RobotLine> newProducedLines() {
            return this.lines;
        }

        private void setOldToNewMapping(final Map<RobotToken, RobotToken> mapping) {
            this.oldToNewTokenMapping.putAll(mapping);
        }

        public Map<RobotToken, RobotToken> mappingBetweenOldAndNewTokens() {
            return Collections.unmodifiableMap(oldToNewTokenMapping);
        }
    }

    private String dump(final List<RobotLine> lines) {
        final StringBuilder strLine = new StringBuilder();

        final int nrOfLines = lines.size();
        for (int i = 0; i < nrOfLines; i++) {
            final RobotLine line = lines.get(i);
            for (final IRobotLineElement elem : line.getLineElements()) {
                if (elem instanceof Separator) {
                    strLine.append(((Separator) elem).getRaw());
                } else {
                    strLine.append(elem.getText());
                }
            }

            strLine.append(line.getEndOfLine().getText());
        }

        return strLine.toString();
    }

    public DumpedResult build() {
        final DumpedResult builded = new DumpedResult();
        if (lines == null) {
            builded.setLines(new ArrayList<RobotLine>(0));
        } else {
            builded.setLines(lines);
        }
        builded.setDumpedContent(dump(builded.newProducedLines()));
        builded.setOldToNewMapping(oldToNewTokenMappingTmp);
        return builded;
    }
}

/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model;

import java.util.List;

import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public interface IDocumentationHolder extends IRegionCacheable<IDocumentationHolder> {

    FilePosition getBeginPosition();

    List<RobotToken> getDocumentationText();

    void setDocumentationText(final int index, final String docText);

    void setDocumentationText(final int index, final RobotToken docText);

    void addDocumentationText(final RobotToken cmPart);

    void removeDocumentationPart(final int index);

    void clearDocumentation();
}

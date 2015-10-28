/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.executableDescriptors.ast.mapping;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.RobotFileOutput.BuildMessage;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public class MappingResult {

    private final List<BuildMessage> messages = new LinkedList<>();
    private final String fileName;
    private final List<RobotToken> foundTokens = new LinkedList<>();
    private FilePosition fp;


    public MappingResult(final FilePosition fp, final String fileName) {
        this.fp = fp;
        this.fileName = fileName;
    }


    public FilePosition getLastFilePosition() {
        return fp;
    }


    public void setLastFilePosition(final FilePosition fp) {
        this.fp = fp;
    }


    public void addBuildMessage(final BuildMessage msg) {
        messages.add(msg);
    }


    public void addBuildMessages(final List<BuildMessage> msgs) {
        messages.addAll(msgs);
    }


    public List<BuildMessage> getMessages() {
        return Collections.unmodifiableList(messages);
    }


    public void addFoundToken(final RobotToken token) {
        foundTokens.add(token);
    }


    public List<RobotToken> getFoundTokens() {
        return Collections.unmodifiableList(foundTokens);
    }


    public String getFilename() {
        return fileName;
    }
}
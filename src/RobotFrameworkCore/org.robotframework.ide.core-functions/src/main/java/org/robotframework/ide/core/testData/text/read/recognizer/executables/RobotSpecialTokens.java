/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.text.read.recognizer.executables;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.text.read.recognizer.ATokenRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;


public class RobotSpecialTokens {

    private static final List<ATokenRecognizer> specialRecognizers = new LinkedList<>();
    static {
        specialRecognizers.add(new CommentActionLiteral());
        specialRecognizers.add(new ForActionLiteral());
        specialRecognizers.add(new ForInActionLiteral());
        specialRecognizers.add(new ForContinueToken());
    }

    private static class ForContinueToken extends ATokenRecognizer {

        protected ForContinueToken() {
            super(Pattern.compile("^(\\s)*" + "[\\\\]" + "(\\s)*"),
                    RobotTokenType.FOR_CONTINUE_TOKEN);
        }


        @Override
        public ATokenRecognizer newInstance() {
            return new ForContinueToken();
        }
    }

    private static class ForInActionLiteral extends ATokenRecognizer {

        protected ForInActionLiteral() {
            super(Pattern.compile("^(\\s)*" + "[i|I](\\s)*[n|N]" + "(\\s)*"),
                    RobotTokenType.IN_TOKEN);
        }


        @Override
        public ATokenRecognizer newInstance() {
            return new ForInActionLiteral();
        }
    }

    private static class ForActionLiteral extends ATokenRecognizer {

        protected ForActionLiteral() {
            super(Pattern.compile("^(\\s)*[:](\\s)*"
                    + "[f|F](\\s)*[o|O](\\s)*[r|R]" + "(\\s)*$"),
                    RobotTokenType.FOR_TOKEN);
        }


        @Override
        public ATokenRecognizer newInstance() {
            return new ForActionLiteral();
        }
    }

    private static class CommentActionLiteral extends ATokenRecognizer {

        protected CommentActionLiteral() {
            super(Pattern.compile("^[ ]?" + createUpperLowerCaseWord("comment")
                    + "$"), RobotTokenType.COMMENT_TOKEN);
        }


        @Override
        public ATokenRecognizer newInstance() {
            return new CommentActionLiteral();
        }
    }


    public List<RobotToken> recognize(final FilePosition fp, String text) {
        List<RobotToken> possibleRobotTokens = new LinkedList<>();
        StringBuilder sb = new StringBuilder(text);
        for (ATokenRecognizer rec : specialRecognizers) {
            ATokenRecognizer newInstance = rec.newInstance();
            if (newInstance.hasNext(sb, fp.getLine())) {
                RobotToken t = newInstance.next();
                t.setStartColumn(t.getStartColumn() + fp.getColumn());
                possibleRobotTokens.add(t);
            }
        }

        return possibleRobotTokens;
    }
}

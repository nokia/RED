/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read.recognizer;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.RobotVersion;

public class RobotSpecialTokens {

    private static final List<ATokenRecognizer> SPECIAL_RECOGNIZERS = newArrayList(
            new ForActionLiteral(),
            new ForActionInRF31Literal(),
            new ForInActionLiteral(),
            new ForContinueToken());

    private List<ATokenRecognizer> recognizersToUse;

    public void initializeFor(final RobotVersion version) {
        recognizersToUse = SPECIAL_RECOGNIZERS.stream()
                .filter(recognizer -> recognizer.isApplicableFor(version))
                .collect(toList());
    }

    public List<RobotToken> recognize(final FilePosition fp, final String text) {
        final List<RobotToken> possibleRobotTokens = new ArrayList<>();
        for (final ATokenRecognizer rec : recognizersToUse) {
            final ATokenRecognizer recognizer = rec.newInstance();
            if (recognizer.hasNext(text, fp.getLine(), fp.getColumn())) {
                final RobotToken token = recognizer.next();
                token.setStartColumn(token.getStartColumn() + fp.getColumn());
                possibleRobotTokens.add(token);
            }
        }
        return possibleRobotTokens;
    }


    private static class ForActionLiteral extends ATokenRecognizer {

        protected ForActionLiteral() {
            super(Pattern.compile("^(\\s)*[:](\\s)*" + "[f|F](\\s)*[o|O](\\s)*[r|R]" + "(\\s)*$"),
                    RobotTokenType.FOR_TOKEN);
        }

        @Override
        public ATokenRecognizer newInstance() {
            return new ForActionLiteral();
        }
    }

    private static class ForActionInRF31Literal extends ATokenRecognizer {

        protected ForActionInRF31Literal() {
            super(Pattern.compile("^(\\s)*" + "FOR" + "(\\s)*$"), RobotTokenType.FOR_TOKEN);
        }

        @Override
        public boolean isApplicableFor(final RobotVersion robotVersion) {
            return robotVersion.isNewerOrEqualTo(new RobotVersion(3, 1));
        }

        @Override
        public ATokenRecognizer newInstance() {
            return new ForActionInRF31Literal();
        }
    }

    private static class ForContinueToken extends ATokenRecognizer {

        protected ForContinueToken() {
            super(Pattern.compile("^(\\s)*" + "[\\\\]" + "(\\s)*"), RobotTokenType.FOR_CONTINUE_TOKEN);
        }

        @Override
        public ATokenRecognizer newInstance() {
            return new ForContinueToken();
        }
    }

    private static class ForInActionLiteral extends ATokenRecognizer {

        protected ForInActionLiteral() {
            super(Pattern.compile("^(\\s)*" + "("
                    + RobotTokenType.IN_TOKEN.getRepresentation()
                            .stream()
                            .map(ATokenRecognizer::createUpperLowerCaseWordWithSpacesInside)
                            .collect(Collectors.joining("|"))
                    + ")" + "(\\s)*$"), RobotTokenType.IN_TOKEN);
        }

        @Override
        public ATokenRecognizer newInstance() {
            return new ForInActionLiteral();
        }
    }
}

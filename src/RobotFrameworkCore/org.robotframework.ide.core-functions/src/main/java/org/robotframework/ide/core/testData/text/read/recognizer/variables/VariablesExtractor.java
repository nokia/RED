/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.text.read.recognizer.variables;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.LinkedListMultimap;


public class VariablesExtractor {

    private static final String VARIABLES_BEGIN_SIGNS = "([$]|[@]|[&]|[%])";
    private static final String VARIABLES_BEGIN_EXTRACTOR = "(\\\\)*"
            + VARIABLES_BEGIN_SIGNS + "(\\s)?[{]";
    private static final Pattern VARIABLES_EXTRACTION_PATTERN = Pattern
            .compile(VARIABLES_BEGIN_EXTRACTOR);
    private static final String INDEX_PATTERN_TEXT = "\\[((?!\\]).)*\\]";
    private static final Pattern INDEX_PATTERN = Pattern
            .compile(INDEX_PATTERN_TEXT);


    public List<VariableDeclarationInformation> extractTextVariablesInformations(
            final String text) {
        List<VariableDeclarationInformation> varDeclarationInfos = new LinkedList<>();

        List<VariableDeclaration> variablesDeclarationPosition = extractSimpleVariablesDeclarationPosition(text);

        Map<VariableDeclaration, VariableDeclarationInformation> declarationToInfo = createPureVariableInformation(
                varDeclarationInfos, variablesDeclarationPosition);
        connectChildVariablesWithParents(varDeclarationInfos,
                variablesDeclarationPosition, declarationToInfo);
        declarationToInfo.clear();

        applyVariableNames(varDeclarationInfos);

        return varDeclarationInfos;
    }


    private void applyVariableNames(
            List<VariableDeclarationInformation> varDeclarationInfos) {
        for (VariableDeclarationInformation varDecInfo : varDeclarationInfos) {
            VariableName varName;
            VariableDeclaration varDec = varDecInfo.getVariableDeclaration();
            String varDecText = varDec.getOriginalText();
            List<VariableDeclarationInformation> subChilds = varDecInfo
                    .getSubChilds();
            if (subChilds.isEmpty()) {
                TextualPosition start = new TextualPosition(varDecText, varDec
                        .getStart().getStartPosition(), varDec.getStart()
                        .getEndPosition());
                TextualPosition end = new TextualPosition(varDecText,
                        varDec.getEnd().startPosition, varDec.getEnd()
                                .getEndPosition());
                varName = new VariableName(false, varDecText, start, end);
            } else {
                int startPositionBegin = varDec.getStart().getStartPosition();
                int startPositionEnd = findNearestBeginPosition(subChilds);
                int endPositionBegin = findFarestEndPosition(subChilds);
                int endPositionEnd = varDec.getEnd().getEndPosition();
                varName = new VariableName(true, varDecText,
                        new TextualPosition(varDecText, startPositionBegin,
                                startPositionEnd), new TextualPosition(
                                varDecText, endPositionBegin, endPositionEnd));
            }

            String variableTextName = getRobotVariableRepresentation(varName);

            varName.setRobotName(variableTextName);
            varDecInfo.setVariableName(varName);
        }
    }


    @VisibleForTesting
    protected String getRobotVariableRepresentation(VariableName varName) {
        String variableTextName = varName.extractNamePart();
        boolean execute = true;
        while(execute) {
            execute = false;
            Matcher matcher = INDEX_PATTERN.matcher(variableTextName);
            char[] textChars = variableTextName.toCharArray();
            while(matcher.find()) {
                int start = matcher.start();
                int end = matcher.end();
                for (int charIndex = start; charIndex < end; charIndex++) {
                    // clean this char - minus one characters are ignored
                    textChars[charIndex] = (char) -1;
                }

                execute = true;
            }

            variableTextName = new String(textChars);
        }
        return variableTextName;
    }


    private int findNearestBeginPosition(
            final List<VariableDeclarationInformation> subChilds) {
        int position = -1;
        for (VariableDeclarationInformation vdi : subChilds) {
            int startPosition = vdi.getVariableDeclaration().getStart()
                    .getStartPosition();
            if (position == -1 || startPosition < position) {
                position = startPosition;
            }
        }

        return position;
    }


    private int findFarestEndPosition(
            final List<VariableDeclarationInformation> subChilds) {
        int position = -1;
        for (VariableDeclarationInformation vdi : subChilds) {
            int endPosition = vdi.getVariableDeclaration().getEnd()
                    .getEndPosition();
            if (position == -1 || endPosition > position) {
                position = endPosition;
            }
        }

        return position;
    }


    private void connectChildVariablesWithParents(
            final List<VariableDeclarationInformation> varDeclarationInfos,
            final List<VariableDeclaration> variablesDeclarationPosition,
            final Map<VariableDeclaration, VariableDeclarationInformation> declarationToInfo) {
        for (VariableDeclarationInformation varDecInfo : varDeclarationInfos) {
            VariableDeclaration parent = findParent(
                    varDecInfo.getVariableDeclaration(),
                    variablesDeclarationPosition);
            if (parent != null) {
                VariableDeclarationInformation parentDeclaration = declarationToInfo
                        .get(parent);
                varDecInfo.setParent(parentDeclaration);
                parentDeclaration.addSubChilds(varDecInfo);
            }
        }
    }


    private Map<VariableDeclaration, VariableDeclarationInformation> createPureVariableInformation(
            List<VariableDeclarationInformation> varDeclarationInfos,
            List<VariableDeclaration> variablesDeclarationPosition) {
        Map<VariableDeclaration, VariableDeclarationInformation> declarationToInfo = new LinkedHashMap<>();

        for (VariableDeclaration varDeclaration : variablesDeclarationPosition) {
            VariableDeclarationInformation varDecInfo = new VariableDeclarationInformation(
                    varDeclaration);
            varDeclarationInfos.add(varDecInfo);
            declarationToInfo.put(varDeclaration, varDecInfo);
        }

        return declarationToInfo;
    }


    @VisibleForTesting
    protected VariableDeclaration findParent(
            final VariableDeclaration variableDeclaration,
            final List<VariableDeclaration> variablesDeclarationPosition) {
        VariableDeclaration toReturn = null;
        for (VariableDeclaration varDec : variablesDeclarationPosition) {
            if (varDec.getStart().getStartPosition() < variableDeclaration
                    .getStart().getStartPosition()
                    && varDec.getEnd().getEndPosition() >= variableDeclaration
                            .getEnd().getEndPosition()) {
                if (toReturn == null
                        || varDec.getStart().getStartPosition() > toReturn
                                .getStart().getStartPosition()) {
                    toReturn = varDec;
                }
            }
        }

        return toReturn;
    }


    @VisibleForTesting
    protected List<VariableDeclaration> extractSimpleVariablesDeclarationPosition(
            final String text) {
        List<VariableDeclaration> variablesDeclaration = new LinkedList<>();
        if (text != null) {
            LinkedListMultimap<Character, Integer> foundChars = getCharsPosition(
                    text, Arrays.asList('}'));
            List<Integer> rightCurrlyBrackets = foundChars.get('}');
            List<TextualPosition> variablesStartPosition = getVariablesStartPosition(text);
            int size = variablesStartPosition.size();
            for (int varIndex = size - 1; varIndex >= 0; varIndex--) {
                TextualPosition varPosition = variablesStartPosition
                        .get(varIndex);
                int indexOfNearestCurrlyBracket = getIndexOfNearestCurrlyBracket(
                        rightCurrlyBrackets, varPosition);
                if (indexOfNearestCurrlyBracket > -1) {
                    Integer currlyBracketPosition = rightCurrlyBrackets
                            .get(indexOfNearestCurrlyBracket);

                    variablesDeclaration.add(new VariableDeclaration(text,
                            varPosition, new TextualPosition(text,
                                    currlyBracketPosition,
                                    currlyBracketPosition + 1)));

                    rightCurrlyBrackets.remove(indexOfNearestCurrlyBracket);
                } else {
                    variablesDeclaration.add(new VariableDeclaration(text,
                            varPosition, new TextualPosition(text, text
                                    .length(), text.length())));
                }
            }
        }

        return variablesDeclaration;
    }


    @VisibleForTesting
    protected int getIndexOfNearestCurrlyBracket(
            final List<Integer> rightCurrlyBrackets,
            final TextualPosition currentVariablePosition) {
        int foundIndex = -1;
        int tokenPosition = -1;

        int bracketsNumber = rightCurrlyBrackets.size();
        for (int bracketIndex = bracketsNumber - 1; bracketIndex >= 0; bracketIndex--) {
            Integer currlyBracket = rightCurrlyBrackets.get(bracketIndex);
            if (currentVariablePosition.getEndPosition() < currlyBracket) {
                if (tokenPosition == -1 || tokenPosition > currlyBracket) {
                    tokenPosition = currlyBracket;
                    foundIndex = bracketIndex;
                }
            }
        }

        return foundIndex;
    }


    @VisibleForTesting
    protected LinkedListMultimap<Character, Integer> getCharsPosition(
            final String text, final List<Character> charsToFind) {
        LinkedListMultimap<Character, Integer> result = LinkedListMultimap
                .create();
        if (text != null && !charsToFind.isEmpty()) {
            char[] textChars = text.toCharArray();
            int textLength = textChars.length;
            for (int charIndex = 0; charIndex < textLength; charIndex++) {
                char currentChar = textChars[charIndex];
                if (charsToFind.contains(currentChar)) {
                    result.put(currentChar, charIndex);
                }
            }
        }

        return result;
    }


    @VisibleForTesting
    protected List<TextualPosition> getVariablesStartPosition(final String text) {
        List<TextualPosition> positions = new LinkedList<>();
        if (text != null) {
            Matcher matcher = VARIABLES_EXTRACTION_PATTERN.matcher(text);
            while(matcher.find()) {
                positions.add(new TextualPosition(text, matcher.start(),
                        matcher.end()));
            }
        }

        return positions;
    }

    public class VariableDeclarationInformation {

        private VariableName variableName;
        private VariableDeclarationInformation parent;
        private final List<VariableDeclarationInformation> subChilds = new LinkedList<>();
        private final VariableDeclaration varDeclaration;


        public VariableDeclarationInformation(
                final VariableDeclaration varDeclaration) {
            this.varDeclaration = varDeclaration;
        }


        private void setVariableName(final VariableName variableName) {
            this.variableName = variableName;
        }


        public VariableName getVariableName() {
            return variableName;
        }


        private void setParent(final VariableDeclarationInformation parent) {
            this.parent = parent;
        }


        public VariableDeclarationInformation getParent() {
            return parent;
        }


        private void addSubChilds(final VariableDeclarationInformation subChild) {
            subChilds.add(subChild);
        }


        public List<VariableDeclarationInformation> getSubChilds() {
            return subChilds;
        }


        public VariableDeclaration getVariableDeclaration() {
            return varDeclaration;
        }
    }

    public class VariableName {

        private String robotName;
        private final boolean hasSubChildsInName;
        private final String originalText;
        private final TextualPosition start;
        private final TextualPosition end;


        public VariableName(final boolean hasSubChildsInName,
                final String originalText, final TextualPosition start,
                final TextualPosition end) {
            this.hasSubChildsInName = hasSubChildsInName;
            this.originalText = originalText;
            this.start = start;
            this.end = end;
        }


        public TextualPosition getStart() {
            return start;
        }


        public TextualPosition getEnd() {
            return end;
        }


        public String getOriginalText() {
            return originalText;
        }


        private void setRobotName(final String robotName) {
            this.robotName = robotName;
        }


        public String getRobotName() {
            return robotName;
        }


        @VisibleForTesting
        protected String extractNamePart() {
            StringBuilder name = new StringBuilder("");
            if (hasSubChildsInName) {
                name.append(originalText.substring(start.getStartPosition(),
                        start.getEndPosition()));
                name.append(originalText.substring(end.getStartPosition(),
                        end.getEndPosition()));
            } else {
                name.append(originalText.substring(start.getStartPosition(),
                        end.getEndPosition()));
            }

            return name.toString();
        }
    }

    public class VariableDeclaration {

        private final String originalText;
        private final TextualPosition start;
        private final TextualPosition end;


        public VariableDeclaration(final String originalText,
                final TextualPosition start, final TextualPosition end) {
            this.originalText = originalText;
            this.start = start;
            this.end = end;
        }


        public TextualPosition getStart() {
            return start;
        }


        public TextualPosition getEnd() {
            return end;
        }


        public String getOriginalText() {
            return originalText;
        }
    }

    public class TextualPosition {

        private final String originalText;
        private final int startPosition;
        private final int endPosition;


        public TextualPosition(final String originalText,
                final int startPosition, final int endPosition) {
            this.originalText = originalText;
            this.startPosition = startPosition;
            this.endPosition = endPosition;
        }


        public String getOriginalText() {
            return originalText;
        }


        public int getStartPosition() {
            return startPosition;
        }


        public int getEndPosition() {
            return endPosition;
        }


        @Override
        public String toString() {
            return String.format("TextualPosition [text=%s, position=[%s:%s]",
                    originalText, startPosition, endPosition);
        }
    }
}

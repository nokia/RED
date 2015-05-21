package org.robotframework.ide.core.testData.text;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;


public class TxtRobotFileLexer {

    private static final int END_OF_FILE = -1;
    private static final String CARRITAGE_RETURN = "\r";


    public List<String> aggregateSeparators(List<String> extractedWords) {
        List<String> consolidated = new LinkedList<>();

        boolean containsPipe = false;

        StringBuilder tempData = new StringBuilder();
        for (int i = 0; i < extractedWords.size(); i++) {
            String word = extractedWords.get(i);

            int currentDataLength = tempData.length();

            if ("|".equals(word)) {
                if (containsPipe) {
                    consolidated.add(tempData.toString().intern());
                    tempData = new StringBuilder();
                    containsPipe = false;
                } else {
                    tempData.append(word);
                    containsPipe = true;
                }
            } else if (" ".equals(word)) {
                if (currentDataLength > 0) {
                    char lastChar = tempData.charAt(currentDataLength - 1);
                    if (lastChar == ' ') {
                        tempData.append(word);
                    } else if (lastChar == '|') {
                        tempData.append(word);
                        consolidated.add(tempData.toString().intern());
                        tempData = new StringBuilder();
                        containsPipe = false;
                    } else {
                        consolidated.add(tempData.toString().intern());
                        tempData = new StringBuilder();
                        containsPipe = false;
                    }
                } else {
                    tempData.append(word);

                    if (i + 1 < extractedWords.size()) {
                        String nextWord = extractedWords.get(i + 1);
                        if (nextWord.length() > 1 && nextWord.startsWith("|")) {
                            String extracted = nextWord.substring(1,
                                    nextWord.length() - 1);
                            extractedWords.set(i + 1, extracted);
                            tempData.append("|");
                            containsPipe = true;
                        }
                    }
                }
            } else if ("\t".equals(word)) {
                if (currentDataLength > 0) {
                    char lastChar = tempData.charAt(currentDataLength - 1);
                    if (lastChar == '\t') {
                        tempData.append(word);
                    } else if (lastChar == '|') {
                        tempData.append(word);
                        consolidated.add(tempData.toString().intern());
                        tempData = new StringBuilder();
                        containsPipe = false;
                    } else {
                        consolidated.add(tempData.toString().intern());
                        tempData = new StringBuilder();
                        containsPipe = false;
                    }
                } else {
                    tempData.append(word);

                    if (i + 1 < extractedWords.size()) {
                        String nextWord = extractedWords.get(i + 1);
                        if (nextWord.length() > 1 && nextWord.startsWith("|")) {
                            String extracted = nextWord.substring(1,
                                    nextWord.length() - 1);
                            extractedWords.set(i + 1, extracted);
                            tempData.append("|");
                            containsPipe = true;
                        }
                    }
                }
            } else {
                List<String> splittedByPipe = splitNotEscapedPipes(word);
                for (int index = 0; index < splittedByPipe.size(); index++) {
                    String currentWord = splittedByPipe.get(index);
                    if (currentDataLength > 0) {
                        consolidated.add(tempData.toString().intern());
                        tempData = new StringBuilder();
                    }
                    containsPipe = false;

                    if (currentWord.equals("|")
                            && index == (splittedByPipe.size() - 1)) {
                        extractedWords.add("|");
                    } else {
                        consolidated.add(currentWord);
                    }
                }
            }
        }

        return consolidated;
    }


    public List<String> splitNotEscapedPipes(String word) {
        char[] chars = (word != null) ? word.toCharArray() : new char[0];
        List<String> splitted = new LinkedList<String>();

        StringBuilder builder = new StringBuilder();

        boolean isEscaped = false;
        for (char c : chars) {
            if (c == '|') {
                if (isEscaped) {
                    builder.append(c);
                    isEscaped = false;
                } else {
                    if (builder.length() > 0) {
                        splitted.add(builder.toString().intern());
                        builder = new StringBuilder();
                    }

                    splitted.add("|".intern());
                }
            } else if (c == '\\') {
                isEscaped = !isEscaped;
                builder.append(c);
            } else {
                isEscaped = false;
                builder.append(c);
            }
        }

        if (builder.length() > 0) {
            splitted.add(builder.toString().intern());
            builder = null;
        }

        return splitted;
    }


    public List<String> extractWords(InputStreamReader reader)
            throws IOException {
        List<String> words = new LinkedList<>();

        StringBuilder unfinishedText = new StringBuilder();
        int currentChar = END_OF_FILE;
        while((currentChar = reader.read()) != END_OF_FILE) {
            unfinishedText = splitWordsBySpaceTabulatorOrNewLine(words,
                    unfinishedText, (char) currentChar);
        }

        if (unfinishedText.length() > 0) {
            words.add(unfinishedText.toString().intern());
        }

        unfinishedText = null;

        return words;
    }


    private StringBuilder splitWordsBySpaceTabulatorOrNewLine(
            List<String> words, StringBuilder unfinishedText, char currentChar) {
        if (currentChar == ' ' || currentChar == '\t' || currentChar == '\r'
                || currentChar == '\n') {
            if (unfinishedText.length() > 0) {
                words.add(unfinishedText.toString().intern());
                unfinishedText = new StringBuilder();
            }

            words.add(("" + currentChar).intern());
        } else {
            unfinishedText.append(currentChar);
        }

        if (currentChar == '\n') {
            int numberOfWords = words.size();
            if (numberOfWords >= 2) {
                String previousWord = words.get(numberOfWords - 2);
                if (previousWord.equals(CARRITAGE_RETURN)) {
                    // CR+LF case (\r\n)
                    words.set(numberOfWords - 2, previousWord + currentChar);
                    words.remove(numberOfWords - 1); // remove since it was
                                                     // added in previous
                                                     // condition check
                }
            } else {
                words.add(("" + currentChar).intern());
            }
        }

        return unfinishedText;
    }
}

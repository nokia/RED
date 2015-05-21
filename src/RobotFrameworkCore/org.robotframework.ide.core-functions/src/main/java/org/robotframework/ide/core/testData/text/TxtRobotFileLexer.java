package org.robotframework.ide.core.testData.text;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;


public class TxtRobotFileLexer {

    private static final String TABULATOR = "\t";
    private static final String SPACE = " ";
    private static final String PIPE = "|";
    private static final int END_OF_FILE = -1;
    private static final String CARRITAGE_RETURN = "\r";


    public List<String> aggregateSeparators(List<String> extractedWords) {
        List<String> consolidated = new LinkedList<>();

        ConsolidationHelper helper = new ConsolidationHelper();

        for (int i = 0; i < extractedWords.size(); i++) {
            String word = extractedWords.get(i);

            if (PIPE.equals(word)) {
                if (helper.containsPipe) {
                    consolidated.add(helper.text.toString().intern());
                    helper.clean();
                } else {
                    helper.text.append(word);
                    helper.containsPipe = true;
                }
            } else if (SPACE.equals(word)) {
                char expectedLastChar = ' ';

                handleSpecialCharSeparator(extractedWords, consolidated,
                        helper, i, word, expectedLastChar);
            } else if (TABULATOR.equals(word)) {
                char expectedLastChar = '\t';
                handleSpecialCharSeparator(extractedWords, consolidated,
                        helper, i, word, expectedLastChar);
            } else {
                handleWordsWithPossiblePipeSeparatorInside(extractedWords,
                        consolidated, helper, word);
            }
        }

        return consolidated;
    }


    private void handleWordsWithPossiblePipeSeparatorInside(
            List<String> extractedWords, List<String> consolidated,
            ConsolidationHelper helper, String word) {
        List<String> splittedByPipe = splitNotEscapedPipes(word);
        for (int index = 0; index < splittedByPipe.size(); index++) {
            String currentWord = splittedByPipe.get(index);
            if (helper.text.length() > 0) {
                consolidated.add(helper.text.toString().intern());
            }
            helper.clean();

            if (currentWord.equals(PIPE)
                    && index == (splittedByPipe.size() - 1)) {
                extractedWords.add(PIPE);
            } else {
                consolidated.add(currentWord);
            }
        }
    }


    private void handleSpecialCharSeparator(List<String> extractedWords,
            List<String> consolidated, ConsolidationHelper helper, int i,
            String word, char expectedLastChar) {
        if (helper.text.length() > 0) {
            char lastChar = helper.text.charAt(helper.text.length() - 1);
            if (lastChar == expectedLastChar) {
                helper.text.append(word);
            } else if (lastChar == '|') {
                helper.text.append(word);
                consolidated.add(helper.text.toString().intern());
                helper.clean();
            } else {
                consolidated.add(helper.text.toString().intern());
                helper.clean();
            }
        } else {
            helper.text.append(word);

            if (i + 1 < extractedWords.size()) {
                String nextWord = extractedWords.get(i + 1);
                if (nextWord.length() > 1 && nextWord.startsWith(PIPE)) {
                    String extracted = nextWord.substring(1,
                            nextWord.length() - 1);
                    extractedWords.set(i + 1, extracted);
                    helper.text.append(PIPE);
                    helper.clean();
                }
            }
        }
    }

    private class ConsolidationHelper {

        private boolean containsPipe = false;
        private StringBuilder text = new StringBuilder();


        private void clean() {
            containsPipe = false;
            text = new StringBuilder();
        }
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

                    splitted.add(PIPE.intern());
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
                if (CARRITAGE_RETURN.equals(previousWord)) {
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

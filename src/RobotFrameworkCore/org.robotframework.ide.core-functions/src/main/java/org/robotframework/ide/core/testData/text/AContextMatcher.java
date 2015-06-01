package org.robotframework.ide.core.testData.text;

import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.robotframework.ide.core.testData.text.TxtRobotFileLexer.TokenizatorOutput;


public abstract class AContextMatcher implements
        Callable<List<RobotTokenContext>> {

    protected final TokenizatorOutput tokenProvider;


    public AContextMatcher(final TokenizatorOutput tokenProvider) {
        this.tokenProvider = tokenProvider;
    }


    protected abstract List<RobotTokenContext> findContexts(
            final TokenizatorOutput tokenProvider);


    @Override
    public List<RobotTokenContext> call() throws Exception {
        List<RobotTokenContext> placesOfContext = findContexts(tokenProvider);

        if (placesOfContext == null) {
            placesOfContext = new LinkedList<>();
        }

        return placesOfContext;
    }


    protected List<RobotTokenContext> buildTableHeaderContext(
            TokenizatorOutput tokenProvider, ContextType contextType,
            List<List<RobotTokenType>> combinationOfWordsToGet) {
        List<RobotTokenContext> contexts = new LinkedList<>();
        List<List<Integer>> matchedNameCombinations = getJoinedSortedWordTokensId(
                tokenProvider, combinationOfWordsToGet);
        for (List<Integer> currentCombinations : matchedNameCombinations) {
            if (currentCombinations != null && !currentCombinations.isEmpty()) {
                RobotTokenContext context = new RobotTokenContext(contextType);
                int firstTokenId = currentCombinations.get(0);
                int lastTokenId = currentCombinations.get(currentCombinations
                        .size() - 1);

                int prefixAsterisksId = findNearestPrefixAsterisks(
                        tokenProvider, firstTokenId);
                if (prefixAsterisksId > -1) {
                    context.addToken(prefixAsterisksId);
                }

                for (Integer tokenId : currentCombinations) {
                    context.addToken(tokenId);
                }

                int suffixAsterisksId = findNearestSuffixAsterisks(
                        tokenProvider, lastTokenId);
                if (suffixAsterisksId > -1) {
                    context.addToken(suffixAsterisksId);
                }

                contexts.add(context);
            }
        }

        return contexts;
    }


    protected List<List<Integer>> getJoinedSortedWordTokensId(
            TokenizatorOutput tokenProvider,
            List<List<RobotTokenType>> combinationOfWordsToGet) {
        List<List<Integer>> temp = new LinkedList<>();
        for (List<RobotTokenType> combination : combinationOfWordsToGet) {
            List<List<Integer>> p = matchAsMatchAsPossibleFromCombination(
                    tokenProvider, combination);
            if (p != null) {
                temp.addAll(p);
            }
        }

        Collections.sort(temp, new ListInListComparator());

        return temp;
    }


    private List<List<Integer>> matchAsMatchAsPossibleFromCombination(
            TokenizatorOutput tokenProvider, List<RobotTokenType> combination) {
        /*
         * Algorithm for this method base on idea that tokens can't repeat, we
         * just search for tokens expected to see in special tokens dictionary,
         * extract them tokens id to joined list. Next we just sort this list of
         * tokens id and we iterate over them with marking position in list of
         * matched elements. In case element was matched previously, we are
         * getting it id and set next element as start position for build next
         * possible combination.
         */
        List<List<Integer>> combinationMatched = new LinkedList<>();

        List<RobotToken> tokens = tokenProvider.getTokens();

        Map<RobotTokenType, Integer> tokenTypeToCurrentPositionInTempList = new HashMap<>();
        clearPosition(tokenTypeToCurrentPositionInTempList, combination);

        List<Integer> joined = getJoinedStoredWordTokenIds(tokenProvider,
                combination);
        List<Integer> currentIds = new LinkedList<>();
        for (int i = 0; i < joined.size(); i++) {
            Integer tokenId = joined.get(i);
            RobotTokenType type = tokens.get(tokenId).getType();
            Integer positionOfOccurancyInJoinedList = tokenTypeToCurrentPositionInTempList
                    .get(type);
            if (positionOfOccurancyInJoinedList != null) {
                if (positionOfOccurancyInJoinedList > -1) {
                    i = positionOfOccurancyInJoinedList + 1;
                    if (!currentIds.isEmpty()) {
                        combinationMatched.add(currentIds);
                        currentIds = new LinkedList<>();
                        clearPosition(tokenTypeToCurrentPositionInTempList,
                                combination);
                    }

                    // special for last one in case previous was the same
                    if (i == joined.size() - 1) {
                        currentIds.add(tokenId);
                    }
                } else {
                    tokenTypeToCurrentPositionInTempList.put(type, i);
                    currentIds.add(tokenId);
                }
            } else {
                throw new ConcurrentModificationException(
                        "List of tokens and special token list are not coherent, expected one of token types "
                                + combination + ", but got " + type);
            }
        }

        if (!currentIds.isEmpty()) {
            combinationMatched.add(currentIds);
        }

        return combinationMatched;
    }


    private void clearPosition(
            Map<RobotTokenType, Integer> tokenTypeToCurrentPositionInTempList,
            List<RobotTokenType> combination) {
        for (RobotTokenType type : combination) {
            tokenTypeToCurrentPositionInTempList.put(type, -1);
        }
    }


    private List<Integer> getJoinedStoredWordTokenIds(
            TokenizatorOutput tokenProvider, List<RobotTokenType> combination) {
        List<Integer> temp = new LinkedList<>();
        Map<RobotTokenType, List<Integer>> indexesOfSpecial = tokenProvider
                .getIndexesOfSpecial();
        for (RobotTokenType combinationElementToken : combination) {
            List<Integer> posInTokenList = indexesOfSpecial
                    .get(combinationElementToken);
            if (posInTokenList != null) {
                temp.addAll(posInTokenList);
            }
        }

        Collections.sort(temp);

        return temp;
    }


    private int findNearestPrefixAsterisks(TokenizatorOutput tokenProvider,
            int currentTableWord) {
        int id = -1;

        List<Integer> positionOfAsterisks = tokenProvider.getIndexesOfSpecial()
                .get(RobotTokenType.TABLE_ASTERISK);
        if (positionOfAsterisks != null) {
            for (int currentAsteriskIndex : positionOfAsterisks) {
                // check if asterisk is before metadata or settings
                if (currentAsteriskIndex < currentTableWord) {
                    // check if current index is greater than previous one
                    if (id < currentAsteriskIndex) {
                        id = currentAsteriskIndex;
                    }
                }
            }
        }

        return id;
    }


    private int findNearestSuffixAsterisks(TokenizatorOutput tokenProvider,
            int currentTableWord) {
        int id = -1;

        List<Integer> positionOfAsterisks = tokenProvider.getIndexesOfSpecial()
                .get(RobotTokenType.TABLE_ASTERISK);
        if (positionOfAsterisks != null) {
            for (int currentAsteriskIndex : positionOfAsterisks) {
                if (currentAsteriskIndex > currentTableWord) {
                    id = currentAsteriskIndex;
                    break;
                }
            }
        }

        return id;
    }

    private final class ListInListComparator implements
            Comparator<List<Integer>> {

        @Override
        public int compare(List<Integer> o1, List<Integer> o2) {
            final int BEFORE = -1;
            final int EQUAL = 0;
            final int AFTER = 1;

            int result = EQUAL;
            if (o1 == null && o2 != null) {
                result = BEFORE;
            } else if (o1 != null && o2 == null) {
                result = AFTER;
            } else if (o1 == o2) {
                result = EQUAL;
            } else if (o1 != null && o2 != null) {
                Collections.sort(o1);
                Collections.sort(o2);

                int o1Size = o1.size();
                int o2Size = o2.size();

                if (o1.isEmpty() && o2.isEmpty()) {
                    result = EQUAL;
                } else if (o1.isEmpty()) {
                    result = BEFORE;
                } else if (o2.isEmpty()) {
                    result = AFTER;
                } else {
                    int sizeToUse = Math.min(o1Size, o2Size);
                    for (int i = 0; i < sizeToUse; i++) {
                        int compareResult = o1.get(i).compareTo(o2.get(i));
                        if (compareResult != EQUAL) {
                            result = compareResult;
                            break;
                        }
                    }

                    if (result == EQUAL) {
                        result = Integer.compare(o1Size, o2Size);
                    }
                }
            }

            return result;
        }
    }
}

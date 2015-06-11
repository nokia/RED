package org.robotframework.ide.core.testData.text;

import java.util.ConcurrentModificationException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.robotframework.ide.core.testData.text.TxtRobotFileLexer.TokenizatorOutput;


public abstract class ATableContextMatcher extends AContextMatcher {

    public ATableContextMatcher(TokenizatorOutput tokenProvider) {
        super(tokenProvider);
    }


    protected List<RobotTokenContext> buildTableHeaderContext(
            TokenizatorOutput tokenProvider, ContextType contextType,
            List<List<RobotTokenType>> combinationOfWordsToGet)
            throws ConcurrentModificationException, InterruptedException,
            ExecutionException {
        List<RobotTokenContext> contexts = new LinkedList<>();
        List<List<Integer>> possibleCombinations = prepareListOfCombinations(
                tokenProvider, combinationOfWordsToGet);

        for (List<Integer> currentCombinations : possibleCombinations) {
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
}

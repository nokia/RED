package org.robotframework.ide.core.testData.text;

import java.util.Collections;
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


    private List<Integer> getJoinedSortedWordTokenIds(
            TokenizatorOutput tokenProvider, RobotTokenType... types) {
        List<Integer> temp = new LinkedList<>();
        Map<RobotTokenType, List<Integer>> indexesOfSpecial = tokenProvider
                .getIndexesOfSpecial();
        for (RobotTokenType type : types) {
            List<Integer> settingTokensAppearsIndexes = indexesOfSpecial
                    .get(type);
            if (settingTokensAppearsIndexes != null) {
                temp.addAll(settingTokensAppearsIndexes);
            }
        }
        Collections.sort(temp);

        return temp;
    }
}

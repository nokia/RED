package org.robotframework.ide.core.testData.text;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.robotframework.ide.core.testData.text.TxtRobotFileLexer.TokenizatorOutput;


public class SettingsTableHeaderSearcher extends AContextMatcher {

    public SettingsTableHeaderSearcher(TokenizatorOutput tokenProvider) {
        super(tokenProvider);
    }


    @Override
    protected List<RobotTokenContext> findContexts(
            TokenizatorOutput tokenProvider) {
        List<RobotTokenContext> contexts = new LinkedList<>();
        // we are getting list of settings and metadata words
        // next we join them together and sort
        // next we are starting iteration over this joined list
        // during iteration we are searching for the closest from left asterisks
        // index
        // and the same for right
        // in the end we just building context if we could and we are adding
        // them to the list
        List<Integer> temp = getJoinedSortedWordTokenIds(tokenProvider);
        for (Integer settingsOrMetadataTokenId : temp) {
            RobotTokenContext context = new RobotTokenContext(
                    ContextType.SETTINGS_TABLE_HEADER);
            int prefixAsterisksId = findNearestPrefixAsterisks(tokenProvider,
                    settingsOrMetadataTokenId);
            if (prefixAsterisksId > -1) {
                context.addToken(prefixAsterisksId);
            }

            context.addToken(settingsOrMetadataTokenId);

            int suffixAsterisksId = findNearestSuffixAsterisks(tokenProvider,
                    settingsOrMetadataTokenId);
            if (suffixAsterisksId > -1) {
                context.addToken(suffixAsterisksId);
            }

            contexts.add(context);
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


    private List<Integer> getJoinedSortedWordTokenIds(
            TokenizatorOutput tokenProvider) {
        List<Integer> temp = new LinkedList<>();
        Map<RobotTokenType, List<Integer>> indexesOfSpecial = tokenProvider
                .getIndexesOfSpecial();
        List<Integer> settingTokensAppearsIndexes = indexesOfSpecial
                .get(RobotTokenType.WORD_SETTING);
        if (settingTokensAppearsIndexes != null) {
            temp.addAll(settingTokensAppearsIndexes);
        }
        List<Integer> metadataTokensAppearsIndexes = indexesOfSpecial
                .get(RobotTokenType.WORD_METADATA);
        if (metadataTokensAppearsIndexes != null) {
            temp.addAll(metadataTokensAppearsIndexes);
        }
        Collections.sort(temp);

        return temp;
    }
}

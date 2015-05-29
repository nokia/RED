package org.robotframework.ide.core.testData.text;

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
        List<RobotTokenContext> tokens = new LinkedList<>();
        List<Integer> tempIds = new LinkedList<>();

        Map<RobotTokenType, List<Integer>> typeToIdInTokenList = tokenProvider
                .getIndexesOfSpecial();

        List<Integer> settingsTableWordPosition = typeToIdInTokenList
                .get(RobotTokenType.WORD_SETTING);
        List<Integer> asterisksInText = typeToIdInTokenList
                .get(RobotTokenType.TABLE_ASTERISK);

        int previousSettingTokenId = -1;
        for (int settingsTokenId : settingsTableWordPosition) {
            int theClosedAsteriskPrefix = findTheClosedAsterisksBeforeSettingWord(
                    asterisksInText, settingsTokenId, previousSettingTokenId);
            if (theClosedAsteriskPrefix >= 0) {
                tempIds.add(theClosedAsteriskPrefix);
            }

            tempIds.add(settingsTokenId);

            int theClosedAsteriskSuffix = findTheClosedAsterisksAfterSettingWord(
                    asterisksInText, settingsTokenId);
            if (theClosedAsteriskSuffix >= 0) {
                tempIds.add(theClosedAsteriskSuffix);
            }

            previousSettingTokenId = settingsTokenId;
        }

        System.out.println(tempIds);

        return tokens;
    }


    private int findTheClosedAsterisksAfterSettingWord(
            final List<Integer> asterisksInText, int settingsTokenId) {
        int id = -1;
        for (Integer asteriskTokenId : asterisksInText) {
            // check if asterisks is after settings text
            if (asteriskTokenId > settingsTokenId) {
                id = asteriskTokenId;
                break;
            }
        }

        return id;
    }


    private int findTheClosedAsterisksBeforeSettingWord(
            final List<Integer> asterisksInText, int settingsTokenId,
            int previousSettingTokenId) {
        int id = -1;

        for (Integer asteriskTokenId : asterisksInText) {
            // check if asterisks is before settings text
            if (settingsTokenId > asteriskTokenId) {
                // check if this id is closer to previous settings - its token
                // id will be greater than previous one
                // and
                // if id of asterisk will be not duplicated because of : *
                // settings settings case
                if (id < asteriskTokenId
                        && asteriskTokenId > previousSettingTokenId) {
                    id = asteriskTokenId;
                }
            }
        }

        return id;
    }
}

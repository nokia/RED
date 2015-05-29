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

        return contexts;
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

package org.robotframework.ide.core.testData.text.contexts;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.robotframework.ide.core.testData.text.AContextMatcher;
import org.robotframework.ide.core.testData.text.ContextType;
import org.robotframework.ide.core.testData.text.RobotToken;
import org.robotframework.ide.core.testData.text.RobotTokenContext;
import org.robotframework.ide.core.testData.text.RobotTokenType;
import org.robotframework.ide.core.testData.text.TxtRobotFileLexer.TokenizatorOutput;


public class CommentsSearcher extends AContextMatcher {

    public CommentsSearcher(TokenizatorOutput tokenProvider) {
        super(tokenProvider);
    }


    @Override
    protected List<RobotTokenContext> findContexts(
            TokenizatorOutput tokenProvider) throws Exception {
        List<RobotToken> tokens = tokenProvider.getTokens();
        Map<RobotTokenType, List<Integer>> indexesOfSpecial = tokenProvider
                .getIndexesOfSpecial();
        List<Integer> wordsCommentIndexes = indexesOfSpecial
                .get(RobotTokenType.WORD_COMMENT);
        List<Integer> hashCharsIndexes = indexesOfSpecial
                .get(RobotTokenType.COMMENT_BEGIN);

        List<Integer> joinedList = getSortedJoinedList(wordsCommentIndexes,
                hashCharsIndexes);

        List<RobotTokenContext> contexts = handleAllComments(tokens, joinedList);

        return contexts;
    }


    private List<RobotTokenContext> handleAllComments(List<RobotToken> tokens,
            List<Integer> joinedList) {
        List<RobotTokenContext> contexts = new LinkedList<>();
        for (int currentCommentId = 0; currentCommentId < joinedList.size(); currentCommentId++) {
            RobotTokenContext commentCtx = new RobotTokenContext(
                    ContextType.COMMENT);
            int commentTextWords = 0;

            Integer commentTokenId = joinedList.get(currentCommentId);
            for (int tokenId = commentTokenId; tokenId < tokens.size(); tokenId++) {
                RobotToken token = tokens.get(tokenId);
                RobotTokenType type = token.getType();
                if (type == RobotTokenType.COMMENT_BEGIN
                        || type == RobotTokenType.WORD_COMMENT) {
                    if (commentTextWords > 0) {
                        break;
                    } else {
                        if (!commentCtx.getTokensId().isEmpty()) {
                            // skip current comment since before nothing
                            // interesting was presenting
                            currentCommentId += 2;
                        }
                        commentCtx.addToken(tokenId);
                    }
                } else if (type == RobotTokenType.END_OF_LINE
                        || type == RobotTokenType.END_OF_FILE) {
                    commentCtx.addToken(tokenId);
                    break;
                } else if (type == RobotTokenType.SPACE
                        || type == RobotTokenType.TABULATOR) {
                    commentCtx.addToken(tokenId);
                } else {
                    commentTextWords++;
                    commentCtx.addToken(tokenId);
                }
            }

            contexts.add(commentCtx);
        }
        return contexts;
    }


    protected List<Integer> getSortedJoinedList(
            List<Integer> wordsCommentIndexes, List<Integer> hashCharsIndexes) {
        List<Integer> joinedList = new LinkedList<>();
        if (wordsCommentIndexes != null && !wordsCommentIndexes.isEmpty()) {
            joinedList.addAll(wordsCommentIndexes);
        }
        if (hashCharsIndexes != null && !hashCharsIndexes.isEmpty()) {
            joinedList.addAll(hashCharsIndexes);
        }
        Collections.sort(joinedList);
        return joinedList;
    }
}

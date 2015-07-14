package org.robotframework.ide.core.testData.model.common;

public class Comment {

    private final CommentDeclaration commentHashOrWord;
    private Text text;


    public Comment(final CommentDeclaration commentStart) {
        this.commentHashOrWord = commentStart;
    }
}

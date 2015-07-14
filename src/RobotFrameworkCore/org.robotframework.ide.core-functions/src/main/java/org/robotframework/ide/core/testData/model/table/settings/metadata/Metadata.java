package org.robotframework.ide.core.testData.model.table.settings.metadata;

import org.robotframework.ide.core.testData.model.common.Comment;


public class Metadata {

    private final MetadataDeclaration metadataWord;
    private MetadataKey key;
    private MetadataValue value;
    private Comment comment;


    public Metadata(final MetadataDeclaration metadataWord) {
        this.metadataWord = metadataWord;
    }
}

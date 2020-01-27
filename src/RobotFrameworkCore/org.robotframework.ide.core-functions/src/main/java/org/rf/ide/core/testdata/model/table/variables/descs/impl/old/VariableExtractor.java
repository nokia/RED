/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.variables.descs.impl.old;

import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

import com.google.common.annotations.VisibleForTesting;

class VariableExtractor {

    private final VariableStructureExtractor structureExtractor;

    private final DeclarationMapper mapper;

    VariableExtractor() {
        this(new DeclarationMapper());
    }

    VariableExtractor(final String allowedVars) {
        this(new DeclarationMapper(allowedVars));
    }

    @VisibleForTesting
    VariableExtractor(final DeclarationMapper mapper) {
        this.structureExtractor = new VariableStructureExtractor();
        this.mapper = mapper;
    }

    MappingResult extract(final RobotToken token) {
        return extract(token.getFilePosition(), token.getText());
    }

    MappingResult extract(final String text) {
        return extract(FilePosition.createNotSet(), text);
    }

    MappingResult extract(final FilePosition fp, final String text) {
        try {
            final Container mainContainer = structureExtractor.buildStructureTree(text);

            final MappingResult result = mapper.map(fp, mainContainer);
            for (final IElementDeclaration dec : result.getMappedElements()) {
                dec.setRobotTokenPosition(fp);
            }
            return result;

        } catch (final Exception e) {
            throw new VariableExtractionException(
                    "An exception occurs during variable extraction at position " + fp + " for text " + text, e);
        }
    }

    @VisibleForTesting
    static class VariableExtractionException extends RuntimeException {

        private static final long serialVersionUID = -8666114255629013896L;

        VariableExtractionException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }
}

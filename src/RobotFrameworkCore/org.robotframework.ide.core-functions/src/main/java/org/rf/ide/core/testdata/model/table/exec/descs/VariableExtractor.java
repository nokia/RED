/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.exec.descs;

import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.Container;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.VariableStructureExtractor;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.DeclarationMapper;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.IElementDeclaration;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.MappingResult;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

import com.google.common.annotations.VisibleForTesting;

public class VariableExtractor {

    private final VariableStructureExtractor structureExtractor;

    private final DeclarationMapper mapper;

    public VariableExtractor() {
        this.structureExtractor = new VariableStructureExtractor();
        this.mapper = new DeclarationMapper();
    }

    public VariableExtractor(final DeclarationMapper mapper) {
        this.structureExtractor = new VariableStructureExtractor();
        this.mapper = mapper;
    }

    public MappingResult extract(final RobotToken token, final String fileName) {
        return extract(token.getFilePosition(), token.getText(), fileName);
    }

    public MappingResult extract(final RobotToken token) {
        return extract(token.getFilePosition(), token.getText(), "");
    }

    public MappingResult extract(final String text) {
        return extract(FilePosition.createNotSet(), text, "");
    }

    private MappingResult extract(final FilePosition fp, final String text, final String fileName) {
        try {
            final Container mainContainer = structureExtractor.buildStructureTree(text);

            final MappingResult result = mapper.map(fp, mainContainer, fileName);
            for (final IElementDeclaration dec : result.getMappedElements()) {
                dec.setRobotTokenPosition(fp);
            }

            return result;
        } catch (final Exception e) {
            throw new VariableExtractionException("An exception occurs during variable extraction in file " + fileName
                    + " at position " + fp + " for text " + text, e);
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

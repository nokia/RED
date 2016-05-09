/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.table.exec.descs.TextPosition;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.ContainerElement;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.ContainerElementType;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.IContainerElement;

public class SimpleElementsMapper {

    private final Map<ContainerElementType, IElementMapper> mappers = new HashMap<>();

    public SimpleElementsMapper() {
        mappers.put(ContainerElementType.TEXT, new TextDeclarationMapper());
        mappers.put(ContainerElementType.CURRLY_BRACKET_CLOSE, new TextDeclarationMapper());
        mappers.put(ContainerElementType.SQUARE_BRACKET_CLOSE, new TextDeclarationMapper());
        mappers.put(ContainerElementType.WHITESPACE, new WhitespaceMapper());
        mappers.put(ContainerElementType.ESCAPE, new EscapeMapper());
        mappers.put(ContainerElementType.VARIABLE_TYPE_ID, new VariableIdentificatorMapper());
    }

    public interface IElementMapper {

        MappingResult map(final MappingResult currentResult, final IContainerElement containerElement,
                final FilePosition fp, final String filename);
    }

    public IElementMapper getMapperFor(final ContainerElementType type) {
        return mappers.get(type);
    }

    private class TextDeclarationMapper extends AMergeAllowedMapper {

        public TextDeclarationMapper() {
            super(Arrays.asList(ContainerElementType.TEXT, ContainerElementType.WHITESPACE));
        }
    }

    private class WhitespaceMapper extends AMergeAllowedMapper {

        public WhitespaceMapper() {
            super(Arrays.asList(ContainerElementType.VARIABLE_TYPE_ID, ContainerElementType.ESCAPE,
                    ContainerElementType.TEXT, ContainerElementType.WHITESPACE));
        }
    }

    private abstract class AMergeAllowedMapper implements IElementMapper {

        private final List<ContainerElementType> mergeAllowedTypes;

        public AMergeAllowedMapper(final List<ContainerElementType> mergeAllowedTypes) {
            this.mergeAllowedTypes = mergeAllowedTypes;
        }

        @Override
        public MappingResult map(final MappingResult currentResult, final IContainerElement containerElement,
                final FilePosition fp, final String filename) {
            MappingResult mr = new MappingResult(fp, filename);
            List<IElementDeclaration> mappedElements = currentResult.getMappedElements();
            TextPosition position = ((ContainerElement) containerElement).getPosition();
            TextDeclaration decText = new TextDeclaration(position, containerElement.getType());

            boolean shouldMapToNew = true;
            if (!mappedElements.isEmpty()) {
                IElementDeclaration lastMapped = mappedElements.get(mappedElements.size() - 1);
                if (lastMapped instanceof JoinedTextDeclarations) {
                    JoinedTextDeclarations joined = (JoinedTextDeclarations) lastMapped;
                    if (containsOnly(joined.getElementsDeclarationInside(), mergeAllowedTypes)) {
                        joined.addElementDeclarationInside(decText);
                        shouldMapToNew = false;
                    }
                }
            }

            if (shouldMapToNew) {
                JoinedTextDeclarations text = new JoinedTextDeclarations();
                text.addElementDeclarationInside(decText);
                mr.addMappedElement(text);
            }

            mr.setLastFilePosition(new FilePosition(fp.getLine(), fp.getColumn() + position.getLength(),
                    fp.getOffset() + position.getLength()));

            return mr;
        }
    }

    private class EscapeMapper implements IElementMapper {

        @Override
        public MappingResult map(final MappingResult currentResult, final IContainerElement containerElement,
                final FilePosition fp, final String filename) {
            return createNewTextDeclaration(containerElement, fp, filename);
        }

    }

    private class VariableIdentificatorMapper implements IElementMapper {

        @Override
        public MappingResult map(final MappingResult currentResult, final IContainerElement containerElement,
                final FilePosition fp, final String filename) {
            return createNewTextDeclaration(containerElement, fp, filename);
        }
    }

    private MappingResult createNewTextDeclaration(final IContainerElement containerElement, final FilePosition fp,
            final String filename) {
        MappingResult mr = new MappingResult(fp, filename);
        TextPosition position = ((ContainerElement) containerElement).getPosition();
        TextDeclaration decText = new TextDeclaration(position, containerElement.getType());
        JoinedTextDeclarations text = new JoinedTextDeclarations();
        text.addElementDeclarationInside(decText);
        mr.addMappedElement(text);
        mr.setLastFilePosition(new FilePosition(fp.getLine(), fp.getColumn() + position.getLength(),
                fp.getOffset() + position.getLength()));

        return mr;
    }

    private boolean containsOnly(final List<IElementDeclaration> mappedElements,
            final List<ContainerElementType> typesAllowed) {
        boolean result = true;
        for (IElementDeclaration dec : mappedElements) {
            if (!containsOnly(dec, typesAllowed)) {
                result = false;
                break;
            }
        }
        return result;
    }

    public boolean containsOnly(final IElementDeclaration elem, final List<ContainerElementType> typesAllowed) {
        boolean result = true;
        final List<ContainerElementType> types = elem.getTypes();
        for (ContainerElementType t : types) {
            if (!typesAllowed.contains(t)) {
                result = false;
                break;
            }
        }

        return result;
    }
}

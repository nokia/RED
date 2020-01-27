/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.variables.descs.impl.old;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.table.variables.descs.impl.old.Container;
import org.rf.ide.core.testdata.model.table.variables.descs.impl.old.ContainerElementType;
import org.rf.ide.core.testdata.model.table.variables.descs.impl.old.DeclarationMapper;
import org.rf.ide.core.testdata.model.table.variables.descs.impl.old.MappingResult;
import org.rf.ide.core.testdata.model.table.variables.descs.impl.old.VariableStructureExtractor;

/**
 * @author lwlodarc
 *
 */
public class DeclarationMapperTest {

    private final DeclarationMapper mapper = new DeclarationMapper();
    private final VariableStructureExtractor extractor = new VariableStructureExtractor();

    @Test
    public void mappingResultIsEmpty_forEmpty() {
        final MappingResult result = mapper.map(new FilePosition(0, 0), new Container(null));

        assertThat(result.getCorrectVariables()).isEmpty();
        assertThat(result.getMappedElements()).isEmpty();
    }

    @Test
    public void mappingResultContainsNoVariable_forTextOnly() {
        final String text = "  just  a  text";
        final Container container = extractor.buildStructureTree(text);
        final MappingResult result = mapper.map(new FilePosition(0, 0), container);

        assertThat(result.getCorrectVariables()).isEmpty();
        assertThat(result.getMappedElements()).hasSize(1);
        assertThat(result.getMappedElements().get(0).isComplex()).isFalse();
        assertThat(result.getMappedElements().get(0).getTypes()).contains(ContainerElementType.TEXT);
    }

    @Test
    public void mappingResultContainsVariable_forSimpleScalarVariable() {
        final String text = "${var}";
        final Container container = extractor.buildStructureTree(text);
        final MappingResult result = mapper.map(new FilePosition(0, 0), container);

        assertThat(result.getCorrectVariables()).hasSize(1);
        assertThat(result.getMappedElements()).hasSize(1);
        assertThat(result.getMappedElements().get(0)).isEqualTo(result.getCorrectVariables().get(0));
    }

    @Test
    public void mappingResultContainsVariable_forSimpleEnvironmentVariable() {
        final String text = "%{env_var}";
        final Container container = extractor.buildStructureTree(text);
        final MappingResult result = mapper.map(new FilePosition(0, 0), container);

        assertThat(result.getCorrectVariables()).hasSize(1);
        assertThat(result.getMappedElements()).hasSize(1);
        assertThat(result.getMappedElements().get(0)).isEqualTo(result.getCorrectVariables().get(0));
    }

    @Test
    public void mappingResultContainsNoVariable_forSimpleEnvironmentVariable() {
        final String text = "%{env_var}";
        final Container container = extractor.buildStructureTree(text);
        final DeclarationMapper mapper = new DeclarationMapper("$@&");
        final MappingResult result = mapper.map(new FilePosition(0, 0), container);

        assertThat(result.getCorrectVariables()).isEmpty();
        assertThat(result.getMappedElements()).hasSize(4);
        assertThat(result.getMappedElements().get(0).isComplex()).isFalse();
        assertThat(result.getMappedElements().get(0).getTypes()).contains(ContainerElementType.VARIABLE_TYPE_ID);
        assertThat(result.getMappedElements().get(1).isComplex()).isFalse();
        assertThat(result.getMappedElements().get(1).getTypes()).contains(ContainerElementType.CURLY_BRACKET_OPEN);
        assertThat(result.getMappedElements().get(2).isComplex()).isFalse();
        assertThat(result.getMappedElements().get(2).getTypes()).contains(ContainerElementType.TEXT);
        assertThat(result.getMappedElements().get(3).isComplex()).isFalse();
        assertThat(result.getMappedElements().get(3).getTypes()).contains(ContainerElementType.CURLY_BRACKET_CLOSE);
    }
}

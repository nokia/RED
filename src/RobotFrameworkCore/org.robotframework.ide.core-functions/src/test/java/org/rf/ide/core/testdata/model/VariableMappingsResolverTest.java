/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.newArrayList;
import static org.rf.ide.core.project.RobotProjectConfig.VariableMapping.create;
import static org.rf.ide.core.testdata.model.VariableMappingsResolver.resolve;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class VariableMappingsResolverTest {

    @Test
    public void testResolvingSimpleMappings() {
        assertThat(resolve(newArrayList())).isEmpty();
        assertThat(resolve(newArrayList(create("${AbC}", "x/y"), create("${dEf}", "y/z"), create("${G_H I}", "a/b/c"))))
                .hasSize(3)
                .containsAllEntriesOf(ImmutableMap.of("${abc}", "x/y", "${def}", "y/z", "${ghi}", "a/b/c"));
    }
}

/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.execution.debug.contexts;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.exec.descs.IExecutableRowDescriptor;
import org.rf.ide.core.testdata.model.table.exec.descs.impl.ForLoopDeclarationRowDescriptor;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class ForLoopTest {

    @Test
    public void theExecutableOfForLoop_isTheExecutableOfLoopHeader() {
        final RobotExecutableRow<?> executable = new RobotExecutableRow<>();

        final ExecutableWithDescriptor loopHeader = mock(ExecutableWithDescriptor.class);
        when(loopHeader.getExecutable()).thenReturn((RobotExecutableRow) executable);

        final ForLoop forLoop = new ForLoop(loopHeader, newArrayList());
        assertThat(forLoop.getExecutable()).isSameAs(executable);
    }

    @Test
    public void theDescriptorOfForLoop_isTheDescriptorOfLoopHeader() {
        final IExecutableRowDescriptor<?> descriptor = new ForLoopDeclarationRowDescriptor<>(
                new RobotExecutableRow<>());

        final ExecutableWithDescriptor loopHeader = mock(ExecutableWithDescriptor.class);
        when(loopHeader.getDescriptor()).thenReturn((IExecutableRowDescriptor) descriptor);

        final ForLoop forLoop = new ForLoop(loopHeader, newArrayList());
        assertThat(forLoop.getDescriptor()).isSameAs(descriptor);
    }

    @Test(expected = ClassCastException.class)
    public void exceptionIsThrown_whenDescriptorOfLoopHeaderIsOfWrongType() {
        final IExecutableRowDescriptor<?> descriptor = mock(IExecutableRowDescriptor.class);

        final ExecutableWithDescriptor loopHeader = mock(ExecutableWithDescriptor.class);
        when(loopHeader.getDescriptor()).thenReturn((IExecutableRowDescriptor) descriptor);

        final ForLoop forLoop = new ForLoop(loopHeader, newArrayList());
        forLoop.getDescriptor();
    }

    @Test
    public void innerExecutablesAreReturnedAsProvidedToConstructor() {
        final ExecutableWithDescriptor loopHeader = mock(ExecutableWithDescriptor.class);

        final ExecutableWithDescriptor exec1 = mock(ExecutableWithDescriptor.class);
        final ExecutableWithDescriptor exec2 = mock(ExecutableWithDescriptor.class);
        final ForLoop forLoop = new ForLoop(loopHeader, newArrayList(exec1, exec2));

        assertThat(forLoop.getInnerExecutables()).containsExactly(exec1, exec2);
    }

    @Test
    public void innerDescriptorsAreReturnedAsTakenFromInnerExecutables() {
        final IExecutableRowDescriptor<?> headerDescriptor = new ForLoopDeclarationRowDescriptor<>(
                new RobotExecutableRow<>());
        final IExecutableRowDescriptor<?> childDescriptor1 = mock(IExecutableRowDescriptor.class);
        final IExecutableRowDescriptor<?> childDescriptor2 = mock(IExecutableRowDescriptor.class);

        final ExecutableWithDescriptor loopHeader = mock(ExecutableWithDescriptor.class);
        final ExecutableWithDescriptor exec1 = mock(ExecutableWithDescriptor.class);
        final ExecutableWithDescriptor exec2 = mock(ExecutableWithDescriptor.class);
        when(loopHeader.getDescriptor()).thenReturn((IExecutableRowDescriptor) headerDescriptor);
        when(exec1.getDescriptor()).thenReturn((IExecutableRowDescriptor) childDescriptor1);
        when(exec2.getDescriptor()).thenReturn((IExecutableRowDescriptor) childDescriptor2);

        final ForLoop forLoop = new ForLoop(loopHeader, newArrayList(exec1, exec2));
        assertThat(forLoop.getChildrenDescriptors()).containsExactly(childDescriptor1, childDescriptor2);
    }
}

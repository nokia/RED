package org.rf.ide.core.execution.debug.contexts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.rf.ide.core.execution.debug.KeywordCallType;
import org.rf.ide.core.execution.debug.RobotBreakpointSupplier;
import org.rf.ide.core.execution.debug.RunningKeyword;

public class KeywordFromLibraryContextTest {

    @Test
    public void thisIsALibraryKeywordContext() {
        assertThat(new KeywordFromLibraryContext().isLibraryKeywordContext()).isTrue();
    }

    @Test
    public void sameContextIsReturned_whenMovingToAnyKeyword() {
        final RobotBreakpointSupplier breakpointSupplier = mock(RobotBreakpointSupplier.class);

        final KeywordFromLibraryContext context = new KeywordFromLibraryContext();
        assertThat(context.moveTo(new RunningKeyword("lib", "kw", KeywordCallType.NORMAL_CALL), breakpointSupplier))
                .isSameAs(context);
    }

}

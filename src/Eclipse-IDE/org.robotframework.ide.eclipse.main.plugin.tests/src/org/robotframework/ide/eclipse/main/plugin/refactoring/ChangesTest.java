/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.refactoring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.eclipse.core.runtime.Path;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.NullChange;
import org.junit.Test;

import com.google.common.base.Optional;

public class ChangesTest {

    @Test
    public void whenCompositeChangeIsNormalized_allNullChangesAreRemoved() {
        final Change actualChange1 = mock(Change.class);
        final Change actualChange2 = mock(Change.class);

        final CompositeChange compositeChange = new CompositeChange("Change");
        compositeChange.add(new NullChange());
        compositeChange.add(actualChange1);
        compositeChange.add(new NullChange());
        compositeChange.add(actualChange2);
        compositeChange.add(new NullChange());
        
        final Change result = Changes.normalizeCompositeChange(compositeChange);
        
        assertThat(result).isSameAs(compositeChange);
        final Change[] children = ((CompositeChange) result).getChildren();
        assertThat(children).hasSize(2);
        assertThat(children[0]).isSameAs(actualChange1);
        assertThat(children[1]).isSameAs(actualChange2);
    }

    @Test
    public void compositeChangeIsNormalizedToNullChange_ifItContainsOnlyNullChanges() {
        final CompositeChange compositeChange1 = new CompositeChange("Change");
        
        final CompositeChange compositeChange2 = new CompositeChange("Change");
        compositeChange2.add(new NullChange());
        compositeChange2.add(new NullChange());
        compositeChange2.add(new NullChange());
        
        assertThat(Changes.normalizeCompositeChange(compositeChange1)).isInstanceOf(NullChange.class);
        assertThat(Changes.normalizeCompositeChange(compositeChange2)).isInstanceOf(NullChange.class);
    }

    @Test
    public void xmlCharactersAreEscapedInGivenPath() {
        assertThat(Changes.excapeXmlCharacters(path("/a/b/c"))).isEqualTo(path("/a/b/c"));
        assertThat(Changes.excapeXmlCharacters(path("/a/b/c/file.txt"))).isEqualTo(path("/a/b/c/file.txt"));
        assertThat(Changes.excapeXmlCharacters(path("/path<&>/dir"))).isEqualTo(path("/path&lt;&amp;&gt;/dir"));
    }

    @Test
    public void affectedPathIsProperlyTransformed() {
        assertThat(Changes.transformAffectedPath(path("a/b/c"), path("a/b/d"), path("x/y/z")))
                .isEqualTo(Optional.absent());
        assertThat(Changes.transformAffectedPath(path("a/b/c"), path("a/b/d"), path("a/y/z")))
                .isEqualTo(Optional.absent());
        assertThat(Changes.transformAffectedPath(path("a/b/c"), path("a/b/d"), path("a/b/c")))
                .isEqualTo(Optional.of(path("a/b/d")));
        assertThat(Changes.transformAffectedPath(path("a/b/c"), path("a/b/d"), path("a/b/c/x")))
                .isEqualTo(Optional.of(path("a/b/d/x")));
    }

    private static Path path(final String path) {
        return new Path(path);
    }

}

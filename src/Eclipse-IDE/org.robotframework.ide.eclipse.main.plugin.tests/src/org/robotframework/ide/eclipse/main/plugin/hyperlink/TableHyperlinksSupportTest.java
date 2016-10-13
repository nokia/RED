/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.hyperlink;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.assertj.core.api.Condition;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.hyperlink.TableHyperlinksSupport;

import com.google.common.base.Optional;

public class TableHyperlinksSupportTest {

    @Test
    public void testHyperlinksRegionMerging() {
        final IHyperlink link1 = createHyperlink(new Region(10, 10));
        final IHyperlink link2 = createHyperlink(new Region(0, 30));
        final IHyperlink link3 = createHyperlink(new Region(15, 10));

        assertThat(calculateMerged()).is(absent());
        assertThat(calculateMerged(link1)).is(containing(new Region(10, 10)));
        assertThat(calculateMerged(link1, link2)).is(containing(new Region(0, 30)));
        assertThat(calculateMerged(link2, link1)).is(containing(new Region(0, 30)));
        assertThat(calculateMerged(link1, link3)).is(containing(new Region(10, 15)));
        assertThat(calculateMerged(link3, link1)).is(containing(new Region(10, 15)));

        TableHyperlinksSupport.getMergedHyperlinkRegion(new ArrayList<IHyperlink>());
    }

    private static IHyperlink createHyperlink(final IRegion region) {
        final IHyperlink link = mock(IHyperlink.class);
        when(link.getHyperlinkRegion()).thenReturn(region);
        return link;
    }

    private static Condition<? super Optional<IRegion>> containing(final IRegion region) {
        return new Condition<Optional<IRegion>>() {

            @Override
            public boolean matches(final Optional<IRegion> optionalRegion) {
                return optionalRegion.isPresent() && optionalRegion.get().equals(region);
            }
        };
    }

    private static Condition<? super Optional<IRegion>> absent() {
        return new Condition<Optional<IRegion>>() {
            @Override
            public boolean matches(final Optional<IRegion> optionalRegion) {
                return !optionalRegion.isPresent();
            }
        };
    }

    private static Optional<IRegion> calculateMerged(final IHyperlink... hyperlinks) {
        return TableHyperlinksSupport.getMergedHyperlinkRegion(newArrayList(hyperlinks));
    }

}

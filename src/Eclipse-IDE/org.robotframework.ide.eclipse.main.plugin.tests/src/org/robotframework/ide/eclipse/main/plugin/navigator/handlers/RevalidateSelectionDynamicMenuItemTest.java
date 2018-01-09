package org.robotframework.ide.eclipse.main.plugin.navigator.handlers;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.jface.action.IContributionItem;
import org.junit.Test;

public class RevalidateSelectionDynamicMenuItemTest {

    private final RevalidateSelectionDynamicMenuItem item = new RevalidateSelectionDynamicMenuItem();

    @Test
    public void testIf_thereIsContributionItemCreated() {
        final IContributionItem[] contributionItems = item.getContributionItems();

        assertThat(contributionItems).hasSize(1);
        assertThat(contributionItems[0].getId()).isEqualTo("org.robotframework.red.menu.dynamic.selection.revalidate");
    }
}

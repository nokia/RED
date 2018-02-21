package org.robotframework.ide.eclipse.main.plugin.search;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.search.ui.text.Match;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.junit.Test;
import org.rf.ide.core.libraries.KeywordSpecification;
import org.rf.ide.core.libraries.LibrarySpecification;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.RedTheme;
import org.robotframework.ide.eclipse.main.plugin.search.SearchResultContentProvider.KeywordWithParent;
import org.robotframework.ide.eclipse.main.plugin.search.SearchResultContentProvider.LibraryWithParent;
import org.robotframework.ide.eclipse.main.plugin.search.SearchResultContentProvider.Libs;
import org.robotframework.red.graphics.ImagesManager;

public class SearchResultLabelProviderTest {

    @Test
    public void resourceLabelIsTakenViaWorkbenchAdapter() {
        final IResource resource = mock(IResource.class);
        final IWorkbenchAdapter workbenchAdapter = mock(IWorkbenchAdapter.class);
        when(workbenchAdapter.getLabel(resource)).thenReturn("label");
        when(resource.getAdapter(IWorkbenchAdapter.class)).thenReturn(workbenchAdapter);

        final SearchResultLabelProvider provider = new SearchResultLabelProvider();
        final StyledString label = provider.getStyledText(resource);
        assertThat(label.getString()).isEqualTo("label");
        assertThat(label.getStyleRanges()).isEmpty();
    }

    @Test
    public void libsLabelIsSimplyLibraries() {
        final SearchResultLabelProvider provider = new SearchResultLabelProvider();
        final StyledString label = provider.getStyledText(new Libs(null));
        assertThat(label.getString()).isEqualTo("Libraries");
        assertThat(label.getStyleRanges()).isEmpty();
    }

    @Test
    public void libraryLabelIsANameOfLibraryWithDecoratedNumberOfMatches() {
        final SearchResultLabelProvider provider = new SearchResultLabelProvider();

        final StyledString label = provider.getStyledText(new LibraryWithParent(null,
                LibrarySpecification.create("myLib"), newArrayList(mock(Match.class), mock(Match.class))));
        assertThat(label.getString()).isEqualTo("myLib (2 matches)");
        assertThat(label.getStyleRanges()).isEqualTo(
                new StyleRange[] { new StyleRange(5, 12, RedTheme.Colors.getEclipseDecorationColor(), null) });
    }

    @Test
    public void keywordLabelIsANameOfKeywordWithDecoratedNumberOfMatches() {
        final SearchResultLabelProvider provider = new SearchResultLabelProvider();

        final StyledString label = provider.getStyledText(new KeywordWithParent(null,
                KeywordSpecification.create("myLib"), newArrayList(mock(Match.class))));
        assertThat(label.getString()).isEqualTo("myLib (1 matches)");
        assertThat(label.getStyleRanges()).isEqualTo(
                new StyleRange[] { new StyleRange(5, 12, RedTheme.Colors.getEclipseDecorationColor(), null) });
    }

    @Test
    public void libraryDocumentationMatchHasLabelMadeFromDocumentationWithHighlightedMatch() {
        final SearchResultLabelProvider provider = new SearchResultLabelProvider();

        final LibrarySpecification libSpec = LibrarySpecification.create("myLib");
        libSpec.setDocumentation("line\nanother line\nandanother\n");

        final StyledString label = provider.getStyledText(new LibraryDocumentationMatch(null, libSpec, 10, 3));
        assertThat(label.getString()).isEqualTo("another line");
        assertThat(label.getStyleRanges()).isEqualTo(
                new StyleRange[] { new StyleRange(5, 3, null, RedTheme.Colors.getEclipseSearchMatchColor()) });
    }

    @Test
    public void keywordDocumentationMatchHasLabelMadeFromDocumentationWithHighlightedMatch() {
        final SearchResultLabelProvider provider = new SearchResultLabelProvider();

        final KeywordSpecification kwSpec = KeywordSpecification.create("myLib");
        kwSpec.setDocumentation("line\nanother line\nandanother\n");

        final StyledString label = provider.getStyledText(new KeywordDocumentationMatch(null, null, kwSpec, 10, 3));
        assertThat(label.getString()).isEqualTo("another line");
        assertThat(label.getStyleRanges()).isEqualTo(
                new StyleRange[] { new StyleRange(5, 3, null, RedTheme.Colors.getEclipseSearchMatchColor()) });
    }

    @Test
    public void arbitraryObjectHasEmptyLabel() {
        final SearchResultLabelProvider provider = new SearchResultLabelProvider();

        final StyledString label = provider.getStyledText(new Object());
        assertThat(label.getString()).isEmpty();
        assertThat(label.getStyleRanges()).isEmpty();

    }

    @Test
    public void resourceImageIsTakenViaWorkbenchAdapter() {
        final IResource resource = mock(IResource.class);
        final IWorkbenchAdapter workbenchAdapter = mock(IWorkbenchAdapter.class);
        when(workbenchAdapter.getImageDescriptor(resource)).thenReturn(RedImages.getElementImage());
        when(resource.getAdapter(IWorkbenchAdapter.class)).thenReturn(workbenchAdapter);

        final SearchResultLabelProvider provider = new SearchResultLabelProvider();
        final Image image = provider.getImage(resource);
        assertThat(image).isSameAs(ImagesManager.getImage(RedImages.getElementImage()));
    }

    @Test
    public void libsElementHasLibraryImage() {
        final SearchResultLabelProvider provider = new SearchResultLabelProvider();

        final Image image = provider.getImage(new Libs(null));
        assertThat(image).isSameAs(ImagesManager.getImage(RedImages.getLibraryImage()));
    }

    @Test
    public void libraryHasBookImage() {
        final SearchResultLabelProvider provider = new SearchResultLabelProvider();

        final Image image = provider.getImage(new LibraryWithParent(null, new LibrarySpecification(), newArrayList()));
        assertThat(image).isSameAs(ImagesManager.getImage(RedImages.getBookImage()));
    }

    @Test
    public void libraryKeywordHasKeywordImage() {
        final SearchResultLabelProvider provider = new SearchResultLabelProvider();

        final Image image = provider.getImage(new KeywordWithParent(null, new KeywordSpecification(), newArrayList()));
        assertThat(image).isSameAs(ImagesManager.getImage(RedImages.getKeywordImage()));
    }

    @Test
    public void matchHasSearchMarkerImage() {
        final SearchResultLabelProvider provider = new SearchResultLabelProvider();

        final Image image = provider.getImage(mock(Match.class));
        assertThat(image).isSameAs(ImagesManager.getImage(RedImages.getSearchMarkerImage()));
    }

    @Test
    public void arbitraryObjectHasNoImage() {
        final SearchResultLabelProvider provider = new SearchResultLabelProvider();

        final Image image = provider.getImage("something");
        assertThat(image).isNull();
    }

}

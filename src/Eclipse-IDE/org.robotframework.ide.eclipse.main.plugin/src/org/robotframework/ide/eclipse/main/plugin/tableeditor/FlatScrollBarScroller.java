package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import org.eclipse.nebula.widgets.nattable.viewport.IScroller;
import org.eclipse.swt.widgets.Listener;

import com.codeaffine.eclipse.swt.widget.scrollbar.FlatScrollBar;

class FlatScrollBarScroller implements IScroller<FlatScrollBar> {

    private final FlatScrollBar scrollBar;

    FlatScrollBarScroller(final FlatScrollBar scrollBar) {
        this.scrollBar = scrollBar;
    }

    @Override
    public FlatScrollBar getUnderlying() {
        return scrollBar;
    }

    @Override
    public boolean isDisposed() {
        return scrollBar.isDisposed();
    }

    @Override
    public void addListener(final int eventType, final Listener listener) {
        scrollBar.addListener(eventType, listener);
    }

    @Override
    public void removeListener(final int eventType, final Listener listener) {
        scrollBar.removeListener(eventType, listener);
    }

    @Override
    public int getSelection() {
        return scrollBar.getSelection();
    }

    @Override
    public void setSelection(final int value) {
        scrollBar.setSelection(value);
    }

    @Override
    public int getMaximum() {
        return scrollBar.getMaximum();
    }

    @Override
    public void setMaximum(final int value) {
        scrollBar.setMaximum(value);
    }

    @Override
    public int getPageIncrement() {
        return scrollBar.getPageIncrement();
    }

    @Override
    public void setPageIncrement(final int value) {
        scrollBar.setPageIncrement(value);
    }

    @Override
    public int getThumb() {
        return scrollBar.getThumb();
    }

    @Override
    public void setThumb(final int value) {
        scrollBar.setThumb(value);
    }

    @Override
    public int getIncrement() {
        return scrollBar.getIncrement();
    }

    @Override
    public void setIncrement(final int value) {
        scrollBar.setIncrement(value);
    }

    @Override
    public boolean getEnabled() {
        return scrollBar.getEnabled();
    }

    @Override
    public void setEnabled(final boolean enabled) {
        scrollBar.setEnabled(enabled);
    }

    @Override
    public boolean getVisible() {
        return scrollBar.getVisible();
    }

    @Override
    public void setVisible(final boolean visible) {
        scrollBar.setVisible(visible);
    }
}

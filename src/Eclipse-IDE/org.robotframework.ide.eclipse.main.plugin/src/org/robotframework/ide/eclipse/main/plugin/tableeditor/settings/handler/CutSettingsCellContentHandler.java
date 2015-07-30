package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.handler;

import org.eclipse.e4.tools.compat.parts.DIHandler;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.handler.E4CutCellContentHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.handler.CutSettingsCellContentHandler.E4CutSettingsCellContentHandler;

import com.google.common.base.Optional;

public class CutSettingsCellContentHandler extends DIHandler<E4CutSettingsCellContentHandler> {

    public CutSettingsCellContentHandler() {
        super(E4CutSettingsCellContentHandler.class);
    }

    public static class E4CutSettingsCellContentHandler extends E4CutCellContentHandler {
        @Override
        protected Optional<? extends EditorCommand> provideCommandForAttributeChange(final RobotElement element,
                final int index, final int noOfColumns) {
            return new SettingsAttributesCommandsProvider().provide(element, index, noOfColumns, "");
        }
    }
}

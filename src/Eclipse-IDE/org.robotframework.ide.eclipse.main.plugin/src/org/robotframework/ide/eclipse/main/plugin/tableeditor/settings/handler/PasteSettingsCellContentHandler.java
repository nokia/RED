package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.handler;

import org.eclipse.e4.tools.compat.parts.DIHandler;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.handler.E4PasteCellContentHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.handler.PasteSettingsCellContentHandler.E4PasteSettingsCellContentHandler;

import com.google.common.base.Optional;

public class PasteSettingsCellContentHandler extends DIHandler<E4PasteSettingsCellContentHandler> {

    public PasteSettingsCellContentHandler() {
        super(E4PasteSettingsCellContentHandler.class);
    }

    public static class E4PasteSettingsCellContentHandler extends E4PasteCellContentHandler {
        @Override
        protected Optional<? extends EditorCommand> provideCommandForAttributeChange(final RobotElement element,
                final int index, final int noOfColumns, final String newAttribute) {
            return new SettingsAttributesCommandsProvider().provide(element, index, noOfColumns, newAttribute);
        }
    }
}

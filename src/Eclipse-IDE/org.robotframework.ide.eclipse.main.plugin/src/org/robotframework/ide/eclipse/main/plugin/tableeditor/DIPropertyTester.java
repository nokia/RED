package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.ui.PlatformUI;

public class DIPropertyTester<C> extends PropertyTester {

    public static final String RECEIVER = "propertyTester/Receiver";
    public static final String PROPERTY = "propertyTester/Property";
    public static final String ARGUMENTS = "propertyTester/Arguments";
    public static final String EXPECTED_VALUE = "propertyTester/ExpectedValue";
    private final C component;

    public DIPropertyTester(final Class<C> clazz) {
        final IEclipseContext context = getActiveContext();
        component = ContextInjectionFactory.make(clazz, context);
    }

    private static IEclipseContext getActiveContext() {
        final IEclipseContext parentContext = getParentContext();
        return parentContext.getActiveLeaf();
    }

    private static IEclipseContext getParentContext() {
        return (IEclipseContext) PlatformUI.getWorkbench().getService(IEclipseContext.class);
    }

    @Override
    public boolean test(final Object receiver, final String property, final Object[] args, final Object expectedValue) {
        final IEclipseContext activeContext = getActiveContext();
        final IEclipseContext child = activeContext.createChild();

        child.set(RECEIVER, receiver);
        child.set(PROPERTY, property);
        child.set(ARGUMENTS, args);
        child.set(EXPECTED_VALUE, expectedValue);
        
        return ((Boolean) ContextInjectionFactory.invoke(component, PropertyTest.class, child)).booleanValue();
    }
    
    /**
     * Use this annotation to tag methods of property tester which tests some properties.
     */
    @Documented
    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface PropertyTest {
        
    }
}

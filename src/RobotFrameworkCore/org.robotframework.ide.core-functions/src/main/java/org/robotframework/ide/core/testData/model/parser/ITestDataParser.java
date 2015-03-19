package org.robotframework.ide.core.testData.model.parser;

import org.robotframework.ide.core.testData.model.parser.result.ParseResult;


/**
 * Give possibility to declare how particular element should be parsed to model
 * tree.
 * 
 * @author wypych
 * @serial RobotFramework 2.8.6
 * @serial 1.0
 * 
 * @param <InputType>
 *            original input type i.e. text, xml element
 * @param <OutputType>
 *            output type means how element should be visible in test data model
 *            for editor
 */
public interface ITestDataParser<InputType, OutputType> {

    /**
     * Should be use to pre-conditional check if parser can handle input data.
     * 
     * @param testDataPart
     * @return an information if we can handle
     * 
     * @serial 1.0
     */
    boolean canHandleElement(InputType testDataPart);


    /**
     * creates result of parsing - information about process itself are included
     * in produced object it is strongly recommend to use
     * {@link #canHandleElement(InputType)} before call this method
     * 
     * @param testDataPart
     * @return result of parsing
     * 
     * @serial 1.0
     */
    ParseResult<InputType, OutputType> parse(InputType testDataPart);


    /**
     * use for internal mapping inside higher-level parser to low-level elements
     * i.e.
     * 
     * <pre>
     * {@code
     *     <high-level-element name="h2">
     *         <low-level-1 name="l1">
     *         </low-level-1>
     *         <low-level-1 name="l2">
     *            <![CDATA here]...
     *         </low-level-1>
     *      </high-level-element>
     * }
     * </pre>
     * 
     * id <code>name="l1"</code> should be handle different than id
     * <code>name="l2"</code> therefore some distinguished should be done in
     * <code>high-level-element</code> level, for this propose this method could
     * be use
     * 
     * this method give possibility if user want in high-level parser to
     * overload current behavior by own class
     * 
     * @return id inside key store (i.e. map)
     * 
     * @serial 1.0
     */
    String getElementUniqueId();
}

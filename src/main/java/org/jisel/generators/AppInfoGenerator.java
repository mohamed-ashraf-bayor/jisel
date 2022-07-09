/**
 * Copyright (c) 2022 Mohamed Ashraf Bayor
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.jisel.generators;

import org.jisel.JiselAnnotationProcessor;

import java.io.IOException;
import java.util.Properties;

/**
 * Exposes a bunch of String constants related to the app information (name, version, main class,...) along with a static
 * function allowing to read from a properties file
 */
public interface AppInfoGenerator {

    /**
     * Default app version String to use only when a severe error occurs while trying to read from a properties file
     */
    String DEFAULT_APP_VERSION = "1.2.0";

    /**
     * Properties file name
     */
    String APPLICATION_PROPERTIES_FILENAME = "application.properties";

    /**
     * Parameter name of the Properties file referring to the current app version
     */
    String INFO_APP_VERSION_PROPERTY_NAME = "info.app.version";

    /**
     * The app annotation processor class qualified name
     */
    String JISEL_ANNOTATION_PROCESSOR_CLASSNAME = JiselAnnotationProcessor.class.getName();

    /**
     * Reads value of the provided parameter name from the provided properties file name. If not found, the provided defaultValue is returned
     *
     * @param fileNameWithExt Properties filename with file extension. ex: "application.properties"
     * @param property        Name of the parameter within the properties file
     * @param defaultValue    String value returned in case the provided property name is not found in the file
     * @return the property value as stored in the properties file
     */
    static String getPropertyValueFromPropsFile(String fileNameWithExt, String property, String defaultValue) {
        var properties = new Properties();
        var in = AppInfoGenerator.class.getClassLoader().getResourceAsStream(fileNameWithExt);
        try {
            properties.load(in);
        } catch (IOException e) {
            return defaultValue;
        }
        return properties.getProperty(property);
    }
}
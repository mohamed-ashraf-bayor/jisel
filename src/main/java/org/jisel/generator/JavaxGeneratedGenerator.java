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
package org.jisel.generator;

import org.jisel.JiselAnnotationProcessor;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Properties;

import static java.lang.String.format;

/**
 * Generates the @javax.annotation.processing.Generated annotation section at the top of the generated record class with the attributes: value, date and comments<br>
 * The generateRecord() method params map is not required
 */
public final class JavaxGeneratedGenerator implements CodeGenerator {

    private static final String DEFAULT_APP_VERSION = "1.0.0";

    private void buildGeneratedAnnotationSection(final StringBuilder recordClassContent) {
        recordClassContent.append(format("""
                        @javax.annotation.processing.Generated(
                            value = "%s",
                            date = "%s",
                            comments = "version: %s"
                        )
                        """,
                JiselAnnotationProcessor.class.getName(),
                ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                getAppVersion()
        ));
    }

    private String getAppVersion() {
        var properties = new Properties();
        var in = this.getClass().getClassLoader().getResourceAsStream("application.properties");
        try {
            properties.load(in);
        } catch (IOException e) {
            return DEFAULT_APP_VERSION;
        }
        return properties.getProperty("info.app.version");
    }

    @Override
    public void generateCode(final StringBuilder classOrInterfaceContent, final List<String> params) {
        buildGeneratedAnnotationSection(classOrInterfaceContent);
    }
}
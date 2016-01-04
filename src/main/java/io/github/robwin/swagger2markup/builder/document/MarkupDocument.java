/*
 *
 *  Copyright 2015 Robert Winkler
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package io.github.robwin.swagger2markup.builder.document;

import io.github.robwin.markup.builder.MarkupDocBuilder;
import io.github.robwin.markup.builder.MarkupDocBuilders;
import io.github.robwin.markup.builder.MarkupLanguage;
import io.github.robwin.swagger2markup.config.Swagger2MarkupConfig;
import io.swagger.models.Swagger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ResourceBundle;

/**
 * @author Robert Winkler
 */
public abstract class MarkupDocument {

    protected static final String DELIMITER = "|";
    protected final String DEFAULT_COLUMN;
    protected final String REQUIRED_COLUMN;
    protected final String SCHEMA_COLUMN;
    protected final String NAME_COLUMN;
    protected final String DESCRIPTION_COLUMN;
    protected final String DESCRIPTION;
    protected final String PRODUCES;
    protected final String CONSUMES;
    protected final String TAGS;
    protected Logger logger = LoggerFactory.getLogger(getClass());
    protected Swagger swagger;
    protected MarkupLanguage markupLanguage;
    protected MarkupDocBuilder markupDocBuilder;

    MarkupDocument(Swagger2MarkupConfig swagger2MarkupConfig){
        this.swagger = swagger2MarkupConfig.getSwagger();
        this.markupLanguage = swagger2MarkupConfig.getMarkupLanguage();
        this.markupDocBuilder = MarkupDocBuilders.documentBuilder(markupLanguage);

        ResourceBundle labels = ResourceBundle.getBundle("lang/labels",
                swagger2MarkupConfig.getOutputLanguage().toLocale());
        DEFAULT_COLUMN = labels.getString("default_column");
        REQUIRED_COLUMN = labels.getString("required_column");
        SCHEMA_COLUMN = labels.getString("schema_column");
        NAME_COLUMN = labels.getString("name_column");
        DESCRIPTION_COLUMN = labels.getString("description_column");
        DESCRIPTION = DESCRIPTION_COLUMN;
        PRODUCES = labels.getString("produces");
        CONSUMES = labels.getString("consumes");
        TAGS = labels.getString("tags");
    }

    /**
     * Builds the MarkupDocument.
     *
     * @return the built MarkupDocument
     * @throws IOException if the files to include are not readable
     */
    public abstract MarkupDocument build() throws IOException ;

    /**
     * Returns a string representation of the document.
     */
    public String toString(){
        return markupDocBuilder.toString();
    }

    /**
     * Writes the content of the builder to a file and clears the builder.
     *
     * @param directory the directory where the generated file should be stored
     * @param fileName the name of the file
     * @param charset the the charset to use for encoding
     * @throws IOException if the file cannot be written
     */
    public void writeToFile(String directory, String fileName, Charset charset) throws IOException{
        markupDocBuilder.writeToFile(directory, fileName, charset);
    }
}

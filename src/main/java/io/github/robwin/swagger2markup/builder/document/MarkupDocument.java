package io.github.robwin.swagger2markup.builder.document;

import com.wordnik.swagger.models.Swagger;
import io.github.robwin.swagger2markup.builder.markup.DocumentBuilder;
import io.github.robwin.swagger2markup.builder.markup.DocumentBuilders;
import io.github.robwin.swagger2markup.builder.markup.MarkupLanguage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Project:   swagger2markup
 * Copyright: Deutsche Telekom AG
 *
 * @author Robert Winkler <robert.winkler@telekom.de>
 * @since 2.0.0
 */
public abstract class MarkupDocument {

    protected static final String DELIMITER = ",";
    protected static final String REQUIRED_COLUMN = "Required";
    protected Logger logger = LoggerFactory.getLogger(getClass());
    protected Swagger swagger;
    protected MarkupLanguage markupLanguage;
    protected DocumentBuilder documentBuilder;

    MarkupDocument(Swagger swagger, MarkupLanguage markupLanguage){
        this.swagger = swagger;
        this.markupLanguage = markupLanguage;
        this.documentBuilder = DocumentBuilders.documentBuilder(markupLanguage);
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
        return documentBuilder.toString();
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
        documentBuilder.writeToFile(directory, fileName, charset);
    }
}

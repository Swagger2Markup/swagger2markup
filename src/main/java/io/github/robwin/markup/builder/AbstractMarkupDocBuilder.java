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
package io.github.robwin.markup.builder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * @author Robert Winkler
 */
public abstract class AbstractMarkupDocBuilder implements MarkupDocBuilder {
    
    protected StringBuilder documentBuilder = new StringBuilder();
    protected String newLine = System.getProperty("line.separator");
    protected Logger logger = LoggerFactory.getLogger(getClass());

    protected void documentTitle(Markup markup, String title){
        documentBuilder.append(markup).append(title).append(newLine).append(newLine);
    }

    protected void sectionTitleLevel1(Markup markup, String title){
        documentBuilder.append(newLine).append(markup).append(title).append(newLine);
    }

    protected void sectionTitleLevel2(Markup markup, String title){
        documentBuilder.append(newLine).append(markup).append(title).append(newLine);
    }

    protected void sectionTitleLevel3(Markup markup, String title){
        documentBuilder.append(newLine).append(markup).append(title).append(newLine);
    }

    protected void sectionTitleLevel4(Markup markup, String title){
        documentBuilder.append(newLine).append(markup).append(title).append(newLine);
    }

    @Override
    public MarkupDocBuilder textLine(String text){
        documentBuilder.append(text).append(newLine);
        return this;
    }

    protected void paragraph(Markup markup, String text){
        documentBuilder.append(markup).append(newLine).append(text).append(newLine).append(newLine);
    }

    protected void listing(Markup markup, String text){
        delimitedTextLine(markup, text);
    }

    protected void delimitedTextLine(Markup markup, String text){
        documentBuilder.append(markup).append(newLine).append(text).append(newLine).append(markup).append(newLine).append(newLine);
    }

    protected void delimitedTextLineWithoutLineBreaks(Markup markup, String text){
        documentBuilder.append(markup).append(text).append(markup).append(newLine);
    }

    protected void preserveLineBreaks(Markup markup){
        documentBuilder.append(markup).append(newLine);
    }

    protected void boldTextLine(Markup markup, String text){
        delimitedTextLineWithoutLineBreaks(markup, text);
    }

    protected void italicTextLine(Markup markup, String text){
        delimitedTextLineWithoutLineBreaks(markup, text);
    }

    protected void unorderedList(Markup markup, List<String> list){
        documentBuilder.append(newLine);
        for(String listEntry : list){
            unorderedListItem(markup, listEntry);
        }
        documentBuilder.append(newLine);
    }

    protected void unorderedListItem(Markup markup, String item) {
        documentBuilder.append(markup).append(item).append(newLine);
    }

    @Override
    public MarkupDocBuilder anchor(String anchor, String text) {
        documentBuilder.append(anchorAsString(anchor, text));
        return this;
    }

    @Override
    public MarkupDocBuilder anchor(String anchor) {
        return anchor(anchor, null);
    }

    @Override
    public MarkupDocBuilder crossReferenceAnchor(String document, String anchor, String text) {
        documentBuilder.append(crossReferenceAnchorAsString(document, anchor, text));
        return this;
    }

    @Override
    public MarkupDocBuilder crossReferenceAnchor(String anchor, String text) {
        return crossReferenceAnchor(null, anchor, text);
    }

    @Override
    public MarkupDocBuilder crossReferenceAnchor(String anchor) {
        return crossReferenceAnchor(null, anchor, null);
    }

    @Override
    public MarkupDocBuilder crossReference(String document, String title, String text) {
        documentBuilder.append(crossReferenceAsString(document, title, text));
        return this;
    }

    @Override
    public MarkupDocBuilder crossReference(String title, String text) {
        return crossReference(null, title, text);
    }

    @Override
    public MarkupDocBuilder crossReference(String title) {
        return crossReference(null, title, null);
    }

    @Override
    public MarkupDocBuilder newLine(){
        documentBuilder.append(newLine);
        return this;
    }

    @Override
    public MarkupDocBuilder table(List<List<String>> cells) {
        return tableWithColumnSpecs(null, cells);
    }

    @Override
    public String toString(){
        return documentBuilder.toString();
    }

    protected String addfileExtension(Markup markup, String fileName) {
        return fileName + "." + markup;
    }

    @Override
    public void writeToFileWithoutExtension(String directory, String fileName, Charset charset) throws IOException {
        Files.createDirectories(Paths.get(directory));
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(directory, fileName), charset)){
            writer.write(documentBuilder.toString());
        }
        if (logger.isInfoEnabled()) {
            logger.info("{} was written to: {}", fileName, directory);
        }
        documentBuilder = new StringBuilder();
    }

    @Override
    public void writeToFile(String directory, String fileName, Charset charset) throws IOException {
        writeToFileWithoutExtension(directory, addfileExtension(fileName), charset);
    }
}

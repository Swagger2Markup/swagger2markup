package io.swagger2markup.builder;

import java.util.List;

/**
 * @author Robert Winkler
 */
public abstract class AbstractDocumentBuilder implements DocumentBuilder {
    
    protected StringBuilder documentBuilder = new StringBuilder();
    protected String newLine = System.getProperty("line.separator");

    protected void documentTitle(Markup markup, String title){
        documentBuilder.append(markup).append(title).append(newLine);
    }

    protected void sectionTitleLevel1(Markup markup, String title){
        documentBuilder.append(markup).append(title).append(newLine);
    }

    protected void sectionTitleLevel2(Markup markup, String title){
        documentBuilder.append(markup).append(title).append(newLine);
    }

    protected void sectionTitleLevel3(Markup markup, String title){
        documentBuilder.append(markup).append(title).append(newLine);
    }

    public DocumentBuilder textLine(String text){
        documentBuilder.append(text).append(newLine);
        return this;
    }

    protected void paragraph(Markup markup, String text){
        documentBuilder.append(markup).append(newLine).append(text).append(newLine).append(newLine);
    }

    protected void listing(Markup markup, String text){
        delimitedTextLine(markup, text);
    }

    private void delimitedTextLine(Markup markup, String text){
        documentBuilder.append(markup).append(newLine).append(text).append(newLine).append(markup).append(newLine).append(newLine);
    }

    protected void preserveLineBreaks(Markup markup){
        documentBuilder.append(markup).append(newLine);
    }

    protected void boldTextLine(Markup markup, String text){
        delimitedTextLine(markup, text);
    }

    protected void italicTextLine(Markup markup, String text){
        delimitedTextLine(markup, text);
    }

    protected void unorderedList(Markup markup, List<String> list){
        for(String listEntry : list){
            documentBuilder.append(markup).append(listEntry).append(newLine);
        }
        documentBuilder.append(newLine);
    }

    public DocumentBuilder newLine(){
        documentBuilder.append(newLine);
        return this;
    }

    @Override
    public String toString(){
        return documentBuilder.toString();
    }
}

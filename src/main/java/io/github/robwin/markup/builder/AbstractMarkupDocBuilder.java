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

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.defaultString;

/**
 * @author Robert Winkler
 */
public abstract class AbstractMarkupDocBuilder implements MarkupDocBuilder {

    /**
     * Explicit line break default behavior for line returns, when not specified. Please, change documentation accordingly.
     */
    private static final boolean LINE_BREAK_DEFAULT = false;

    private static final Pattern ANCHOR_UNIGNORABLE_PATTERN = Pattern.compile("[^0-9a-zA-Z-_]+");
    private static final Pattern ANCHOR_IGNORABLE_PATTERN = Pattern.compile("[\\s@#&(){}\\[\\]!$*%+=/:.;,?\\\\<>|]+");
    private static final String ANCHOR_SEPARATION_CHARACTERS = "_-";
    private static final int MAX_TITLE_LEVEL = 4;

    protected StringBuilder documentBuilder = new StringBuilder();
    protected String newLine = System.getProperty("line.separator");
    protected Logger logger = LoggerFactory.getLogger(getClass());

    protected String anchorPrefix = null;

    @Override
    public MarkupDocBuilder withAnchorPrefix(String prefix) {
        this.anchorPrefix = prefix;
        return this;
    }

    @Override
    public String getAnchorPrefix() {
        return this.anchorPrefix;
    }

    protected void documentTitle(Markup markup, String title){
        documentBuilder.append(markup).append(title).append(newLine).append(newLine);
    }

    protected void sectionTitleLevel1(Markup markup, String title, String anchor){
        documentBuilder.append(newLine);
        if (anchor != null)
            anchor(anchor).newLine();
        documentBuilder.append(markup).append(title).append(newLine);
    }

    @Override
    public MarkupDocBuilder sectionTitleWithAnchorLevel1(String title) {
        return sectionTitleWithAnchorLevel1(title, title);
    }

    protected void sectionTitleLevel2(Markup markup, String title, String anchor){
        documentBuilder.append(newLine);
        if (anchor != null)
            anchor(anchor).newLine();
        documentBuilder.append(markup).append(title).append(newLine);
    }

    @Override
    public MarkupDocBuilder sectionTitleWithAnchorLevel2(String title) {
        return sectionTitleWithAnchorLevel2(title, title);
    }

    protected void sectionTitleLevel3(Markup markup, String title, String anchor){
        documentBuilder.append(newLine);
        if (anchor != null)
            anchor(anchor).newLine();
        documentBuilder.append(markup).append(title).append(newLine);
    }

    @Override
    public MarkupDocBuilder sectionTitleWithAnchorLevel3(String title) {
        return sectionTitleWithAnchorLevel3(title, title);
    }

    protected void sectionTitleLevel4(Markup markup, String title, String anchor){
        documentBuilder.append(newLine);
        if (anchor != null)
            anchor(anchor).newLine();
        documentBuilder.append(markup).append(title).append(newLine);
    }

    @Override
    public MarkupDocBuilder sectionTitleWithAnchorLevel4(String title) {
        return sectionTitleWithAnchorLevel4(title, title);
    }

    @Override
    public MarkupDocBuilder textLine(String text, boolean forceLineBreak){
        text(text);
        newLine(forceLineBreak);
        return this;
    }

    @Override
    public MarkupDocBuilder textLine(String text){
        textLine(text, LINE_BREAK_DEFAULT);
        return this;
    }

    @Override
    public MarkupDocBuilder text(String text){
        documentBuilder.append(text);
        return this;
    }

    protected void paragraph(Markup markup, String text){
        documentBuilder.append(markup).append(newLine).append(text).append(newLine).append(newLine);
    }

    protected void listing(Markup markup, String text){
        delimitedBlockText(markup, text);
    }

    protected void delimitedBlockText(Markup markup, String text){
        documentBuilder.append(markup).append(newLine).append(text).append(newLine).append(markup).append(newLine).append(newLine);
    }

    protected void delimitedTextWithoutLineBreaks(Markup markup, String text){
        documentBuilder.append(markup).append(text).append(markup);
    }

    protected void preserveLineBreaks(Markup markup){
        documentBuilder.append(markup).append(newLine);
    }

    protected void boldText(Markup markup, String text){
        delimitedTextWithoutLineBreaks(markup, text);
    }

    @Override
    public MarkupDocBuilder boldTextLine(String text, boolean forceLineBreak){
        boldText(text);
        newLine(forceLineBreak);
        return this;
    }

    @Override
    public MarkupDocBuilder boldTextLine(String text){
        return boldTextLine(text, LINE_BREAK_DEFAULT);
    }

    protected void italicText(Markup markup, String text){
        delimitedTextWithoutLineBreaks(markup, text);
    }

    @Override
    public MarkupDocBuilder italicTextLine(String text, boolean forceLineBreak) {
        italicText(text);
        newLine(forceLineBreak);
        return this;
    }

    @Override
    public MarkupDocBuilder italicTextLine(String text) {
        return italicTextLine(text, LINE_BREAK_DEFAULT);
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
    public MarkupDocBuilder anchor(String anchor) {
        return anchor(anchor, null);
    }

    /**
     * Generic normalization algorithm for all markups (less common denominator character set).
     * Key points :
     * - Anchor is normalized (Normalized.Form.NFD)
     * - Punctuations (excluding [-_]) and spaces are replaced with escape character (depends on markup : Markup.E)
     * - Beginning, ending separation characters [-_] are ignored, repeating separation characters are simplified (keep first one)
     * - Anchor is trimmed and lower cased
     * - If the anchor still contains forbidden characters (non-ASCII, ...), replace the whole anchor with an hash (MD5).
     * - Add the anchor prefix if configured
     */
    protected String normalizeAnchor(Markup spaceEscape, String anchor) {
        String normalizedAnchor = defaultString(anchorPrefix) + anchor.trim();
        normalizedAnchor = Normalizer.normalize(normalizedAnchor, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        normalizedAnchor = ANCHOR_IGNORABLE_PATTERN.matcher(normalizedAnchor).replaceAll(spaceEscape.toString());
        normalizedAnchor = normalizedAnchor.replaceAll(String.format("([%1$s])([%1$s]+)", ANCHOR_SEPARATION_CHARACTERS), "$1");
        normalizedAnchor = StringUtils.strip(normalizedAnchor, ANCHOR_SEPARATION_CHARACTERS);
        normalizedAnchor = normalizedAnchor.trim().toLowerCase();

        String validAnchor = ANCHOR_UNIGNORABLE_PATTERN.matcher(normalizedAnchor).replaceAll("");
        if (validAnchor.length() != normalizedAnchor.length())
            normalizedAnchor = DigestUtils.md5Hex(normalizedAnchor);
        else
            normalizedAnchor = validAnchor;

        return normalizedAnchor;
    }


    @Override
    public MarkupDocBuilder crossReferenceRaw(String anchor, String text) {
        return crossReferenceRaw(null, anchor, text);
    }

    @Override
    public MarkupDocBuilder crossReferenceRaw(String anchor) {
        return crossReferenceRaw(null, anchor, null);
    }

    @Override
    public MarkupDocBuilder crossReference(String anchor, String text) {
        return crossReference(null, anchor, text);
    }

    @Override
    public MarkupDocBuilder crossReference(String anchor) {
        return crossReference(null, anchor, null);
    }

    protected void newLine(Markup markup, boolean forceLineBreak){
        if (forceLineBreak)
            documentBuilder.append(markup);
        documentBuilder.append(newLine);
    }

    @Override
    public MarkupDocBuilder newLine(){
        newLine(LINE_BREAK_DEFAULT);
        return this;
    }

    @Override
    public MarkupDocBuilder importMarkup(String text) {
        return importMarkup(text, 0);
    }

    protected void importMarkup(Markup titlePrefix, String text, int levelOffset) {
        if (levelOffset > MAX_TITLE_LEVEL)
            throw new IllegalArgumentException(String.format("Specified levelOffset (%d) > max levelOffset (%d)", levelOffset, MAX_TITLE_LEVEL));
        if (levelOffset < -MAX_TITLE_LEVEL)
            throw new IllegalArgumentException(String.format("Specified levelOffset (%d) < min levelOffset (%d)", levelOffset, -MAX_TITLE_LEVEL));

        StringBuffer leveledText = new StringBuffer();
        Matcher sections = Pattern.compile(String.format("^(%s{1,%d})\\s+(.*)$", titlePrefix, MAX_TITLE_LEVEL + 1), Pattern.MULTILINE).matcher(text);

        while (sections.find()) {
            int titleLevel = sections.group(1).length() - 1;
            String title = sections.group(2);

            if (titleLevel + levelOffset > MAX_TITLE_LEVEL)
                throw new IllegalArgumentException(String.format("Specified levelOffset (%d) set title '%s' level (%d) > max title level (%d)", levelOffset, title, titleLevel, MAX_TITLE_LEVEL));
            if (titleLevel + levelOffset < 0)
                throw new IllegalArgumentException(String.format("Specified levelOffset (%d) set title '%s' level (%d) < 0", levelOffset, title, titleLevel));
            else
                sections.appendReplacement(leveledText, StringUtils.repeat(titlePrefix.toString(), 1 + titleLevel + levelOffset) + " " + title);
        }
        sections.appendTail(leveledText);

        documentBuilder.append(newLine);
        documentBuilder.append(leveledText.toString());
        documentBuilder.append(newLine);
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

    /**
     * 2 newLines are needed at the end of file for file to be included without protection.
     */
    @Override
    public void writeToFileWithoutExtension(String directory, String fileName, Charset charset) throws IOException {
        Files.createDirectories(Paths.get(directory));
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(directory, fileName), charset)) {
            writer.write(documentBuilder.toString());
            writer.write(newLine);
            writer.write(newLine);
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

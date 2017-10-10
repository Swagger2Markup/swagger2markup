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
package io.github.swagger2markup.markup.builder.internal;

import io.github.swagger2markup.markup.builder.MarkupBlockStyle;
import io.github.swagger2markup.markup.builder.MarkupDocBuilder;
import io.github.swagger2markup.markup.builder.MarkupLanguage;
import nl.jworks.markdown_to_asciidoc.Converter;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
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
    protected static final boolean LINE_BREAK_DEFAULT = false;

    protected static final Pattern ANCHOR_UNIGNORABLE_PATTERN = Pattern.compile("[^0-9a-zA-Z-_]+");
    protected static final Pattern ANCHOR_IGNORABLE_PATTERN = Pattern.compile("[\\s@#&(){}\\[\\]!$*%+=/:.;,?\\\\<>|]+");
    protected static final String ANCHOR_SEPARATION_CHARACTERS = "_-";
    protected static final int MAX_TITLE_LEVEL = 5;
    protected static final String NEW_LINES = "\\r\\n|\\r|\\n";
    protected static final String WHITESPACE = " ";

    protected StringBuilder documentBuilder = new StringBuilder();
    protected String newLine;
    protected Logger logger = LoggerFactory.getLogger(getClass());

    protected String anchorPrefix = null;

    public AbstractMarkupDocBuilder() {
        this(System.getProperty("line.separator"));
    }

    public AbstractMarkupDocBuilder(String newLine) {
        this.newLine = newLine;
    }

    protected abstract MarkupLanguage getMarkupLanguage();

    @Override
    public MarkupDocBuilder withAnchorPrefix(String prefix) {
        this.anchorPrefix = prefix;
        return this;
    }

    @Override
    public String getAnchorPrefix() {
        return this.anchorPrefix;
    }

    protected void documentTitle(Markup markup, String title) {
        Validate.notBlank(title, "title must not be blank");
        documentBuilder.append(markup).append(replaceNewLinesWithWhiteSpace(title)).append(newLine).append(newLine);
    }

    protected void sectionTitleLevel(Markup markup, int level, String title) {
        Validate.notBlank(title, "title must not be blank");
        Validate.inclusiveBetween(1, MAX_TITLE_LEVEL, level);
        documentBuilder.append(newLine);
        documentBuilder.append(StringUtils.repeat(markup.toString(), level + 1)).append(" ").append(replaceNewLinesWithWhiteSpace(title)).append(newLine);
    }

    protected void sectionTitleWithAnchorLevel(Markup markup, int level, String title, String anchor) {
        Validate.notBlank(title, "title must not be blank");
        Validate.inclusiveBetween(1, MAX_TITLE_LEVEL, level);
        documentBuilder.append(newLine);
        if (anchor == null)
            anchor = title;
        anchor(replaceNewLinesWithWhiteSpace(anchor)).newLine();
        documentBuilder.append(StringUtils.repeat(markup.toString(), level + 1)).append(" ").append(replaceNewLinesWithWhiteSpace(title)).append(newLine);
    }

    @Override
    public MarkupDocBuilder sectionTitleWithAnchorLevel(int level, String title) {
        return sectionTitleWithAnchorLevel(level, title, null);
    }

    @Override
    public MarkupDocBuilder sectionTitleLevel1(String title) {
        return sectionTitleLevel(1, title);
    }

    @Override
    public MarkupDocBuilder sectionTitleWithAnchorLevel1(String title, String anchor) {
        return sectionTitleWithAnchorLevel(1, title, anchor);
    }

    @Override
    public MarkupDocBuilder sectionTitleWithAnchorLevel1(String title) {
        return sectionTitleWithAnchorLevel1(title, null);
    }

    @Override
    public MarkupDocBuilder sectionTitleLevel2(String title) {
        return sectionTitleLevel(2, title);
    }

    @Override
    public MarkupDocBuilder sectionTitleWithAnchorLevel2(String title, String anchor) {
        return sectionTitleWithAnchorLevel(2, title, anchor);
    }

    @Override
    public MarkupDocBuilder sectionTitleWithAnchorLevel2(String title) {
        return sectionTitleWithAnchorLevel2(title, null);
    }

    @Override
    public MarkupDocBuilder sectionTitleLevel3(String title) {
        return sectionTitleLevel(3, title);
    }

    @Override
    public MarkupDocBuilder sectionTitleWithAnchorLevel3(String title, String anchor) {
        return sectionTitleWithAnchorLevel(3, title, anchor);
    }

    @Override
    public MarkupDocBuilder sectionTitleWithAnchorLevel3(String title) {
        return sectionTitleWithAnchorLevel3(title, null);
    }

    @Override
    public MarkupDocBuilder sectionTitleLevel4(String title) {
        return sectionTitleLevel(4, title);
    }

    @Override
    public MarkupDocBuilder sectionTitleWithAnchorLevel4(String title, String anchor) {
        return sectionTitleWithAnchorLevel(4, title, anchor);
    }

    @Override
    public MarkupDocBuilder sectionTitleWithAnchorLevel4(String title) {
        return sectionTitleWithAnchorLevel4(title, null);
    }

    @Override
    public MarkupDocBuilder sectionTitleLevel5(String title) {
        return sectionTitleLevel(5, title);
    }

    @Override
    public MarkupDocBuilder sectionTitleWithAnchorLevel5(String title, String anchor) {
        return sectionTitleWithAnchorLevel(5, title, anchor);
    }

    @Override
    public MarkupDocBuilder sectionTitleWithAnchorLevel5(String title) {
        return sectionTitleWithAnchorLevel5(title, null);
    }

    @Override
    public MarkupDocBuilder textLine(String text, boolean forceLineBreak) {
        Validate.notNull(text, "text must not be null");
        text(replaceNewLines(text));
        newLine(forceLineBreak);
        return this;
    }

    @Override
    public MarkupDocBuilder textLine(String text) {
        textLine(text, LINE_BREAK_DEFAULT);
        return this;
    }

    @Override
    public MarkupDocBuilder text(String text) {
        Validate.notNull(text, "text must not be null");
        documentBuilder.append(replaceNewLines(text));
        return this;
    }

    @Override
    public MarkupDocBuilder paragraph(String text) {
        return paragraph(text, false);
    }

    @Override
    public MarkupDocBuilder block(String text, MarkupBlockStyle style) {
        Validate.notBlank(text, "text must not be blank");
        return block(replaceNewLines(text), style, null, null);
    }

    @Override
    public MarkupDocBuilder listingBlock(String text) {
        Validate.notBlank(text, "text must not be blank");
        return listingBlock(replaceNewLines(text), null);
    }

    protected void delimitedBlockText(Markup begin, String text, Markup end, boolean skipLeadingNewline) {
        Validate.notBlank(text, "text must not be blank");
        if (!StringUtils.isBlank(begin.toString()))
            documentBuilder.append(begin);
        if (!skipLeadingNewline)
            documentBuilder.append(newLine);

        documentBuilder.append(replaceNewLines(text)).append(newLine);
        if (!StringUtils.isBlank(end.toString()))
            documentBuilder.append(end).append(newLine);
        documentBuilder.append(newLine);

    }

    protected void delimitedBlockText(Markup begin, String text, Markup end) {
        Validate.notBlank(text, "text must not be blank");
        if (!StringUtils.isBlank(begin.toString()))
            documentBuilder.append(begin).append(newLine);

        documentBuilder.append(replaceNewLines(text)).append(newLine);
        if (!StringUtils.isBlank(end.toString()))
            documentBuilder.append(end).append(newLine);

        documentBuilder.append(newLine);
    }

    protected void delimitedTextWithoutLineBreaks(Markup begin, String text, Markup end) {
        Validate.notBlank(text, "text must not be blank");
        if (!StringUtils.isBlank(begin.toString()))
            documentBuilder.append(begin);
        documentBuilder.append(replaceNewLines(text));
        if (!StringUtils.isBlank(end.toString()))
            documentBuilder.append(end);
    }

    protected void delimitedBlockText(Markup markup, String text) {
        delimitedBlockText(markup, text, markup);
    }

    protected void delimitedTextWithoutLineBreaks(Markup markup, String text) {
        delimitedTextWithoutLineBreaks(markup, text, markup);
    }

    protected void literalText(Markup markup, String text) {
        delimitedTextWithoutLineBreaks(markup, text);
    }

    @Override
    public MarkupDocBuilder literalTextLine(String text, boolean forceLineBreak) {
        Validate.notBlank(text, "text must not be blank");
        literalText(replaceNewLines(text));
        newLine(forceLineBreak);
        return this;
    }

    @Override
    public MarkupDocBuilder literalTextLine(String text) {
        return literalTextLine(text, LINE_BREAK_DEFAULT);
    }

    protected void boldText(Markup markup, String text) {
        delimitedTextWithoutLineBreaks(markup, text);
    }

    @Override
    public MarkupDocBuilder boldTextLine(String text, boolean forceLineBreak) {
        Validate.notBlank(text, "text must not be blank");
        boldText(replaceNewLines(text));
        newLine(forceLineBreak);
        return this;
    }

    @Override
    public MarkupDocBuilder boldTextLine(String text) {
        return boldTextLine(text, LINE_BREAK_DEFAULT);
    }

    protected void italicText(Markup markup, String text) {
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

    protected void unorderedList(Markup markup, List<String> list) {
        Validate.notEmpty(list, "list must not be empty");
        documentBuilder.append(newLine);
        for (String listEntry : list) {
            unorderedListItem(markup, listEntry);
        }
        documentBuilder.append(newLine);
    }

    protected void unorderedListItem(Markup markup, String item) {
        Validate.notBlank(item, "item must not be blank");
        documentBuilder.append(markup).append(item).append(newLine);
    }

    @Override
    public MarkupDocBuilder anchor(String anchor) {
        Validate.notBlank(anchor, "anchor must not be blank");
        return anchor(anchor, null);
    }

    /*
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

    protected void newLine(Markup markup, boolean forceLineBreak) {
        if (forceLineBreak)
            documentBuilder.append(markup);
        documentBuilder.append(newLine);
    }

    @Override
    public MarkupDocBuilder newLine() {
        newLine(LINE_BREAK_DEFAULT);
        return this;
    }

    @Override
    public MarkupDocBuilder importMarkup(Reader markupText, MarkupLanguage markupLanguage) {
        Validate.notNull(markupText, "markupText must not be null");
        Validate.notNull(markupLanguage, "markupLanguage must not be null");
        return importMarkup(markupText, markupLanguage, 0);
    }

    protected String convert(String markupText, MarkupLanguage markupLanguage) {
        if (markupLanguage == getMarkupLanguage())
            return markupText;
        else {
            if (markupLanguage == MarkupLanguage.MARKDOWN && getMarkupLanguage() == MarkupLanguage.ASCIIDOC) {
                return Converter.convertMarkdownToAsciiDoc(markupText) + newLine;
            } else {
                return markupText;
            }
        }
    }

    protected void importMarkupStyle1(Pattern titlePattern, Markup titlePrefix, Reader markupText, MarkupLanguage markupLanguage, int levelOffset) {
        Validate.isTrue(levelOffset <= MAX_TITLE_LEVEL, String.format("Specified levelOffset (%d) > max levelOffset (%d)", levelOffset, MAX_TITLE_LEVEL));
        Validate.isTrue(levelOffset >= -MAX_TITLE_LEVEL, String.format("Specified levelOffset (%d) < min levelOffset (%d)", levelOffset, -MAX_TITLE_LEVEL));

        StringBuffer leveledText = new StringBuffer();
        try (BufferedReader bufferedReader = new BufferedReader(markupText)) {
            String readLine;
            while ((readLine = bufferedReader.readLine()) != null) {
                Matcher titleMatcher = titlePattern.matcher(readLine);

                while (titleMatcher.find()) {
                    int titleLevel = titleMatcher.group(1).length() - 1;
                    String title = titleMatcher.group(2);

                    if (titleLevel + levelOffset > MAX_TITLE_LEVEL)
                        throw new IllegalArgumentException(String.format("Specified levelOffset (%d) set title '%s' level (%d) > max title level (%d)", levelOffset, title, titleLevel, MAX_TITLE_LEVEL));
                    if (titleLevel + levelOffset < 0)
                        throw new IllegalArgumentException(String.format("Specified levelOffset (%d) set title '%s' level (%d) < 0", levelOffset, title, titleLevel));
                    else
                        titleMatcher.appendReplacement(leveledText, Matcher.quoteReplacement(String.format("%s %s", StringUtils.repeat(titlePrefix.toString(), 1 + titleLevel + levelOffset), title)));
                }
                titleMatcher.appendTail(leveledText);
                leveledText.append(newLine);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to import Markup", e);
        }

        if (!StringUtils.isBlank(leveledText)) {
            documentBuilder.append(newLine);
            documentBuilder.append(convert(leveledText.toString(), markupLanguage));
            documentBuilder.append(newLine);
        }
    }

    protected void importMarkupStyle2(Pattern titlePattern, String titleFormat, boolean startFrom0, Reader markupText, MarkupLanguage markupLanguage, int levelOffset) {
        Validate.isTrue(levelOffset <= MAX_TITLE_LEVEL, String.format("Specified levelOffset (%d) > max levelOffset (%d)", levelOffset, MAX_TITLE_LEVEL));
        Validate.isTrue(levelOffset >= -MAX_TITLE_LEVEL, String.format("Specified levelOffset (%d) < min levelOffset (%d)", levelOffset, -MAX_TITLE_LEVEL));

        StringBuffer leveledText = new StringBuffer();
        try (BufferedReader bufferedReader = new BufferedReader(markupText)) {
            String readLine;
            while ((readLine = bufferedReader.readLine()) != null) {
                Matcher titleMatcher = titlePattern.matcher(readLine);

                while (titleMatcher.find()) {
                    int titleLevel = Integer.valueOf(titleMatcher.group(1)) - (startFrom0 ? 0 : 1);
                    String title = titleMatcher.group(2);

                    if (titleLevel + levelOffset > MAX_TITLE_LEVEL)
                        throw new IllegalArgumentException(String.format("Specified levelOffset (%d) set title '%s' level (%d) > max title level (%d)", levelOffset, title, titleLevel, MAX_TITLE_LEVEL));
                    if (titleLevel + levelOffset < 0)
                        throw new IllegalArgumentException(String.format("Specified levelOffset (%d) set title '%s' level (%d) < 0", levelOffset, title, titleLevel));
                    else
                        titleMatcher.appendReplacement(leveledText, Matcher.quoteReplacement(String.format(titleFormat, (startFrom0 ? 0 : 1) + titleLevel + levelOffset, title)));
                }
                titleMatcher.appendTail(leveledText);
                leveledText.append(newLine);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to import Markup", e);
        }

        if (!StringUtils.isBlank(leveledText)) {
            documentBuilder.append(newLine);
            documentBuilder.append(convert(leveledText.toString(), markupLanguage));
            documentBuilder.append(newLine);
        }
    }

    @Override
    public MarkupDocBuilder table(List<List<String>> cells) {
        Validate.notEmpty(cells, "cells must not be null");
        return tableWithColumnSpecs(null, cells);
    }

    @Override
    public String toString() {
        return documentBuilder.toString();
    }

    @Override
    public Path addFileExtension(Path file) {
        return file.resolveSibling(addFileExtension(file.getFileName().toString()));
    }

    /**
     * 2 newLines are needed at the end of file for file to be included without protection.
     */
    @Override
    public void writeToFileWithoutExtension(Path file, Charset charset, OpenOption... options) {
        try {
            Files.createDirectories(file.getParent());
        } catch (IOException e) {
            throw new RuntimeException("Failed create directory", e);
        }
        try (BufferedWriter writer = Files.newBufferedWriter(file, charset, options)) {
            writer.write(toString());
            writer.write(newLine);
            writer.write(newLine);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write file", e);
        }
        if (logger.isInfoEnabled()) {
            logger.info("Markup document written to: {}", file);
        }
    }

    public String replaceNewLines(String content, String replacement) {
        return content.replaceAll(NEW_LINES, Matcher.quoteReplacement(replacement));
    }

    public String replaceNewLines(String content) {
        return replaceNewLines(content, newLine);
    }

    public String replaceNewLinesWithWhiteSpace(String content) {
        return replaceNewLines(content, WHITESPACE);
    }

    @Override
    public void writeToFile(Path file, Charset charset, OpenOption... options) {
        writeToFileWithoutExtension(file.resolveSibling(addFileExtension(file.getFileName().toString())), charset, options);
    }
}

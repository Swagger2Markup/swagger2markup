package io.github.swagger2markup.adoc.converter;

import io.github.swagger2markup.adoc.converter.internal.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.asciidoctor.ast.List;
import org.asciidoctor.ast.*;
import org.asciidoctor.converter.ConverterFor;
import org.asciidoctor.converter.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.github.swagger2markup.adoc.converter.internal.Delimiters.*;

@ConverterFor(AsciidocConverter.NAME)
public class AsciidocConverter extends StringConverter {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public static final String NAME = "adoc";

    private final Pattern emptyLineOrStartWith = Pattern.compile("(?m)^\\s*(?:\\r?\\n)|(?m)^\\s+");
    private final Pattern coListItemIdPattern = Pattern.compile(".*-(\\d+)");
    private final Pattern tableColumnsStylePattern = Pattern.compile("((\\d+)\\*)?([<^>])?(\\.[<^>])?(\\d+)?([adehlmsv])?");

    private static final java.util.List<String> attributeToExclude = Arrays.asList(
            "localtime",
            "filetype",
            "asciidoctor-version",
            "doctime",
            "localyear",
            "docdate",
            "localdate",
            "localdatetime",
            "docdatetime",
            "backend",
            "basebackend",
            "doctitle",
            "docyear"
    );

    private static final String[] supportedUrlSchemes = new String[]{
            "http",
            "https",
            "ftp",
            "irc",
            "mailto"
    };

    public AsciidocConverter(String backend, Map<String, Object> opts) {
        super(backend, opts);
    }

    /**
     * Converts an {@link ContentNode} using the specified transform along
     * with additional options. If a transform is not specified, implementations
     * typically derive one from the {@link ContentNode#getNodeName()} property.
     *
     * <p>Implementations are free to decide how to carry out the conversion. In
     * the case of the built-in converters, the tranform value is used to
     * dispatch to a handler method. The TemplateConverter uses the value of
     * the transform to select a template to render.
     *
     * @param node      The concrete instance of FlowNode to convert
     * @param transform An optional String transform that hints at which transformation
     *                  should be applied to this node. If a transform is not specified,
     *                  the transform is typically derived from the value of the
     *                  node's node_name property. (optional, default: null)
     * @param opts      An optional map of options that provide additional hints about
     *                  how to convert the node. (optional, default: empty map)
     * @return the converted result
     */
    @Override
    public String convert(ContentNode node, String transform, Map<Object, Object> opts) {
        if (null == transform) {
            transform = node.getNodeName();
        }
        switch (transform) {
            case "inline_quoted":
                return convertInlineQuoted((PhraseNode) node);
            case "paragraph":
                return convertParagraph((StructuralNode) node);
            case "inline_anchor":
                return convertInlineAnchor((PhraseNode) node);
            case "section":
                return convertSection((Section) node);
            case "listing":
                return convertListing((Block) node);
            case "literal":
                return convertLiteral((StructuralNode) node);
            case "ulist":
                return convertUList((List) node);
            case "olist":
                return convertOList((List) node);
            case "dlist":
                return convertDescriptionList((DescriptionList) node);
            case "admonition":
                return convertAdmonition((Block) node);
            case "colist":
                return convertCoList((List) node);
            case "embedded":
            case "document":
                return convertEmbedded((Document) node);
            case "example":
                return convertExample((Block) node);
            case "floating_title":
                return convertFloatingTitle((StructuralNode) node);
            case "image":
                return convertImage((StructuralNode) node);
            case "inline_break":
                return convertInlineBreak(node);
            case "inline_button":
                return convertInlineButton(node);
            case "inline_callout":
                return convertInlineCallout(node);
            case "inline_footnote":
                return convertInlineFootnote(node);
            case "inline_image":
                return convertInlineImage((PhraseNode) node);
            case "inline_indexterm":
                return convertInlineIndexTerm(node);
            case "inline_kbd":
                return convertInlineKbd(node);
            case "inline_menu":
                return convertInlineMenu(node);
            case "open":
                return convertOpen((StructuralNode) node);
            case "page_break":
                return convertPageBreak(node);
            case "preamble":
                return convertPreamble((StructuralNode) node);
            case "quote":
                return convertQuote((StructuralNode) node);
            case "sidebar":
                return convertSidebar((StructuralNode) node);
            case "stem":
                return convertStem(node);
            case "table":
                return convertTable((Table) node);
            case "thematic_break":
                return convertThematicBreak(node);
            case "verse":
                return convertVerse((StructuralNode) node);
            case "video":
                return convertVideo(node);
            case "toc":
                return convertToc(node);
            case "pass":
                return convertPass(node);
            case "audio":
                return convertAudio(node);
            // didn't exist on html converter
            case "list":
                return convertList((List) node);
            case "list_item":
                return convertListItem((ListItem) node);
            case "descriptionListEntry":
                return convertDescriptionListEntry((DescriptionListEntry) node, true);
            default:
                logger.debug("Don't know how to convert: " + transform);
                return null;
        }
    }

    private String convertInlineAnchor(PhraseNode node) {
        logger.debug("convertInlineAnchor");
        String type = node.getType();
        switch (type) {
            case "xref": {
                String attrs;
                String text;
                String path = Optional.ofNullable(node.getAttributes().get("path")).orElse("").toString();
                if (StringUtils.isNotBlank(path)) {
                    ArrayList<String> list = new ArrayList<>();
                    if (StringUtils.isNotBlank(node.getRole())) {
                        list.add(" class=\"#{node.role}\"");
                    }
                    append_link_constraint_attrs(node, list);
                    attrs = String.join(" ", list);
                    text = StringUtils.isNotBlank(node.getText()) ? node.getText() : path;
                } else {
                    attrs = StringUtils.isNotBlank(node.getRole()) ? " class=\"" + node.getRole() + "\"" : "";
                    text = node.getText();
                    if (StringUtils.isNotBlank(text)) {
                        text = node.getAttributes().get("refid").toString();
                    }
                }
                return node.getTarget() + ATTRIBUTES_BEGIN + text + (StringUtils.isNotBlank(attrs) ? "," + attrs : "") + ATTRIBUTES_END;
            }
            case "ref":
                return node.getId();
            case "link": {
                ArrayList<String> attrs = new ArrayList<>();
                String target = node.getTarget();
                String includePrefix = !StringUtils.startsWithAny(target, supportedUrlSchemes) ? "include::" : "";

                String text = node.getText();
                if (!target.equals(text)) {
                    attrs.add(text);
                }
                if (StringUtils.isNotBlank(node.getId())) {
                    attrs.add("id=\"" + node.getId() + "\"");
                }
                String role = node.getRole();
                if (StringUtils.isNotBlank(role) && !role.equals("bare")) {
                    attrs.add("role=\"" + role + "\"");
                }
                String title = node.getAttribute("title", "").toString();
                if (StringUtils.isNotBlank(title)) {
                    attrs.add("title=\"" + title + "\"");
                }
                return includePrefix + target + ATTRIBUTES_BEGIN + String.join(",", attrs) + ATTRIBUTES_END;
            }
            case "bibref":
                return node.getId() + ATTRIBUTES_BEGIN + (StringUtils.isNotBlank(node.getReftext()) ? node.getReftext() : node.getId()) + ATTRIBUTES_END;
            default:
                logger.warn("unknown anchor type: " + node.getType());
                return null;
        }
    }

    private String convertAdmonition(Block node) {
        logger.debug("convertAdmonition");
        StringBuilder sb = new StringBuilder();

        java.util.List<StructuralNode> blocks = node.getBlocks();
        if (blocks.isEmpty()) {
            sb.append(node.getStyle()).append(": ").append(node.getSource());
        } else {
            appendTitle(node, sb);
            sb.append(ATTRIBUTES_BEGIN).append(node.getStyle()).append(ATTRIBUTES_END)
                    .append(LINE_SEPARATOR).append(DELIMITER_EXAMPLE).append(LINE_SEPARATOR);
            appendChildBlocks(node, sb);
            sb.append(DELIMITER_EXAMPLE).append(LINE_SEPARATOR);
        }
        return sb.toString();
    }

    private String convertInlineQuoted(PhraseNode node) {
        logger.debug("convertInlineQuoted");
        StringBuilder sb = new StringBuilder();
        String marker = "";
        switch (node.getType()) {
            case "monospaced":
                marker = "`";
                break;
            case "emphasis":
                marker = "_";
                break;
            case "strong":
                marker = "*";
                break;
            case "superscript":
                marker = "^";
                break;
            case "subscript":
                marker = "~";
                break;
            case "double":
            case "single":
            case "mark":
            case "asciimath":
            case "latexmath":
                marker = "";
                break;
        }
        sb.append(marker).append(node.getText()).append(marker);
        return sb.toString();
    }

    private String convertFloatingTitle(StructuralNode node) {
        logger.debug("convertFloatingTitle");
        return ATTRIBUTES_BEGIN + "discrete" + ATTRIBUTES_END + LINE_SEPARATOR +
                repeat(node.getLevel() + 1, TITLE) + ' ' + node.getTitle() + LINE_SEPARATOR;
    }

    private String convertExample(Block node) {
        logger.debug("convertExample");
        StringBuilder sb = new StringBuilder();
        appendTitle(node, sb);
        sb.append(DELIMITER_EXAMPLE).append(LINE_SEPARATOR);
        appendChildBlocks(node, sb);
        sb.append(DELIMITER_EXAMPLE).append(LINE_SEPARATOR);
        return sb.toString();
    }

    private String convertInlineButton(ContentNode node) {
        logger.debug("convertInlineButton: name" + node.getNodeName());
        return "convertInlineButton";
    }

    private String convertInlineCallout(ContentNode node) {
        logger.debug("convertInlineCallout: name" + node.getNodeName());
        return "convertInlineCallout";
    }

    private String convertInlineBreak(ContentNode node) {
        logger.debug("convertInlineBreak: name" + node.getNodeName());
        return "convertInlineBreak";
    }

    private String convertInlineFootnote(ContentNode node) {
        logger.debug("convertInlineFootnote: name" + node.getNodeName());
        return "convertInlineFootnote";
    }

    private String convertInlineImage(PhraseNode node) {
        logger.debug("convertInlineImage");
        if(node.getType().equals("icon")){
            return (new IconNode(node)).toAsciiDocContent();
        } else {
            return (new BlockImageNode(node)).toAsciiDocContent();
        }
    }

    private String convertInlineIndexTerm(ContentNode node) {
        logger.debug("convertInlineIndexTerm: name" + node.getNodeName());
        return "convertInlineIndexTerm";
    }

    private String convertInlineKbd(ContentNode node) {
        logger.debug("convertInlineKbd: name" + node.getNodeName());
        return "convertInlineKbd";
    }

    private String convertInlineMenu(ContentNode node) {
        logger.debug("convertInlineMenu: name" + node.getNodeName());
        return "convertInlineMenu";
    }

    private String convertOpen(StructuralNode node) {
        logger.debug("convertOpen");
        StringBuilder sb = new StringBuilder();

        switch (node.getStyle()) {
            case "abstract":
                sb.append(ATTRIBUTES_BEGIN).append("abstract").append(ATTRIBUTES_END).append(LINE_SEPARATOR);
                break;
            case "open":
                sb.append(DELIMITER_OPEN_BLOCK).append(LINE_SEPARATOR);
        }
        sb.append(Optional.ofNullable(((Block) node).getSource()).orElse(""));
        appendChildBlocks(node, sb);

        if ("open".equals(node.getStyle())) {
            sb.append(DELIMITER_OPEN_BLOCK).append(LINE_SEPARATOR);
        }
        return sb.toString();
    }

    private String convertPageBreak(ContentNode node) {
        logger.debug("convertPageBreak: name" + node.getNodeName());
        return DELIMITER_PAGE_BREAK + LINE_SEPARATOR;
    }

    private String convertQuote(StructuralNode node) {
        logger.debug("convertQuote");
        StringBuilder sb = new StringBuilder();
        appendTitle(node, sb);
        sb.append(ATTRIBUTES_BEGIN);
        java.util.List<String> attrs = new ArrayList<>();
        if (StringUtils.isNotBlank(node.getStyle())) {
            attrs.add("quote");
        }
        appendAttributeTo(node, attrs, "attribution");
        appendAttributeTo(node, attrs, "citetitle");
        sb.append(String.join(",", attrs)).append(ATTRIBUTES_END).append(LINE_SEPARATOR);
        java.util.List<StructuralNode> blocks = node.getBlocks();
        if (!blocks.isEmpty()) {
            sb.append("____").append(LINE_SEPARATOR);
            appendChildBlocks(node, sb);
            sb.append("____").append(LINE_SEPARATOR);
        } else {
            sb.append(((Block) node).getSource());
        }

        return sb.toString();
    }

    private String convertSidebar(StructuralNode node) {
        logger.debug("convertSidebar");
        StringBuilder sb = new StringBuilder();
        appendTitle(node, sb);
        appendChildBlocks(node, sb);
        return sb.toString();
    }

    private String convertStem(ContentNode node) {
        logger.debug("convertStem: name" + node.getNodeName());
        return "convertStem";
    }

    private String convertThematicBreak(ContentNode node) {
        logger.debug("convertThematicBreak: name" + node.getNodeName());
        return DELIMITER_THEMATIC_BREAK + LINE_SEPARATOR;
    }

    private String convertVerse(StructuralNode node) {
        logger.debug("convertVerse");
        StringBuilder sb = new StringBuilder();
        appendTitle(node, sb);
        sb.append(ATTRIBUTES_BEGIN);
        java.util.List<String> attrs = new ArrayList<>();
        if (StringUtils.isNotBlank(node.getStyle())) {
            attrs.add("verse");
        }
        appendAttributeTo(node, attrs, "attribution");
        appendAttributeTo(node, attrs, "citetitle");
        sb.append(String.join(",", attrs)).append(ATTRIBUTES_END).append(LINE_SEPARATOR);
        String source = ((Block) node).getSource();
        boolean matches = emptyLineOrStartWith.matcher(source).find();
        if (matches) {
            sb.append(DELIMITER_VERSE).append(LINE_SEPARATOR);
        }
        sb.append(source);
        if (matches) {
            sb.append(LINE_SEPARATOR).append(DELIMITER_VERSE);
        }
        sb.append(LINE_SEPARATOR);
        return sb.toString();
    }

    private String convertVideo(ContentNode node) {
        logger.debug("convertVideo: name" + node.getNodeName());
        return "convertVideo";
    }

    private String convertToc(ContentNode node) {
        logger.debug("convertToc: name" + node.getNodeName());
        return "convertToc";
    }

    private String convertPass(ContentNode node) {
        logger.debug("convertPass: name" + node.getNodeName());
        return "convertPass";
    }

    private String convertAudio(ContentNode node) {
        logger.debug("convertAudio: name" + node.getNodeName());
        return "convertAudio";
    }

    private String convertCell(Cell node) {
        logger.debug("convertCell");
        return node.getSource();
    }

    private String convertRow(Row node, java.util.List<TableCellStyle> columnStyles) {
        logger.debug("convertRow");
        StringBuilder sb = new StringBuilder();
        node.getCells().forEach(cell -> {
            boolean addNewLine = false;
            int colspan = cell.getColspan();
            if (colspan != 0) {
                addNewLine = true;
                sb.append(colspan).append('+');
            }
            int rowspan = cell.getRowspan();
            if (rowspan != 0) {
                addNewLine = true;
                sb.append('.').append(rowspan).append('+');
            }
            int index = cell.getColumn().getColumnNumber() - 1;
            TableCellStyle tableCellStyle = (columnStyles.size() > index) ? columnStyles.get(index) : null;

            boolean hAlignmentAdded = false;
            TableCellHorizontalAlignment hAlignment = TableCellHorizontalAlignment.fromName(cell.getHorizontalAlignment().name());
            if ((null != hAlignment) && (null == tableCellStyle || hAlignment != tableCellStyle.horizontalAlignment)) {
                hAlignmentAdded  = true;
                addNewLine = true;
                sb.append(hAlignment.getDelimiter());
            }

            TableCellVerticalAlignment vAlignment = TableCellVerticalAlignment.fromName(cell.getVerticalAlignment().name());
            if ((null != vAlignment) && (null == tableCellStyle || hAlignmentAdded || vAlignment != tableCellStyle.verticalAlignment)) {
                addNewLine = true;
                sb.append(vAlignment.getDelimiter());
            }

            Style style = Style.fromName(cell.getAttribute("style", "").toString());
            if (null != style && (null == tableCellStyle || style != tableCellStyle.style)) {
                addNewLine = true;
                sb.append(style.getShortHand());
            }
            sb.append(DELIMITER_CELL).append(convertCell(cell));
            if (addNewLine) {
                sb.append(LINE_SEPARATOR);
            } else {
                sb.append(' ');
            }
        });
        return sb.toString();
    }

    private String convertTable(Table node) {
        logger.debug("convertTable");
        java.util.List<TableCellStyle> columnStyles = new ArrayList<>();
        for (String col : node.getAttribute("cols", "").toString().split(",")) {
            Matcher matcher = tableColumnsStylePattern.matcher(col);
            if (matcher.find()) {
                int multiplier = 1;
                String multiplierGroup = matcher.group(2);
                if (null != multiplierGroup) {
                    try {
                        multiplier = Integer.parseInt(multiplierGroup);
                    } catch (NumberFormatException ignored) {
                    }
                }
                int width = 0;
                try {
                    width = Integer.parseInt(matcher.group(5));
                } catch (NumberFormatException ignored) {
                }
                TableCellStyle tableCellStyle = new TableCellStyle(
                        TableCellHorizontalAlignment.fromString(matcher.group(3)),
                        TableCellVerticalAlignment.fromString(matcher.group(4)),
                        Style.fromString(matcher.group(6)),
                        width
                );
                for (int i = 0; i < multiplier; i++) {
                    columnStyles.add(tableCellStyle);
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        appendTitle(node, sb);
        sb.append(new TableNode(node).toAsciiDocContent());
        sb.append(DELIMITER_TABLE).append(LINE_SEPARATOR);
        appendRows(node.getHeader(), sb, columnStyles);
        appendRows(node.getBody(), sb, columnStyles);
        appendRows(node.getFooter(), sb, columnStyles);
        sb.append(DELIMITER_TABLE).append(LINE_SEPARATOR);
        return sb.toString();
    }

    private void appendRows(java.util.List<Row> rows, StringBuilder sb, java.util.List<TableCellStyle> columnStyles) {
        rows.forEach(row -> sb.append(convertRow(row, columnStyles)).append(LINE_SEPARATOR));
    }

    private String convertDescriptionList(DescriptionList node) {
        logger.debug("convertDescriptionList");
        StringBuilder result = new StringBuilder();

        appendTitle(node, result);
        String style = Optional.ofNullable(node.getStyle()).orElse("");
        switch (style) {
            case STYLE_HORIZONTAL:
                result.append(ATTRIBUTES_BEGIN).append(STYLE_HORIZONTAL).append(ATTRIBUTES_END).append(LINE_SEPARATOR);
                node.getItems().forEach(item -> result.append(convertDescriptionListEntry(item, false)).append(LINE_SEPARATOR));
                break;
            case STYLE_Q_AND_A:
                result.append(ATTRIBUTES_BEGIN).append(STYLE_Q_AND_A).append(ATTRIBUTES_END).append(LINE_SEPARATOR);
            default:
                node.getItems().forEach(item -> result.append(convertDescriptionListEntry(item, true)).append(LINE_SEPARATOR));
                break;
        }
        result.append(LINE_SEPARATOR);

        return result.toString();
    }

    private String convertDescriptionListEntry(DescriptionListEntry node, Boolean descriptionOnNewLine) {
        logger.debug("convertDescriptionListEntry");
        StringBuilder result = new StringBuilder();
        node.getTerms().forEach(term -> result.append(Optional.ofNullable(term.getSource()).orElse("")).append(MARKER_D_LIST_ITEM).append(LINE_SEPARATOR));
        ListItem description = node.getDescription();
        if (null != description) {
            if (descriptionOnNewLine) {
                result.append(LINE_SEPARATOR);
            }
            result.append(Optional.ofNullable(description.getSource()).orElse(""));
            appendChildBlocks(description, result);
        }
        return result.toString();
    }

    private String convertListing(Block node) {
        logger.debug("convertListing");
        StringBuilder sb = new StringBuilder();
        appendTitle(node, sb);
        if (STYLE_SOURCE.equals(node.getStyle())) {
            sb.append(new SourceNode(node).toAsciiDocContent());
        } else {
            sb.append(new BlockListingNode(node).toAsciiDocContent());
        }
        return sb.toString();
    }

    private String convertUList(List node) {
        logger.debug("convertUList");
        StringBuilder result = new StringBuilder();
        appendStyle(node, result);
        appendTitle(node, result);
        appendChildBlocks(node, result);
        result.append(LINE_SEPARATOR);
        return result.toString();
    }

    private String convertOList(List node) {
        logger.debug("convertOList");
        StringBuilder result = new StringBuilder();
        java.util.List<String> attrs = new ArrayList<>();
        String start = node.getAttribute("start", "").toString();
        if (StringUtils.isNotBlank(start)) {
            attrs.add("start=" + start);
        }
        if (node.isOption("reversed")) {
            attrs.add("%reversed");
        }
        if (!attrs.isEmpty()) {
            result.append(ATTRIBUTES_BEGIN).append(String.join(",", attrs)).append(ATTRIBUTES_END).append(LINE_SEPARATOR);
        }
        appendTitle(node, result);
        appendChildBlocks(node, result);
        result.append(LINE_SEPARATOR);
        return result.toString();
    }

    private String convertCoList(List node) {
        logger.debug("convertCoList");
        StringBuilder result = new StringBuilder();
        appendChildBlocks(node, result);
        return result.toString();
    }

    private String convertListItem(ListItem node) {
        logger.debug("convertListItem");
        StringBuilder result = new StringBuilder();

        String marker = Optional.ofNullable(node.getMarker()).orElse(repeat(node.getLevel(), MARKER_LIST_ITEM));

        String coids = node.getAttribute("coids", "").toString();
        Matcher matcher = coListItemIdPattern.matcher(coids);
        if (matcher.find()) {
            marker = marker.replaceAll("\\d+", matcher.group(1));
        }

        result.append(marker).append(" ");

        if (node.hasAttribute("checkbox")) {
            result.append('[');
            if (node.hasAttribute("checked")) {
                result.append('x');
            } else {
                result.append(' ');
            }
            result.append(']').append(' ');
        }

        result.append(Optional.ofNullable(node.getSource()).orElse(""));
        result.append(LINE_SEPARATOR);
        appendChildBlocks(node, result);
        return result.toString();
    }

    private String convertList(List node) {
        logger.debug("convertList");
        return node.getContent().toString();
    }

    private String convertPreamble(StructuralNode node) {
        logger.debug("convertPreamble");
        StringBuilder sb = new StringBuilder();
//        appendTitle(node, sb);
//        appendId(node, sb);
//        appendStyle(node, sb);
//        appendRoles(node, sb);
//        appendSource((Block) node, sb);
        sb.append(new ParagraphAttributes(node).toAsciiDocContent());
        appendChildBlocks(node, sb);
        return sb.toString();
    }

    private String convertImage(StructuralNode node) {
        logger.debug("convertImage");
        StringBuilder sb = new StringBuilder();
        appendTitle(node, sb);
        appendRoles(node, sb);
        sb.append(new BlockImageNode(node).toAsciiDocContent());
        return sb.toString();
    }

    private String convertLiteral(StructuralNode node) {
        logger.debug("convertLiteral");
        return ATTRIBUTES_BEGIN + node.getContext() + ATTRIBUTES_END + LINE_SEPARATOR +
                unescapeContent(node.getContent().toString()) + LINE_SEPARATOR;
    }

    private String convertParagraph(StructuralNode node) {
        logger.debug("convertParagraph");
        return new ParagraphAttributes(node).toAsciiDocContent() +
                ((Block) node).getSource() + LINE_SEPARATOR;
    }

    private String convertSection(Section node) {
        logger.debug("convertSection");
        StringBuilder sb = new StringBuilder();
        sb.append(new DelimitedBlockNode(node).toAsciiDocContent()).append(StringUtils.repeat(TITLE, node.getLevel() + 1))
                .append(" ").append(unescapeContent(node.getTitle())).append(LINE_SEPARATOR).append(LINE_SEPARATOR);
        appendChildBlocks(node, sb);

        return sb.toString();
    }

    private String convertEmbedded(Document node) {
        logger.debug("convertEmbedded");
        StringBuilder sb = new StringBuilder();

        sb.append(DOCUMENT_TITLE).append(unescapeContent(node.getDoctitle()));
        Map<String, Object> attributes = node.getAttributes();
        String authors = attributes.getOrDefault("authors", "").toString();
        if (!authors.isEmpty()) {
            sb.append(LINE_SEPARATOR).append(authors).append(LINE_SEPARATOR);
        }

        String revDetails = Stream.of(attributes.get("revnumber"), attributes.get("revdate")).filter(Objects::nonNull)
                .filter(o -> !o.toString().isEmpty()).map(Object::toString)
                .collect(Collectors.joining(", "));

        if (!revDetails.isEmpty()) {
            sb.append("v").append(revDetails).append(LINE_SEPARATOR);
        }

        attributes.forEach((k, v) -> {
            if (!attributeToExclude.contains(k) && v != null && !v.toString().isEmpty())
                sb.append(LINE_SEPARATOR).append(COLON).append(k).append(COLON).append(" ").append(v);
        });
        String content = node.getContent().toString();
        if (StringUtils.isNotBlank(content)) {
            sb.append(LINE_SEPARATOR).append(LINE_SEPARATOR).append(content);
        }
        return sb.toString();
    }

    private String unescapeContent(String content) {
        return StringEscapeUtils.unescapeHtml4(content);
    }


    private void append_link_constraint_attrs(ContentNode node, java.util.List<String> attrs) {
        String rel = node.getAttribute("nofollow-option").toString();
        String window = node.getAttributes().get("window").toString();
        if (StringUtils.isNotBlank(window)) {
            attrs.add("target = \"#{window}\"");
            if (window.equals("_blank") || (node.getAttributes().containsKey("option-noopener"))) {
                if (StringUtils.isNotBlank(rel)) {
                    attrs.add("rel = \"" + rel + "noopener\"");
                } else {
                    attrs.add(" rel=\"noopener\"");
                }
            }
        } else if (StringUtils.isNotBlank(rel)) {
            attrs.add("rel = " + rel + "\"");
        }
    }

    private String repeat(int count, String with) {
        return new String(new char[count]).replace("\0", with);
    }

    private void appendChildBlocks(StructuralNode node, StringBuilder sb) {
        node.getBlocks().forEach(block -> sb.append(block.convert()).append(LINE_SEPARATOR));
    }

    private void appendTitle(StructuralNode node, StringBuilder sb) {
        String title = node.getTitle();
        if (StringUtils.isNotBlank(title)) {
            sb.append(".").append(unescapeContent(title)).append(LINE_SEPARATOR);
        }
    }

    private void appendSource(Block node, StringBuilder sb) {
        String source = node.getSource();
        if (StringUtils.isNotBlank(source)) {
            sb.append(unescapeContent(source)).append(LINE_SEPARATOR);
        }
    }

    private void appendStyle(StructuralNode node, StringBuilder sb) {
        String style = node.getStyle();
        if (StringUtils.isNotBlank(style)) {
            sb.append(ATTRIBUTES_BEGIN).append(style).append(ATTRIBUTES_END).append(LINE_SEPARATOR);
        }
    }

    private void appendRoles(StructuralNode node, StringBuilder sb) {
        java.util.List<String> roles = node.getRoles();
        if (!roles.isEmpty()) {
            sb.append(ATTRIBUTES_BEGIN).append(".").append(String.join(".", roles))
                    .append(ATTRIBUTES_END).append(LINE_SEPARATOR);
        }
    }

    private void appendAttributeTo(StructuralNode node, java.util.List<String> attrs, String name) {
        String attribution = node.getAttribute(name, "").toString();
        if (StringUtils.isNotBlank(attribution)) {
            attrs.add(attribution);
        }
    }
}

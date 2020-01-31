package io.github.swagger2markup.internal.helper;

import io.github.swagger2markup.adoc.ast.impl.DocumentImpl;
import io.github.swagger2markup.adoc.ast.impl.ParagraphBlockImpl;
import org.apache.commons.lang3.StringUtils;
import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.ast.Table;

public class OpenApiHelpers {

    public static void appendDescription(StructuralNode node, String description) {
        if (StringUtils.isNotBlank(description)) {
            Block paragraph = new ParagraphBlockImpl(node);
            paragraph.setSource(description);
            node.append(paragraph);
        }
    }

    public static Document generateInnerDoc(Table table, String documentContent) {
        return generateInnerDoc(table, documentContent, "");
    }

    public static Document generateInnerDoc(Table table, String documentContent, String id) {
        Document innerDoc = new DocumentImpl(table);
        if (StringUtils.isNotBlank(id)) {
            innerDoc.setId(id);
        }

        Block paragraph = new ParagraphBlockImpl(innerDoc);
        paragraph.setSource(documentContent);
        innerDoc.append(paragraph);
        return innerDoc;
    }

    public static String requiredIndicator(boolean isRequired, String labelRequired, String labelOptional) {
        return italicUnconstrained(isRequired ? labelRequired : labelOptional).toLowerCase();
    }

    public static String superScript(String str) {
        return "^" + str + "^";
    }

    public static String subScript(String str) {
        return "~" + str + "~";
    }

    public static String italicUnconstrained(String str) {
        return "__" + str + "__";
    }

    public static String boldUnconstrained(String str) {
        return "**" + str + "**";
    }

    public static String monospaced(String str) {
        return "`" + str + "`";
    }
}

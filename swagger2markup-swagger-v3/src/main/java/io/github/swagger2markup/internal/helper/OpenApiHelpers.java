package io.github.swagger2markup.internal.helper;

import io.github.swagger2markup.adoc.ast.impl.DocumentImpl;
import io.github.swagger2markup.adoc.ast.impl.ParagraphBlockImpl;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import org.apache.commons.lang3.StringUtils;
import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.ast.Table;

import java.util.List;

import static io.github.swagger2markup.adoc.converter.internal.Delimiters.LINE_SEPARATOR;

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

    public static String getSchemaTypeAsString(Schema schema) {
        StringBuilder stringBuilder = new StringBuilder();
        if (schema instanceof ArraySchema) {
            stringBuilder.append("< ");
            Schema<?> items = ((ArraySchema) schema).getItems();
            stringBuilder.append(getSchemaType(items));
            stringBuilder.append(" > ");
            stringBuilder.append(schema.getType());
        } else {
            List enumList = schema.getEnum();
            if (enumList != null) {
                stringBuilder.append("enum (");
                for (Object value : enumList) {
                    stringBuilder.append(value.toString());
                    stringBuilder.append(",");
                }
                stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                stringBuilder.append(')');
            } else {
                stringBuilder.append(getSchemaType(schema));
                String format = schema.getFormat();
                if (format != null) {
                    stringBuilder.append(' ');
                    stringBuilder.append('(');
                    stringBuilder.append(format);
                    stringBuilder.append(')');
                }
            }
        }
        return stringBuilder.toString();
    }

    private static String getSchemaType(Schema<?> schema) {
        String type = schema.getType();
        if (StringUtils.isNotEmpty(type)) {
            return type;
        } else {
            return generateRefLink(schema.get$ref());
        }
    }

    private static String generateRefLink(String ref) {
        if (StringUtils.isNotBlank(ref)) {
            String anchor = ref.toLowerCase().replaceFirst("#", "").replaceAll("/", "_");
            return "<<" + anchor + ">>" + LINE_SEPARATOR;
        }
        return "";
    }
}

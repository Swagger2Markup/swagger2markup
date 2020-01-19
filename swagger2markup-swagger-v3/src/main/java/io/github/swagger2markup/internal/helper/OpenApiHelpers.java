package io.github.swagger2markup.internal.helper;

import io.github.swagger2markup.adoc.ast.impl.DocumentImpl;
import io.github.swagger2markup.adoc.ast.impl.ParagraphBlockImpl;
import org.apache.commons.lang3.StringUtils;
import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.ast.Table;

public class OpenApiHelpers {

    public static final String LABEL_DEFAULT = "Default";
    public static final String LABEL_DEPRECATED = "Deprecated";
    public static final String LABEL_EXCLUSIVE_MAXIMUM = "Exclusive Maximum";
    public static final String LABEL_EXCLUSIVE_MINIMUM = "Exclusive Minimum";
    public static final String LABEL_FORMAT = "Format";
    public static final String LABEL_MAXIMUM = "Maximum";
    public static final String LABEL_MAX_ITEMS = "Maximum Items";
    public static final String LABEL_MAX_LENGTH = "Maximum Length";
    public static final String LABEL_MAX_PROPERTIES = "Maximum Properties";
    public static final String LABEL_MINIMUM = "Minimum";
    public static final String LABEL_MIN_ITEMS = "Minimum Items";
    public static final String LABEL_MIN_LENGTH = "Minimum Length";
    public static final String LABEL_MIN_PROPERTIES = "Minimum Properties";
    public static final String LABEL_MULTIPLE_OF = "Multiple Of";
    public static final String LABEL_NO_LINKS = "No Links";
    public static final String LABEL_NULLABLE = "Nullable";
    public static final String LABEL_OPERATION = "Operation";
    public static final String LABEL_OPTIONAL = "Optional";
    public static final String LABEL_PARAMETERS = "Parameters";
    public static final String LABEL_READ_ONLY = "Read Only";
    public static final String LABEL_REQUIRED = "Required";
    public static final String LABEL_SERVER = "Server";
    public static final String LABEL_TERMS_OF_SERVICE = "Terms Of Service";
    public static final String LABEL_TITLE = "Title";
    public static final String LABEL_TYPE = "Type";
    public static final String LABEL_UNIQUE_ITEMS = "Unique Items";
    public static final String LABEL_WRITE_ONLY = "Write Only";
    public static final String SECTION_TITLE_COMPONENTS = "Components";
    public static final String SECTION_TITLE_PARAMETERS = "Parameters";
    public static final String SECTION_TITLE_PATHS = "Paths";
    public static final String SECTION_TITLE_SCHEMAS = "Schemas";
    public static final String SECTION_TITLE_SECURITY = "Security";
    public static final String SECTION_TITLE_SERVERS = "Servers";
    public static final String SECTION_TITLE_OVERVIEW = "Overview";
    public static final String SECTION_TITLE_TAGS = "Tags";
    public static final String TABLE_HEADER_DEFAULT = "Default";
    public static final String TABLE_HEADER_DESCRIPTION = "Description";
    public static final String TABLE_HEADER_HTTP_CODE = "Code";
    public static final String TABLE_HEADER_LINKS = "Links";
    public static final String TABLE_HEADER_NAME = "Name";
    public static final String TABLE_HEADER_POSSIBLE_VALUES = "Possible Values";
    public static final String TABLE_HEADER_SCHEMA = "Schema";
    public static final String TABLE_HEADER_SCOPES = "Scopes";
    public static final String TABLE_HEADER_TYPE = "Type";
    public static final String TABLE_HEADER_VARIABLE = "Variable";
    public static final String TABLE_TITLE_HEADERS = "Headers";
    public static final String TABLE_TITLE_PARAMETERS = "Parameters";
    public static final String TABLE_TITLE_PROPERTIES = "Properties";
    public static final String TABLE_TITLE_RESPONSES = "Responses";
    public static final String TABLE_TITLE_SECURITY = "Security";
    public static final String TABLE_TITLE_SERVER_VARIABLES = "Server Variables";

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

    public static String requiredIndicator(boolean isRequired) {
        return italicUnconstrained(isRequired ? LABEL_REQUIRED : LABEL_OPTIONAL).toLowerCase();
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

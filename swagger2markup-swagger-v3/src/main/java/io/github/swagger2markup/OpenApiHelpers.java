package io.github.swagger2markup;

import io.github.swagger2markup.adoc.ast.impl.DocumentImpl;
import io.github.swagger2markup.adoc.ast.impl.ParagraphBlockImpl;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.apache.commons.lang3.StringUtils;
import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.StructuralNode;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.github.swagger2markup.adoc.converter.internal.Delimiters.LINE_SEPARATOR;

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
    public static final String LABEL_NULLABLE = "Nullable";
    public static final String LABEL_READ_ONLY = "Read Only";
    public static final String LABEL_REQUIRED = "Required";
    public static final String LABEL_OPTIONAL = "Optional";
    public static final String LABEL_TITLE = "Title";
    public static final String LABEL_TYPE = "Type";
    public static final String LABEL_UNIQUE_ITEMS = "Unique Items";
    public static final String LABEL_WRITE_ONLY = "Write Only";
    public static final String SECTION_TITLE_PATHS = "Paths";
    public static final String SECTION_TITLE_SERVERS = "Servers";
    public static final String TABLE_HEADER_DEFAULT = "Default";
    public static final String TABLE_HEADER_DESCRIPTION = "Description";
    public static final String TABLE_HEADER_HTTP_CODE = "Code";
    public static final String TABLE_HEADER_LINKS = "Links";
    public static final String TABLE_HEADER_NAME = "Name";
    public static final String TABLE_HEADER_POSSIBLE_VALUES = "Possible Values";
    public static final String TABLE_HEADER_SCHEMA = "Schema";
    public static final String TABLE_HEADER_TYPE = "Type";
    public static final String TABLE_HEADER_VARIABLE = "Variable";
    public static final String TABLE_TITLE_HEADERS = "Headers";
    public static final String TABLE_TITLE_PARAMETERS = "Parameters";
    public static final String TABLE_TITLE_RESPONSES = "Responses";
    public static final String TABLE_TITLE_SERVER_VARIABLES = "Server Variables";

    public static void appendDescription(StructuralNode node, String description) {
        if (StringUtils.isNotBlank(description)) {
            Block paragraph = new ParagraphBlockImpl(node);
            paragraph.setSource(description);
            node.append(paragraph);
        }
    }

    public static Document generateSchemaDocument(StructuralNode parent, Schema schema) {
        Document schemaDocument = new DocumentImpl(parent);
        if (null == schema) return schemaDocument;

        StringBuilder sb = new StringBuilder();

        appendDescription(schemaDocument, schema.getDescription());

        Map<String, Object> schemasValueProperties = new HashMap<String, Object>() {{
            put(LABEL_TITLE, schema.getTitle());
            put(LABEL_TYPE, schema.getType());
            put(LABEL_DEFAULT, schema.getDefault());
            put(LABEL_FORMAT, schema.getFormat());
            put(LABEL_MAXIMUM, schema.getMaximum());
            put(LABEL_MINIMUM, schema.getMinimum());
            put(LABEL_MAX_LENGTH, schema.getMaxLength());
            put(LABEL_MIN_LENGTH, schema.getMinLength());
            put(LABEL_MAX_ITEMS, schema.getMaxItems());
            put(LABEL_MIN_ITEMS, schema.getMinItems());
            put(LABEL_MAX_PROPERTIES, schema.getMaxProperties());
            put(LABEL_MIN_PROPERTIES, schema.getMinProperties());
            put(LABEL_MULTIPLE_OF, schema.getMultipleOf());
        }};
        Map<String, Boolean> schemasBooleanProperties = new HashMap<String, Boolean>() {{
            put(LABEL_DEPRECATED, schema.getDeprecated());
            put(LABEL_NULLABLE, schema.getNullable());
            put(LABEL_READ_ONLY, schema.getReadOnly());
            put(LABEL_WRITE_ONLY, schema.getWriteOnly());
            put(LABEL_UNIQUE_ITEMS, schema.getUniqueItems());
            put(LABEL_EXCLUSIVE_MAXIMUM, schema.getExclusiveMaximum());
            put(LABEL_EXCLUSIVE_MINIMUM, schema.getExclusiveMinimum());
        }};

        Stream<String> schemaBooleanStream = schemasBooleanProperties.entrySet().stream()
                .filter(e -> null != e.getValue() && e.getValue())
                .map(e -> italicUnconstrained(e.getKey().toLowerCase()));
        Stream<String> schemaValueStream = schemasValueProperties.entrySet().stream()
            .filter(e -> null != e.getValue() && StringUtils.isNotBlank(e.getValue().toString()))
                .map(e -> e.getKey().toLowerCase() + ": " + e.getValue());

        ParagraphBlockImpl paragraphBlock = new ParagraphBlockImpl(schemaDocument);
        paragraphBlock.setSource(Stream.concat(schemaBooleanStream, schemaValueStream).collect(Collectors.joining(" +" + LINE_SEPARATOR)));
        schemaDocument.append(paragraphBlock);
        return schemaDocument;
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

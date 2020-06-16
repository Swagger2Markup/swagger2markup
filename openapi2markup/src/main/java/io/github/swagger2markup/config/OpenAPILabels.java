package io.github.swagger2markup.config;

import io.github.swagger2markup.Labels;
import io.github.swagger2markup.OpenSchema2MarkupConfig;

import java.util.ResourceBundle;

public class OpenAPILabels extends Labels {

    public static final String LABEL_CONTENT = "label_content";
    public static final String LABEL_DEFAULT = "label_default";
    public static final String LABEL_DEPRECATED = "label_deprecated";
    public static final String LABEL_EXAMPLE = "label_example";
    public static final String LABEL_EXAMPLES = "label_examples";
    public static final String LABEL_EXCLUSIVE_MAXIMUM = "label_exclusive_maximum";
    public static final String LABEL_EXCLUSIVE_MINIMUM = "label_exclusive_minimum";
    public static final String LABEL_EXTERNAL_VALUE = "label_external_value";
    public static final String LABEL_FORMAT = "label_format";
    public static final String LABEL_MAXIMUM = "label_maximum";
    public static final String LABEL_MAX_ITEMS = "label_max_items";
    public static final String LABEL_MAX_LENGTH = "label_max_length";
    public static final String LABEL_MAX_PROPERTIES = "label_max_properties";
    public static final String LABEL_MINIMUM = "label_minimum";
    public static final String LABEL_MIN_ITEMS = "label_min_items";
    public static final String LABEL_MIN_LENGTH = "label_min_length";
    public static final String LABEL_MIN_PROPERTIES = "label_min_properties";
    public static final String LABEL_MULTIPLE_OF = "label_multiple_of";
    public static final String LABEL_NO_LINKS = "label_no_links";
    public static final String LABEL_NULLABLE = "label_nullable";
    public static final String LABEL_OPERATION = "label_operation";
    public static final String LABEL_OPTIONAL = "label_optional";
    public static final String LABEL_PARAMETERS = "label_parameters";
    public static final String LABEL_READ_ONLY = "label_read_only";
    public static final String LABEL_REQUIRED = "label_required";
    public static final String LABEL_SERVER = "label_server";
    public static final String LABEL_TERMS_OF_SERVICE = "label_terms_of_service";
    public static final String LABEL_TITLE = "label_title";
    public static final String LABEL_TYPE = "label_type";
    public static final String LABEL_UNIQUE_ITEMS = "label_unique_items";
    public static final String LABEL_WRITE_ONLY = "label_write_only";
    public static final String SECTION_TITLE_COMPONENTS = "section_title_components";
    public static final String SECTION_TITLE_PARAMETERS = "section_title_parameters";
    public static final String SECTION_TITLE_PATHS = "section_title_paths";
    public static final String SECTION_TITLE_SCHEMAS = "section_title_schemas";
    public static final String SECTION_TITLE_SECURITY = "section_title_security";
    public static final String SECTION_TITLE_SERVERS = "section_title_servers";
    public static final String SECTION_TITLE_OVERVIEW = "section_title_overview";
    public static final String SECTION_TITLE_TAGS = "section_title_tags";
    public static final String SECTION_TITLE_RESPONSES = "section_title_responses";
    public static final String SECTION_TITLE_HEADERS = "section_title_headers";
    public static final String SECTION_TITLE_LINKS = "section_title_links";
    public static final String TABLE_HEADER_DEFAULT = "table_header_default";
    public static final String TABLE_HEADER_DESCRIPTION = "table_header_description";
    public static final String TABLE_HEADER_HTTP_CODE = "table_header_http_code";
    public static final String TABLE_HEADER_LINKS = "table_header_links";
    public static final String TABLE_HEADER_NAME = "table_header_name";
    public static final String TABLE_HEADER_POSSIBLE_VALUES = "table_header_possible_values";
    public static final String TABLE_HEADER_SCHEMA = "table_header_schema";
    public static final String TABLE_HEADER_SCOPES = "table_header_scopes";
    public static final String TABLE_HEADER_TYPE = "table_header_type";
    public static final String TABLE_HEADER_VARIABLE = "table_header_variable";
    public static final String TABLE_TITLE_HEADERS = "table_title_headers";
    public static final String TABLE_TITLE_PARAMETERS = "table_title_parameters";
    public static final String TABLE_TITLE_PROPERTIES = "table_title_properties";
    public static final String TABLE_TITLE_RESPONSES = "table_title_responses";
    public static final String TABLE_TITLE_SECURITY = "table_title_security";
    public static final String TABLE_TITLE_SERVER_VARIABLES = "table_title_server_variables";

    public OpenAPILabels(OpenSchema2MarkupConfig config) {
        super(ResourceBundle.getBundle("io/github/swagger2markup/lang/labels", config.getLanguage().toLocale()));
    }
}

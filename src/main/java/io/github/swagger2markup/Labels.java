/*
 * Copyright 2016 Robert Winkler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.swagger2markup;

import java.util.ResourceBundle;

public class Labels {

    public static final String DEFAULT_COLUMN = "default_column";
    public static final String MAXLENGTH_COLUMN = "maxlength_column";
    public static final String MINLENGTH_COLUMN = "minlength_column";
    public static final String LENGTH_COLUMN = "length_column";
    public static final String PATTERN_COLUMN = "pattern_column";
    public static final String MINVALUE_COLUMN = "minvalue_column";
    public static final String MINVALUE_EXCLUSIVE_COLUMN = "minvalue_exclusive_column";
    public static final String MAXVALUE_COLUMN = "maxvalue_column";
    public static final String MAXVALUE_EXCLUSIVE_COLUMN = "maxvalue_exclusive_column";
    public static final String EXAMPLE_COLUMN = "example_column";
    public static final String SCHEMA_COLUMN = "schema_column";
    public static final String NAME_COLUMN = "name_column";
    public static final String DESCRIPTION_COLUMN = "description_column";
    public static final String SCOPES_COLUMN = "scopes_column";
    public static final String DESCRIPTION = DESCRIPTION_COLUMN;
    public static final String PRODUCES = "produces";
    public static final String CONSUMES = "consumes";
    public static final String TAGS = "tags";
    public static final String NO_CONTENT = "no_content";
    public static final String FLAGS_COLUMN = "flags.column";
    public static final String FLAGS_REQUIRED = "flags.required";
    public static final String FLAGS_OPTIONAL = "flags.optional";
    public static final String FLAGS_READ_ONLY = "flags.read_only";

    // Overview Document
    public static final String CONTACT_INFORMATION = "contact_information";
    public static final String CONTACT_NAME = "contact_name";
    public static final String CONTACT_EMAIL = "contact_email";
    public static final String LICENSE_INFORMATION = "license_information";
    public static final String LICENSE = "license";
    public static final String LICENSE_URL = "license_url";
    public static final String TERMS_OF_SERVICE = "terms_of_service";
    public static final String CURRENT_VERSION = "current_version";
    public static final String VERSION = "version";
    public static final String OVERVIEW = "overview";
    public static final String URI_SCHEME = "uri_scheme";
    public static final String HOST = "host";
    public static final String BASE_PATH = "base_path";
    public static final String SCHEMES = "schemes";

    //Security Document
    public static final String SECURITY = "security";
    public static final String TYPE = "security_type";
    public static final String NAME = "security_name";
    public static final String IN = "security_in";
    public static final String FLOW = "security_flow";
    public static final String AUTHORIZATION_URL = "security_authorizationUrl";
    public static final String TOKEN_URL = "security_tokenUrl";

    //Definitions Document
    public static final String DEFINITIONS = "definitions";
    public static final String POLYMORPHISM_COLUMN = "polymorphism.column";
    public static final String POLYMORPHISM_DISCRIMINATOR_COLUMN = "polymorphism.discriminator";
    public static final String TYPE_COLUMN = "type_column";
    public static final String POLYMORPHISM_NATURE_COMPOSITION = "polymorphism.nature.COMPOSITION";
    public static final String POLYMORPHISM_NATURE_INHERITANCE = "polymorphism.nature.INHERITANCE";

    //Paths Document
    public static final String RESPONSE = "response";
    public static final String REQUEST = "request";
    public static final String PATHS = "paths";
    public static final String RESOURCES = "resources";
    public static final String OPERATIONS = "operations";
    public static final String PARAMETERS = "parameters";
    public static final String BODY_PARAMETER = "body_parameter";
    public static final String RESPONSES = "responses";
    public static final String HEADERS_COLUMN = "headers_column";
    public static final String EXAMPLE_REQUEST = "example_request";
    public static final String EXAMPLE_RESPONSE = "example_response";
    public static final String HTTP_CODE_COLUMN = "http_code_column";
    public static final String DEPRECATED_OPERATION = "operation.deprecated";
    public static final String UNKNOWN = "unknown";

    private ResourceBundle resourceBundle;

    public Labels(Swagger2MarkupConfig config) {
        this.resourceBundle = ResourceBundle.getBundle("io/github/swagger2markup/lang/labels", config.getOutputLanguage().toLocale());
    }

    /**
     * Gets a label for the given key from this resource bundle.
     *
     * @param key the key for the desired label
     * @return the label for the given key
     */
    public String getLabel(String key) {
        return resourceBundle.getString(key);
    }
}

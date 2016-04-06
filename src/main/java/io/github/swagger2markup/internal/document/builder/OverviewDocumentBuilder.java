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
package io.github.swagger2markup.internal.document.builder;

import io.github.swagger2markup.Swagger2MarkupConverter;
import io.github.swagger2markup.Swagger2MarkupExtensionRegistry;
import io.github.swagger2markup.internal.document.MarkupDocument;
import io.github.swagger2markup.markup.builder.MarkupDocBuilder;
import io.github.swagger2markup.spi.OverviewDocumentExtension;
import io.swagger.models.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static io.github.swagger2markup.spi.OverviewDocumentExtension.Context;
import static io.github.swagger2markup.spi.OverviewDocumentExtension.Position;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.*;

public class OverviewDocumentBuilder extends MarkupDocumentBuilder {

    private static final String OVERVIEW_ANCHOR = "overview";
    private final String OVERVIEW;
    private final String CURRENT_VERSION;
    private final String VERSION;
    private final String CONTACT_INFORMATION;
    private final String CONTACT_NAME;
    private final String CONTACT_EMAIL;
    private final String LICENSE_INFORMATION;
    private final String LICENSE;
    private final String LICENSE_URL;
    private final String TERMS_OF_SERVICE;
    private final String URI_SCHEME;
    private final String HOST;
    private final String BASE_PATH;
    private final String SCHEMES;

    public OverviewDocumentBuilder(Swagger2MarkupConverter.Context context, Swagger2MarkupExtensionRegistry extensionRegistry, Path outputPath){
        super(context, extensionRegistry, outputPath);

        ResourceBundle labels = ResourceBundle.getBundle("io/github/swagger2markup/lang/labels", config.getOutputLanguage().toLocale());
        OVERVIEW = labels.getString("overview");
        CURRENT_VERSION = labels.getString("current_version");
        VERSION = labels.getString("version");
        CONTACT_INFORMATION = labels.getString("contact_information");
        CONTACT_NAME = labels.getString("contact_name");
        CONTACT_EMAIL = labels.getString("contact_email");
        LICENSE_INFORMATION = labels.getString("license_information");
        LICENSE = labels.getString("license");
        LICENSE_URL = labels.getString("license_url");
        TERMS_OF_SERVICE = labels.getString("terms_of_service");
        URI_SCHEME = labels.getString("uri_scheme");
        HOST = labels.getString("host");
        BASE_PATH = labels.getString("base_path");
        SCHEMES = labels.getString("schemes");
    }

    /**
     * Builds the overview MarkupDocument.
     *
     * @return the overview MarkupDocument
     */
    @Override
    public MarkupDocument build(){
        Swagger swagger = globalContext.getSwagger();
        Info info = swagger.getInfo();
        buildDocumentTitle(info.getTitle());
        applyOverviewDocumentExtension(new Context(Position.DOCUMENT_BEFORE, this.markupDocBuilder));
        buildOverviewTitle(OVERVIEW);
        applyOverviewDocumentExtension(new Context(Position.DOCUMENT_BEGIN, this.markupDocBuilder));
        buildDescriptionParagraph(info.getDescription(), this.markupDocBuilder);
        buildVersionInfoSection(info.getVersion());
        buildContactInfoSection(info.getContact());
        buildLicenseInfoSection(info.getLicense(), info.getTermsOfService());
        buildUriSchemeSection(swagger);
        buildTagsSection(swagger.getTags());
        buildConsumesSection(swagger.getConsumes());
        buildProducesSection(swagger.getProduces());
        applyOverviewDocumentExtension(new Context(Position.DOCUMENT_END, this.markupDocBuilder));
        applyOverviewDocumentExtension(new Context(Position.DOCUMENT_AFTER, this.markupDocBuilder));
        return new MarkupDocument(markupDocBuilder);
    }

    private void buildDocumentTitle(String title) {
        this.markupDocBuilder.documentTitle(title);
    }

    private void buildOverviewTitle(String title) {
        this.markupDocBuilder.sectionTitleWithAnchorLevel1(title, OVERVIEW_ANCHOR);
    }

    private void buildVersionInfoSection(String version) {
        if(isNotBlank(version)){
            this.markupDocBuilder.sectionTitleLevel2(CURRENT_VERSION);

            MarkupDocBuilder paragraph = this.markupDocBuilder.copy(false);
            paragraph.italicText(VERSION).textLine(COLON + version);
            this.markupDocBuilder.paragraph(paragraph.toString(), true);
        }
    }

    private void buildContactInfoSection(Contact contact) {
        if(contact != null){
            this.markupDocBuilder.sectionTitleLevel2(CONTACT_INFORMATION);
            MarkupDocBuilder paragraph = this.markupDocBuilder.copy(false);
            if(isNotBlank(contact.getName())){
                paragraph.italicText(CONTACT_NAME).textLine(COLON + contact.getName());
            }
            if(isNotBlank(contact.getEmail())){
                paragraph.italicText(CONTACT_EMAIL).textLine(COLON + contact.getEmail());
            }
            this.markupDocBuilder.paragraph(paragraph.toString(), true);
        }
    }

    private void buildLicenseInfoSection(License license, String termOfService) {
        if (
                (license != null && (isNotBlank(license.getName()) || isNotBlank(license.getUrl()))) ||
                        (isNotBlank(termOfService))
                ) {
            this.markupDocBuilder.sectionTitleLevel2(LICENSE_INFORMATION);
            MarkupDocBuilder paragraph = this.markupDocBuilder.copy(false);
            if (license != null && isNotBlank(license.getName())) {
                paragraph.italicText(LICENSE).textLine(COLON + license.getName());
            }
            if (license != null && isNotBlank(license.getUrl())) {
                paragraph.italicText(LICENSE_URL).textLine(COLON + license.getUrl());
            }
            if(isNotBlank(termOfService)){
                paragraph.italicText(TERMS_OF_SERVICE).textLine(COLON + termOfService);
            }

            this.markupDocBuilder.paragraph(paragraph.toString(), true);
        }
    }

    private void buildUriSchemeSection(Swagger swagger) {
        if(isNotBlank(swagger.getHost()) || isNotBlank(swagger.getBasePath()) || isNotEmpty(swagger.getSchemes())) {
            this.markupDocBuilder.sectionTitleLevel2(URI_SCHEME);
            MarkupDocBuilder paragraph = this.markupDocBuilder.copy(false);
            if (isNotBlank(swagger.getHost())) {
                paragraph.italicText(HOST).textLine(COLON + swagger.getHost());
            }
            if (isNotBlank(swagger.getBasePath())) {
                paragraph.italicText(BASE_PATH).textLine(COLON + swagger.getBasePath());
            }
            if (isNotEmpty(swagger.getSchemes())) {
                List<String> schemes = new ArrayList<>();
                for (Scheme scheme : swagger.getSchemes()) {
                    schemes.add(scheme.toString());
                }
                paragraph.italicText(SCHEMES).textLine(COLON + join(schemes, ", "));
            }
            this.markupDocBuilder.paragraph(paragraph.toString(), true);

        }
    }

    private void buildTagsSection(List<Tag> tags) {
        if(isNotEmpty(tags)){
            this.markupDocBuilder.sectionTitleLevel2(TAGS);
            List<String> tagsList = new ArrayList<>();
            for(Tag tag : tags){
                String name = tag.getName();
                String description = tag.getDescription();
                if(isNoneBlank(description)){
                    tagsList.add(name + COLON + description);
                }else{
                    tagsList.add(name);
                }
            }
            this.markupDocBuilder.unorderedList(tagsList);
        }
    }

    private void buildConsumesSection(List<String> consumes) {
        if (isNotEmpty(consumes)) {
            this.markupDocBuilder.sectionTitleLevel2(CONSUMES);
            this.markupDocBuilder.newLine();
            for (String consume : consumes) {
                this.markupDocBuilder.unorderedListItem(literalText(consume));
            }
            this.markupDocBuilder.newLine();
        }
    }

    private void buildProducesSection(List<String> produces) {
        if (isNotEmpty(produces)) {
            this.markupDocBuilder.sectionTitleLevel2(PRODUCES);
            this.markupDocBuilder.newLine();
            for (String produce : produces) {
                this.markupDocBuilder.unorderedListItem(literalText(produce));
            }
            this.markupDocBuilder.newLine();
        }
    }

    /**
     * Apply extension context to all OverviewContentExtension
     *
     * @param context context
     */
    private void applyOverviewDocumentExtension(Context context) {
        for (OverviewDocumentExtension extension : extensionRegistry.getOverviewDocumentExtensions()) {
            extension.apply(context);
        }
    }

}

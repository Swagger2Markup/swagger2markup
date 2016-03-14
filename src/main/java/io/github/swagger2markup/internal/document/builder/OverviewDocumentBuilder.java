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
import io.github.swagger2markup.internal.document.MarkupDocument;
import io.github.swagger2markup.spi.OverviewDocumentExtension;
import io.swagger.models.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static io.github.swagger2markup.spi.OverviewDocumentExtension.*;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.*;

public class OverviewDocumentBuilder extends MarkupDocumentBuilder {

    private static final String OVERVIEW_ANCHOR = "overview";
    private static final String COLON = " : ";
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

    public OverviewDocumentBuilder(Swagger2MarkupConverter.Context context, Path outputPath){
        super(context, outputPath);

        ResourceBundle labels = ResourceBundle.getBundle("io/github/robwin/swagger2markup/lang/labels", config.getOutputLanguage().toLocale());
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
        buildDescriptionParagraph(info.getDescription());
        buildVersionInfoSection(info.getVersion());
        buildContactInfoSection(info.getContact());
        buildLicenseInfoSection(info.getLicense(), info.getTermsOfService());
        buildUriSchemeSection(swagger);
        buildTagsSection(swagger.getTags());
        buildConsumesSection(swagger.getConsumes());
        buildProducesSection(swagger.getProduces());
        applyOverviewDocumentExtension(new Context(Position.DOCUMENT_END, this.markupDocBuilder));
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
            this.markupDocBuilder.textLine(VERSION + COLON + version);
        }
    }

    private void buildContactInfoSection(Contact contact) {
        if(contact != null){
            this.markupDocBuilder.sectionTitleLevel2(CONTACT_INFORMATION);
            if(isNotBlank(contact.getName())){
                this.markupDocBuilder.textLine(CONTACT_NAME + COLON + contact.getName());
            }
            if(isNotBlank(contact.getEmail())){
                this.markupDocBuilder.textLine(CONTACT_EMAIL + COLON + contact.getEmail());
            }
        }
    }

    private void buildLicenseInfoSection(License license, String termOfService) {
        if(license != null && (isNotBlank(license.getName()) || isNotBlank(license.getUrl()))) {
            this.markupDocBuilder.sectionTitleLevel2(LICENSE_INFORMATION);
            if (isNotBlank(license.getName())) {
                this.markupDocBuilder.textLine(LICENSE + COLON + license.getName());
            }
            if (isNotBlank(license.getUrl())) {
                this.markupDocBuilder.textLine(LICENSE_URL + COLON + license.getUrl());
            }
        }
        if(isNotBlank(termOfService)){
            this.markupDocBuilder.textLine(TERMS_OF_SERVICE + COLON + termOfService);
        }
    }

    private void buildUriSchemeSection(Swagger swagger) {
        if(isNotBlank(swagger.getHost()) || isNotBlank(swagger.getBasePath()) || isNotEmpty(swagger.getSchemes())) {
            this.markupDocBuilder.sectionTitleLevel2(URI_SCHEME);
            if (isNotBlank(swagger.getHost())) {
                this.markupDocBuilder.textLine(HOST + COLON + swagger.getHost());
            }
            if (isNotBlank(swagger.getBasePath())) {
                this.markupDocBuilder.textLine(BASE_PATH + COLON + swagger.getBasePath());
            }
            if (isNotEmpty(swagger.getSchemes())) {
                List<String> schemes = new ArrayList<>();
                for (Scheme scheme : swagger.getSchemes()) {
                    schemes.add(scheme.toString());
                }
                this.markupDocBuilder.textLine(SCHEMES + COLON + join(schemes, ", "));
            }
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
                    tagsList.add(name + COLON +   description);
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
            this.markupDocBuilder.unorderedList(consumes);
        }
    }

    private void buildProducesSection(List<String> consumes) {
        if (isNotEmpty(consumes)) {
            this.markupDocBuilder.sectionTitleLevel2(PRODUCES);
            this.markupDocBuilder.unorderedList(consumes);
        }
    }

    /**
     * Apply extension context to all OverviewContentExtension
     *
     * @param context context
     */
    private void applyOverviewDocumentExtension(Context context) {
        for (OverviewDocumentExtension extension : globalContext.getExtensionRegistry().getExtensions(OverviewDocumentExtension.class)) {
            extension.apply(context);
        }
    }

}

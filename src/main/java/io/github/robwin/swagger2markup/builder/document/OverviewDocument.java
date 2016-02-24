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
package io.github.robwin.swagger2markup.builder.document;

import io.github.robwin.swagger2markup.Swagger2MarkupConverter;
import io.github.robwin.swagger2markup.extension.OverviewContentExtension;
import io.swagger.models.*;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.*;

public class OverviewDocument extends MarkupDocument {

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

    public OverviewDocument(Swagger2MarkupConverter.Context context, String outputDirectory){
        super(context, outputDirectory);

        ResourceBundle labels = ResourceBundle.getBundle("lang/labels", config.getOutputLanguage().toLocale());
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
     * Builds the MarkupDocument.
     *
     * @return the built MarkupDocument
     */
    @Override
    public MarkupDocument build(){
        overview();
        return this;
    }

    private void addOverviewTitle(String title) {
        this.markupDocBuilder.sectionTitleWithAnchorLevel1(title, OVERVIEW_ANCHOR);
    }


    /**
     * Builds the document header of the swagger model
     */
    private void overview() {
        Swagger swagger = globalContext.swagger;
        Info info = swagger.getInfo();
        this.markupDocBuilder.documentTitle(info.getTitle());

        applyOverviewExtension(new OverviewContentExtension.Context(OverviewContentExtension.Position.DOC_BEFORE, this.markupDocBuilder));
        addOverviewTitle(OVERVIEW);
        applyOverviewExtension(new OverviewContentExtension.Context(OverviewContentExtension.Position.DOC_BEGIN, this.markupDocBuilder));

        if(isNotBlank(info.getDescription())){
            this.markupDocBuilder.textLine(info.getDescription());
        }
        if(isNotBlank(info.getVersion())){
            this.markupDocBuilder.sectionTitleLevel2(CURRENT_VERSION);
            this.markupDocBuilder.textLine(VERSION + info.getVersion());
        }
        Contact contact = info.getContact();
        if(contact != null){
            this.markupDocBuilder.sectionTitleLevel2(CONTACT_INFORMATION);
            if(isNotBlank(contact.getName())){
                this.markupDocBuilder.textLine(CONTACT_NAME + contact.getName());
            }
            if(isNotBlank(contact.getEmail())){
                this.markupDocBuilder.textLine(CONTACT_EMAIL + contact.getEmail());
            }
        }

        License license = info.getLicense();
        if(license != null && (isNotBlank(license.getName()) || isNotBlank(license.getUrl()))) {
            this.markupDocBuilder.sectionTitleLevel2(LICENSE_INFORMATION);
            if (isNotBlank(license.getName())) {
                this.markupDocBuilder.textLine(LICENSE + license.getName());
            }
            if (isNotBlank(license.getUrl())) {
                this.markupDocBuilder.textLine(LICENSE_URL + license.getUrl());
            }
        }
        if(isNotBlank(info.getTermsOfService())){
            this.markupDocBuilder.textLine(TERMS_OF_SERVICE + info.getTermsOfService());
        }

        if(isNotBlank(swagger.getHost()) || isNotBlank(swagger.getBasePath()) || isNotEmpty(swagger.getSchemes())) {
            this.markupDocBuilder.sectionTitleLevel2(URI_SCHEME);
            if (isNotBlank(swagger.getHost())) {
                this.markupDocBuilder.textLine(HOST + swagger.getHost());
            }
            if (isNotBlank(swagger.getBasePath())) {
                this.markupDocBuilder.textLine(BASE_PATH + swagger.getBasePath());
            }
            if (isNotEmpty(swagger.getSchemes())) {
                List<String> schemes = new ArrayList<>();
                for (Scheme scheme : swagger.getSchemes()) {
                    schemes.add(scheme.toString());
                }
                this.markupDocBuilder.textLine(SCHEMES + join(schemes, ", "));
            }
        }

        if(isNotEmpty(swagger.getTags())){
            this.markupDocBuilder.sectionTitleLevel2(TAGS);
            List<String> tags = new ArrayList<>();
            for(Tag tag : swagger.getTags()){
                String name = tag.getName();
                String description = tag.getDescription();
                if(isNoneBlank(description)){
                    tags.add(name + ": " +   description);
                }else{
                    tags.add(name);
                }
            }
            this.markupDocBuilder.unorderedList(tags);
        }

        if(isNotEmpty(swagger.getConsumes())){
            this.markupDocBuilder.sectionTitleLevel2(CONSUMES);
            this.markupDocBuilder.unorderedList(swagger.getConsumes());
        }

        if(isNotEmpty(swagger.getProduces())){
            this.markupDocBuilder.sectionTitleLevel2(PRODUCES);
            this.markupDocBuilder.unorderedList(swagger.getProduces());
        }

        applyOverviewExtension(new OverviewContentExtension.Context(OverviewContentExtension.Position.DOC_END, this.markupDocBuilder));
        applyOverviewExtension(new OverviewContentExtension.Context(OverviewContentExtension.Position.DOC_AFTER, this.markupDocBuilder));

    }

    /**
     * Apply extension context to all OverviewContentExtension
     *
     * @param context context
     */
    private void applyOverviewExtension(OverviewContentExtension.Context context) {
        for (OverviewContentExtension extension : globalContext.extensionRegistry.getExtensions(OverviewContentExtension.class)) {
            extension.apply(globalContext, context);
        }
    }

}

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

import com.wordnik.swagger.models.*;
import io.github.robwin.markup.builder.MarkupLanguage;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OverviewDocument extends MarkupDocument {

    private static final String OVERVIEW = "Overview";
    private static final String CURRENT_VERSION = "Version information";
    private static final String VERSION = "Version: ";
    private static final String CONTACT_INFORMATION = "Contact information";
    private static final String CONTACT_NAME = "Contact: ";
    private static final String CONTACT_EMAIL = "Contact Email: ";
    private static final String LICENSE_INFORMATION = "License information";
    private static final String LICENSE = "License: ";
    private static final String LICENSE_URL = "License URL: ";
    private static final String TERMS_OF_SERVICE = "Terms of service: ";
    private static final String URI_SCHEME = "URI scheme";
    private static final String HOST = "Host: ";
    private static final String BASE_PATH = "BasePath: ";
    private static final String SCHEMES = "Schemes: ";

    public OverviewDocument(Swagger swagger, MarkupLanguage markupLanguage){
        super(swagger, markupLanguage);
    }

    /**
     * Builds the MarkupDocument.
     *
     * @return the built MarkupDocument
     * @throws java.io.IOException if the files to include are not readable
     */
    @Override
    public MarkupDocument build() throws IOException {
        overview();
        return this;
    }


    /**
     * Builds the document header of the swagger model
     */
    private void overview() {
        Info info = swagger.getInfo();
        this.markupDocBuilder.documentTitle(info.getTitle());
        this.markupDocBuilder.sectionTitleLevel1(OVERVIEW);
        if(StringUtils.isNotBlank(info.getDescription())){
            this.markupDocBuilder.textLine(info.getDescription());
            this.markupDocBuilder.newLine();
        }
        if(StringUtils.isNotBlank(info.getVersion())){
            this.markupDocBuilder.sectionTitleLevel2(CURRENT_VERSION);
            this.markupDocBuilder.textLine(VERSION + info.getVersion());
            this.markupDocBuilder.newLine();
        }
        Contact contact = info.getContact();
        if(contact != null){
            this.markupDocBuilder.sectionTitleLevel2(CONTACT_INFORMATION);
            if(StringUtils.isNotBlank(contact.getName())){
                this.markupDocBuilder.textLine(CONTACT_NAME + contact.getName());
            }
            if(StringUtils.isNotBlank(contact.getEmail())){
                this.markupDocBuilder.textLine(CONTACT_EMAIL + contact.getEmail());
            }
            this.markupDocBuilder.newLine();
        }

        License license = info.getLicense();
        if(license != null && (StringUtils.isNotBlank(license.getName()) || StringUtils.isNotBlank(license.getUrl()))) {
            this.markupDocBuilder.sectionTitleLevel2(LICENSE_INFORMATION);
            if (StringUtils.isNotBlank(license.getName())) {
                this.markupDocBuilder.textLine(LICENSE + license.getName());
            }
            if (StringUtils.isNotBlank(license.getUrl())) {
                this.markupDocBuilder.textLine(LICENSE_URL + license.getUrl());
            }
            this.markupDocBuilder.newLine();
        }
        if(StringUtils.isNotBlank(info.getTermsOfService())){
            this.markupDocBuilder.textLine(TERMS_OF_SERVICE + info.getTermsOfService());
            this.markupDocBuilder.newLine();
        }

        this.markupDocBuilder.sectionTitleLevel2(URI_SCHEME);
        if(StringUtils.isNotBlank(swagger.getHost())){
            this.markupDocBuilder.textLine(HOST + swagger.getHost());
        }
        if(StringUtils.isNotBlank(swagger.getBasePath())){
            this.markupDocBuilder.textLine(BASE_PATH + swagger.getBasePath());
        }
        if(CollectionUtils.isNotEmpty(swagger.getSchemes())){
            List<String> schemes = new ArrayList<>();
            for(Scheme scheme : swagger.getSchemes()){
                schemes.add(scheme.toString());
            }
            this.markupDocBuilder.textLine(SCHEMES + StringUtils.join(schemes, ", "));

        }
        this.markupDocBuilder.newLine();

        if(CollectionUtils.isNotEmpty(swagger.getTags())){
            this.markupDocBuilder.sectionTitleLevel2(TAGS);
            List<String> tags = new ArrayList<>();
            for(Tag tag : swagger.getTags()){
                String name = tag.getName();
                String description = tag.getDescription();
                if(StringUtils.isNoneBlank(description)){
                    tags.add(name + ": " +   description);
                }
                tags.add(name);
            }
            this.markupDocBuilder.unorderedList(tags);
            this.markupDocBuilder.newLine();
        }

        if(CollectionUtils.isNotEmpty(swagger.getConsumes())){
            this.markupDocBuilder.sectionTitleLevel2(CONSUMES);
            this.markupDocBuilder.unorderedList(swagger.getConsumes());
            this.markupDocBuilder.newLine();
        }

        if(CollectionUtils.isNotEmpty(swagger.getProduces())){
            this.markupDocBuilder.sectionTitleLevel2(PRODUCES);
            this.markupDocBuilder.unorderedList(swagger.getProduces());
            this.markupDocBuilder.newLine();
        }

    }
}

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
package io.github.swagger2markup.internal.document;

import io.github.swagger2markup.Labels;
import io.github.swagger2markup.Swagger2MarkupConverter;
import io.github.swagger2markup.internal.component.*;
import io.github.swagger2markup.markup.builder.MarkupDocBuilder;
import io.github.swagger2markup.spi.MarkupComponent;
import io.swagger.models.Contact;
import io.swagger.models.Info;
import io.swagger.models.Swagger;
import io.swagger.models.Tag;
import org.apache.commons.lang3.Validate;

import java.util.List;

import static io.github.swagger2markup.internal.utils.MarkupDocBuilderUtils.markupDescription;
import static io.github.swagger2markup.spi.OverviewDocumentExtension.Context;
import static io.github.swagger2markup.spi.OverviewDocumentExtension.Position;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class OverviewDocument extends MarkupComponent<OverviewDocument.Parameters> {

    public static final int SECTION_TITLE_LEVEL = 2;
    private static final String OVERVIEW_ANCHOR = "overview";
    private final VersionInfoComponent versionInfoComponent;
    private final ContactInfoComponent contactInfoComponent;
    private final LicenseInfoComponent licenseInfoComponent;
    private final UriSchemeComponent uriSchemeComponent;
    private final TagsComponent tagsComponent;
    private final ProducesComponent producesComponent;
    private final ConsumesComponent consumesComponent;

    public OverviewDocument(Swagger2MarkupConverter.Context context) {
        super(context);
        versionInfoComponent = new VersionInfoComponent(context);
        contactInfoComponent = new ContactInfoComponent(context);
        licenseInfoComponent = new LicenseInfoComponent(context);
        uriSchemeComponent = new UriSchemeComponent(context);
        tagsComponent = new TagsComponent(context);
        producesComponent = new ProducesComponent(context);
        consumesComponent = new ConsumesComponent(context);
    }

    public static OverviewDocument.Parameters parameters(Swagger swagger) {
        return new OverviewDocument.Parameters(swagger);
    }

    /**
     * Builds the overview MarkupDocument.
     *
     * @return the overview MarkupDocument
     */
    @Override
    public MarkupDocBuilder apply(MarkupDocBuilder markupDocBuilder, OverviewDocument.Parameters params) {
        Swagger swagger = params.swagger;
        Info info = swagger.getInfo();
        buildDocumentTitle(markupDocBuilder, info.getTitle());
        applyOverviewDocumentExtension(new Context(Position.DOCUMENT_BEFORE, markupDocBuilder));
        buildOverviewTitle(markupDocBuilder, labels.getLabel(Labels.OVERVIEW));
        applyOverviewDocumentExtension(new Context(Position.DOCUMENT_BEGIN, markupDocBuilder));
        buildDescriptionParagraph(markupDocBuilder, info.getDescription());
        buildVersionInfoSection(markupDocBuilder, info);
        buildContactInfoSection(markupDocBuilder, info.getContact());
        buildLicenseInfoSection(markupDocBuilder, info);
        buildUriSchemeSection(markupDocBuilder, swagger);
        buildTagsSection(markupDocBuilder, swagger.getTags());
        buildConsumesSection(markupDocBuilder, swagger.getConsumes());
        buildProducesSection(markupDocBuilder, swagger.getProduces());
        applyOverviewDocumentExtension(new Context(Position.DOCUMENT_END, markupDocBuilder));
        applyOverviewDocumentExtension(new Context(Position.DOCUMENT_AFTER, markupDocBuilder));
        return markupDocBuilder;
    }

    private void buildDocumentTitle(MarkupDocBuilder markupDocBuilder, String title) {
        markupDocBuilder.documentTitle(title);
    }

    private void buildOverviewTitle(MarkupDocBuilder markupDocBuilder, String title) {
        markupDocBuilder.sectionTitleWithAnchorLevel1(title, OVERVIEW_ANCHOR);
    }

    void buildDescriptionParagraph(MarkupDocBuilder markupDocBuilder, String description) {
        if (isNotBlank(description)) {
            markupDocBuilder.paragraph(markupDescription(config.getSwaggerMarkupLanguage(), markupDocBuilder, description));
        }
    }

    private void buildVersionInfoSection(MarkupDocBuilder markupDocBuilder, Info info) {
        if (info != null) {
            versionInfoComponent.apply(markupDocBuilder, VersionInfoComponent.parameters(info, SECTION_TITLE_LEVEL));
        }
    }

    private void buildContactInfoSection(MarkupDocBuilder markupDocBuilder, Contact contact) {
        if (contact != null) {
            contactInfoComponent.apply(markupDocBuilder, ContactInfoComponent.parameters(contact, SECTION_TITLE_LEVEL));
        }
    }

    private void buildLicenseInfoSection(MarkupDocBuilder markupDocBuilder, Info info) {
        if (info != null) {
            licenseInfoComponent.apply(markupDocBuilder, LicenseInfoComponent.parameters(info, SECTION_TITLE_LEVEL));
        }
    }

    private void buildUriSchemeSection(MarkupDocBuilder markupDocBuilder, Swagger swagger) {
        uriSchemeComponent.apply(markupDocBuilder, UriSchemeComponent.parameters(swagger, SECTION_TITLE_LEVEL));
    }

    private void buildTagsSection(MarkupDocBuilder markupDocBuilder, List<Tag> tags) {
        if (isNotEmpty(tags)) {
            tagsComponent.apply(markupDocBuilder, TagsComponent.parameters(tags, SECTION_TITLE_LEVEL));
        }
    }

    private void buildConsumesSection(MarkupDocBuilder markupDocBuilder, List<String> consumes) {
        if (isNotEmpty(consumes)) {
            consumesComponent.apply(markupDocBuilder, ConsumesComponent.parameters(consumes, SECTION_TITLE_LEVEL));
        }
    }

    private void buildProducesSection(MarkupDocBuilder markupDocBuilder, List<String> produces) {
        if (isNotEmpty(produces)) {
            producesComponent.apply(markupDocBuilder, ProducesComponent.parameters(produces, SECTION_TITLE_LEVEL));
        }
    }

    /**
     * Apply extension context to all OverviewContentExtension
     *
     * @param context context
     */
    private void applyOverviewDocumentExtension(Context context) {
        extensionRegistry.getOverviewDocumentExtensions().forEach(extension -> extension.apply(context));
    }

    public static class Parameters {
        private final Swagger swagger;

        public Parameters(Swagger swagger) {
            this.swagger = Validate.notNull(swagger, "Swagger must not be null");
        }
    }

}

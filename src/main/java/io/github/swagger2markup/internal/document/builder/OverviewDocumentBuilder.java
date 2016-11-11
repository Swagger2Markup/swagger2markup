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
import io.github.swagger2markup.internal.component.*;
import io.github.swagger2markup.internal.document.MarkupDocument;
import io.swagger.models.Contact;
import io.swagger.models.Info;
import io.swagger.models.Swagger;
import io.swagger.models.Tag;
import org.apache.commons.lang3.StringUtils;

import java.io.StringReader;
import java.nio.file.Path;
import java.util.List;

import static io.github.swagger2markup.spi.OverviewDocumentExtension.Context;
import static io.github.swagger2markup.spi.OverviewDocumentExtension.Position;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class OverviewDocumentBuilder extends MarkupDocumentBuilder {

    private static final String OVERVIEW_ANCHOR = "overview";
    public static final int SECTION_TITLE_LEVEL = 2;

    public OverviewDocumentBuilder(Swagger2MarkupConverter.Context context, Swagger2MarkupExtensionRegistry extensionRegistry, Path outputPath){
        super(context, extensionRegistry, outputPath);
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
        buildOverviewTitle(labels.getString(Labels.OVERVIEW));
        applyOverviewDocumentExtension(new Context(Position.DOCUMENT_BEGIN, this.markupDocBuilder));
        buildDescriptionParagraph(info.getDescription());
        buildVersionInfoSection(info);
        buildContactInfoSection(info.getContact());
        buildLicenseInfoSection(info);
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

    void buildDescriptionParagraph(String description) {
        if (isNotBlank(description)) {
            markupDocBuilder.paragraph(swaggerMarkupDescription(description));
        }
    }

    /**
     * Returns converted markup text from Swagger.
     *
     * @param markupText text to convert, or empty string
     * @return converted markup text, or an empty string if {@code markupText} == null
     */
    String swaggerMarkupDescription(String markupText) {
        if (markupText == null)
            return StringUtils.EMPTY;
        return copyMarkupDocBuilder().importMarkup(new StringReader(markupText), globalContext.getConfig().getSwaggerMarkupLanguage()).toString().trim();
    }

    private void buildVersionInfoSection(Info info) {
        if (info != null) {
            new VersionInfoComponent(componentContext, info, SECTION_TITLE_LEVEL).render();
        }
    }

    private void buildContactInfoSection(Contact contact) {
        if(contact != null){
            new ContactInfoComponent(componentContext, contact, SECTION_TITLE_LEVEL).render();
        }
    }

    private void buildLicenseInfoSection(Info info) {
        if (info != null) {
            new LicenseInfoComponent(componentContext, info, SECTION_TITLE_LEVEL).render();
        }
    }

    private void buildUriSchemeSection(Swagger swagger) {
        new UriSchemeComponent(componentContext, swagger, SECTION_TITLE_LEVEL).render();
    }

    private void buildTagsSection(List<Tag> tags) {
        if(isNotEmpty(tags)){
            new TagsComponent(componentContext, tags, SECTION_TITLE_LEVEL).render();
        }
    }

    private void buildConsumesSection(List<String> consumes) {
        if (isNotEmpty(consumes)) {
            new ConsumesComponent(componentContext, consumes, SECTION_TITLE_LEVEL).render();
        }
    }

    private void buildProducesSection(List<String> produces) {
        if (isNotEmpty(produces)) {
            new ProducesComponent(componentContext, produces, SECTION_TITLE_LEVEL).render();
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

}

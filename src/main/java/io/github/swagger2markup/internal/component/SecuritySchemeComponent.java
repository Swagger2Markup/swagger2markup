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
package io.github.swagger2markup.internal.component;


import ch.netzwerg.paleo.StringColumn;
import com.google.common.base.Joiner;
import io.github.swagger2markup.Swagger2MarkupConverter;
import io.github.swagger2markup.internal.resolver.DocumentResolver;
import io.github.swagger2markup.markup.builder.MarkupDocBuilder;
import io.github.swagger2markup.model.PathOperation;
import io.github.swagger2markup.spi.MarkupComponent;
import io.github.swagger2markup.spi.PathsDocumentExtension;
import io.swagger.models.auth.SecuritySchemeDefinition;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Validate;

import java.util.List;
import java.util.Map;

import static ch.netzwerg.paleo.ColumnIds.StringColumnId;
import static io.github.swagger2markup.Labels.*;
import static io.github.swagger2markup.internal.utils.MarkupDocBuilderUtils.*;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class SecuritySchemeComponent extends MarkupComponent<SecuritySchemeComponent.Parameters> {

    private final Map<String, SecuritySchemeDefinition> securityDefinitions;
    private final DocumentResolver securityDocumentResolver;
    private final TableComponent tableComponent;

    public SecuritySchemeComponent(Swagger2MarkupConverter.Context context,
                                   DocumentResolver securityDocumentResolver) {
        super(context);
        this.securityDefinitions = context.getSwagger().getSecurityDefinitions();
        this.securityDocumentResolver = Validate.notNull(securityDocumentResolver, "SecurityDocumentResolver must not be null");
        this.tableComponent = new TableComponent(context);
    }

    public static SecuritySchemeComponent.Parameters parameters(PathOperation operation,
                                                                int titleLevel) {
        return new SecuritySchemeComponent.Parameters(operation, titleLevel);
    }

    @Override
    public MarkupDocBuilder apply(MarkupDocBuilder markupDocBuilder, Parameters params) {
        PathOperation operation = params.operation;
        MarkupDocBuilder securityBuilder = copyMarkupDocBuilder(markupDocBuilder);
        List<Map<String, List<String>>> securitySchemes = operation.getOperation().getSecurity();
        applyPathsDocumentExtension(new PathsDocumentExtension.Context(PathsDocumentExtension.Position.OPERATION_SECURITY_BEGIN, securityBuilder, operation));
        if (CollectionUtils.isNotEmpty(securitySchemes)) {
            StringColumn.Builder typeColumnBuilder = StringColumn.builder(StringColumnId.of(labels.getLabel(TYPE_COLUMN)))
                    .putMetaData(TableComponent.WIDTH_RATIO, "3");
            StringColumn.Builder nameColumnBuilder = StringColumn.builder(StringColumnId.of(labels.getLabel(NAME_COLUMN)))
                    .putMetaData(TableComponent.WIDTH_RATIO, "4");
            StringColumn.Builder scopeColumnBuilder = StringColumn.builder(StringColumnId.of(labels.getLabel(SCOPES_COLUMN)))
                    .putMetaData(TableComponent.WIDTH_RATIO, "13")
                    .putMetaData(TableComponent.HEADER_COLUMN, "true");


            for (Map<String, List<String>> securityScheme : securitySchemes) {
                for (Map.Entry<String, List<String>> securityEntry : securityScheme.entrySet()) {
                    String securityKey = securityEntry.getKey();
                    String type = labels.getLabel(UNKNOWN);
                    if (securityDefinitions != null && securityDefinitions.containsKey(securityKey)) {
                        type = securityDefinitions.get(securityKey).getType();
                    }

                    typeColumnBuilder.add(boldText(markupDocBuilder, type));
                    nameColumnBuilder.add(boldText(markupDocBuilder, crossReference(markupDocBuilder, securityDocumentResolver.apply(securityKey), securityKey, securityKey)));
                    scopeColumnBuilder.add(Joiner.on(",").join(securityEntry.getValue()));
                }
            }

            securityBuilder = tableComponent.apply(securityBuilder, TableComponent.parameters(typeColumnBuilder.build(),
                    nameColumnBuilder.build(),
                    scopeColumnBuilder.build()));
        }
        applyPathsDocumentExtension(new PathsDocumentExtension.Context(PathsDocumentExtension.Position.OPERATION_SECURITY_END, securityBuilder, operation));
        String securityContent = securityBuilder.toString();

        applyPathsDocumentExtension(new PathsDocumentExtension.Context(PathsDocumentExtension.Position.OPERATION_SECURITY_BEFORE, markupDocBuilder, operation));
        if (isNotBlank(securityContent)) {
            markupDocBuilder.sectionTitleLevel(params.titleLevel, labels.getLabel(SECURITY));
            markupDocBuilder.text(securityContent);
        }
        applyPathsDocumentExtension(new PathsDocumentExtension.Context(PathsDocumentExtension.Position.OPERATION_SECURITY_AFTER, markupDocBuilder, operation));
        return markupDocBuilder;
    }

    /**
     * Apply extension context to all OperationsContentExtension.
     *
     * @param context context
     */
    private void applyPathsDocumentExtension(PathsDocumentExtension.Context context) {
        extensionRegistry.getPathsDocumentExtensions().forEach(extension -> extension.apply(context));
    }

    public static class Parameters {
        private final PathOperation operation;
        private final int titleLevel;

        public Parameters(PathOperation operation,
                          int titleLevel) {
            this.operation = Validate.notNull(operation, "PathOperation must not be null");
            this.titleLevel = titleLevel;
        }
    }
}

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

import io.github.swagger2markup.markup.builder.MarkupDocBuilder;
import io.swagger.models.Swagger;

import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.join;

public class UriSchemeComponent extends MarkupComponent {

    private final int titleLevel;
    private final Swagger swagger;

    public UriSchemeComponent(Context context,
                              Swagger swagger,
                              int titleLevel){
        super(context);
        this.swagger = swagger;
        this.titleLevel = titleLevel;
    }

    @Override
    public MarkupDocBuilder render() {
        if(isNotBlank(swagger.getHost()) || isNotBlank(swagger.getBasePath()) || isNotEmpty(swagger.getSchemes())) {
            this.markupDocBuilder.sectionTitleLevel(titleLevel, labels.getString(Labels.URI_SCHEME));
            MarkupDocBuilder paragraphBuilder = copyMarkupDocBuilder();
            if (isNotBlank(swagger.getHost())) {
                paragraphBuilder.italicText(labels.getString(Labels.HOST))
                        .textLine(COLON + swagger.getHost());
            }
            if (isNotBlank(swagger.getBasePath())) {
                paragraphBuilder.italicText(labels.getString(Labels.BASE_PATH))
                        .textLine(COLON + swagger.getBasePath());
            }
            if (isNotEmpty(swagger.getSchemes())) {
                List<String> schemes = swagger.getSchemes().stream()
                        .map(Enum::toString)
                        .collect(Collectors.toList());
                paragraphBuilder.italicText(labels.getString(Labels.SCHEMES))
                        .textLine(COLON + join(schemes, ", "));
            }
            this.markupDocBuilder.paragraph(paragraphBuilder.toString(), true);
        }
        return markupDocBuilder;
    }
}

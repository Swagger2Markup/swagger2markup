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

import io.github.swagger2markup.Swagger2MarkupConverter;
import io.github.swagger2markup.markup.builder.MarkupDocBuilder;
import io.github.swagger2markup.spi.MarkupComponent;
import io.swagger.models.Info;
import io.swagger.models.License;
import org.apache.commons.lang3.Validate;

import static io.github.swagger2markup.Labels.*;
import static io.github.swagger2markup.internal.utils.MarkupDocBuilderUtils.copyMarkupDocBuilder;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class LicenseInfoComponent extends MarkupComponent<LicenseInfoComponent.Parameters> {

    public LicenseInfoComponent(Swagger2MarkupConverter.Context context) {
        super(context);
    }

    public static LicenseInfoComponent.Parameters parameters(Info info,
                                                             int titleLevel) {
        return new LicenseInfoComponent.Parameters(info, titleLevel);
    }

    @Override
    public MarkupDocBuilder apply(MarkupDocBuilder markupDocBuilder, Parameters params) {
        Info info = params.info;
        License license = info.getLicense();
        String termOfService = info.getTermsOfService();
        if ((license != null && (isNotBlank(license.getName()) || isNotBlank(license.getUrl()))) || isNotBlank(termOfService)) {
            markupDocBuilder.sectionTitleLevel(params.titleLevel, labels.getLabel(LICENSE_INFORMATION));
            MarkupDocBuilder paragraph = copyMarkupDocBuilder(markupDocBuilder);
            if (license != null) {
                if (isNotBlank(license.getName())) {
                    paragraph.italicText(labels.getLabel(LICENSE)).textLine(COLON + license.getName());
                }
                if (isNotBlank(license.getUrl())) {
                    paragraph.italicText(labels.getLabel(LICENSE_URL)).textLine(COLON + license.getUrl());
                }
            }

            paragraph.italicText(labels.getLabel(TERMS_OF_SERVICE)).textLine(COLON + termOfService);

            markupDocBuilder.paragraph(paragraph.toString(), true);
        }

        return markupDocBuilder;
    }

    public static class Parameters {
        private final int titleLevel;
        private final Info info;

        public Parameters(Info info,
                          int titleLevel) {
            this.info = Validate.notNull(info, "Info must not be null");
            this.titleLevel = titleLevel;
        }
    }
}

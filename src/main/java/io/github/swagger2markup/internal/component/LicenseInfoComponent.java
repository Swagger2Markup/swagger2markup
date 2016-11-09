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
import io.swagger.models.Info;
import io.swagger.models.License;
import org.apache.commons.lang3.Validate;

import static io.github.swagger2markup.internal.component.Labels.*;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class LicenseInfoComponent extends MarkupComponent {

    private final int titleLevel;
    private final Info info;

    public LicenseInfoComponent(Context context,
                                Info info,
                                int titleLevel){
        super(context);
        this.info = Validate.notNull(info, "Info must not be null");
        this.titleLevel = titleLevel;
    }

    @Override
    public MarkupDocBuilder render() {
        License license = info.getLicense();
        String termOfService = info.getTermsOfService();
        if((license != null && (isNotBlank(license.getName()) || isNotBlank(license.getUrl()))) || isNotBlank(termOfService)) {
            this.markupDocBuilder.sectionTitleLevel(titleLevel, labels.getString(LICENSE_INFORMATION));
            MarkupDocBuilder paragraph = copyMarkupDocBuilder();
            if (license != null) {
                if (isNotBlank(license.getName())) {
                    paragraph.italicText(labels.getString(LICENSE)).textLine(COLON + license.getName());
                }
                if (isNotBlank(license.getUrl())) {
                    paragraph.italicText(labels.getString(LICENSE_URL)).textLine(COLON + license.getUrl());
                }
            }

            paragraph.italicText(labels.getString(TERMS_OF_SERVICE)).textLine(COLON + termOfService);

            this.markupDocBuilder.paragraph(paragraph.toString(), true);
        }

        return markupDocBuilder;
    }
}

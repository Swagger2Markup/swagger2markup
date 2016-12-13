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


import io.github.swagger2markup.Labels;
import io.github.swagger2markup.Swagger2MarkupConverter;
import io.github.swagger2markup.markup.builder.MarkupDocBuilder;
import io.github.swagger2markup.spi.MarkupComponent;
import io.swagger.models.Contact;
import org.apache.commons.lang3.Validate;

import static io.github.swagger2markup.internal.utils.MarkupDocBuilderUtils.copyMarkupDocBuilder;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class ContactInfoComponent extends MarkupComponent<ContactInfoComponent.Parameters> {

    public ContactInfoComponent(Swagger2MarkupConverter.Context context) {
        super(context);
    }

    public static ContactInfoComponent.Parameters parameters(Contact contact,
                                                             int titleLevel) {
        return new ContactInfoComponent.Parameters(contact, titleLevel);
    }

    @Override
    public MarkupDocBuilder apply(MarkupDocBuilder markupDocBuilder, Parameters params) {
        Contact contact = params.contact;
        if (isNotBlank(contact.getName()) || isNotBlank(contact.getEmail())) {
            markupDocBuilder.sectionTitleLevel(params.titleLevel, labels.getLabel(Labels.CONTACT_INFORMATION));
            MarkupDocBuilder paragraphBuilder = copyMarkupDocBuilder(markupDocBuilder);
            if (isNotBlank(contact.getName())) {
                paragraphBuilder.italicText(labels.getLabel(Labels.CONTACT_NAME))
                        .textLine(COLON + contact.getName());
            }
            if (isNotBlank(contact.getEmail())) {
                paragraphBuilder.italicText(labels.getLabel(Labels.CONTACT_EMAIL))
                        .textLine(COLON + contact.getEmail());
            }
            markupDocBuilder.paragraph(paragraphBuilder.toString(), true);
        }
        return markupDocBuilder;
    }

    public static class Parameters {
        private final Contact contact;
        private final int titleLevel;

        public Parameters(Contact contact,
                          int titleLevel) {
            this.contact = Validate.notNull(contact, "Contact must not be null");
            this.titleLevel = titleLevel;
        }
    }
}

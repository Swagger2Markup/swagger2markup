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
import io.swagger.models.Contact;
import org.jsoup.helper.Validate;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class ContactInfoComponent extends MarkupComponent {

    private final Contact contact;
    private final int titleLevel;

    public ContactInfoComponent(Context context,
                                Contact contact,
                                int titleLevel){
        super(context);
        Validate.notNull(contact, "Contact must not be null");
        this.contact = contact;
        this.titleLevel = titleLevel;
    }

    @Override
    public MarkupDocBuilder render() {
        if (isNotBlank(contact.getName()) || isNotBlank(contact.getEmail())){
            this.markupDocBuilder.sectionTitleLevel(titleLevel, labels.getString(Labels.CONTACT_INFORMATION));
            MarkupDocBuilder paragraphBuilder = copyMarkupDocBuilder();
            if (isNotBlank(contact.getName())) {
                paragraphBuilder.italicText(labels.getString(Labels.CONTACT_NAME))
                        .textLine(COLON + contact.getName());
            }
            if (isNotBlank(contact.getEmail())) {
                paragraphBuilder.italicText(labels.getString(Labels.CONTACT_EMAIL))
                        .textLine(COLON + contact.getEmail());
            }
            this.markupDocBuilder.paragraph(paragraphBuilder.toString(), true);
        }
        return markupDocBuilder;
    }

}

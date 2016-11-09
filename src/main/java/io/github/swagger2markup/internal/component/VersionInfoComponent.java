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
import org.jsoup.helper.Validate;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class VersionInfoComponent extends MarkupComponent {

    private final int titleLevel;
    private final Info info;

    public VersionInfoComponent(Context context,
                                Info info,
                                int titleLevel){
        super(context);
        Validate.notNull(info, "Info must not be null");
        this.info = info;
        this.titleLevel = titleLevel;
    }

    @Override
    public MarkupDocBuilder render() {
        String version = info.getVersion();
        if(isNotBlank(version)){
            this.markupDocBuilder.sectionTitleLevel(titleLevel, labels.getString(Labels.CURRENT_VERSION));
            MarkupDocBuilder paragraphBuilder = copyMarkupDocBuilder();
            paragraphBuilder.italicText(labels.getString(Labels.VERSION)).textLine(COLON + version);
            this.markupDocBuilder.paragraph(paragraphBuilder.toString(), true);
        }
        return markupDocBuilder;
    }
}

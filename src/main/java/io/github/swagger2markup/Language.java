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

package io.github.swagger2markup;

import java.util.Locale;

/**
 * @author Maksim Myshkin
 */
public enum Language {
    EN(Locale.ENGLISH),
    RU(new Locale("ru")),
    FR(Locale.FRENCH),
    DE(Locale.GERMAN),
    TR(new Locale("tr")),
    ZH(Locale.CHINESE),
    ES(new Locale("es")),
    BR(new Locale("pt", "BR")),
    JA(Locale.JAPANESE);

    private final Locale lang;

    Language(final Locale lang) {
        this.lang = lang;
    }

    public Locale toLocale() {
        return lang;
    }
}

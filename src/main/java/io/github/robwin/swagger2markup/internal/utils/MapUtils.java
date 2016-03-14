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
package io.github.robwin.swagger2markup.internal.utils;

import java.util.*;

public class MapUtils {

    /**
     * Returns the keys of the Map either ordered or as-is, if the comparator is null.
     *
     * @param map the Map
     * @param comparator the comparator to use.
     * @return the keySet of the Map
     */
    public static Set<String> toKeySet(Map<String, ?> map, Comparator<String> comparator){
        Set<String> keys;
        if (comparator == null)
            keys = new LinkedHashSet<>();
        else
            keys = new TreeSet<>(comparator);
        keys.addAll(map.keySet());
        return keys;
    }
}

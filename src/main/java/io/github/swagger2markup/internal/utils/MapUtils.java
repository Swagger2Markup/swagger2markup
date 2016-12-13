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
package io.github.swagger2markup.internal.utils;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

public class MapUtils {

    /**
     * Returns the the Map either ordered or as-is, if the comparator is null.
     *
     * @param map        the Map
     * @param comparator the comparator to use.
     * @return the keySet of the Map
     */
    public static <K, V> Map<K, V> toSortedMap(Map<K, V> map, Comparator<? super K> comparator) {
        Map<K, V> sortedMap;
        if (comparator == null)
            sortedMap = new LinkedHashMap<>();
        else
            sortedMap = new TreeMap<>(comparator);
        sortedMap.putAll(map);
        return sortedMap;
    }
}

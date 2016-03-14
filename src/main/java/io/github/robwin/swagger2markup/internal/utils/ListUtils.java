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

public class ListUtils {

    /**
     * Returns the List as an Set either ordered or as-is, if the comparator is null.
     *
     * @param list the List
     * @param comparator the comparator to use.
     * @return the Set
     */
    public static Set<String> toSet(List<String> list, Comparator<String> comparator){
        Set<String> set;
        if (comparator == null)
            set = new LinkedHashSet<>();
        else
            set = new TreeSet<>(comparator);
        set.addAll(list);
        return set;
    }


}

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
package io.github.swagger2markup.builder;

import com.google.common.base.Optional;
import io.github.swagger2markup.markup.builder.MarkupLanguage;
import io.github.swagger2markup.GroupBy;
import io.github.swagger2markup.Language;
import io.github.swagger2markup.OrderBy;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationConverter;
import org.apache.commons.configuration.MapConfiguration;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class Swagger2MarkupProperties {

    private final Configuration configuration;

    public Swagger2MarkupProperties(Properties properties) {
        this(ConfigurationConverter.getConfiguration(properties));
    }

    public Swagger2MarkupProperties(Map<String, String> map) {
        this(new MapConfiguration(map));
    }

    public Swagger2MarkupProperties(Configuration configuration){
        this.configuration = configuration;
    }

    /**
     * Returns an optional String property value associated with the given key.
     * @param key the property name to resolve
     */
    public Optional<String> getString(String key){
        return Optional.fromNullable(configuration.getString(key));
    }

    /**
     * Return the String property value associated with the given key, or
     * {@code defaultValue} if the key cannot be resolved.
     * @param key the property name to resolve
     * @param defaultValue the default value to return if no value is found
     */
    public String getString(String key, String defaultValue){
        return configuration.getString(key, defaultValue);
    }

    /**
     * Return the int property value associated with the given key, or
     * {@code defaultValue} if the key cannot be resolved.
     * @param key the property name to resolve
     * @param defaultValue the default value to return if no value is found
     */
    public int getInt(String key, int defaultValue){
        return configuration.getInt(key, defaultValue);
    }

    /**
     * Returns an optional Integer property value associated with the given key.
     * @param key the property name to resolve
     */
    public Optional<Integer> getInteger(String key){
        return Optional.fromNullable(configuration.getInteger(key, null));
    }

    /**
     * Return the int property value associated with the given key (never {@code null}).
     * @throws IllegalStateException if the key cannot be resolved
     */
    public int getRequiredInt(String key){
        Optional<Integer> value = getInteger(key);
        if(value.isPresent()){
            return value.get();
        }
        throw new IllegalStateException(String.format("required key [%s] not found", key));
    }

    /**
     * Return the boolean property value associated with the given key (never {@code null}).
     * @throws IllegalStateException if the key cannot be resolved
     */
    public boolean getRequiredBoolean(String key){
        Boolean value = configuration.getBoolean(key, null);
        if(value != null){
            return value;
        }else{
            throw new IllegalStateException(String.format("required key [%s] not found", key));
        }
    }

    /**
     * Return the boolean property value associated with the given key, or
     * {@code defaultValue} if the key cannot be resolved.
     * @param key the property name to resolve
     * @param defaultValue the default value to return if no value is found
     */
    public boolean getBoolean(String key, boolean defaultValue){
        return configuration.getBoolean(key, defaultValue);
    }

    /**
     * Return the URI property value associated with the given key, or
     * {@code defaultValue} if the key cannot be resolved.
     * @param key the property name to resolve
     * @throws IllegalStateException if the value cannot be mapped to the enum
     */
    public Optional<URI> getURI(String key){
        Optional<String> property = getString(key);
        if(property.isPresent()){
            return Optional.of(URI.create(property.get()));
        }else{
            return Optional.absent();
        }
    }

    /**
     * Return the URI property value associated with the given key (never {@code null}).
     * @throws IllegalStateException if the key cannot be resolved
     */
    public URI getRequiredURI(String key){
        Optional<String> property = getString(key);
        if(property.isPresent()){
            return URI.create(property.get());
        }else{
            throw new IllegalStateException(String.format("required key [%s] not found", key));
        }
    }

    /**
     * Return the Path property value associated with the given key, or
     * {@code defaultValue} if the key cannot be resolved.
     * @param key the property name to resolve
     * @throws IllegalStateException if the value cannot be mapped to the enum
     */
    public Optional<Path> getPath(String key){
        Optional<String> property = getString(key);
        if(property.isPresent()){
            return Optional.of(Paths.get(property.get()));
        }else{
            return Optional.absent();
        }
    }

    /**
     * Return the Path property value associated with the given key (never {@code null}).
     * @throws IllegalStateException if the key cannot be resolved
     */
    public Path getRequiredPath(String key){
        Optional<String> property = getString(key);
        if(property.isPresent()){
            return Paths.get(property.get());
        }else{
            throw new IllegalStateException(String.format("required key [%s] not found", key));
        }
    }

    /**
     * Return the MarkupLanguage property value associated with the given key, or
     * {@code defaultValue} if the key cannot be resolved.
     * @param key the property name to resolve
     */
    public Optional<MarkupLanguage> getMarkupLanguage(String key){
        Optional<String> property = getString(key);
        if(property.isPresent()){
            return Optional.of(MarkupLanguage.valueOf(property.get()));
        }else{
            return Optional.absent();
        }
    }

    /**
     * Return the MarkupLanguage property value associated with the given key (never {@code null}).
     * @throws IllegalStateException if the key cannot be resolved
     */
    public MarkupLanguage getRequiredMarkupLanguage(String key){
        return MarkupLanguage.valueOf(configuration.getString(key));
    }

    /**
     * Return the Language property value associated with the given key, or
     * {@code defaultValue} if the key cannot be resolved.
     * @param key the property name to resolve
     */
    public Language getLanguage(String key){
        return Language.valueOf(configuration.getString(key));
    }

    /**
     * Return the GroupBy property value associated with the given key, or
     * {@code defaultValue} if the key cannot be resolved.
     * @param key the property name to resolve
     * @throws IllegalStateException if the value cannot be mapped to the enum
     */
    public GroupBy getGroupBy(String key){
        return GroupBy.valueOf(configuration.getString(key));
    }

    /**
     * Return the OrderBy property value associated with the given key, or
     * {@code defaultValue} if the key cannot be resolved.
     * @param key the property name to resolve
     * @throws IllegalStateException if the value cannot be mapped to the enum
     */
    public OrderBy getOrderBy(String key){
        return OrderBy.valueOf(configuration.getString(key));
    }

    /**
     * Return the String property value associated with the given key (never {@code null}).
     * @throws IllegalStateException if the key cannot be resolved
     */
    public String getRequiredString(String key) throws IllegalStateException{
        Optional<String> property = getString(key);
        if(property.isPresent()){
            return property.get();
        }else{
            throw new IllegalStateException(String.format("required key [%s] not found", key));
        }
    }

    /**
     * Return the list of keys.
     *
     * @return the list of keys.
     */
    public List<String> getKeys(){
        return IteratorUtils.toList(configuration.getKeys());
    }

    /**
     * Get the list of the keys contained in the configuration that match the
     * specified prefix. For instance, if the configuration contains the
     * following keys:<br>
     * {@code swagger2markup.extensions.folder1, swagger2markup.extensions.folder2, swagger2markup.folder3},<br>
     * an invocation of {@code getKeys("swagger2markup.extensions");}<br>
     * will return the key below:<br>
     * {@code swagger2markup.extensions.folder1, swagger2markup.extensions.folder2}.<br>
     * Note that the prefix itself is included in the result set if there is a
     * matching key. The exact behavior - how the prefix is actually
     * interpreted - depends on a concrete implementation.
     *
     * @param prefix The prefix to test against.
     *
     * @return the list of keys.
     */
    public List<String> getKeys(String prefix){
        return IteratorUtils.toList(configuration.getKeys(prefix));
    }
}

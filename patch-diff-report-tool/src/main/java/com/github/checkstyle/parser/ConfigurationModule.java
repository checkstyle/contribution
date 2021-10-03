////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2021 the original author or authors.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
////////////////////////////////////////////////////////////////////////////////

package com.github.checkstyle.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parsed "module" tag from configuration XML.
 *
 * @author attatrol
 *
 */
class ConfigurationModule {

    /**
     * This module name.
     */
    private String name;

    /**
     * Child modules of this module.
     */
    private List<ConfigurationModule> children = new ArrayList<>();

    /**
     * Properties of this module.
     */
    private Map<String, List<String>> properties = new HashMap<>();

    /**
     * Basic ctor, creates instance without any child or property.
     * Children should be added with addChild method.
     * Properties should be added with addProperty method.
     *
     * @param name
     *        name of this module.
     */
    ConfigurationModule(String name) {
        this.name = name;
    }

    /**
     * Returns the module name.
     *
     * @return the module name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the submodules of this module.
     *
     * @return the submodules of this module
     */
    public List<ConfigurationModule> getChildren() {
        return children;
    }

    /**
     * Returns the properties of this module.
     *
     * @return the properties of this module
     */
    public Map<String, List<String>> getProperties() {
        return properties;
    }

    /**
     * Appends new child module to this module.
     *
     * @param module
     *        child module.
     */
    public void addChild(ConfigurationModule module) {
        children.add(module);
    }

    /**
     * Adds new property to the list of properties or adds new
     * token values for an existing property.
     *
     * @param propertyName
     *        name of the adding property.
     * @param propertyTokenValue
     *        the adding property token value.
     */
    public void addProperty(String propertyName, String propertyTokenValue) {
        if (properties.containsKey(propertyName)) {
            final List<String> values = properties.get(propertyName);
            values.add(propertyTokenValue);
        }
        else {
            final List<String> values = new ArrayList<>();
            values.add(propertyTokenValue);
            properties.put(propertyName, values);
        }
    }

}

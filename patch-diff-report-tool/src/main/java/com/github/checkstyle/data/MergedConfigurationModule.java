////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2022 the original author or authors.
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

package com.github.checkstyle.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Describes prepared for output merged "module" tags
 * which had the same name and the same location in the
 * tree structure of each configuration XML.
 *
 * @author attatrol
 *
 */
public final class MergedConfigurationModule {

    /**
     * Module name that contains names of all parents sequentially.
     */
    private final String fullModuleName;

    /**
     * Simple module name.
     */
    private final String simpleModuleName;

    /**
     * Module properties from the base module.
     * If null then no base module is present.
     */
    private final Map<String, List<String>> baseModuleProperties;

    /**
     * Module properties from the patch module.
     * If null then no base module is present.
     */
    private final Map<String, List<String>> patchModuleProperties;

    /**
     * Child modules of this module.
     */
    private final List<MergedConfigurationModule> children = new ArrayList<>();

    /**
     * True if properties from both configuration modules are identical.
     */
    private final boolean hasIdenticalProperties;

    /**
     * Basic ctor, creates instance without any child.
     * Children should be added with addChild method.
     *
     * @param baseModuleProperties
     *        properties of the base module, may be null.
     * @param patchModuleProperties
     *        properties of the patch module, may be null.
     * @param simpleName
     *        simple name of the module.
     * @param parentName
     *        full name of the parent.
     */
    public MergedConfigurationModule(Map<String, List<String>> baseModuleProperties,
            Map<String, List<String>> patchModuleProperties,
            String simpleName, String parentName) {
        this.baseModuleProperties = baseModuleProperties;
        this.patchModuleProperties = patchModuleProperties;
        simpleModuleName = simpleName;
        fullModuleName = parentName + "/" + simpleModuleName;
        hasIdenticalProperties = compareProperties(baseModuleProperties, patchModuleProperties);
    }

    /**
     * Returns the base module properties.
     *
     * @return the base module properties
     */
    public Map<String, List<String>> getBaseModuleProperties() {
        return baseModuleProperties;
    }

    /**
     * Returns the patch module properties.
     *
     * @return the patch module properties
     */
    public Map<String, List<String>> getPatchModuleProperties() {
        return patchModuleProperties;
    }

    /**
     * Returns the full module name.
     *
     * @return the full module name
     */
    public String getFullModuleName() {
        return fullModuleName;
    }

    /**
     * Returns the simple (short) module name.
     *
     * @return the simple module name
     */
    public String getSimpleModuleName() {
        return simpleModuleName;
    }

    /**
     * Returns the list of submodules.
     *
     * @return the list of submodules
     */
    public List<MergedConfigurationModule> getChildren() {
        return children;
    }

    /**
     * Returns the {@code true} if properties from both base and patch configuration
     * modules are identical.
     *
     * @return the {@code true} if properties from both configuration modules are identical
     */
    public boolean isHasIdenticalProperties() {
        return hasIdenticalProperties;
    }

    /**
     * Adds single child module to the set of children.
     *
     * @param child
     *        the child module.
     */
    public void addChild(MergedConfigurationModule child) {
        children.add(child);
    }

    /**
     * Checks if patch module is present in this merge.
     *
     * @return true if patch module is present.
     */
    public boolean hasPatchModule() {
        return patchModuleProperties != null;
    }

    /**
     * Checks if base module is present in this merge.
     *
     * @return true if base module is present.
     */
    public boolean hasBaseModule() {
        return baseModuleProperties != null;
    }

    /**
     * Compares properties from base and patch modules.
     *
     * @param baseModuleProperties
     *        base module properties.
     * @param patchModuleProperties
     *        patch module properties.
     * @return true, if properties are equal.
     */
    private static boolean compareProperties(Map<String, List<String>> baseModuleProperties,
            Map<String, List<String>> patchModuleProperties) {
        if (baseModuleProperties != null && patchModuleProperties != null) {
            return isSubset(baseModuleProperties, patchModuleProperties)
                    && isSubset(patchModuleProperties, baseModuleProperties);
        }
        else {
            return false;
        }
    }

    /**
     * Checks if properties from one source are a subset for
     * properties from another source.
     *
     * @param properties
     *        subset properties.
     * @param set
     *        set properties.
     * @return true, if subset relation between properties is present.
     */
    private static boolean isSubset(Map<String, List<String>> properties,
            Map<String, List<String>> set) {
        final Iterator<Map.Entry<String, List<String>>> iter = properties.entrySet().iterator();
        boolean result = true;
        while (result && iter.hasNext()) {
            final Map.Entry<String, List<String>> entry = iter.next();
            final List<String> setTokens = set.get(entry.getKey());
            if (setTokens == null) {
                result = false;
                break;
            }
            else {
                for (String token : entry.getValue()) {
                    if (!setTokens.contains(token)) {
                        result = false;
                        break;
                    }
                }
            }
        }
        return result;
    }

}

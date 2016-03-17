////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2016 the original author or authors.
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
import java.util.List;
import java.util.Map;

import com.github.checkstyle.data.MergedConfigurationModule;

/**
 * Merges parsed configurations into
 * one single entity ready for easy output.
 *
 * @author attatrol
 *
 */
final class ConfigurationMerger {

    /**
     * Private ctor, please use merge method.
     */
    private ConfigurationMerger() {

    }

    /**
     * Merges two ConfigurationModule instances with respect to their tree structure.
     *
     * @param baseModule
     *        base ConfigurationModule instance.
     * @param patchModule
     *        parsed ConfigurationModule instance.
     * @param parentName
     *        full name of the parent module.
     * @return merged configuration modules.
     */
    public static MergedConfigurationModule merge(ConfigurationModule baseModule,
            ConfigurationModule patchModule, String parentName) {
        //creation of paired module
        final MergedConfigurationModule doubledModule =
                getMergedConfigurationModule(baseModule, patchModule, parentName);
        //getting silbings
        final List<ConfigurationModule> baseChildren;
        if (baseModule != null) {
            baseChildren = baseModule.getChildren();
        }
        else {
            baseChildren = new ArrayList<>(0);
        }
        final List<ConfigurationModule> patchChildren;
        if (patchModule != null) {
            patchChildren = patchModule.getChildren();
        }
        else {
            patchChildren = new ArrayList<>(0);
        }
        //flags for detection of used patch siblings.
        final boolean[] usedPatchParentChildren = new boolean[patchChildren.size()];
        //processing of base siblings
        for (ConfigurationModule module : baseChildren) {
            final String moduleName = module.getName();
            final ConfigurationModule patchSibling =
                    getSibling(moduleName, patchChildren, usedPatchParentChildren);
            doubledModule.addChild(merge(module, patchSibling, doubledModule.getFullModuleName()));
        }
        //processing of residual patch siblings
        for (int i = 0; i < patchChildren.size(); i++) {
            if (!usedPatchParentChildren[i]) {
                doubledModule.addChild(merge(null, patchChildren.get(i),
                        doubledModule.getFullModuleName()));
            }
        }
        return doubledModule;
    }

    /**
     * Produces new MergedConfigurationModule.
     *
     * @param baseModule
     *        configuration module from base tree.
     * @param patchModule
     *        configuration module from patch tree.
     * @param parentName
     *        full name of the parent module.
     * @return new MergedConfigurationModule instance.
     */
    private static MergedConfigurationModule getMergedConfigurationModule(
            ConfigurationModule baseModule, ConfigurationModule patchModule, String parentName) {
        String simpleName = null;
        final Map<String, List<String>> baseModuleProperties;
        if (baseModule != null) {
            baseModuleProperties = baseModule.getProperties();
            simpleName = baseModule.getName();
        }
        else {
            baseModuleProperties = null;
        }
        final Map<String, List<String>> patchModuleProperties;
        if (patchModule != null) {
            patchModuleProperties = patchModule.getProperties();
            simpleName = patchModule.getName();
        }
        else {
            patchModuleProperties = null;
        }
        return new MergedConfigurationModule(baseModuleProperties,
                patchModuleProperties, simpleName, parentName);
    }

    /**
     * Finds sibiling module by name,
     * raises flag of being used for found result.
     *
     * @param moduleName
     *        name of searched module.
     * @param moduleList
     *        search range.
     * @param useFlags
     *        flag of being used for modules
     * @return result of search, may be null.
     */
    private static ConfigurationModule getSibling(String moduleName,
            List<ConfigurationModule> moduleList, boolean[] useFlags) {
        ConfigurationModule result = null;
        for (int i = 0; i < moduleList.size(); i++) {
            if (!useFlags[i]) {
                final ConfigurationModule module = moduleList.get(i);
                if (module.getName().equals(moduleName)) {
                    result = module;
                    useFlags[i] = true;
                    break;
                }
            }
        }
        return result;
    }

}

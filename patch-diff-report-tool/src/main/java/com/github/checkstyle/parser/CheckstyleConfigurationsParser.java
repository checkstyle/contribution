///////////////////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code and other text files for adherence to a set of rules.
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
///////////////////////////////////////////////////////////////////////////////////////////////

package com.github.checkstyle.parser;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.Iterator;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import com.github.checkstyle.data.MergedConfigurationModule;

/**
 * Implements StAX parser for checkstyle configuration XML files.
 *
 * @author attatrol
 *
 */
public final class CheckstyleConfigurationsParser {

    /**
     * Name for virtual root module.
     */
    private static final String ROOT_MODULE_NAME = "";

    /**
     * String value for "module" tag.
     */
    private static final String MODULE_TAG = "module";

    /**
     * String value for "property" tag.
     */
    private static final String PROPERTY_TAG = "property";

    /**
     * String value for "name" attribute.
     */
    private static final String NAME_ATTR = "name";

    /**
     * String value for "value" attribute.
     */
    private static final String VALUE_ATTR = "value";

    /**
     * String value for "message" tag.
     */
    private static final String MESSAGE_TAG = "message";

    /**
     * String value for "key" attribute.
     */
    private static final String KEY_ATTR = "key";

    /**
     * Private ctor, see parse method.
     */
    private CheckstyleConfigurationsParser() {

    }

    /**
     * Parses both configuration XML files, then merges them into one
     * entity, which is ready for output.
     *
     * @param baseConfigPath
     *        path to the base configuration xml.
     * @param patchConfigPath
     *        path to the patch configuration xml.
     * @return merged configurations.
     * @throws FileNotFoundException
     *         if files not found.
     * @throws XMLStreamException
     *         on internal parser error.
     */
    public static MergedConfigurationModule parse(Path baseConfigPath, Path patchConfigPath)
            throws FileNotFoundException, XMLStreamException {
        final ConfigurationModule baseRoot =
                parseConfiguration(baseConfigPath, ROOT_MODULE_NAME);
        final ConfigurationModule patchRoot =
                parseConfiguration(patchConfigPath, ROOT_MODULE_NAME);
        return ConfigurationMerger.merge(baseRoot, patchRoot, ROOT_MODULE_NAME);
    }

    /**
     * Parses single configuration XML.
     *
     * @param xml
     *        Path to XML configuration.
     * @param rootName
     *        name of the virtual root of module tree.
     * @return root of module tree.
     * @throws FileNotFoundException
     *         if files not found.
     * @throws XMLStreamException
     *         on internal parser error.
     */
    private static ConfigurationModule parseConfiguration(Path xml, String rootName)
            throws FileNotFoundException, XMLStreamException {
        final ConfigurationModule root = new ConfigurationModule(rootName);
        final XMLEventReader reader = StaxUtils.createReader(xml);
        parseModule(reader, root);
        return root;
    }

    /**
     * Recursively processes parsing of the configuration XML file with StAX parser.
     * Configuration XML has recursive tree structure
     * where module tags are nested into other module tags, also
     * duplicates of the same modules may be present in one configuration,
     * so there are no other option but to parse configuration
     * into the same tree structure.
     *
     * @param reader
     *        StAX parser interface.
     * @param parent
     *        parent module tag.
     * @throws XMLStreamException
     *         on internal StAX parser error.
     */
    private static void parseModule(XMLEventReader reader, ConfigurationModule parent)
            throws XMLStreamException {
        while (reader.hasNext()) {
            final XMLEvent event = reader.nextEvent();
            if (event.isStartElement()) {
                final StartElement startElement = event.asStartElement();
                final String startElementName = startElement.getName()
                        .getLocalPart();
                // module tag encounter
                if (startElementName.equals(MODULE_TAG)) {
                    processModuleTag(reader, startElement, parent);
                }
                // property tag encounter
                else if (startElementName.equals(PROPERTY_TAG)) {
                    processPropertyTag(startElement, parent);
                }
                // message tag encounter
                else if (startElementName.equals(MESSAGE_TAG)) {
                    processMessageTag(startElement, parent);
                }
            }
            if (event.isEndElement()) {
                final EndElement endElement = event.asEndElement();
                if (endElement.getName().getLocalPart().equals(MODULE_TAG)) {
                    // return from recursive method
                    break;
                }
            }
        }
    }

    /**
     * Parses single "module" tag.
     *
     * @param reader
     *        StAX parser interface.
     * @param startElement
     *        start element of the tag.
     * @param parent
     *        parent module instance.
     * @throws XMLStreamException
     *         on internal StAX failure.
     */
    private static void processModuleTag(XMLEventReader reader, StartElement startElement,
            ConfigurationModule parent) throws XMLStreamException {
        String childModuleName = null;
        final Iterator<Attribute> attributes = startElement
                .getAttributes();
        while (attributes.hasNext()) {
            final Attribute attribute = attributes.next();
            if (attribute.getName().toString()
                    .equals(NAME_ATTR)) {
                childModuleName = attribute.getValue();
            }
        }
        final ConfigurationModule childModule =
                new ConfigurationModule(childModuleName);
        parseModule(reader, childModule);
        parent.addChild(childModule);
    }

    /**
     * Parses single "property" tag.
     *
     * @param startElement
     *        start element of the tag.
     * @param parent
     *        parent module instance.
     */
    private static void processPropertyTag(StartElement startElement,
            ConfigurationModule parent) {
        String propertyName = null;
        String propertyValue = null;
        final Iterator<Attribute> attributes = startElement
                .getAttributes();
        while (attributes.hasNext()) {
            final Attribute attribute = attributes.next();
            final String attributeName = attribute.getName().toString();
            if (attributeName.equals(NAME_ATTR)) {
                propertyName = attribute.getValue();
            }
            else if (attributeName.equals(VALUE_ATTR)) {
                propertyValue = attribute.getValue();
            }
        }
        parent.addProperty(propertyName, propertyValue);
    }

    /**
     * Parses single "message" tag.
     *
     * @param startElement
     *        start element of the tag.
     * @param parent
     *        parent module instance.
     */
    private static void processMessageTag(StartElement startElement,
            ConfigurationModule parent) {
        String propertyName = null;
        String propertyValue = null;
        final Iterator<Attribute> attributes = startElement
                .getAttributes();
        while (attributes.hasNext()) {
            final Attribute attribute = attributes.next();
            final String attributeName = attribute.getName().toString();
            if (attributeName.equals(KEY_ATTR)) {
                propertyName = attribute.getValue();
            }
            else if (attributeName.equals(VALUE_ATTR)) {
                propertyValue = attribute.getValue();
            }
        }
        parent.addProperty(propertyName, propertyValue);
    }

}

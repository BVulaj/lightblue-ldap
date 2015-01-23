/*
 Copyright 2015 Red Hat, Inc. and/or its affiliates.

 This file is part of lightblue.

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.redhat.lightblue.metadata.ldap.model;

import java.util.HashSet;
import java.util.Set;

import com.redhat.lightblue.common.ldap.LdapFieldNameTranslator;

/**
 * Container for special ldap properties parsed from the metadata.json file.
 *
 * @author dcrissman
 *
 * @see com.redhat.lightblue.metadata.ldap.parser.LdapPropertyParser
 */
public class LdapProperty implements LdapFieldNameTranslator{

    private final Set<FieldAttributeMapping> fieldsToAttributes = new HashSet<FieldAttributeMapping>();

    /**
     * Returns an immutable copy of the internal collection of {@link FieldAttributeMapping}s.
     * @return a collection of {@link FieldAttributeMapping}s.
     */
    public Set<FieldAttributeMapping> getFieldsToAttributes(){
        return new HashSet<FieldAttributeMapping>(fieldsToAttributes);
    }

    @Override
    public String translateFieldName(String fieldName){
        for(FieldAttributeMapping f2a : fieldsToAttributes){
            if(f2a.getFieldName().equalsIgnoreCase(fieldName)){
                return f2a.getAttributeName();
            }
        }
        return fieldName;
    }

    @Override
    public String translateAttributeName(String attributeName){
        for(FieldAttributeMapping f2a : fieldsToAttributes){
            if(f2a.getAttributeName().equalsIgnoreCase(attributeName)){
                return f2a.getFieldName();
            }
        }
        return attributeName;
    }

    /**
     * Adds a {@link FieldAttributeMapping} to this {@link LdapProperty}.
     * @param fieldAttributeMapping - {@link FieldAttributeMapping}
     */
    public void addFieldToAttribute(FieldAttributeMapping fieldAttributeMapping){
        fieldsToAttributes.add(fieldAttributeMapping);
    }

}

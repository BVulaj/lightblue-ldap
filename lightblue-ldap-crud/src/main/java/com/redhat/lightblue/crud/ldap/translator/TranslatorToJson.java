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
package com.redhat.lightblue.crud.ldap.translator;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.redhat.lightblue.metadata.ArrayElement;
import com.redhat.lightblue.metadata.ArrayField;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.FieldCursor;
import com.redhat.lightblue.metadata.FieldTreeNode;
import com.redhat.lightblue.metadata.ObjectArrayElement;
import com.redhat.lightblue.metadata.ObjectField;
import com.redhat.lightblue.metadata.ReferenceField;
import com.redhat.lightblue.metadata.SimpleArrayElement;
import com.redhat.lightblue.metadata.SimpleField;
import com.redhat.lightblue.metadata.Type;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.Path;

/**
 * Defines a class that take a response from a datasource and translates it
 * into something Lightblue can use.
 *
 * @author dcrissman
 *
 * @param <S> - The source type that this {@link TranslatorToJson} converts
 * to json.
 */
public abstract class TranslatorToJson<S> {

    private final JsonNodeFactory factory;
    protected final EntityMetadata entityMetadata;

    public TranslatorToJson(JsonNodeFactory factory, EntityMetadata entityMetadata){
        this.factory = factory;
        this.entityMetadata = entityMetadata;
    }

    protected JsonNode toJson(Type type, Object value){
        return type.toJson(factory, value);
    }

    /**
     * Translates the source to a {@link JsonDoc}.
     * @param source - Object containing the source data.
     * @return {@link JsonDoc}
     */
    public JsonDoc translate(S source){
        FieldCursor cursor = entityMetadata.getFieldCursor();

        if (cursor.firstChild()) {
            ObjectNode node = factory.objectNode();

            iterateOverNodes(source, node, cursor);

            return new JsonDoc(node);
        }

        //TODO: What to do in case of a null value here?
        return null;
    }

    private void iterateOverNodes(S source, ObjectNode targetNode, FieldCursor cursor){
        do {
            appendToJsonNode(source, targetNode, cursor);
        } while(cursor.nextSibling());
    }

    protected void appendToJsonNode(S source, ObjectNode targetNode, FieldCursor fieldCursor){
        FieldTreeNode field = fieldCursor.getCurrentNode();
        String fieldName = field.getName();
        Path path = fieldCursor.getCurrentPath();

        JsonNode newJsonNode = null;
        Object value = getValueFor(source, path);

        if (field instanceof ObjectField) {
            newJsonNode = translate(source, (ObjectField)field, fieldCursor);
        }
        else if(value != null){
            if (field instanceof SimpleField) {
                newJsonNode = translate((SimpleField)field, value);
            }
            else if (field instanceof ArrayField){
                newJsonNode = translate((ArrayField)field, value, fieldCursor);
            }
            else if (field instanceof ReferenceField) {
                newJsonNode = translate((ReferenceField)field, value);
            }
            else{
                throw new UnsupportedOperationException("Unknown Field type: " + field.getClass().getName());
            }
        }

        targetNode.set(fieldName, newJsonNode);
    }

    protected JsonNode translate(S source, ObjectField field, FieldCursor fieldCursor){
        if(!fieldCursor.firstChild()){
            //TODO: Should an exception be thrown here?
            return null;
        }

        ObjectNode node = factory.objectNode();

        iterateOverNodes(source, node, fieldCursor);

        fieldCursor.parent();

        return node;
    }

    protected JsonNode translate(ArrayField field, Object o, FieldCursor fieldCursor){
        if(!fieldCursor.firstChild()){
            //TODO: Should an exception be thrown here?
            return null;
        }

        FieldTreeNode node = fieldCursor.getCurrentNode();

        ArrayElement arrayElement = field.getElement();
        ArrayNode valueNode = factory.arrayNode();

        List<? extends Object> values;
        if (arrayElement instanceof SimpleArrayElement) {
            values = getSimpleArrayValues(o);
        }
        else if(arrayElement instanceof ObjectArrayElement){
            values = getObjectArrayValues(o);
        }
        else{
            throw new UnsupportedOperationException("ArrayElement type is not supported: " + node.getClass().getName());
        }

        for(Object value : values){
            valueNode.add(toJson(node.getType(), value));
        }

        fieldCursor.parent();
        return valueNode;
    }

    protected abstract JsonNode translate(ReferenceField field, Object o);
    protected abstract JsonNode translate(SimpleField field, Object o);

    protected abstract Object getValueFor(S source, Path path);
    protected abstract List<? extends Object> getSimpleArrayValues(Object o);
    protected abstract List<? extends Object> getObjectArrayValues(Object o);

}

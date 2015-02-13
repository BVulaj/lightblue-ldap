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

import com.redhat.lightblue.common.ldap.LightblueUtil;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.Field;
import com.redhat.lightblue.metadata.FieldTreeNode;
import com.redhat.lightblue.metadata.Fields;
import com.redhat.lightblue.metadata.ObjectField;
import com.redhat.lightblue.util.JsonNodeCursor;
import com.redhat.lightblue.util.Path;

public abstract class NonPersistedPredefinedFieldTranslatorFromJson<T> extends TranslatorFromJson<T>{

    public NonPersistedPredefinedFieldTranslatorFromJson(EntityMetadata md) {
        super(md);
    }

    @Override
    protected void translate(JsonNodeCursor cursor, T target){
        Path path = cursor.getCurrentPath();
        FieldTreeNode fieldNode = entityMetadata.resolve(path);
        if(fieldNode instanceof Field){
            String datasourceFieldName = ((Field) fieldNode).getName();

            Fields fields = entityMetadata.getFields();
            if(fieldNode instanceof ObjectField){
                fields = ((ObjectField) fieldNode).getFields();
            }

            if(LightblueUtil.isFieldObjectType(datasourceFieldName)
                    || LightblueUtil.isFieldAnArrayCount(datasourceFieldName, fields)){
                /*
                 * Indicates the field is auto-generated for lightblue purposes. These fields
                 * should not be inserted into LDAP.
                 */
                return;
            }
        }

        super.translate(cursor, target);
    }

    /**
     * The metadata field name may not always match the underlying datasource name
     * for that same field. Overriding this method provides the opportunity for
     * implementations to translate as appropriate. If not overridden, this method
     * will simply return the last section of the {@link Path}.
     * @param path - {@link Path} for the field in question.
     * @return the name of the field as known by the datasource.
     */
    protected String getFieldNameAsKnownByDatasource(Path path){
        return path.getLast();
    }

}

/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.jdbi.util;

import com.intel.dcsg.cpg.io.UUID;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import org.skife.jdbi.v2.StatementContext;

/**
 *
 * @author jbuhacoff
 */
public class UUIDMapper {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UUIDMapper.class);
    
    public UUID getUUID(ResultSet rs, StatementContext sc, String fieldName) throws SQLException {
        // find the column type for the given field name
        ResultSetMetaData meta = rs.getMetaData();
        int index = 0;
        int max = meta.getColumnCount();
        for(int i=1; i<=max; i++) {
            if( fieldName.equalsIgnoreCase(meta.getColumnName(i)))  {
                index = i;
            }
        }
        if( index == 0 ) {
            throw new SQLException("UUID column not found: "+fieldName);
        }
        int type = meta.getColumnType(index); // postgresql uuid  (1111)    mysql  BINARY (-2) .   other mysql names for getColumnTypeName:  mysql: BINARY, VARCHAR, BLOB, BIT, INT, DATE
        if( type == java.sql.Types.BINARY || type == java.sql.Types.VARBINARY ) {
            return UUID.valueOf(rs.getBytes(index));
        }
        if( type == java.sql.Types.CHAR || type == java.sql.Types.VARCHAR ) {
            return UUID.valueOf(rs.getString(index));
        }
        String typeName = meta.getColumnTypeName(index);
        if( "uuid".equalsIgnoreCase(typeName) ) { // postgresql  "uuid" column 
            return UUID.valueOf((java.util.UUID)rs.getObject(index));
        }
        log.debug("Found UUID column at index {} but type is not supported", index);
        return null;
    }
}

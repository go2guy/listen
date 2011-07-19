package com.interact.listen

import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types
import org.hibernate.usertype.UserType

class PersistentFileUri implements UserType {
    private static final SQL_TYPES = [Types.VARCHAR] as int[]

    @Override
    int[] sqlTypes() {
        SQL_TYPES
    }

    @Override
    Class returnedClass() {
        File
    }

    @Override
    boolean equals(Object x, Object y) {
        (x && y && x.equals(y)) || (!x && !y)
    }

    @Override
    public int hashCode(Object x) {
        x.hashCode()
    }

    @Override
    public Object deepCopy(Object value) {
        value
    }

    @Override
    public boolean isMutable() {
        false
    }

    @Override
    Serializable disassemble(Object value) {
        value
    }

    @Override
    Object assemble(Serializable cached, Object owner) {
        cached
    }

    @Override
    Object replace(Object original, Object target, Object owner) {
        original
    }

    @Override
    Object nullSafeGet(ResultSet resultSet, String[] names, Object owner) {
        String path = resultSet.getString(names[0])
        if(!path) {
            return null
        }

        URI uri = new URI(path)
        return new File(uri)
    }

    @Override
    void nullSafeSet(PreparedStatement statement, Object value, int index) {
        statement.setString(index, value?.toURI()?.toString())
    }
}

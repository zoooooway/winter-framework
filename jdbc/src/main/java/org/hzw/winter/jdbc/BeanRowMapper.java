package org.hzw.winter.jdbc;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author hzw
 */
public class BeanRowMapper<T> implements RowMapper<T> {

    Class<T> clazz;
    Map<String, Method> methodMap = new HashMap<>();
    Map<String, Field> fieldMap = new HashMap<>();

    public BeanRowMapper(Class<T> clazz) throws NoSuchMethodException {
        this.clazz = clazz;
        Method[] methods = clazz.getMethods();
        for (Method m : methods) {
            if (m.getParameters().length == 1 && m.getName().startsWith("set")) {
                String label =   Character.toLowerCase(m.getName().charAt(3)) + m.getName().substring(4);
                m.setAccessible(true);
                methodMap.put(label, m);
            }
        }

        Field[] fields = clazz.getFields();
        for (Field f : fields) {
            f.setAccessible(true);
            fieldMap.put(f.getName(), f);
        }
    }

    @Override
    public T mapRow(ResultSet rs, int rowNum) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();

        try {
            T t = clazz.getDeclaredConstructor().newInstance();

            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                String columnName = metaData.getColumnName(i);
                Method method = methodMap.get(columnName);
                if (method != null) {
                    method.invoke(t, rs.getObject(i));
                    continue;
                }

                Field field = fieldMap.get(columnName);
                field.set(t, rs.getObject(i));
            }

            return t;
        } catch (Exception e) {
            throw new SQLException(e);
        }

    }
}

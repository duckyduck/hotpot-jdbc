package com.hotpot.jdbc.core;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ArraySqlParameterSource implements SqlParameterSource {

    private Object[] params;

    private MapSqlParameterSource valueIndex;

    public ArraySqlParameterSource(MapSqlParameterSource valueIndex, Object[] params) {
        this.params = params;
        this.valueIndex = valueIndex;
    }

    @Override
    public boolean hasValue(String paramName) {
        return this.valueIndex.hasValue(paramName);
    }

    @Override
    public Object getValue(String paramName) throws IllegalArgumentException {
        if (params != null && params.length > 0) {
            Object bean = this.valueIndex.getValue(paramName);
            if (bean instanceof Integer) {
                return params[(int) bean];

            }
            for (Object p : params) {
                try {
                    return ((Method) this.valueIndex.getValue(paramName)).invoke(p);
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    @Override
    public int getSqlType(String paramName) {
        return this.valueIndex.getSqlType(paramName);
    }

    @Override
    public String getTypeName(String paramName) {
        return this.valueIndex.getTypeName(paramName);
    }

}

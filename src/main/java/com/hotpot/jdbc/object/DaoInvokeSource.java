package com.hotpot.jdbc.object;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * Created by Anker on 16/7/4.
 */
public class DaoInvokeSource {

    private String sql;

    private Class<?> returnType;

    private MapSqlParameterSource parameter;

    private NamedParameterJdbcTemplate template;

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public Class<?> getReturnType() {
        return returnType;
    }

    public void setReturnType(Class<?> returnType) {
        this.returnType = returnType;
    }

    public MapSqlParameterSource getParameter() {
        return parameter;
    }

    public NamedParameterJdbcTemplate getTemplate() {
        return template;
    }

    public void setTemplate(NamedParameterJdbcTemplate template) {
        this.template = template;
    }

    public void setParameter(MapSqlParameterSource parameter) {
        this.parameter = parameter;
    }

}

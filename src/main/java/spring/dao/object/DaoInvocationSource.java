package spring.dao.object;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

public class DaoInvocationSource {

    private String sql;

    private String[] keys;

    private Class<?> returnType;

    private MapSqlParameterSource parameter;

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String[] getKeys() {
        return keys;
    }

    public void setKeys(String[] keys) {
        this.keys = keys;
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

    public void setParameter(MapSqlParameterSource parameter) {
        this.parameter = parameter;
    }

}

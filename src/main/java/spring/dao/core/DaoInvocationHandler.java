package spring.dao.core;

import org.springframework.aop.support.AopUtils;
import org.springframework.cglib.core.ReflectUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.util.StringUtils;
import spring.dao.annotation.Param;
import spring.dao.annotation.Query;
import spring.dao.annotation.Update;
import spring.dao.object.DaoInvocationSource;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
class DaoInvocationHandler implements InvocationHandler {

    private NamedParameterJdbcTemplate template;

    private Map<Method, DaoInvocationSource> sources = new HashMap<>();

    DaoInvocationHandler(NamedParameterJdbcTemplate template) {
        this.template = template;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (AopUtils.isEqualsMethod(method)) {
            return equals(args[0]);
        }
        if (AopUtils.isHashCodeMethod(method)) {
            return hashCode();
        }

        if (method.isAnnotationPresent(Query.class)) {
            return executeQuery(method, args);
        }
        if (method.isAnnotationPresent(Update.class)) {
            return executeUpdate(method, args);
        }
        return null;
    }

    private Object executeQuery(Method method, Object[] args) {
        DaoInvocationSource source = sources.get(method);
        if (source == null) {
            source = createQuerySource(method, args);
            sources.put(method, source);
        }
        ArraySqlParameterSource parameter = new ArraySqlParameterSource(source.getParameter(), args);
        BeanPropertyRowMapper mapper = new BeanPropertyRowMapper(source.getReturnType());
        if (method.getReturnType().equals(List.class)) {
            return template.query(source.getSql(), parameter, mapper);
        }
        return template.queryForObject(source.getSql(), parameter, mapper);
    }

    private Object executeUpdate(Method method, Object[] args) {
        DaoInvocationSource source = sources.get(method);
        if (source == null) {
            source = createUpdateSource(method, args);
            sources.put(method, source);
        }
        ArraySqlParameterSource parameter = new ArraySqlParameterSource(source.getParameter(), args);
        if (source.getKeys().length > 0) {
            GeneratedKeyHolder holder = new GeneratedKeyHolder();
            template.update(source.getSql(), parameter, holder, source.getKeys());
            return holder.getKeys().get(source.getKeys()[0]);
        }
        if (method.getReturnType().equals(Long.TYPE) || method.getReturnType().equals(Long.class)) {
            GeneratedKeyHolder holder = new GeneratedKeyHolder();
            template.update(source.getSql(), parameter, holder);
            return holder.getKey().longValue();
        }
        return template.update(source.getSql(), parameter);
    }

    private DaoInvocationSource createQuerySource(Method method, Object[] args) {
        Query query = method.getAnnotation(Query.class);
        DaoInvocationSource source = new DaoInvocationSource();
        source.setSql(StringUtils.isEmpty(query.value()) ? query.sql() : query.value());
        source.setReturnType(method.getReturnType().equals(List.class) ? query.type() : method.getReturnType());
        source.setParameter(createParameter(method.getParameterAnnotations(), args));
        return source;
    }

    private DaoInvocationSource createUpdateSource(Method method, Object[] args) {
        Update update = method.getAnnotation(Update.class);
        DaoInvocationSource source = new DaoInvocationSource();
        source.setSql(StringUtils.isEmpty(update.value()) ? update.sql() : update.value());
        source.setParameter(createParameter(method.getParameterAnnotations(), args));
        source.setReturnType(method.getReturnType());
        source.setKeys(update.keys());
        return source;
    }

    private MapSqlParameterSource createParameter(Annotation[][] params, Object[] args) {
        MapSqlParameterSource parameter = new MapSqlParameterSource();
        if (params.length > 0 && params[0].length > 0) {
            for (int i = 0; i < params.length; i++) {
                for (Annotation a : params[i]) {
                    if (a instanceof Param) {
                        parameter.addValue(((Param) a).value(), i);
                    }
                }
            }
        } else {
            for (Object o : args) {
                PropertyDescriptor[] descriptors = ReflectUtils.getBeanGetters(o.getClass());
                for (PropertyDescriptor d : descriptors) {
                    parameter.addValue(d.getName(), d.getReadMethod());
                }
            }
        }
        return parameter;
    }

}

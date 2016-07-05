package com.hotpot.jdbc.core;

import com.hotpot.jdbc.annotation.Dao;
import com.hotpot.jdbc.annotation.Param;
import com.hotpot.jdbc.annotation.Query;
import com.hotpot.jdbc.object.DaoInvokeSource;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.cglib.core.ReflectUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DaoInvocationHandler implements InvocationHandler {

    private static final String DEF_DS_NAME = "dataSource";
    private static final String DEF_JT_NAME = "jdbcTemplate";
    private static final String DEF_NPJT_NAME = "namedParameterJdbcTemplate";

    private DefaultListableBeanFactory factory;
    private Map<Method, DaoInvokeSource> sources = new HashMap<>();

    public DaoInvocationHandler(DefaultListableBeanFactory factory) {
        this.factory = factory;
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
            DaoInvokeSource source = sources.get(method);
            if (source == null) {
                source = new DaoInvokeSource();
                Query query = method.getAnnotation(Query.class);
                source.setSql(!StringUtils.isEmpty(query.value()) ? query.value() : query.sql());
                source.setReturnType(method.getReturnType().equals(List.class) ? query.type() : method.getReturnType());

                Dao dao = method.getDeclaringClass().getAnnotation(Dao.class);
                Annotation[][] annotations = method.getParameterAnnotations();
                MapSqlParameterSource parameter = new MapSqlParameterSource();
                if (annotations.length > 0 && annotations[0].length > 0) {
                    for (int i = 0; i < annotations.length; i++) {
                        for (Annotation a : annotations[i]) {
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
                source.setParameter(parameter);

                Object template;
                if (!StringUtils.isEmpty(dao.value())) {
                    template = factory.getBean(dao.value());
                } else {
                    template = factory.getBean(DEF_NPJT_NAME);
                    if (template == null) {
                        template = factory.getBean(DEF_JT_NAME);
                    }
                    if (template == null) {
                        template = factory.getBean(DEF_DS_NAME);
                    }
                }
                if (template instanceof NamedParameterJdbcTemplate) {
                    source.setTemplate((NamedParameterJdbcTemplate) template);
                } else if (template instanceof JdbcTemplate) {
                    source.setTemplate(new NamedParameterJdbcTemplate((JdbcTemplate) template));
                } else if (template instanceof DataSource) {
                    source.setTemplate(new NamedParameterJdbcTemplate((DataSource) template));
                }

                sources.put(method, source);
            }

            ArraySqlParameterSource parameter = new ArraySqlParameterSource(source.getParameter(), args);
            BeanPropertyRowMapper mapper = new BeanPropertyRowMapper(source.getReturnType());
            if (method.getReturnType().equals(List.class)) {
                return source.getTemplate().query(source.getSql(), parameter, mapper);
            }
            return source.getTemplate().queryForObject(source.getSql(), parameter, mapper);
        }
        return null;
    }

}

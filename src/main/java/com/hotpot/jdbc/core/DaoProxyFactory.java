package com.hotpot.jdbc.core;

import com.hotpot.jdbc.annotation.Dao;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@Configuration
public class DaoProxyFactory {

    @Autowired
    private DefaultListableBeanFactory factory;

    public static final String PROXY = "com.hotpot.jdbc.core.proxy";

    @Bean(name = PROXY)
    public Object proxy() throws ClassNotFoundException {
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false) {
            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                return beanDefinition.getMetadata().isInterface();
            }
        };
        provider.addIncludeFilter(new AnnotationTypeFilter(Dao.class, false));
        String[] names = factory.getBeanNamesForAnnotation(ComponentScan.class);
        List<Class> classes = new ArrayList();
        for (String n : names) {
            ComponentScan scan = ((AnnotatedGenericBeanDefinition) factory.getBeanDefinition(n)).getBeanClass().getAnnotation(ComponentScan.class);
            for (String base : scan.value()) {
                Set<BeanDefinition> beans = provider.findCandidateComponents(base);
                for (BeanDefinition bean : beans) {
                    Class dao = Class.forName(bean.getBeanClassName());
                    classes.add(dao);
                }
            }
        }
        return Proxy.newProxyInstance(DaoProxyFactory.class.getClassLoader(), classes.toArray(new Class[]{}), new DaoInvocationHandler(factory));
    }

}

package com.hotpot.jdbc.core;

import com.hotpot.jdbc.annotation.Dao;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.Set;

public class DaoDefinitionRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry beanDefinitionRegistry) {
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false) {
            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                return beanDefinition.getMetadata().isInterface();
            }
        };
        provider.addIncludeFilter(new AnnotationTypeFilter(Dao.class, false));

        String[] packages = (String[]) annotationMetadata.getAnnotationAttributes(ComponentScan.class.getName()).get("value");

        for (String p : packages) {
            Set<BeanDefinition> beans = provider.findCandidateComponents(p);
            for (BeanDefinition bean : beans) {
                try {
                    Class dao = Class.forName(bean.getBeanClassName());

                    ConstructorArgumentValues values = new ConstructorArgumentValues();
                    values.addGenericArgumentValue(dao);

                    RootBeanDefinition rbd = new RootBeanDefinition();
                    rbd.setTargetType(dao);
                    rbd.setConstructorArgumentValues(values);
                    rbd.setScope(RootBeanDefinition.SCOPE_SINGLETON);
                    rbd.setFactoryBeanName(DaoProxyFactory.class.getName());
                    rbd.setUniqueFactoryMethodName(DaoProxyFactory.FACTORY_METHOD_NAME);

                    beanDefinitionRegistry.registerBeanDefinition(dao.getName(), rbd);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}

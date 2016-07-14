package spring.dao.core;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;
import spring.dao.annotation.Dao;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
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

        Set<String> packages = getPackagesToScan(annotationMetadata);

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
                    rbd.setScope(BeanDefinition.SCOPE_SINGLETON);
                    rbd.setFactoryBeanName(DaoProxyFactory.class.getName());
                    rbd.setUniqueFactoryMethodName(DaoProxyFactory.FACTORY_METHOD_NAME);

                    beanDefinitionRegistry.registerBeanDefinition(dao.getName(), rbd);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

        GenericBeanDefinition gbd = new GenericBeanDefinition();
        gbd.setSynthetic(true);
        gbd.setBeanClass(DaoProxyFactory.class);
        gbd.setScope(BeanDefinition.SCOPE_SINGLETON);
        gbd.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);

        beanDefinitionRegistry.registerBeanDefinition(DaoProxyFactory.class.getName(), gbd);

    }

    private Set<String> getPackagesToScan(AnnotationMetadata metadata) {
        AnnotationAttributes attributes = AnnotationAttributes
                .fromMap(metadata.getAnnotationAttributes(ComponentScan.class.getName()));
        String[] value = attributes.getStringArray("value");
        String[] basePackages = attributes.getStringArray("basePackages");
        Class<?>[] basePackageClasses = attributes.getClassArray("basePackageClasses");
        Set<String> packagesToScan = new LinkedHashSet<String>();
        packagesToScan.addAll(Arrays.asList(value));
        packagesToScan.addAll(Arrays.asList(basePackages));
        for (Class<?> basePackageClass : basePackageClasses) {
            packagesToScan.add(ClassUtils.getPackageName(basePackageClass));
        }
        if (packagesToScan.isEmpty()) {
            return Collections
                    .singleton(ClassUtils.getPackageName(metadata.getClassName()));
        }
        return packagesToScan;
    }

}

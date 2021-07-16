package it.gov.pagopa.tkm.config;

import it.gov.pagopa.tkm.aop.tableresult.AopLoggingTableResult;
import it.gov.pagopa.tkm.entity.tableresult.LoggingBatchResult;
import it.gov.pagopa.tkm.repository.tableresult.BatchResultRepository;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

@Configuration
public class BatchResultConfig implements ImportBeanDefinitionRegistrar {
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        AutoConfigurationPackages.register(registry, BatchResultRepository.class.getPackage().getName());
        AutoConfigurationPackages.register(registry, LoggingBatchResult.class.getPackage().getName());
        register(registry, AopLoggingTableResult.class);
    }

    private void register(BeanDefinitionRegistry registry, Class bean) {
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(AopLoggingTableResult.class).setLazyInit(true);
        registry.registerBeanDefinition(bean.getName(), beanDefinitionBuilder.getBeanDefinition());
    }
}

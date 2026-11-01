package com.team.gestorproyectos.shared.infrastructure.config;

import java.lang.reflect.AnnotatedElement;

import org.springframework.aop.Advisor;
import org.springframework.aop.support.ComposablePointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Role;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.annotation.TransactionAnnotationParser;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttribute;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import com.team.gestorproyectos.shared.application.ServicioAplicacion;
import com.team.gestorproyectos.shared.application.Transaccional;

/**
 * Conecta la capa de aplicación con Spring sin que ella lo sepa: registra como beans las clases
 * con {@link ServicioAplicacion} y abre transacciones donde haya {@link Transaccional}.
 */
@Configuration
@ComponentScan(basePackages = "com.team.gestorproyectos", useDefaultFilters = false,
        includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = ServicioAplicacion.class))
public class ServiciosAplicacionConfig {

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    static Advisor transaccionalAdvisor(BeanFactory beanFactory) {
        // El TransactionManager se busca en el BeanFactory al primer uso, no al arrancar
        TransactionInterceptor interceptor = new TransactionInterceptor();
        interceptor.setTransactionAttributeSource(new AnnotationTransactionAttributeSource(new TransaccionalParser()));
        interceptor.setBeanFactory(beanFactory);
        ComposablePointcut pointcut = new ComposablePointcut(new AnnotationMatchingPointcut(Transaccional.class, true))
                .union(AnnotationMatchingPointcut.forMethodAnnotation(Transaccional.class));
        return new DefaultPointcutAdvisor(pointcut, interceptor);
    }

    /** Traduce {@link Transaccional} al atributo de transacción de Spring (deshace ante RuntimeException). */
    private static final class TransaccionalParser implements TransactionAnnotationParser {

        @Override
        public boolean isCandidateClass(Class<?> targetClass) {
            return true;
        }

        @Override
        public TransactionAttribute parseTransactionAnnotation(AnnotatedElement element) {
            Transaccional transaccional = AnnotatedElementUtils.findMergedAnnotation(element, Transaccional.class);
            if (transaccional == null) {
                return null;
            }
            RuleBasedTransactionAttribute atributo = new RuleBasedTransactionAttribute();
            atributo.setReadOnly(transaccional.soloLectura());
            return atributo;
        }
    }
}

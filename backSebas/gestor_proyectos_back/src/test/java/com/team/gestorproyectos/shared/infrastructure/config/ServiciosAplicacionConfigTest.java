package com.team.gestorproyectos.shared.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.aopalliance.intercept.MethodInterceptor;
import org.junit.jupiter.api.Test;
import org.springframework.aop.Advisor;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.interceptor.TransactionAttribute;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import com.team.gestorproyectos.cliente.application.GestionarClientesService;
import com.team.gestorproyectos.cliente.application.ListarClientesService;
import com.team.gestorproyectos.cliente.domain.model.DatosCliente;
import com.team.gestorproyectos.cliente.domain.port.in.GestionarClientesUseCase;
import com.team.gestorproyectos.cliente.domain.port.in.ListarClientesUseCase;

@SpringBootTest
class ServiciosAplicacionConfigTest {

    @Autowired
    ListarClientesUseCase listar;

    @Autowired
    GestionarClientesUseCase gestionar;

    @Autowired
    Advisor transaccionalAdvisor;

    @Test
    void losServiciosDeAplicacionSonBeansConTransaccion() {
        assertThat(AopUtils.isAopProxy(listar)).isTrue();
        assertThat(AopUtils.isAopProxy(gestionar)).isTrue();
    }

    @Test
    void respetaSoloLectura() throws NoSuchMethodException {
        var origen = ((TransactionInterceptor) (MethodInterceptor) transaccionalAdvisor.getAdvice())
                .getTransactionAttributeSource();

        TransactionAttribute lectura = origen.getTransactionAttribute(
                ListarClientesService.class.getMethod("listar", Long.class), ListarClientesService.class);
        TransactionAttribute escritura = origen.getTransactionAttribute(
                GestionarClientesService.class.getMethod("crear", Long.class, DatosCliente.class),
                GestionarClientesService.class);

        assertThat(lectura.isReadOnly()).isTrue();
        assertThat(escritura.isReadOnly()).isFalse();
        assertThat(escritura.rollbackOn(new RuntimeException())).isTrue();
    }
}

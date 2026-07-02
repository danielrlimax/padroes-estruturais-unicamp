package br.unicamp.padroesestruturais.legacy;

import br.unicamp.padroesestruturais.legacy.domain.FormaPagamento;
import br.unicamp.padroesestruturais.legacy.domain.Pedido;
import br.unicamp.padroesestruturais.legacy.domain.ResultadoCobranca;
import br.unicamp.padroesestruturais.legacy.externo.GatewayIndisponivelException;
import br.unicamp.padroesestruturais.legacy.externo.PaySecureGateway;
import br.unicamp.padroesestruturais.legacy.externo.TransacaoExterna;
import br.unicamp.padroesestruturais.legacy.gateway.GatewayFactory;
import br.unicamp.padroesestruturais.legacy.gateway.GatewayPagamento;
import br.unicamp.padroesestruturais.legacy.gateway.adapter.PaySecureGatewayAdapter;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PaySecureGatewayTest {

    @Test
    void deveAprovarTransacaoDentroDoLimite() throws GatewayIndisponivelException {
        PaySecureGateway gateway = new PaySecureGateway();

        Map<String, Object> dados = new HashMap<>();
        dados.put("orderId", "PED-001");
        dados.put("customerName", "Joao Silva");
        dados.put("amount", 500.0);
        dados.put("currency", "BRL");

        TransacaoExterna transacao = gateway.processarTransacao(dados);

        assertEquals(200, transacao.getCodigoStatus());
        assertNotNull(transacao.getReferenciaExterna());
        assertTrue(transacao.getReferenciaExterna().startsWith("PSEC-"));
        assertEquals(500.0, transacao.getValorProcessado());
        assertEquals("BRL", transacao.getMoeda());
    }

    @Test
    void deveRecusarTransacaoAcimaDoLimite() throws GatewayIndisponivelException {
        PaySecureGateway gateway = new PaySecureGateway();

        Map<String, Object> dados = new HashMap<>();
        dados.put("orderId", "PED-003");
        dados.put("customerName", "Construtora ABC Ltda");
        dados.put("amount", 15000.0);
        dados.put("currency", "BRL");

        TransacaoExterna transacao = gateway.processarTransacao(dados);

        assertEquals(402, transacao.getCodigoStatus());
    }

    @Test
    void deveLancarExcecaoParaValorInvalido() {
        PaySecureGateway gateway = new PaySecureGateway();

        Map<String, Object> dados = new HashMap<>();
        dados.put("orderId", "PED-004");
        dados.put("amount", -10.0);

        assertThrows(GatewayIndisponivelException.class, () -> gateway.processarTransacao(dados));
    }

    @Test
    void deveLancarExcecaoParaDadosIncompletos() {
        PaySecureGateway gateway = new PaySecureGateway();
        Map<String, Object> dados = new HashMap<>();

        assertThrows(GatewayIndisponivelException.class, () -> gateway.processarTransacao(dados));
    }

    @Test
    void adapterDeveAprovarCobrancaViaCartaoCreditoDentroDoLimite() {
        GatewayPagamento adapter = new PaySecureGatewayAdapter();
        Pedido pedido = new Pedido("PED-005", "Ana Lima", "Monitor", 900.0);

        ResultadoCobranca resultado = adapter.processarCobranca(
                pedido,
                900.0,
                FormaPagamento.CARTAO_CREDITO
        );

        assertEquals("PED-005", resultado.getPedidoId());
        assertEquals("APROVADA", resultado.getStatus());
        assertEquals(900.0, resultado.getValorCobrado(), 0.001);
        assertEquals(FormaPagamento.CARTAO_CREDITO, resultado.getFormaPagamento());
        assertNotNull(resultado.getReferencia());
        assertTrue(resultado.getReferencia().startsWith("PSEC-"));
    }

    @Test
    void adapterDeveRecusarCobrancaViaCartaoCreditoAcimaDoLimite() {
        GatewayPagamento adapter = new PaySecureGatewayAdapter();
        Pedido pedido = new Pedido("PED-006", "Empresa ABC", "Servidor", 15000.0);

        ResultadoCobranca resultado = adapter.processarCobranca(
                pedido,
                15000.0,
                FormaPagamento.CARTAO_CREDITO
        );

        assertEquals("RECUSADA", resultado.getStatus());
        assertEquals(15000.0, resultado.getValorCobrado(), 0.001);
        assertEquals(FormaPagamento.CARTAO_CREDITO, resultado.getFormaPagamento());
        assertNotNull(resultado.getReferencia());
        assertTrue(resultado.getReferencia().startsWith("PSEC-"));
    }

    @Test
    void adapterDeveConverterExcecaoDoGatewayExternoEmResultadoRecusado() {
        GatewayPagamento adapter = new PaySecureGatewayAdapter();
        Pedido pedido = new Pedido("PED-007", "Cliente Teste", "Produto invalido", -10.0);

        ResultadoCobranca resultado = adapter.processarCobranca(
                pedido,
                -10.0,
                FormaPagamento.CARTAO_CREDITO
        );

        assertEquals("RECUSADA", resultado.getStatus());
        assertEquals(-10.0, resultado.getValorCobrado(), 0.001);
        assertEquals(FormaPagamento.CARTAO_CREDITO, resultado.getFormaPagamento());
        assertNull(resultado.getReferencia());
    }

    @Test
    void factoryDeveRetornarPaySecureGatewayAdapterParaCartaoCredito() {
        GatewayPagamento gateway = GatewayFactory.obterGateway(FormaPagamento.CARTAO_CREDITO);

        assertInstanceOf(PaySecureGatewayAdapter.class, gateway);
    }
}
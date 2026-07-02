package br.unicamp.padroesestruturais.legacy;

import br.unicamp.padroesestruturais.legacy.domain.FormaPagamento;
import br.unicamp.padroesestruturais.legacy.domain.Pedido;
import br.unicamp.padroesestruturais.legacy.domain.ResultadoCobranca;
import br.unicamp.padroesestruturais.legacy.externo.ChargeRequest;
import br.unicamp.padroesestruturais.legacy.externo.ChargeResponse;
import br.unicamp.padroesestruturais.legacy.externo.ChargeStatus;
import br.unicamp.padroesestruturais.legacy.externo.WalletPaySDK;
import br.unicamp.padroesestruturais.legacy.gateway.GatewayFactory;
import br.unicamp.padroesestruturais.legacy.gateway.GatewayPagamento;
import br.unicamp.padroesestruturais.legacy.gateway.adapter.WalletPayGatewayAdapter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WalletPaySDKTest {

    @Test
    void deveConfirmarCobrancaDentroDoLimite() {
        WalletPaySDK sdk = new WalletPaySDK();

        ChargeResponse resposta = sdk.charge(new ChargeRequest("PED-001", "Joao Silva", 50000));

        assertEquals(ChargeStatus.CONFIRMED, resposta.getStatus());
        assertNotNull(resposta.getWalletTransactionId());
        assertTrue(resposta.getWalletTransactionId().startsWith("WPAY-"));
    }

    @Test
    void deveRecusarCobrancaAcimaDoLimite() {
        WalletPaySDK sdk = new WalletPaySDK();

        ChargeResponse resposta = sdk.charge(new ChargeRequest("PED-003", "Construtora ABC Ltda", 1_500_000));

        assertEquals(ChargeStatus.DECLINED, resposta.getStatus());
    }

    @Test
    void deveFalharParaValorInvalido() {
        WalletPaySDK sdk = new WalletPaySDK();

        ChargeResponse resposta = sdk.charge(new ChargeRequest("PED-004", "Cliente X", 0));

        assertEquals(ChargeStatus.FAILED, resposta.getStatus());
        assertNull(resposta.getWalletTransactionId());
    }

    @Test
    void adapterDeveConfirmarCobrancaViaCarteiraDigitalDentroDoLimite() {
        GatewayPagamento adapter = new WalletPayGatewayAdapter();
        Pedido pedido = new Pedido("PED-005", "Bruno Souza", "Teclado", 500.0);

        ResultadoCobranca resultado = adapter.processarCobranca(
                pedido,
                500.0,
                FormaPagamento.CARTEIRA_DIGITAL
        );

        assertEquals("PED-005", resultado.getPedidoId());
        assertEquals("APROVADA", resultado.getStatus());
        assertEquals(500.0, resultado.getValorCobrado(), 0.001);
        assertEquals(FormaPagamento.CARTEIRA_DIGITAL, resultado.getFormaPagamento());
        assertNotNull(resultado.getReferencia());
        assertTrue(resultado.getReferencia().startsWith("WPAY-"));
    }

    @Test
    void adapterDeveConverterReaisParaCentavosAntesDeEnviarAoSdk() {
        GatewayPagamento adapter = new WalletPayGatewayAdapter();
        Pedido pedido = new Pedido("PED-006", "Cliente Limite", "Compra limite", 10000.01);

        ResultadoCobranca resultado = adapter.processarCobranca(
                pedido,
                10000.01,
                FormaPagamento.CARTEIRA_DIGITAL
        );

        assertEquals("RECUSADA", resultado.getStatus());
        assertEquals(10000.01, resultado.getValorCobrado(), 0.001);
        assertEquals(FormaPagamento.CARTEIRA_DIGITAL, resultado.getFormaPagamento());
        assertNotNull(resultado.getReferencia());
        assertTrue(resultado.getReferencia().startsWith("WPAY-"));
    }

    @Test
    void adapterDeveRecusarCobrancaViaCarteiraDigitalComValorInvalido() {
        GatewayPagamento adapter = new WalletPayGatewayAdapter();
        Pedido pedido = new Pedido("PED-007", "Cliente Zero", "Produto zero", 0.0);

        ResultadoCobranca resultado = adapter.processarCobranca(
                pedido,
                0.0,
                FormaPagamento.CARTEIRA_DIGITAL
        );

        assertEquals("RECUSADA", resultado.getStatus());
        assertEquals(0.0, resultado.getValorCobrado(), 0.001);
        assertEquals(FormaPagamento.CARTEIRA_DIGITAL, resultado.getFormaPagamento());
        assertNull(resultado.getReferencia());
    }

    @Test
    void factoryDeveRetornarWalletPayGatewayAdapterParaCarteiraDigital() {
        GatewayPagamento gateway = GatewayFactory.obterGateway(FormaPagamento.CARTEIRA_DIGITAL);

        assertInstanceOf(WalletPayGatewayAdapter.class, gateway);
    }
}
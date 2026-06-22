package br.unicamp.padroesestruturais.legacy;

import br.unicamp.padroesestruturais.legacy.decorator.DescontoFidelidadeDecorator;
import br.unicamp.padroesestruturais.legacy.decorator.JurosParcelamentoDecorator;
import br.unicamp.padroesestruturais.legacy.decorator.SeguroTransacaoDecorator;
import br.unicamp.padroesestruturais.legacy.decorator.TaxaAntecipacaoRecebiveisDecorator;
import br.unicamp.padroesestruturais.legacy.decorator.TaxaEmissaoNotaFiscalDecorator;
import br.unicamp.padroesestruturais.legacy.decorator.TaxaInternacionalDecorator;
import br.unicamp.padroesestruturais.legacy.decorator.ValorBase;
import br.unicamp.padroesestruturais.legacy.decorator.ValorCobranca;
import br.unicamp.padroesestruturais.legacy.domain.FormaPagamento;
import br.unicamp.padroesestruturais.legacy.domain.Pedido;
import br.unicamp.padroesestruturais.legacy.domain.ResultadoCobranca;
import br.unicamp.padroesestruturais.legacy.service.CobrancaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CobrancaServiceTest {

    private CobrancaService service;
    private Pedido pedido;

    @BeforeEach
    void setUp() {
        service = new CobrancaService();
        pedido = new Pedido("PED-001", "Joao Silva", "Notebook Dell XPS 15", 1000.0);
    }

    @Test
    void deveCobrarViaBoletoSemAjustes() {
        ValorCobranca valorCobranca = new ValorBase(pedido.getValorBase());

        ResultadoCobranca resultado = service.cobrar(pedido, FormaPagamento.BOLETO, valorCobranca);

        assertEquals("APROVADA", resultado.getStatus());
        assertEquals(1000.0, resultado.getValorCobrado(), 0.001);
        assertEquals(FormaPagamento.BOLETO, resultado.getFormaPagamento());
    }

    @Test
    void deveCobrarViaPixSemAjustes() {
        ValorCobranca valorCobranca = new ValorBase(pedido.getValorBase());

        ResultadoCobranca resultado = service.cobrar(pedido, FormaPagamento.PIX, valorCobranca);

        assertEquals("APROVADA", resultado.getStatus());
        assertEquals(FormaPagamento.PIX, resultado.getFormaPagamento());
    }

    @Test
    void deveCobrarViaCartaoCreditoSemAjustes() {
        ValorCobranca valorCobranca = new ValorBase(pedido.getValorBase());

        ResultadoCobranca resultado = service.cobrar(pedido, FormaPagamento.CARTAO_CREDITO, valorCobranca);

        assertEquals("APROVADA", resultado.getStatus());
        assertNotNull(resultado.getReferencia());
        assertTrue(resultado.getReferencia().startsWith("PSEC-"));
    }

    @Test
    void deveRecusarCartaoCreditoParaValorAcimaDoLimite() {
        Pedido pedidoCaro = new Pedido("PED-003", "Construtora ABC Ltda", "Servidor", 15000.0);
        ValorCobranca valorCobranca = new ValorBase(pedidoCaro.getValorBase());

        ResultadoCobranca resultado = service.cobrar(pedidoCaro, FormaPagamento.CARTAO_CREDITO, valorCobranca);

        assertEquals("RECUSADA", resultado.getStatus());
    }

    @Test
    void deveLancarExcecaoQuandoFormaDePagamentoForNula() {
        ValorCobranca valorCobranca = new ValorBase(pedido.getValorBase());

        assertThrows(
                NullPointerException.class,
                () -> service.cobrar(pedido, null, valorCobranca)
        );
    }

    @Test
    void deveLancarExcecaoQuandoPedidoForNulo() {
        ValorCobranca valorCobranca = new ValorBase(1000.0);

        assertThrows(
                NullPointerException.class,
                () -> service.cobrar(null, FormaPagamento.BOLETO, valorCobranca)
        );
    }

    @Test
    void deveLancarExcecaoQuandoValorCobrancaForNulo() {
        assertThrows(
                NullPointerException.class,
                () -> service.cobrar(pedido, FormaPagamento.BOLETO, null)
        );
    }

    @Test
    void naoAplicarNenhumAjusteMantemValorBase() {
        ValorCobranca valorCobranca = new ValorBase(1000.0);

        double valor = service.calcularValorFinal(valorCobranca);

        assertEquals(1000.0, valor, 0.001);
    }

    @Test
    void deveAplicarDescontoDeFidelidade() {
        ValorCobranca valorCobranca = new DescontoFidelidadeDecorator(new ValorBase(1000.0));

        double valor = service.calcularValorFinal(valorCobranca);

        assertEquals(950.0, valor, 0.001);
    }

    @Test
    void deveAplicarJurosDeParcelamento() {
        ValorCobranca valorCobranca = new JurosParcelamentoDecorator(new ValorBase(1000.0));

        double valor = service.calcularValorFinal(valorCobranca);

        assertEquals(1029.9, valor, 0.001);
    }

    @Test
    void deveAplicarTaxaInternacional() {
        ValorCobranca valorCobranca = new TaxaInternacionalDecorator(new ValorBase(1000.0));

        double valor = service.calcularValorFinal(valorCobranca);

        assertEquals(1050.0, valor, 0.001);
    }

    @Test
    void deveAplicarSeguro() {
        ValorCobranca valorCobranca = new SeguroTransacaoDecorator(new ValorBase(1000.0));

        double valor = service.calcularValorFinal(valorCobranca);

        assertEquals(1004.90, valor, 0.001);
    }

    @Test
    void deveAplicarTaxaAntecipacaoRecebiveis() {
        ValorCobranca valorCobranca = new TaxaAntecipacaoRecebiveisDecorator(new ValorBase(1000.0));

        double valor = service.calcularValorFinal(valorCobranca);

        assertEquals(1015.0, valor, 0.001);
    }

    @Test
    void deveAplicarTaxaEmissaoNotaFiscal() {
        ValorCobranca valorCobranca = new TaxaEmissaoNotaFiscalDecorator(new ValorBase(1000.0));

        double valor = service.calcularValorFinal(valorCobranca);

        assertEquals(1002.50, valor, 0.001);
    }

    @Test
    void deveAplicarTodosOsAjustesNaOrdemDefinida() {
        ValorCobranca valorCobranca = new ValorBase(1000.0);

        valorCobranca = new DescontoFidelidadeDecorator(valorCobranca);
        valorCobranca = new JurosParcelamentoDecorator(valorCobranca);
        valorCobranca = new TaxaInternacionalDecorator(valorCobranca);
        valorCobranca = new SeguroTransacaoDecorator(valorCobranca);
        valorCobranca = new TaxaAntecipacaoRecebiveisDecorator(valorCobranca);
        valorCobranca = new TaxaEmissaoNotaFiscalDecorator(valorCobranca);

        double esperado = 1000.0;
        esperado = esperado * 0.95;
        esperado = esperado * 1.0299;
        esperado = esperado * 1.05;
        esperado = esperado + 4.90;
        esperado = esperado * 1.015;
        esperado = esperado + 2.50;

        double valor = service.calcularValorFinal(valorCobranca);

        assertEquals(esperado, valor, 0.001);
    }

    @Test
    void devePermitirComporDecoratorsEmOrdemDiferente() {
        ValorCobranca valorCobranca = new ValorBase(1000.0);

        valorCobranca = new TaxaEmissaoNotaFiscalDecorator(valorCobranca);
        valorCobranca = new SeguroTransacaoDecorator(valorCobranca);
        valorCobranca = new DescontoFidelidadeDecorator(valorCobranca);

        double esperado = 1000.0;
        esperado = esperado + 2.50;
        esperado = esperado + 4.90;
        esperado = esperado * 0.95;

        double valor = service.calcularValorFinal(valorCobranca);

        assertEquals(esperado, valor, 0.001);
    }

    @Test
    void deveCobrarEmLoteParaTodosPedidos() {
        List<Pedido> pedidos = Arrays.asList(
                new Pedido("PED-001", "Joao Silva", "Notebook", 1000.0),
                new Pedido("PED-002", "Maria Santos", "Cadeira", 500.0)
        );

        List<ValorCobranca> valoresCobranca = Arrays.asList(
                new ValorBase(1000.0),
                new ValorBase(500.0)
        );

        List<ResultadoCobranca> resultados = service.cobrarEmLote(
                pedidos,
                FormaPagamento.PIX,
                valoresCobranca
        );

        assertEquals(2, resultados.size());

        for (ResultadoCobranca resultado : resultados) {
            assertEquals("APROVADA", resultado.getStatus());
        }
    }

    @Test
    void cobrancaEmLoteDeveAplicarAjustesATodosPedidos() {
        List<Pedido> pedidos = Arrays.asList(
                new Pedido("PED-001", "Joao Silva", "Notebook", 1000.0),
                new Pedido("PED-002", "Maria Santos", "Cadeira", 2000.0)
        );

        List<ValorCobranca> valoresCobranca = Arrays.asList(
                new DescontoFidelidadeDecorator(new ValorBase(1000.0)),
                new DescontoFidelidadeDecorator(new ValorBase(2000.0))
        );

        List<ResultadoCobranca> resultados = service.cobrarEmLote(
                pedidos,
                FormaPagamento.BOLETO,
                valoresCobranca
        );

        assertEquals(950.0, resultados.get(0).getValorCobrado(), 0.001);
        assertEquals(1900.0, resultados.get(1).getValorCobrado(), 0.001);
    }

    @Test
    void cobrancaEmLoteDeveLancarExcecaoQuandoQuantidadeDeValoresForDiferenteDaQuantidadeDePedidos() {
        List<Pedido> pedidos = Arrays.asList(
                new Pedido("PED-001", "Joao Silva", "Notebook", 1000.0),
                new Pedido("PED-002", "Maria Santos", "Cadeira", 2000.0)
        );

        List<ValorCobranca> valoresCobranca = List.of(
                new ValorBase(1000.0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.cobrarEmLote(pedidos, FormaPagamento.BOLETO, valoresCobranca)
        );
    }
}
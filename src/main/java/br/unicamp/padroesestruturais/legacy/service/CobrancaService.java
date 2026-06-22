package br.unicamp.padroesestruturais.legacy.service;

import br.unicamp.padroesestruturais.legacy.decorator.ValorCobranca;
import br.unicamp.padroesestruturais.legacy.domain.FormaPagamento;
import br.unicamp.padroesestruturais.legacy.domain.Pedido;
import br.unicamp.padroesestruturais.legacy.domain.ResultadoCobranca;
import br.unicamp.padroesestruturais.legacy.externo.GatewayIndisponivelException;
import br.unicamp.padroesestruturais.legacy.externo.PaySecureGateway;
import br.unicamp.padroesestruturais.legacy.externo.TransacaoExterna;
import br.unicamp.padroesestruturais.legacy.gateway.GatewayPagamentoInterno;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CobrancaService {

    public ResultadoCobranca cobrar(Pedido pedido, FormaPagamento forma, ValorCobranca valorCobranca) {
        validarDadosCobranca(pedido, forma, valorCobranca);

        double valorFinal = calcularValorFinal(valorCobranca);

        if (forma == FormaPagamento.BOLETO || forma == FormaPagamento.PIX) {
            GatewayPagamentoInterno gateway = new GatewayPagamentoInterno();
            return gateway.cobrar(pedido.getId(), pedido.getCliente(), valorFinal, forma);
        }

        if (forma == FormaPagamento.CARTAO_CREDITO) {
            return cobrarComCartaoCredito(pedido, forma, valorFinal);
        }

        throw new IllegalArgumentException("Forma de pagamento nao suportada: " + forma);
    }

    public List<ResultadoCobranca> cobrarEmLote(
            List<Pedido> pedidos,
            FormaPagamento forma,
            List<ValorCobranca> valoresCobranca
    ) {
        Objects.requireNonNull(pedidos, "A lista de pedidos nao pode ser nula.");
        Objects.requireNonNull(valoresCobranca, "A lista de valores de cobranca nao pode ser nula.");

        if (pedidos.size() != valoresCobranca.size()) {
            throw new IllegalArgumentException("Cada pedido deve possuir um valor de cobranca correspondente.");
        }

        List<ResultadoCobranca> resultados = new ArrayList<>();

        for (int i = 0; i < pedidos.size(); i++) {
            resultados.add(cobrar(pedidos.get(i), forma, valoresCobranca.get(i)));
        }

        return resultados;
    }

    public double calcularValorFinal(ValorCobranca valorCobranca) {
        Objects.requireNonNull(valorCobranca, "O valor de cobranca nao pode ser nulo.");
        return valorCobranca.calcular();
    }

    private void validarDadosCobranca(Pedido pedido, FormaPagamento forma, ValorCobranca valorCobranca) {
        Objects.requireNonNull(pedido, "O pedido nao pode ser nulo.");
        Objects.requireNonNull(forma, "A forma de pagamento nao pode ser nula.");
        Objects.requireNonNull(valorCobranca, "O valor de cobranca nao pode ser nulo.");
    }

    private ResultadoCobranca cobrarComCartaoCredito(Pedido pedido, FormaPagamento forma, double valorFinal) {
        PaySecureGateway gateway = new PaySecureGateway();

        Map<String, Object> dadosTransacao = new HashMap<>();
        dadosTransacao.put("orderId", pedido.getId());
        dadosTransacao.put("customerName", pedido.getCliente());
        dadosTransacao.put("amount", valorFinal);
        dadosTransacao.put("currency", "BRL");

        try {
            TransacaoExterna transacao = gateway.processarTransacao(dadosTransacao);
            String status = transacao.getCodigoStatus() == 200 ? "APROVADA" : "RECUSADA";

            return new ResultadoCobranca(
                    pedido.getId(),
                    valorFinal,
                    status,
                    transacao.getReferenciaExterna(),
                    forma
            );

        } catch (GatewayIndisponivelException e) {
            return new ResultadoCobranca(
                    pedido.getId(),
                    valorFinal,
                    "RECUSADA",
                    null,
                    forma
            );
        }
    }
}
package br.unicamp.padroesestruturais.legacy.gateway.adapter;

import br.unicamp.padroesestruturais.legacy.domain.FormaPagamento;
import br.unicamp.padroesestruturais.legacy.domain.Pedido;
import br.unicamp.padroesestruturais.legacy.domain.ResultadoCobranca;
import br.unicamp.padroesestruturais.legacy.externo.ChargeRequest;
import br.unicamp.padroesestruturais.legacy.externo.ChargeResponse;
import br.unicamp.padroesestruturais.legacy.externo.ChargeStatus;
import br.unicamp.padroesestruturais.legacy.externo.WalletPaySDK;
import br.unicamp.padroesestruturais.legacy.gateway.GatewayPagamento;

public class WalletPayGatewayAdapter implements GatewayPagamento {

    private final WalletPaySDK walletPaySDK = new WalletPaySDK();

    @Override
    public ResultadoCobranca processarCobranca(Pedido pedido, double valorFinal, FormaPagamento forma) {
        long valorEmCentavos = Math.round(valorFinal * 100);

        ChargeRequest request = new ChargeRequest(
                pedido.getId(),
                pedido.getCliente(),
                valorEmCentavos
        );

        ChargeResponse response = walletPaySDK.charge(request);

        String status = response.getStatus() == ChargeStatus.CONFIRMED
                ? "APROVADA"
                : "RECUSADA";

        return new ResultadoCobranca(
                pedido.getId(),
                valorFinal,
                status,
                response.getWalletTransactionId(),
                forma
        );
    }
}
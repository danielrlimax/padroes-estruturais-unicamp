package br.unicamp.padroesestruturais.legacy.gateway;  

import br.unicamp.padroesestruturais.legacy.domain.FormaPagamento; 
import br.unicamp.padroesestruturais.legacy.gateway.adapter.GatewayPagamentoInternoAdapter ; 
import br.unicamp.padroesestruturais.legacy.gateway.adapter.PaySecureGatewayAdapter; 
import br.unicamp.padroesestruturais.legacy.gateway.adapter.WalletPayGatewayAdapter;  

public class GatewayFactory {      
    public static GatewayPagamento obterGateway(FormaPagamento forma) {         
        return switch (forma) {             
            case BOLETO, PIX -> new GatewayPagamentoInternoAdapter();             
            case CARTAO_CREDITO -> new PaySecureGatewayAdapter();             
            case CARTEIRA_DIGITAL -> new WalletPayGatewayAdapter();             
            default -> throw new IllegalArgumentException("Forma de pagamento nao suportada: " + forma);        
        };     
    }
}

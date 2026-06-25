package br.unicamp.padroesestruturais.legacy.gateway.adapter;

import br.unicamp.padroesestruturais.legacy.domain.FormaPagamento; 
import br.unicamp.padroesestruturais.legacy.domain.Pedido;
import br.unicamp.padroesestruturais.legacy.domain.ResultadoCobranca; 
import br.unicamp.padroesestruturais.legacy.externo.WalletPaySDK; 
import br.unicamp.padroesestruturais.legacy.gateway.GatewayPagamento;  

public class WalletPayGatewayAdapter implements GatewayPagamento {          
    private final WalletPaySDK walletPaySDK = new WalletPaySDK();      
    
    @Override     
    public ResultadoCobranca processarCobranca(Pedido pedido, double valorFinal, FormaPagamento forma) {         
        try {             
            String referencia = walletPaySDK.realizarPagamento(pedido.getId(), valorFinal);             
            return new ResultadoCobranca(pedido.getId(), valorFinal, "APROVADA", referencia, forma);         
        } catch (Exception e) {
            return new ResultadoCobranca(pedido.getId(), valorFinal, "RECUSADA", null, forma);         
        }     
    } 
} 
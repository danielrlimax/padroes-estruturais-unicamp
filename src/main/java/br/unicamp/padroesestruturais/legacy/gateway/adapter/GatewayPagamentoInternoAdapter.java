package br.unicamp.padroesestruturais.legacy.gateway.adapter;  

import br.unicamp.padroesestruturais.legacy.domain.FormaPagamento; 
import br.unicamp.padroesestruturais.legacy.domain.Pedido; 
import br.unicamp.padroesestruturais.legacy.domain.ResultadoCobranca; 
import br.unicamp.padroesestruturais.legacy.gateway.GatewayPagamento; 
import br.unicamp.padroesestruturais.legacy.gateway.GatewayPagamentoInterno;  

public class GatewayPagamentoInternoAdapter implements GatewayPagamento {         

    private final GatewayPagamentoInterno gateway = new GatewayPagamentoInterno();    

    @Override     
    public ResultadoCobranca processarCobranca(Pedido pedido, double valorFinal, FormaPagamento forma) {
        return gateway.cobrar(pedido.getId(), pedido.getCliente(), valorFinal, forma);     
    } 
} 
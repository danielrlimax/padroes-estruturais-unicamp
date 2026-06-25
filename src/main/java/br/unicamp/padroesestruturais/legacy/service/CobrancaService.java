package br.unicamp.padroesestruturais.legacy.service; 

import br.unicamp.padroesestruturais.legacy.domain.FormaPagamento; 
import br.unicamp.padroesestruturais.legacy.domain.Pedido; 
import br.unicamp.padroesestruturais.legacy.domain.ResultadoCobranca; 
import br.unicamp.padroesestruturais.legacy.gateway.GatewayFactory; 
import br.unicamp.padroesestruturais.legacy.gateway.GatewayPagamento; 
import java.util.ArrayList; 
import java.util.List;  

public class CobrancaService {      
    private static final double TAXA_DESCONTO_FIDELIDADE = 0.05;     
    private static final double TAXA_JUROS_PARCELAMENTO = 0.0299;     
    private static final double TAXA_OPERACAO_INTERNACIONAL = 0.05;     
    private static final double VALOR_SEGURO = 4.90;      
    
    public ResultadoCobranca cobrar(Pedido pedido, FormaPagamento forma,                                      
        boolean aplicarDescontoFidelidade,                                      
        boolean aplicarJurosParcelamento,                                     
        boolean aplicarTaxaInternacional,                                      
        boolean aplicarSeguro) {          
        double valorFinal = calcularValorFinal(pedido.getValorBase(), aplicarDescontoFidelidade,                 
        aplicarJurosParcelamento, aplicarTaxaInternacional, aplicarSeguro);          
        GatewayPagamento gateway = GatewayFactory.obterGateway(forma);         
        return gateway.processarCobranca(pedido, valorFinal, forma);     
    }      

    public List<ResultadoCobranca> cobrarEmLote(List<Pedido> pedidos, FormaPagamento forma,                                                   
        boolean aplicarDescontoFidelidade,                                                  
        boolean aplicarJurosParcelamento,                                                   
        boolean aplicarTaxaInternacional,                                                   
        boolean aplicarSeguro) {          
        List<ResultadoCobranca> resultados = new ArrayList<>();         
        GatewayPagamento gateway = GatewayFactory.obterGateway(forma);         
        for (Pedido pedido : pedidos) {             
            double valorFinal = calcularValorFinal(pedido.getValorBase(), aplicarDescontoFidelidade,                     
            aplicarJurosParcelamento, aplicarTaxaInternacional, aplicarSeguro);             
            resultados.add(gateway.processarCobranca(pedido, valorFinal, forma));         
        }         
        return resultados;     
    }     
        
    public double calcularValorFinal(double valorBase,                                       
        boolean aplicarDescontoFidelidade,                                       
        boolean aplicarJurosParcelamento,                                       
        boolean aplicarTaxaInternacional,                                       
        boolean aplicarSeguro) {         
        double valor = valorBase;          
        if (aplicarDescontoFidelidade) {             
            valor = valor - (valor * TAXA_DESCONTO_FIDELIDADE);         
        }         
        if (aplicarJurosParcelamento) {             
            valor = valor + (valor * TAXA_JUROS_PARCELAMENTO);         
        }         
        if (aplicarTaxaInternacional) {             
            valor = valor + (valor * TAXA_OPERACAO_INTERNACIONAL);         
        }         
        if (aplicarSeguro) {             
           valor = valor + VALOR_SEGURO;         
        }          
        return valor;     
    } 
} 
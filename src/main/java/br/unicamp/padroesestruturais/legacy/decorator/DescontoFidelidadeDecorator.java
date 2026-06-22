package br.unicamp.padroesestruturais.legacy.decorator;

public class DescontoFidelidadeDecorator extends AjusteValorDecorator {

    private static final double FATOR_DESCONTO_FIDELIDADE = 0.95;

    public DescontoFidelidadeDecorator(ValorCobranca valorCobranca) {
        super(valorCobranca);
    }

    @Override
    public double calcular() {
        return valorCobranca.calcular() * FATOR_DESCONTO_FIDELIDADE;
    }
}
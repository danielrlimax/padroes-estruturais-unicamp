package br.unicamp.padroesestruturais.legacy.decorator;

public class SeguroTransacaoDecorator extends AjusteValorDecorator {

    private static final double VALOR_SEGURO_TRANSACAO = 4.90;

    public SeguroTransacaoDecorator(ValorCobranca valorCobranca) {
        super(valorCobranca);
    }

    @Override
    public double calcular() {
        return valorCobranca.calcular() + VALOR_SEGURO_TRANSACAO;
    }
}
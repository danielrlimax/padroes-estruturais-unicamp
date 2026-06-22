package br.unicamp.padroesestruturais.legacy.decorator;

public class TaxaAntecipacaoRecebiveisDecorator extends AjusteValorDecorator {

    private static final double TAXA_ANTECIPACAO_RECEBIVEIS = 0.015;

    public TaxaAntecipacaoRecebiveisDecorator(ValorCobranca valorCobranca) {
        super(valorCobranca);
    }

    @Override
    public double calcular() {
        return valorCobranca.calcular() * (1 + TAXA_ANTECIPACAO_RECEBIVEIS);
    }
}
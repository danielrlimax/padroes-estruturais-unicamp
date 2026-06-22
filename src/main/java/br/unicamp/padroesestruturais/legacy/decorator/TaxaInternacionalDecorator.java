package br.unicamp.padroesestruturais.legacy.decorator;

public class TaxaInternacionalDecorator extends AjusteValorDecorator {

    private static final double TAXA_OPERACAO_INTERNACIONAL = 0.05;

    public TaxaInternacionalDecorator(ValorCobranca valorCobranca) {
        super(valorCobranca);
    }

    @Override
    public double calcular() {
        return valorCobranca.calcular() * (1 + TAXA_OPERACAO_INTERNACIONAL);
    }
}
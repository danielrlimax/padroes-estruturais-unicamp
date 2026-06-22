package br.unicamp.padroesestruturais.legacy.decorator;

public class JurosParcelamentoDecorator extends AjusteValorDecorator {

    private static final double TAXA_JUROS_PARCELAMENTO = 0.0299;

    public JurosParcelamentoDecorator(ValorCobranca valorCobranca) {
        super(valorCobranca);
    }

    @Override
    public double calcular() {
        return valorCobranca.calcular() * (1 + TAXA_JUROS_PARCELAMENTO);
    }
}
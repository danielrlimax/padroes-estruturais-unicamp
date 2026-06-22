package br.unicamp.padroesestruturais.legacy.decorator;

import java.util.Objects;

public abstract class AjusteValorDecorator implements ValorCobranca {

    protected final ValorCobranca valorCobranca;

    protected AjusteValorDecorator(ValorCobranca valorCobranca) {
        this.valorCobranca = Objects.requireNonNull(
                valorCobranca,
                "O valor cobrado decorado nao pode ser nulo."
        );
    }
}
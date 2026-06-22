package br.unicamp.padroesestruturais.legacy.decorator;

public class ValorBase implements ValorCobranca{
    private final double valor;

    public ValorBase(double valor){
        if(!Double.isFinite(valor)){
            throw new IllegalArgumentException("O valor base deve ser um numero finito.");
        }

        this.valor = valor;
    }

    @Override
    public double calcular(){
        return valor;
    }
}

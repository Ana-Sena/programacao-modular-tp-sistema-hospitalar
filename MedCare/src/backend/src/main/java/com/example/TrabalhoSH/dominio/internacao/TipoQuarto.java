package com.example.TrabalhoSH.dominio.internacao;

/** Apartamento e UTI são individuais; enfermaria admite capacidade configurável. */
public enum TipoQuarto {
    ENFERMARIA, APARTAMENTO, UTI;

    public void validarCapacidade(int capacidade) {
        if (capacidade <= 0) {
            throw new IllegalArgumentException("Capacidade máxima deve ser positiva.");
        }
        if (this != ENFERMARIA && capacidade != 1) {
            throw new IllegalArgumentException("Apartamento e UTI devem possuir uma única vaga.");
        }
    }
}

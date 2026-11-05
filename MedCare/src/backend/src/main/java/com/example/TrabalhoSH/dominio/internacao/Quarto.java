package com.example.TrabalhoSH.dominio.internacao;

public class Quarto {

    private final String id;
    private final String numero;
    private final int andar;
    private final TipoQuarto tipo;
    private final int capacidadeMaxima;
    private int ocupacaoAtual;

    public Quarto(String id, String numero, int andar, TipoQuarto tipo, int capacidadeMaxima) {
        if (id == null || id.isBlank() || numero == null || numero.isBlank()) {
            throw new IllegalArgumentException("Identificador e número do quarto são obrigatórios.");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("Tipo do quarto é obrigatório.");
        }
        tipo.validarCapacidade(capacidadeMaxima);
        this.id = id.strip();
        this.numero = numero.strip();
        this.andar = andar;
        this.tipo = tipo;
        this.capacidadeMaxima = capacidadeMaxima;
    }

    public boolean possuiVaga() {
        return ocupacaoAtual < capacidadeMaxima;
    }

    // Apenas as operações de internação do mesmo pacote alteram a ocupação.
    void ocupar() {
        if (!possuiVaga()) {
            throw new IllegalStateException("Quarto sem vagas disponíveis.");
        }
        ocupacaoAtual++;
    }

    void liberar() {
        if (ocupacaoAtual == 0) {
            throw new IllegalStateException("O quarto não possui vagas ocupadas para liberar.");
        }
        ocupacaoAtual--;
    }

    public String getId() {
        return id;
    }

    public String getNumero() {
        return numero;
    }

    public int getAndar() {
        return andar;
    }

    public TipoQuarto getTipo() {
        return tipo;
    }

    public int getCapacidadeMaxima() {
        return capacidadeMaxima;
    }

    public int getOcupacaoAtual() {
        return ocupacaoAtual;
    }

    public SituacaoQuarto getSituacao() {
        return possuiVaga() ? SituacaoQuarto.DISPONIVEL : SituacaoQuarto.OCUPADO;
    }
}

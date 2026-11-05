package com.example.TrabalhoSH.api.quarto;

import com.example.TrabalhoSH.dominio.internacao.Quarto;
import com.example.TrabalhoSH.dominio.internacao.SituacaoQuarto;
import com.example.TrabalhoSH.dominio.internacao.TipoQuarto;

public record QuartoResposta(String id, String numero, int andar, TipoQuarto tipo,
                             int capacidadeMaxima, int ocupacaoAtual, SituacaoQuarto situacao) {

    public static QuartoResposta de(Quarto quarto) {
        return new QuartoResposta(quarto.getId(), quarto.getNumero(), quarto.getAndar(),
                quarto.getTipo(), quarto.getCapacidadeMaxima(), quarto.getOcupacaoAtual(), quarto.getSituacao());
    }
}

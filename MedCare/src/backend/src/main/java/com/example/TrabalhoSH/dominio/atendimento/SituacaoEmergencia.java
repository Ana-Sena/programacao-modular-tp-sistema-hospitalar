package com.example.TrabalhoSH.dominio.atendimento;

/** A emergência pode sair da espera diretamente para atendimento, sem confirmação. */
public enum SituacaoEmergencia {
    AGUARDANDO_AVALIACAO,
    EM_ATENDIMENTO,
    CONCLUIDA
}

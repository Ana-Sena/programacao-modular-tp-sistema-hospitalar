package com.example.TrabalhoSH.model;

public class ClinicoGeral extends ProfissionalSaude {

    public ClinicoGeral() {
        super();
    }

    public ClinicoGeral(String nome, String registroProfissional, String telefone, String email) {
        super(nome, registroProfissional, "Clínico Geral", telefone, email);
    }

    public String realizarAvaliacaoInicial(String observacoes) {
        return "Avaliação inicial realizada. Observações: " + observacoes;
    }

    public String encaminharPaciente(String especialidade) {
        return "Paciente encaminhado para atendimento em: " + especialidade;
    }

    public String registrarConsulta(String informacoes) {
        return "Consulta registrada: " + informacoes;
    }
}
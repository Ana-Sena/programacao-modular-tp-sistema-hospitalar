package com.example.TrabalhoSH.model;

public class Especialista extends ProfissionalSaude{

    public Especialista(){
        super();
    }

    public Especialista(String nome, String registroProfissional,String especialidade, String telefone, String email) {
        super(nome, registroProfissional, especialidade, telefone, email);
    }

    public boolean atendeEspecialidade(String especialidadeSolicitada) {
        return getEspecialidade().equalsIgnoreCase(especialidadeSolicitada);
    }

    public String registrarAtendimento(String observacoes) {
        return "Atendimento especializado registrado: " + observacoes;
    }
}

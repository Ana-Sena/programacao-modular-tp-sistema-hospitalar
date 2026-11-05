package com.example.TrabalhoSH.api.paciente;

import com.example.TrabalhoSH.dominio.paciente.Endereco;
import com.example.TrabalhoSH.dominio.paciente.Paciente;
import java.time.LocalDate;

public record PacienteResposta(String id, String nome, String cpf, LocalDate dataNascimento,
                               String telefone, String email, Endereco endereco) {

    public static PacienteResposta de(Paciente paciente) {
        return new PacienteResposta(paciente.getId(), paciente.getNome(), paciente.getCpf(),
                paciente.getDataNascimento(), paciente.getTelefone(), paciente.getEmail(), paciente.getEndereco());
    }
}

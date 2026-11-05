package com.example.TrabalhoSH.dominio.paciente;

import java.time.LocalDate;

/** Cadastro do paciente; a identidade e o CPF não mudam após a criação. */
public class Paciente {

    private final String id;
    private final String cpf;
    private String nome;
    private LocalDate dataNascimento;
    private String telefone;
    private String email;
    private Endereco endereco;

    public Paciente(String id, String nome, String cpf, LocalDate dataNascimento,
                    String telefone, String email, Endereco endereco) {
        this.id = Endereco.exigirTexto(id, "Identificador do paciente");
        this.cpf = validarCpf(cpf);
        atualizar(nome, dataNascimento, telefone, email, endereco);
    }

    /** Valida todos os campos antes de modificar o cadastro. */
    public void atualizar(String nome, LocalDate dataNascimento, String telefone,
                          String email, Endereco endereco) {
        String novoNome = Endereco.exigirTexto(nome, "Nome");
        String novoTelefone = Endereco.exigirTexto(telefone, "Telefone");
        String novoEmail = Endereco.exigirTexto(email, "E-mail");
        if (!novoEmail.matches("[^\\s@]+@[^\\s@.]+(?:\\.[^\\s@.]+)+")) {
            throw new IllegalArgumentException("E-mail inválido.");
        }
        if (dataNascimento == null || dataNascimento.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Data de nascimento é obrigatória e não pode ser futura.");
        }
        if (endereco == null) {
            throw new IllegalArgumentException("Endereço é obrigatório.");
        }
        this.nome = novoNome;
        this.dataNascimento = dataNascimento;
        this.telefone = novoTelefone;
        this.email = novoEmail;
        this.endereco = endereco;
    }

    public static String validarCpf(String valor) {
        String cpf = Endereco.exigirTexto(valor, "CPF");
        if (!cpf.matches("(?:\\d{11}|\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2})")) {
            throw new IllegalArgumentException("CPF inválido.");
        }
        cpf = cpf.replace(".", "").replace("-", "");
        if (cpf.chars().distinct().count() == 1) {
            throw new IllegalArgumentException("CPF inválido.");
        }
        for (int tamanho = 9; tamanho <= 10; tamanho++) {
            int soma = 0;
            for (int i = 0; i < tamanho; i++) {
                soma += (cpf.charAt(i) - '0') * (tamanho + 1 - i);
            }
            int digito = (soma * 10) % 11;
            if (digito == 10) {
                digito = 0;
            }
            if (digito != cpf.charAt(tamanho) - '0') {
                throw new IllegalArgumentException("CPF inválido.");
            }
        }
        return cpf;
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getCpf() {
        return cpf;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public String getTelefone() {
        return telefone;
    }

    public String getEmail() {
        return email;
    }

    public Endereco getEndereco() {
        return endereco;
    }
}

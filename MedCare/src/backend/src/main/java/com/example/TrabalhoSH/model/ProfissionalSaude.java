package model;

public abstract class ProfissionalSaude {

    private String nome;
    private String registroProfissional;
    private String especialidade;
    private String telefone;
    private String email;

    public ProfissionalSaude() {
    }

    public ProfissionalSaude(String nome, String registroProfissional,
                             String especialidade, String telefone, String email) {
        this.nome = nome;
        this.registroProfissional = registroProfissional;
        this.especialidade = especialidade;
        this.telefone = telefone;
        this.email = email;
    }

    // Getters e Setters
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getRegistroProfissional() {
        return registroProfissional;
    }

    public void setRegistroProfissional(String registroProfissional) {
        this.registroProfissional = registroProfissional;
    }

    public String getEspecialidade() {
        return especialidade;
    }

    public void setEspecialidade(String especialidade) {
        this.especialidade = especialidade;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String exibirDados() {
        return "Nome: " + nome +
                "\nRegistro profissional: " + registroProfissional +
                "\nEspecialidade: " + especialidade +
                "\nTelefone: " + telefone +
                "\nE-mail: " + email;
    }
}
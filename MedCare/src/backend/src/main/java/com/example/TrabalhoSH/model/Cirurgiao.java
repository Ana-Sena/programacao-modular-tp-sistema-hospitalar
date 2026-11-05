package model;

public class Cirurgiao extends ProfissionalSaude{
    public Cirurgiao(){
        super();
    }

    public Cirurgiao(String nome, String registroProfissional,String especialidade, String telefone, String email) {
        super(nome, registroProfissional, "Cirurgião", telefone, email);
    }

    public String registrarProcedimento(ProcedimentoCirurgico procedimento) {

        if (procedimento == null) {
            throw new IllegalArgumentException(
                    "O procedimento cirúrgico não pode ser nulo."
            );
        }
        return "Procedimento cirúrgico registrado: " +
                procedimento.getDescricao();
    }
}

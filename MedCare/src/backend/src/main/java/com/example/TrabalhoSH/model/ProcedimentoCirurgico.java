package model;
import java.time.LocalDateTime;
import enums.SituacaoProcedimento;

public class ProcedimentoCirurgico {

    private int id;
    private Paciente paciente;
    private Cirurgiao cirurgiao;
    private String descricao;
    private String tipo;
    private String observacoesPreOperatorias;
    private String observacoesPosOperatorias;
    private LocalDateTime dataHoraPrevista;
    private LocalDateTime dataHoraInicio;
    private LocalDateTime dataHoraFim;
    private SituacaoProcedimento situacao;

    public ProcedimentoCirurgico(int id, Paciente paciente, Cirurgiao cirurgiao, String descricao, String tipo, String observacoesPreOperatorias, String observacoesPosOperatorias, LocalDateTime dataHoraPrevista, LocalDateTime dataHoraInicio, LocalDateTime dataHoraFim) {
        this.id = id;
        this.paciente = paciente;
        this.cirurgiao = cirurgiao;
        this.descricao = descricao;
        this.tipo = tipo;
        this.observacoesPreOperatorias = observacoesPreOperatorias;
        this.observacoesPosOperatorias = observacoesPosOperatorias;
        this.dataHoraPrevista = dataHoraPrevista;
        this.dataHoraInicio = dataHoraInicio;
        this.dataHoraFim = dataHoraFim;
        this.situacao = SituacaoProcedimento.AGENDADO;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public Cirurgiao getCirurgiao() {
        return cirurgiao;
    }

    public void setCirurgiao(Cirurgiao cirurgiao) {
        this.cirurgiao = cirurgiao;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getObservacoesPreOperatorias() {
        return observacoesPreOperatorias;
    }

    public void setObservacoesPreOperatorias(String observacoesPreOperatorias) {
        this.observacoesPreOperatorias = observacoesPreOperatorias;
    }

    public String getObservacoesPosOperatorias() {
        return observacoesPosOperatorias;
    }

    public void setObservacoesPosOperatorias(String observacoesPosOperatorias) {
        this.observacoesPosOperatorias = observacoesPosOperatorias;
    }

    public LocalDateTime getDataHoraPrevista() {
        return dataHoraPrevista;
    }

    public void setDataHoraPrevista(LocalDateTime dataHoraPrevista) {
        this.dataHoraPrevista = dataHoraPrevista;
    }

    public LocalDateTime getDataHoraInicio() {
        return dataHoraInicio;
    }

    public void setDataHoraInicio(LocalDateTime dataHoraInicio) {
        this.dataHoraInicio = dataHoraInicio;
    }

    public LocalDateTime getDataHoraFim() {
        return dataHoraFim;
    }

    public void setDataHoraFim(LocalDateTime dataHoraFim) {
        this.dataHoraFim = dataHoraFim;
    }

    public SituacaoProcedimento getSituacao() {
        return situacao;
    }

    public void setSituacao(SituacaoProcedimento situacao) {
        this.situacao = situacao;
    }

    public void iniciarProcedimento() {
        if (this.situacao != SituacaoProcedimento.AGENDADO) {
            throw new IllegalStateException(
                    "Só é possível iniciar um procedimento que esteja agendado."
            );
        }
        this.situacao = SituacaoProcedimento.EM_ANDAMENTO;
        this.dataHoraInicio = LocalDateTime.now();
    }

    public void concluirProcedimento() {

        if (this.situacao != SituacaoProcedimento.EM_ANDAMENTO) {
            throw new IllegalStateException(
                    "Só é possível concluir um procedimento que esteja em andamento."
            );
        }
        this.situacao = SituacaoProcedimento.CONCLUIDO;
        this.dataHoraFim = LocalDateTime.now();
    }


    public void cancelarProcedimento() {
        if (this.situacao == SituacaoProcedimento.CONCLUIDO ||
                this.situacao == SituacaoProcedimento.CANCELADO) {
            throw new IllegalStateException(
                    "Não é possível cancelar um procedimento concluído ou já cancelado."
            );
        }
        this.situacao = SituacaoProcedimento.CANCELADO;
    }
}

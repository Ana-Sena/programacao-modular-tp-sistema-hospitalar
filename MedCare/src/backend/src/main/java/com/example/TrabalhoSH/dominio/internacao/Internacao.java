package com.example.TrabalhoSH.dominio.internacao;

import com.example.TrabalhoSH.dominio.paciente.Paciente;
import java.time.LocalDate;

/** A entrada ocupa uma vaga e a alta a libera, preservando as associações para consulta. */
public class Internacao {

    private final String id;
    private final Paciente paciente;
    private final String medicoResponsavelId;
    private final Quarto quarto;
    private final LocalDate dataEntrada;
    private final LocalDate dataPrevistaAlta;
    private final String observacoes;
    private LocalDate dataEfetivaAlta;
    private SituacaoInternacao situacao;

    public Internacao(String id, Paciente paciente, String medicoResponsavelId, Quarto quarto,
                      LocalDate dataEntrada, LocalDate dataPrevistaAlta, String observacoes) {
        if (id == null || id.isBlank() || medicoResponsavelId == null || medicoResponsavelId.isBlank()) {
            throw new IllegalArgumentException("Identificador e médico responsável são obrigatórios.");
        }
        if (paciente == null || quarto == null) {
            throw new IllegalArgumentException("Paciente e quarto são obrigatórios.");
        }
        if (dataEntrada == null || dataEntrada.isAfter(LocalDate.now())
                || dataEntrada.isBefore(paciente.getDataNascimento())) {
            throw new IllegalArgumentException("Entrada deve ocorrer entre o nascimento do paciente e hoje.");
        }
        if (dataPrevistaAlta != null && dataPrevistaAlta.isBefore(dataEntrada)) {
            throw new IllegalArgumentException("Previsão de alta não pode ser anterior à entrada.");
        }
        this.id = id.strip();
        this.paciente = paciente;
        // O módulo de profissionais ainda não possui serviço de consulta por identificador.
        this.medicoResponsavelId = medicoResponsavelId.strip();
        this.quarto = quarto;
        this.dataEntrada = dataEntrada;
        this.dataPrevistaAlta = dataPrevistaAlta;
        this.observacoes = observacoes;
        quarto.ocupar();
        this.situacao = SituacaoInternacao.ATIVA;
    }

    public void registrarAlta(LocalDate dataAlta) {
        if (dataAlta == null || dataAlta.isBefore(dataEntrada) || dataAlta.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Alta deve ocorrer entre a entrada e hoje.");
        }
        if (!estaAtiva()) {
            if (dataAlta.equals(dataEfetivaAlta)) {
                return;
            }
            throw new IllegalStateException("A internação já recebeu alta; sua data não pode ser alterada.");
        }
        quarto.liberar();
        dataEfetivaAlta = dataAlta;
        situacao = SituacaoInternacao.ALTA;
    }

    public boolean estaAtiva() { return situacao == SituacaoInternacao.ATIVA; }
    public String getId() { return id; }
    public Paciente getPaciente() { return paciente; }
    public String getMedicoResponsavelId() { return medicoResponsavelId; }
    public Quarto getQuarto() { return quarto; }
    public LocalDate getDataEntrada() { return dataEntrada; }
    public LocalDate getDataPrevistaAlta() { return dataPrevistaAlta; }
    public LocalDate getDataEfetivaAlta() { return dataEfetivaAlta; }
    public String getObservacoes() { return observacoes; }
    public SituacaoInternacao getSituacao() { return situacao; }
}

package com.example.TrabalhoSH.dominio.paciente;

import java.util.Locale;
import java.util.Set;

/** Endereço validado e imutável, compartilhado com o cadastro do paciente. */
public record Endereco(String logradouro, String numero, String complemento, String bairro,
                       String cidade, String uf, String cep) {

    private static final Set<String> UFS = Set.of("AC", "AL", "AP", "AM", "BA", "CE", "DF",
            "ES", "GO", "MA", "MT", "MS", "MG", "PA", "PB", "PR", "PE", "PI", "RJ",
            "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO");

    public Endereco {
        logradouro = exigirTexto(logradouro, "Logradouro");
        numero = exigirTexto(numero, "Número");
        bairro = exigirTexto(bairro, "Bairro");
        cidade = exigirTexto(cidade, "Cidade");
        uf = exigirTexto(uf, "UF").toUpperCase(Locale.ROOT);
        if (!UFS.contains(uf)) {
            throw new IllegalArgumentException("UF inválida.");
        }
        cep = exigirTexto(cep, "CEP");
        if (!cep.matches("\\d{5}-?\\d{3}")) {
            throw new IllegalArgumentException("CEP deve conter oito dígitos.");
        }
        cep = cep.replace("-", "");
        complemento = complemento == null ? "" : complemento.strip();
    }

    public String enderecoCompleto() {
        return logradouro + ", " + numero + (complemento.isEmpty() ? "" : ", " + complemento)
                + " - " + bairro + ", " + cidade + "/" + uf + " - CEP " + cep;
    }

    static String exigirTexto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(campo + " é obrigatório.");
        }
        return valor.strip();
    }
}

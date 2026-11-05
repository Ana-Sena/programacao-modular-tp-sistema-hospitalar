package com.example.TrabalhoSH.api.quarto;

import com.example.TrabalhoSH.dominio.internacao.Quarto;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/** O monitor deste serviço também protege entrada e alta no serviço de internações. */
@Service
public class QuartoService {

    private final Map<String, Quarto> registros = new LinkedHashMap<>();

    public synchronized QuartoResposta cadastrar(CadastroQuarto entrada) {
        if (entrada == null || entrada.andar() == null || entrada.capacidadeMaxima() == null) {
            throw new IllegalArgumentException("Dados do quarto, andar e capacidade máxima são obrigatórios.");
        }
        Quarto quarto = new Quarto(UUID.randomUUID().toString(), entrada.numero(), entrada.andar(),
                entrada.tipo(), entrada.capacidadeMaxima());
        if (registros.values().stream().anyMatch(atual -> atual.getNumero().equalsIgnoreCase(quarto.getNumero()))) {
            throw new IllegalStateException("Já existe quarto com este número.");
        }
        registros.put(quarto.getId(), quarto);
        return QuartoResposta.de(quarto);
    }

    public synchronized List<QuartoResposta> listar() {
        return registros.values().stream().map(QuartoResposta::de).toList();
    }

    public synchronized QuartoResposta buscar(String id) {
        return QuartoResposta.de(obter(id));
    }

    /** Retorna a associação interna, sem aceitar ocupação fornecida pelo cliente. */
    public synchronized Quarto obter(String id) {
        Quarto quarto = registros.get(id);
        if (quarto == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Quarto não encontrado.");
        }
        return quarto;
    }
}

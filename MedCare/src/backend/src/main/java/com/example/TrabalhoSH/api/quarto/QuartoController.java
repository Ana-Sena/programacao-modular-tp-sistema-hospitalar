package com.example.TrabalhoSH.api.quarto;

import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/quartos")
public class QuartoController {

    private final QuartoService service;

    public QuartoController(QuartoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<QuartoResposta> cadastrar(@RequestBody CadastroQuarto entrada) {
        QuartoResposta resposta = service.cadastrar(entrada);
        URI local = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(resposta.id()).toUri();
        return ResponseEntity.created(local).body(resposta);
    }

    @GetMapping
    public List<QuartoResposta> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public QuartoResposta buscar(@PathVariable String id) {
        return service.buscar(id);
    }
}

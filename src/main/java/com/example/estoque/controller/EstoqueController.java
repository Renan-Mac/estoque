package com.example.estoque.controller;

import com.example.estoque.domain.Pedido;
import com.example.estoque.domain.Produto;
import com.example.estoque.exception.ForaDeEstoqueException;
import com.example.estoque.service.ProdutoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/estoque")
public class EstoqueController {

    private final ProdutoService service;

    public EstoqueController(ProdutoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<?> cadastraProduto(@RequestBody Produto produto) {
        if (produto.getDescricao() == null || produto.getDescricao().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Descrição do produto é obrigatória.");
        }

        if (produto.getQtd() <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Quantidade do produto deve ser maior que zero.");
        }

        if (produto.getPreco() <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Preço do produto deve ser maior que zero.");
        }

        service.cadastrarProduto(produto);

        return ResponseEntity.ok(produto);
    }



    @GetMapping
    public ResponseEntity<List<Produto>> listarProdutos() {
        return ResponseEntity.ok().body(service.encontrarTodos());
    }

    @GetMapping("/{nome}")
    public ResponseEntity<Produto> buscaProduto(@PathVariable String nome) {
        return ResponseEntity.ok().body(service.encontrarPorNome(nome));
    }

    @PostMapping("/atualizar")
    public ResponseEntity<String> atualizarEstoque(@RequestBody Pedido pedido) {
        // Verifica se a lista de itens do pedido está vazia
        if (pedido.getItens() == null || pedido.getItens().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Itens do pedido não podem ser vazios.");
        }

        try {
            // Tenta atualizar o estoque
            service.atualizarEstoque(pedido);
        } catch (ForaDeEstoqueException e) {
            // Caso o estoque seja insuficiente
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }

        // Caso a atualização seja bem-sucedida
        return ResponseEntity.ok().body("Estoque Atualizado");
    }
}


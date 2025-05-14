package com.example.estoque.repository;


import com.example.estoque.entity.ProdutoEntity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ProdutoRepositoryTest {


    @Autowired
    private ProdutoRepository produtoRepository;

    private ProdutoEntity produto;
    @BeforeEach
    public void setUp() {
        produtoRepository.deleteAll();

        produto = new ProdutoEntity();
        produto.setNome("ProdTeste");
        produto.setPreco(100.0);
        produtoRepository.save(produto);
    }

    @Test
    void findByNome() {
        ProdutoEntity produtoEncontrado = produtoRepository.findByNome("ProdTeste");
        assertNotNull(produtoEncontrado);
        assertEquals("ProdTeste", produtoEncontrado.getNome());
    }
    @Test
    void givenProdutoNotFound_whenFindByNome_thenReturnNull() {
        String nomeInexistente = "Produto Inexistente";

        ProdutoEntity produtoEncontrado = produtoRepository.findByNome(nomeInexistente);

        assertNull(produtoEncontrado);
    }
}
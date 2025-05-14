package com.example.estoque.service;

import com.example.estoque.domain.ItemPedido;
import com.example.estoque.domain.Pedido;
import com.example.estoque.domain.Produto;
import com.example.estoque.entity.ProdutoEntity;
import com.example.estoque.exception.ForaDeEstoqueException;
import com.example.estoque.repository.ProdutoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProdutoServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @InjectMocks
    private ProdutoService produtoService;

    private Produto produto;
    private ProdutoEntity produtoEntity;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        produto = new Produto("Ovo de codorna", "ovo de codorna novinho", 15.50, 5);
        produtoEntity = new ProdutoEntity(produto);
    }

    @Test
    void cadastrarProduto_ProdutoExistente() {
        when(produtoRepository.findByNome(produto.getNome())).thenReturn(produtoEntity);

        produtoService.cadastrarProduto(produto);

        verify(produtoRepository).save(produtoEntity);
        assertEquals(5, produtoEntity.getQtd());
    }

    @Test
    void cadastrarProduto_ProdutoNovo() {
        when(produtoRepository.findByNome(produto.getNome())).thenReturn(null);

        produtoService.cadastrarProduto(produto);

        verify(produtoRepository).save(any(ProdutoEntity.class));
    }

    @Test
    void encontrarTodos() {
        when(produtoRepository.findAll()).thenReturn(List.of(produtoEntity));

        var produtos = produtoService.encontrarTodos();

        assertNotNull(produtos);
        assertEquals(1, produtos.size());
        assertEquals(produto.getNome(), produtos.get(0).getNome());
    }

    @Test
    void atualizarEstoque_Sucesso() {
        // Arrange
        Pedido pedido = mock(Pedido.class);
        ItemPedido itemPedido = new ItemPedido();
        itemPedido.setQtd(5);
        itemPedido.setId(1L);

        ProdutoEntity produtoEntidade = new ProdutoEntity(produto);
        produtoEntidade.setQtd(10);

        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produtoEntidade));

        when(pedido.getItens()).thenReturn(List.of(itemPedido));

        when(produtoRepository.save(produtoEntidade)).thenReturn(produtoEntidade);

        produtoService.atualizarEstoque(pedido);


        verify(produtoRepository).save(produtoEntidade);
        assertEquals(5, produtoEntidade.getQtd());
    }



    @Test
    void atualizarEstoque_ForaDeEstoque() {
        ProdutoEntity produtoEntidade = new ProdutoEntity(produto);
        Pedido pedido = mock(Pedido.class);
        ItemPedido itemPedido = new ItemPedido();
        itemPedido.setQtd(200);
        itemPedido.setId(1L);

        when(produtoRepository.findById(itemPedido.getId())).thenReturn(Optional.of(produtoEntidade));

        when(produtoRepository.findByNome(produto.getNome())).thenReturn(produtoEntidade);

        when(pedido.getItens()).thenReturn(List.of(itemPedido));

        ForaDeEstoqueException exception = assertThrows(ForaDeEstoqueException.class, () -> {
            produtoService.atualizarEstoque(pedido);
        });
        assertEquals("Produto Ovo de codorna possui apenas: 5 em estoque", exception.getMessage());
    }

    @Test
    void encontrarPorNome() {
        when(produtoRepository.findByNome(produto.getNome())).thenReturn(produtoEntity);

        Produto resultado = produtoService.encontrarPorNome(produto.getNome());

        assertNotNull(resultado);
        assertEquals(produto.getNome(), resultado.getNome());
    }
}

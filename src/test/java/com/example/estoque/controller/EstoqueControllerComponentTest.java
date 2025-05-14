package com.example.estoque.controller;

import com.example.estoque.domain.ItemPedido;
import com.example.estoque.domain.Pedido;
import com.example.estoque.domain.Produto;
import com.example.estoque.exception.ForaDeEstoqueException;
import com.example.estoque.exception.ProdutoNaoEncontradoException;
import com.example.estoque.service.ProdutoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
public class EstoqueControllerComponentTest {

    @MockitoBean
    private ProdutoService service;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void givenValidProduto_whenCadastrar_thenSuccess() throws Exception {
        // Given
        var produto = new Produto();
        produto.setNome("Notebook");
        produto.setDescricao("Notebook i7 16GB RAM");
        produto.setQtd(5);
        produto.setPreco(2500.0);

        // When
        var request = MockMvcRequestBuilders.post("/estoque")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(produto));

        var response = mockMvc.perform(request);

        // Then
        response.andDo(
                MockMvcResultHandlers.print()
        ).andExpect(
                MockMvcResultMatchers.status().isOk()
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.nome").value("Notebook")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.qtd").value(5)
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.preco").value(2500.0)
        );
    }

    @Test
    public void givenInvalidProduto_whenCadastrar_thenBadRequest() throws Exception {
        // Given
        var produtoInvalido = new Produto();
        produtoInvalido.setDescricao("Descrição do produto");
        produtoInvalido.setQtd(0);
        produtoInvalido.setPreco(2500.0);

        // When
        var request = MockMvcRequestBuilders.post("/estoque")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(produtoInvalido));

        var response = mockMvc.perform(request);

        // Then
        response.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string("Quantidade do produto deve ser maior que zero."));
    }


    @Test
    public void givenExistingProdutos_whenListar_thenReturnAllProdutos() throws Exception {
        // Given
        var produto1 = new Produto();
        produto1.setNome("Notebook");
        produto1.setDescricao("Notebook i7 16GB RAM");
        produto1.setQtd(5);
        produto1.setPreco(2500.0);

        var produto2 = new Produto();
        produto2.setNome("Monitor");
        produto2.setDescricao("Monitor 24 polegadas");
        produto2.setQtd(10);
        produto2.setPreco(800.0);

        List<Produto> produtos = Arrays.asList(produto1, produto2);
        when(service.encontrarTodos()).thenReturn(produtos);

        // When
        var request = MockMvcRequestBuilders.get("/estoque")
                .accept(MediaType.APPLICATION_JSON);

        var response = mockMvc.perform(request);

        // Then
        response.andDo(
                MockMvcResultHandlers.print()
        ).andExpect(
                MockMvcResultMatchers.status().isOk()
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$").isArray()
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.length()").value(2)
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$[0].nome").value("Notebook")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$[1].nome").value("Monitor")
        );
    }

    @Test
    public void givenExistingProduto_whenBuscarPorNome_thenReturnProduto() throws Exception {
        // Given
        var produto = new Produto();
        produto.setNome("Notebook");
        produto.setDescricao("Notebook i7 16GB RAM");
        produto.setQtd(5);
        produto.setPreco(2500.0);

        when(service.encontrarPorNome("Notebook")).thenReturn(produto);

        // When
        var request = MockMvcRequestBuilders.get("/estoque/Notebook")
                .accept(MediaType.APPLICATION_JSON);

        var response = mockMvc.perform(request);

        // Then
        response.andDo(
                MockMvcResultHandlers.print()
        ).andExpect(
                MockMvcResultMatchers.status().isOk()
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.nome").value("Notebook")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.qtd").value(5)
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.preco").value(2500.0)
        );
    }


    @Test
    public void givenValidPedido_whenAtualizarEstoque_thenSuccess() throws Exception {
        // Given
        var pedido = new Pedido();
        var item1 = new ItemPedido();
        item1.setId(1L);
        item1.setQtd(2);

        var item2 = new ItemPedido();
        item2.setId(2L);
        item2.setQtd(1);

        pedido.setItens(Arrays.asList(item1, item2));

        // When
        var request = MockMvcRequestBuilders.post("/estoque/atualizar")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pedido));

        var response = mockMvc.perform(request);

        // Then
        response.andDo(
                MockMvcResultHandlers.print()
        ).andExpect(
                MockMvcResultMatchers.status().isOk()
        ).andExpect(
                MockMvcResultMatchers.content().string("Estoque Atualizado")
        );
    }

    @Test
    public void givenPedidoWithProdutoOutOfStock_whenAtualizarEstoque_thenBadRequest() throws Exception {
        // Given
        var pedido = new Pedido();
        var item1 = new ItemPedido();
        item1.setId(1L);
        item1.setQtd(20);

        pedido.setItens(Arrays.asList(item1));

        doThrow(new ForaDeEstoqueException("Produto fora de estoque")).when(service).atualizarEstoque(any(Pedido.class));

        // When
        var request = MockMvcRequestBuilders.post("/estoque/atualizar")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pedido));

        var response = mockMvc.perform(request);

        // Then
        response.andDo(
                MockMvcResultHandlers.print()
        ).andExpect(
                MockMvcResultMatchers.status().isBadRequest()
        ).andExpect(
                MockMvcResultMatchers.content().string("Produto fora de estoque")
        );
    }

    @Test
    public void givenInvalidPedido_whenAtualizarEstoque_thenBadRequest() throws Exception {
        // Given
        var pedidoInvalido = new Pedido();
        pedidoInvalido.setItens(new ArrayList<>());

        // When
        var request = MockMvcRequestBuilders.post("/estoque/atualizar")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pedidoInvalido));

        var response = mockMvc.perform(request);

        // Then
        response.andDo(
                MockMvcResultHandlers.print()
        ).andExpect(
                MockMvcResultMatchers.status().isBadRequest()
        );
    }
}
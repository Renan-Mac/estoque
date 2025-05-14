package com.example.estoque.service;

import com.example.estoque.domain.Pedido;
import com.example.estoque.domain.Produto;
import com.example.estoque.entity.ProdutoEntity;
import com.example.estoque.exception.ForaDeEstoqueException;
import com.example.estoque.repository.ProdutoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProdutoService {

    ProdutoRepository repository;

    public ProdutoService(ProdutoRepository repository) {
        this.repository = repository;
    }

    public void cadastrarProduto(Produto produto){

        ProdutoEntity estoque = repository.findByNome(produto.getNome());
        if(estoque != null){
            estoque.setQtd(produto.getQtd());
            repository.save(estoque);
        }else {
            repository.save(new ProdutoEntity(produto));
        }
    }

    public List<Produto> encontrarTodos(){
        return repository
                .findAll()
                .stream()
                .map(Produto::new)
                .collect(Collectors.toList());
    }

    public void atualizarEstoque(Pedido pedido) {
        pedido.getItens().forEach(item -> {
            ProdutoEntity produto = repository.findById(item.getId())
                    .orElseThrow(() -> new ForaDeEstoqueException("Produto com ID " + item.getId() + " não encontrado"));

            if (produto.getQtd() < item.getQtd()) {
                throw new ForaDeEstoqueException(
                        "Produto " + produto.getNome() + " possui apenas: " + produto.getQtd() + " em estoque");
            }

            produto.setQtd(produto.getQtd() - item.getQtd());
            repository.save(produto);
        });
    }


    public Produto encontrarPorNome(String nome) {
        return new Produto(repository.findByNome(nome));
    }
}

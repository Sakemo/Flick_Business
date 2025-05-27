// src/pages/ProdutosPage.tsx
import React, { useState, useEffect, useCallback } from 'react';
import { LuPlus, LuX } from 'react-icons/lu';
import { getProdutos, deleteProduto } from '../services/produtoService';
import { getCategorias } from '../services/categoriaService';
import { ProdutoResponse, CategoriaResponse } from '../types/domain';
import ProdutosTable from '../components/produtos/ProdutosTable'; // Criaremos depois
import ProdutosFormModal from '../components/produtos/ProdutosFormModal'; // Criaremos depois
import Button from '../components/ui/Button';
import Select from '../components/ui/Select'; // Usaremos para filtro
import Card from '../components/ui/Card'; // Para envolver a tabela/filtros
import clsx from 'clsx';
import ProdutoDetalhesDrawer from '../components/produtos/ProdutoDetalhesDrawer';

const ProdutosPage: React.FC = () => {
  // --- Estados ---
  const [produtos, setProdutos] = useState<ProdutoResponse[]>([]);
  const [categorias, setCategorias] = useState<CategoriaResponse[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [filtroCategoriaId, setFiltroCategoriaId] = useState<string>(''); // Usar string para o <select>
  const [isModalOpen, setIsModalOpen] = useState<boolean>(false);
  const [produtoParaEditar, setProdutoParaEditar] = useState<ProdutoResponse | null>(null);

  // --- Funções de Busca ---
  const fetchProdutos = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const params = filtroCategoriaId ? { categoriaId: parseInt(filtroCategoriaId) } : {};
      const data = await getProdutos(params);
      console.log('LOG: PRODUTOS - ', JSON.stringify(data, null, 2));
      setProdutos(data);
    } catch (err) {
      setError('Falha ao carregar produtos.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  }, [filtroCategoriaId]);

  const fetchCategorias = useCallback(async () => {
    try {
      const data = await getCategorias();
      setCategorias(data);
    } catch (err) {
      console.error('Falha ao carregar categorias:', err);
      // Pode setar um erro específico para categorias ou apenas logar
    }
  }, []);

  // --- Efeitos ---
  useEffect(() => {
    fetchCategorias();
  }, [fetchCategorias]); // Busca categorias ao montar

  useEffect(() => {
    fetchProdutos();
  }, [fetchProdutos]); // Busca produtos ao montar e quando o filtro muda

  // --- Handlers de Ação ---
  const handleOpenAddModal = () => {
    setProdutoParaEditar(null); // Garante que é modo de adição
    setIsModalOpen(true);
  };

  const handleOpenEditModal = (produto: ProdutoResponse) => {
    setProdutoParaEditar(produto);
    setIsModalOpen(true);
  };

  const handleCloseModal = () => {
    setIsModalOpen(false);
    setProdutoParaEditar(null); // Limpa seleção ao fechar
  };

  const handleSaveSuccess = () => {
    handleCloseModal();
    fetchProdutos(); // Recarrega a lista após salvar
    // TODO: Adicionar notificação de sucesso (toast)
  };

  const handleDelete = async (id: number) => {
    // Simples confirmação, idealmente usar um modal de confirmação
    if (window.confirm('Tem certeza que deseja deletar este produto?')) {
      try {
        setLoading(true); // Pode ter um loading específico para delete
        await deleteProduto(id);
        fetchProdutos(); // Recarrega a lista
        // TODO: Adicionar notificação de sucesso (toast)
      } catch (err) {
        setError('Falha ao deletar produto.');
        console.error(err);
        // TODO: Adicionar notificação de erro (toast)
      } finally {
        setLoading(false);
      }
    }
  };

  const handleFilterChange = (event: React.ChangeEvent<HTMLSelectElement>) => {
    setFiltroCategoriaId(event.target.value);
  };

  const clearFilter = () => {
    setFiltroCategoriaId('');
  };

  const [produtoSelecionadoDetalhes, setProdutoSelecionadoDetalhes] =
    useState<ProdutoResponse | null>(null);

  const handleVerDetalhesProduto = (produto: ProdutoResponse) => {
    if (produtoSelecionadoDetalhes && produtoSelecionadoDetalhes.id === produto.id) {
      setProdutoSelecionadoDetalhes(null);
    } else {
      setProdutoSelecionadoDetalhes(produto);
    }
  };

  const handleFecharDetalhes = () => {
    setProdutoSelecionadoDetalhes(null);
  };

  // --- Renderização ---
  return (
    <div>
      <div className="flex justify-between items-center mb-8">
        <h1 className="text-2xl font-bold text-text-primary dark:text-white">
          Gerenciamento de Produtos
        </h1>
        <Button className="p-4" onClick={handleOpenAddModal} iconLeft={<LuPlus className="mr-1" />}>
          Adicionar Produto
        </Button>
      </div>

      <Card className="mb-6" padding="md">
        <div className="flex flex-col sm:flex-row gap-4 items-center">
          <div className="flex-grow">
            <Select
              id="filtroCategoria"
              label="Filtrar por Categoria"
              value={filtroCategoriaId}
              onChange={handleFilterChange}
              disabled={loading}
            >
              <option value="">Todas as Categorias</option>
              {categorias.map((cat) => (
                <option key={cat.id} value={cat.id.toString()}>
                  {cat.nome}
                </option>
              ))}
            </Select>
          </div>
          {filtroCategoriaId && (
            <Button
              variant="ghost"
              size="sm"
              onClick={clearFilter}
              iconLeft={<LuX />}
              className="mt-4 sm:mt-0"
            >
              Limpar Filtro
            </Button>
          )}
        </div>
      </Card>

      <div className="flex flex-col lg:flex-row">
        <div
          className={clsx(
            'transition-all duration-300 ease-in-out',
            produtoSelecionadoDetalhes ? 'lg:w-2/3 mr-4' : 'lg:w-full'
          )}
        >
          {/* Conteúdo Principal: Tabela ou Mensagens */}
          {loading && <p className="p-6 text-center text-text-secondary">Carregando produtos...</p>}
          {error && <p className="p-6 text-center text-red-500">{error}</p>}
          {!loading && !error && (
            <ProdutosTable
              produtos={produtos}
              onEdit={handleOpenEditModal}
              onDelete={handleDelete}
              onRowClick={handleVerDetalhesProduto}
              selectedRowId={produtoSelecionadoDetalhes?.id}
            />
          )}
        </div>

        <div
          className={clsx(
            'transition-all duration-300 ease-in-out mt-6 lg:mt-0',
            produtoSelecionadoDetalhes
              ? 'lg:w-1/3 opacity-100'
              : 'lg-w-0 opacity-0 lg:overflow-hidden'
          )}
        >
          {produtoSelecionadoDetalhes && (
            <ProdutoDetalhesDrawer
              produto={produtoSelecionadoDetalhes}
              onClose={handleFecharDetalhes}
              onEdit={handleOpenEditModal}
            />
          )}
        </div>
      </div>

      {/* Modal de Formulário (renderizado condicionalmente) */}
      {isModalOpen && (
        <ProdutosFormModal
          isOpen={isModalOpen}
          onClose={handleCloseModal}
          onSaveSuccess={handleSaveSuccess}
          produtoInicial={produtoParaEditar}
        />
      )}
    </div>
  );
};

export default ProdutosPage;

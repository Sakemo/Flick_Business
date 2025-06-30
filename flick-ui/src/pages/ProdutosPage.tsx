/* eslint-disable @typescript-eslint/no-explicit-any */
// src/pages/ProdutosPage.tsx
//TODO: FILTRO DE APENAS ATIVOS
import React, { useState, useEffect, useCallback } from 'react';
import { LuPlus, LuSearch, LuX } from 'react-icons/lu';
import { getProdutos, deleteProduto, deleteProdutoFisicamente } from '../services/produtoService';
import { getCategorias } from '../services/categoriaService';
import { ProdutoResponse, CategoriaResponse } from '../types/domain';
import ProdutosTable from '../components/produtos/ProdutosTable'; // Criaremos depois
import ProdutosFormModal from '../components/produtos/ProdutosFormModal'; // Criaremos depois
import Button from '../components/ui/Button';
import Select from '../components/ui/Select'; // Usaremos para filtro
import Card from '../components/ui/Card'; // Para envolver a tabela/filtros
import clsx from 'clsx';
import ProdutoDetalhesDrawer from '../components/produtos/ProdutoDetalhesDrawer';
import { useTranslation } from 'react-i18next';
import Input from '../components/ui/Input';


const ProdutosPage: React.FC = () => {
  // --- Estados ---
  const [produtos, setProdutos] = useState<ProdutoResponse[]>([]);
  const [categorias, setCategorias] = useState<CategoriaResponse[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [filtroCategoriaId, setFiltroCategoriaId] = useState<string>(''); // Usar string para o <select>
  const [isModalOpen, setIsModalOpen] = useState<boolean>(false);
  const [produtoParaEditar, setProdutoParaEditar] = useState<ProdutoResponse | null>(null);

  const [filtroNome, setFiltroNome] = useState<string>('');

  const { t } = useTranslation();

  // --- Funções de Busca ---
  const fetchProdutos = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const params: { categoriaId?: number; nome?: string } = {};
      if (filtroCategoriaId) params.categoriaId = parseInt(filtroCategoriaId);
      if (filtroNome.trim() !== '') params.nome = filtroNome.trim();
      const data = await getProdutos(params);
      console.log('LOG: PRODUTOS - ', JSON.stringify(data, null, 2));
      setProdutos(data);
    } catch (err) {
      setError('Falha ao carregar produtos.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  }, [filtroCategoriaId, filtroNome]);

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

  //TODO: Modal de confirmação
  const handleDelete = async (id: number, nomeProduto: string, status: boolean) => {
    const action = status ? t('siteFeedback.inactive') : t('siteFeedback.active');
    if (
      window.confirm(
        t('siteFeedback.confirmDelete') +
          `\n${t('userActions.status')}: ${action}\n${t('produtos.objectName')}: "${nomeProduto}"`
      )
    ) {
      try {
        setLoading(true);
        await deleteProduto(id);
        fetchProdutos();
        // TODO: Adicionar notificação de sucesso (toast)
      } catch (err) {
        setError(t('siteFeedback.deleteError'));
        console.error(err);
        // TODO: Adicionar notificação de erro (toast)
      } finally {
        setLoading(false);
      }
    }
  };

  const handleDeletarFisicamente = async (id: number, nomeProduto: string) => {
    if (
      window.confirm(
        `ATENÇÃO: ${t('siteFeedback.confirmDelete')} ${t('produtos.objectName')}: "${nomeProduto}"\n${t('siteFeedback.deleteError')}`
      )
    ) {
      try {
        setLoading(true);
        await deleteProdutoFisicamente(id);
        fetchProdutos();
        setProdutoSelecionadoDetalhes(null);
      } catch (err: any) {
        const errorMsg =
          err.response?.data?.message || 'Falha ao deletar permanentemente o produto';
        setError(errorMsg);
        console.error(err);
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
    setFiltroNome('');
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
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 items-end">
          <Input
            label={t('userActions.searchBy') + ' ' + t('produtos.objectName')}
            placeholder={t('userActions.searchBy') + '...'}
            value={filtroNome}
            onChange={(e) => setFiltroNome(e.target.value)}
            iconLeft={<LuSearch />}
          />
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
            produtoSelecionadoDetalhes ? 'lg:w-2/3 lg:mr-4' : 'lg:w-full'
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
              onDeletePerm={handleDeletarFisicamente}
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

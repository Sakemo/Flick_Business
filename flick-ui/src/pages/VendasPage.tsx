import { useCallback, useEffect, useState } from 'react';
import { FormaPagamento, VendaResponse } from '../types/domain';
import { ClienteResponse } from '../types/domain';
import { GetVendasParams, deleteVendaFisicamente, getVendas } from '../services/vendaService';
import { getClientes } from '../services/clienteService';
import Button from '../components/ui/Button';
import { LuCalendarDays, LuCalendarX, LuPlus, LuX } from 'react-icons/lu';
import Card from '../components/ui/Card';
import Input from '../components/ui/Input';
import Select from '../components/ui/Select';
import VendasTable from '../components/vendas/VendasTable';
import VendaFormModal from '../components/vendas/VendaFormModal';
import VendaDetalhesModal from '../components/vendas/VendaDetalhesModal';
import { format } from 'date-fns';
import AutoCompleteInput from '../components/common/AutoCompleteInput';
import { getProdutos as fetchAllProdutos } from '../services/produtoService';
import { AxiosError } from 'axios';
import Pagination from '../components/common/Pagination';

const VendasPage: React.FC = () => {
  const [vendas, setVendas] = useState<VendaResponse[]>([]);
  const [clientes, setClientes] = useState<ClienteResponse[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const [currentPage, setCurrentPage] = useState<number>(0);
  const [totalPages, setTotalPages] = useState<number>(0);

  const [isNovaVendaModalOpen, setIsNovaVendaModalOpen] = useState<boolean>(false);
  const [vendaSelecionadaDetalhes, setVendasSelecionadaDetalhes] = useState<VendaResponse | null>(
    null
  );

  const [filtroDataInicio, setFiltroDataInicio] = useState<string>('');
  const [filtroDataFim, setFiltroDataFim] = useState<string>('');
  const [filtroClienteId, setFiltroClienteId] = useState<string>('');
  const [filtroFormaPagamento, setFiltroFormaPagamento] = useState<string>('');
  const [filtroHojeAtivo, setFiltroHojeAtivo] = useState<boolean>(false);

  const [filtroProduto, setFiltroProduto] = useState<{ value: number; label: string } | null>(null);
  const [todosOsProdutos, setTodosOsProdutos] = useState<{ value: number; label: string }[]>([]);

  const opcoesOrdenacaoVendas = [
    { value: 'dataVenda,desc', label: 'Mais Recentes' },
    { value: 'dataVenda,asc', label: 'Mais Antigo' },
    { value: 'valorTotal,desc', label: 'Maior Valor' },
    { value: 'valorTotal,asc', label: 'Menor Valor' },
    { value: 'cliente.nome,asc', label: 'Cliente (A-Z)' },
    { value: 'cliente.nome,desc', label: 'Cliente (Z-A)' },
  ];

  const [ordemVendas, setOrdemVendas] = useState<string>(opcoesOrdenacaoVendas[0].value);

  const fetchVendas = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const params: GetVendasParams = { page: currentPage, size: 8 };
      if (filtroDataInicio) params.inicio = `${filtroDataInicio}T00:00:00`;
      if (filtroDataFim) params.fim = `${filtroDataFim}T23:59:59`;
      if (filtroClienteId) params.clienteId = parseInt(filtroClienteId);
      if (filtroFormaPagamento) params.formaPagamento = filtroFormaPagamento;
      if (filtroProduto) params.produtoId = filtroProduto.value;
      if (ordemVendas) params.orderBy = ordemVendas;

      const pageResponse = await getVendas(params);
      setVendas(pageResponse.content);
      setTotalPages(pageResponse.totalPages);
    } catch (err) {
      setError('Erro ao buscar vendas');
      console.error('Erro ao buscar vendas:', err);
    } finally {
      setLoading(false);
    }
  }, [
    currentPage,
    filtroDataInicio,
    filtroDataFim,
    filtroClienteId,
    filtroFormaPagamento,
    filtroProduto,
    ordemVendas,
  ]);

  const fetchClientesParaFiltro = useCallback(async () => {
    try {
      const data = await getClientes({ apenasAtivos: true });
      setClientes(data);
    } catch (err) {
      console.error('Erro ao buscar clientes:', err);
    }
  }, []);

  const fetchProdutosParaAutoComplete = useCallback(async () => {
    try {
      const produtosData = await fetchAllProdutos();
      setTodosOsProdutos(
        produtosData.map((p) => ({ value: p.id, label: `${p.nome}(ID: ${p.id})` }))
      );
    } catch (error) {
      console.error('Erro ao buscar produtos para autocomplete: ', error);
    }
  }, []);

  useEffect(() => {
    fetchClientesParaFiltro();
    fetchProdutosParaAutoComplete();
  }, [fetchClientesParaFiltro, fetchProdutosParaAutoComplete]);

  useEffect(() => {
    fetchVendas();
  }, [fetchVendas]);

  const handleOpenNovalVendaModal = () => setIsNovaVendaModalOpen(true);
  const handleCloseNovaVendaModal = () => setIsNovaVendaModalOpen(false);
  const handleNovaVendaSuccess = () => {
    handleCloseNovaVendaModal();
    fetchVendas();
    //TODO: Toast
  };

  const handleVerDetalhesVenda = (venda: VendaResponse) => {
    setVendasSelecionadaDetalhes(venda);
  };

  const handleFecharDetalhesVenda = () => {
    setVendasSelecionadaDetalhes(null);
  };

  const handleToggleFiltroHoje = () => {
    if (filtroHojeAtivo) {
      setFiltroDataInicio('');
      setFiltroDataFim('');
      setFiltroHojeAtivo(false);
    } else {
      const hoje = new Date();
      const hojeFormatado = format(hoje, 'yyyy-MM-dd');

      setFiltroDataInicio(hojeFormatado);
      setFiltroDataFim(hojeFormatado);

      setFiltroHojeAtivo(true);
    }
  };

  const handleDeleteVendaFisicamente = async (id: number, vendaDisplayInfo: string) => {
    if (
      window.confirm(
        `ATENÇÃO: Deletar permanentemente a Venda ${vendaDisplayInfo} e todos os seus itens? ESTA AÇÃO É IRREVERSÍVEL e afetará estoque e saldos mas não excluirá os produtos da tabela de PRODUTOS.`
      )
    ) {
      try {
        setLoading(true);
        await deleteVendaFisicamente(id);
        fetchVendas();
        if (vendaSelecionadaDetalhes && vendaSelecionadaDetalhes.id === id) {
          setVendasSelecionadaDetalhes(null);
        }
        //TODO: Toast
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
      } catch (err: any) {
        const axiosError = err as AxiosError<{ message?: string }>;
        setError(axiosError.response?.data?.message || 'Falha ao deletar venda permanentemente');
        console.error(err);
      } finally {
        setLoading(false);
      }
    }
  };

  const handlePageChange = (page: number) => {
    setCurrentPage(page);
  };

  const clearFilters = () => {
    setFiltroDataInicio('');
    setFiltroDataFim('');
    setFiltroClienteId('');
    setFiltroFormaPagamento('');
    setFiltroHojeAtivo(false);
    setFiltroProduto(null);
    /**setOrdemVendas(opcoesOrdenacaoVendas[0].value); Reseta ordenação padrão */
    /**setCurrentPage(0) reseta a */
  };

  return (
    <>
      <div className="flex flex-wrap justify-between item-center fap-4 mb-8">
        <h1 className="text-2xl lg:text-3xl font-semibold text-text-primary dark:text-white">
          Vendas
        </h1>
        <Button onClick={handleOpenNovalVendaModal} iconLeft={<LuPlus className="mr-1" />}>
          Registrar Nova Venda
        </Button>
      </div>

      <Card className="mb-6 p-card-padding">
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-[2fr_1fr_1fr_3fr_2fr_2fr_auto_auto] gap-4 items-end">
          <div className="flex flex-col h-full justify-end">
            <Button
              onClick={handleToggleFiltroHoje}
              variant={filtroHojeAtivo ? 'secondary' : 'primary'}
              iconLeft={
                filtroHojeAtivo ? (
                  <LuCalendarX className="mr-1 h-4 w-4" />
                ) : (
                  <LuCalendarDays className="mr-1 h-4 w-4" />
                )
              }
            >
              {filtroHojeAtivo ? 'Ver tudo' : 'Vendas do dia'}
            </Button>
          </div>
          <Input
            label="Data Início"
            type="date"
            value={filtroDataInicio}
            onChange={(e) => {
              setFiltroDataInicio(e.target.value);
              setFiltroHojeAtivo(false);
            }}
            disabled={loading}
          />
          <Input
            label="Data Fim"
            type="date"
            value={filtroDataFim}
            onChange={(e) => {
              setFiltroDataFim(e.target.value);
              setFiltroHojeAtivo(false);
            }}
            disabled={loading}
          />
          <AutoCompleteInput
            label="Filtrar por Produto"
            placeholder="Digite para buscar produto..."
            options={todosOsProdutos}
            value={filtroProduto}
            onChange={(selected) => {
              setFiltroProduto(selected as { value: number; label: string } | null);
              // fetchVendas() será chamado pelo useEffect que depende de filtroProduto
            }}
            // isLoading={loadingProdutosAutocomplete} // Se tiver loading separado
          />
          <Select
            label="Cliente"
            value={filtroClienteId}
            onChange={(e) => setFiltroClienteId(e.target.value)}
          >
            <option value="">Todos os clientes</option>
            {clientes.map((c) => (
              <option key={c.id} value={c.id.toString()}>
                {c.nome}
              </option>
            ))}
          </Select>
          <Select
            label="Forma de Pagamento"
            value={filtroFormaPagamento}
            onChange={(e) => setFiltroFormaPagamento(e.target.value)}
          >
            <option value="">Todas as formas</option>
            {Object.values(FormaPagamento).map((fp) => (
              <option key={fp} value={fp}>
                {fp.replace('_', ' ')}
              </option>
            ))}
          </Select>
          <Button variant="ghost" onClick={clearFilters} className="lg:mt-auto" size="icon">
            <LuX />
          </Button>
          <Select
            label="Ordenar Por"
            value={ordemVendas}
            onChange={(e) => setOrdemVendas(e.target.value)}
          >
            {opcoesOrdenacaoVendas.map((opt) => (
              <option key={opt.value} value={opt.value}>
                {opt.label}
              </option>
            ))}
          </Select>
        </div>
      </Card>

      <div className="flex justify-between items-center mb-4">
        {/** TODO: card de produto mais vendido */}
      </div>

      {loading && <p className="p-6 text-center">Carregando vendas...</p>}
      {error && <p className="p-6 text-center text-red-500">{error}</p>}
      {!loading && !error && (
        <>
          <VendasTable
            vendas={vendas}
            onViewDetails={handleVerDetalhesVenda}
            onDelete={handleDeleteVendaFisicamente}
          />
          <Pagination
            currentPage={currentPage}
            totalPages={totalPages}
            onPageChange={handlePageChange}
          />
        </>
      )}

      {isNovaVendaModalOpen && (
        <VendaFormModal
          isOpen={isNovaVendaModalOpen}
          onClose={handleCloseNovaVendaModal}
          onSaveSuccess={handleNovaVendaSuccess}
        />
      )}

      {vendaSelecionadaDetalhes && (
        <VendaDetalhesModal
          venda={vendaSelecionadaDetalhes}
          isOpen={!!vendaSelecionadaDetalhes}
          onClose={handleFecharDetalhesVenda}
        />
      )}
    </>
  );
};

export default VendasPage;

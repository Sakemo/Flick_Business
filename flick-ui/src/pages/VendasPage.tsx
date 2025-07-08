// React and hooks
import { useCallback, useEffect, useMemo, useState } from 'react';

// Types and domain models
import { FormaPagamento, VendaResponse } from '../types/domain';
import { ClienteResponse } from '../types/domain';

// Services
import { 
  GetVendasParams, 
  GroupSummary, 
  TotalPorPagamento, 
  deleteVendaFisicamente, 
  getVendas, 
  getVendasGrossTotal, 
  getVendasSummary, 
  getVendasTotalPorPagamento
} from '../services/vendaService';
import { getClientes } from '../services/clienteService';
import { getProdutos as fetchAllProdutos } from '../services/produtoService';
import { getTotalExpenses } from '../services/despesaService';

// UI Components
import Button from '../components/ui/Button';
import Card from '../components/ui/Card';
import Input from '../components/ui/Input';
import Select from '../components/ui/Select';
import Pagination from '../components/common/Pagination';
import AutoCompleteInput from '../components/common/AutoCompleteInput';
import VendasTable from '../components/vendas/VendasTable';
import VendaFormModal from '../components/vendas/VendaFormModal';
import VendaDetalhesModal from '../components/vendas/VendaDetalhesModal';
import ValueTotalCard from '../components/vendas/valueTotalCard';

// Icons
import { LuPlus, LuX } from 'react-icons/lu';

// Utils and helpers
import { endOfMonth, endOfYear, format, startOfMonth, startOfYear } from 'date-fns';
import { TableRow } from '../hooks/GroupHeader';
import { formatVendaDate } from '../utils/formatters';
import { useTranslation } from 'react-i18next';
import { AxiosError } from 'axios';
import DateFilterDropdown, { DateFilterOption } from '../components/common/DateFilterDropdown';
import TotalPorPagamentoCard from '../components/vendas/TotalPagamentoCard';

const VendasPage: React.FC = () => {
  const { t } = useTranslation(); 
  const [vendas, setVendas] = useState<VendaResponse[]>([]);
  const [clientes, setClientes] = useState<ClienteResponse[]>([]);
  const [totalExpenses, setTotalExpenses] = useState<number>(0);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const [grossTotal, setGrossTotal] = useState<number>(0);
  const [totaisPorPagamento, setTotaisPorPagamento] = useState<TotalPorPagamento[]>([]);

  const [currentPage, setCurrentPage] = useState<number>(0);
  const [totalPages, setTotalPages] = useState<number>(0);

  const [isNovaVendaModalOpen, setIsNovaVendaModalOpen] = useState<boolean>(false);
  const [vendaSelecionadaDetalhes, setVendasSelecionadaDetalhes] = useState<VendaResponse | null>(
    null
  );

  const [groupSummaries, setGroupSummaries] = useState<Record<string, GroupSummary>>({});

  const today = new Date();
  const firstDayOfMonth = startOfMonth(today);
  const lastDayOfMonth = endOfMonth(today);
  const formatDateForInput = (date: Date): string => {
    return format(date, 'yyyy-MM-dd')
  }

  const [filtroDataInicio, setFiltroDataInicio] = useState<string>(formatDateForInput(firstDayOfMonth));
  const [filtroDataFim, setFiltroDataFim] = useState<string>(formatDateForInput(lastDayOfMonth));
  const [filtroClienteId, setFiltroClienteId] = useState<string>('');
  const [filtroFormaPagamento, setFiltroFormaPagamento] = useState<string>('');
  const [dateFilter, setDateFilter] = useState<DateFilterOption>('this_month');
  
  // const [filtroHojeAtivo, setFiltroHojeAtivo] = useState<boolean>(false);

  const [filtroProduto, setFiltroProduto] = useState<{ value: number; label: string } | null>(null);
  const [todosOsProdutos, setTodosOsProdutos] = useState<{ value: number; label: string }[]>([]);

  const opcoesOrdenacaoVendas = [
    { value: 'dataVenda,desc', label: t('filter.mostRecent') },
    { value: 'dataVenda,asc', label: t('filter.oldest') },
    { value: 'valorTotal,desc', label: t('filter.highestValue') },
    { value: 'valorTotal,asc', label: t('filter.lowestValue') },
    { value: 'cliente.nome,asc', label: `${t('clientes.objectName')} (A-Z)` },
    { value: 'cliente.nome,desc', label: `${(t('clientes.objectName'))} (Z-A)` },
  ];

  const [ordemVendas, setOrdemVendas] = useState<string>(opcoesOrdenacaoVendas[0].value);

  const netProfit = useMemo(()  => {
    return grossTotal - totalExpenses;
  }, [grossTotal, totalExpenses]);

  const processedVendas: TableRow[] = useMemo(() => {
    console.log('processedVendas: vendas:', vendas, 'ordemVendas:', ordemVendas);
    if (!vendas || vendas.length === 0){
      console.log('processedVendas: vendas vazias');
      return [];
    }

    const orderProperty = ordemVendas.split(',')[0];
    console.log('processedVendas: orderProperty:', orderProperty);

    if (orderProperty !== "dataVenda" && orderProperty !== 'cliente.nome') {
      console.log('processedVendas: ordem não é dataVenda nem cliente.nome, retornando vendas');
      return vendas;
    }

    const newRows: TableRow[] = [];
    const addedHeaders = new Set<string>();

    for (const venda of vendas){
      let itemGroupKey = '';
      if (orderProperty === 'dataVenda') {
        itemGroupKey = venda.dataVenda.split('T')[0];
      } else if (orderProperty === 'cliente.nome'){
        itemGroupKey = venda.cliente?.id.toString() || 'no-client';
      }

      if(itemGroupKey && !addedHeaders.has(itemGroupKey)){
        const summary = groupSummaries[itemGroupKey];
        if(summary){
          const headerTitle = [];
          if (orderProperty === 'dataVenda'){
            const day = formatVendaDate(summary.groupTitle, false);
            headerTitle[0] = day[0] === '1' ? t('filter.today') : day[0] === '0' ? t('filter.yesterday') : day; 
          }  else if (orderProperty === 'cliente.nome'){
            headerTitle[0] =  `Total de ${summary.groupTitle}`;
          }
          const headerTitleStr = Array.isArray(headerTitle[0]) ? headerTitle[0].join(' ') : headerTitle[0] ?? '';
          newRows.push({
            isGroupHeader: true,
            groupKey: summary.groupKey,
            title: headerTitleStr,
            value: summary.totalValue,
            itemCount: 0
          });
          addedHeaders.add(itemGroupKey);
        }
      }
      newRows.push(venda);
    }

    return newRows;
  }, [vendas, ordemVendas, groupSummaries, t])

  const fetchVendas = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const params: GetVendasParams = { page: currentPage, size: 8, orderBy: ordemVendas };

      
      // Definir data inicio
      if (filtroDataInicio) {
        params.inicio = new Date(`${filtroDataInicio}T00:00:00.000Z`).toISOString();
      }
      
      // Definir data fim
      if (filtroDataFim){
        params.fim = new Date(`${filtroDataFim}T23:59:59.999Z`).toISOString();
      }
	
      if (filtroClienteId) params.clienteId = parseInt(filtroClienteId);
      if (filtroFormaPagamento) params.formaPagamento = filtroFormaPagamento;
      if (filtroProduto) params.produtoId = filtroProduto.value;

      const [pageResponse, grossTotalResponse, expensesResponse, totaisPagamentoResponse] = await Promise.all([
        getVendas(params),
        getVendasGrossTotal(params),
        getTotalExpenses({
          begin: params.inicio,
          end: params.fim
        }),
        getVendasTotalPorPagamento({ inicio: params.inicio, fim:params.fim })
      ]);
      setVendas(pageResponse.content);
      setTotalPages(pageResponse.totalPages);
      setGrossTotal(grossTotalResponse);
      setTotalExpenses(expensesResponse);
      setTotaisPorPagamento(totaisPagamentoResponse);

        const orderProperty = ordemVendas.split(',')[0];
        if (orderProperty === "dataVenda" || orderProperty === 'cliente.nome') {
            const summaryResponse = await getVendasSummary(params);
            const summaryMap = summaryResponse.reduce((acc, summary) => {
                acc[summary.groupKey] = summary;
                return acc;
            }, {} as Record<string, GroupSummary>);
            setGroupSummaries(summaryMap);
        } else {
            setGroupSummaries({});
        }
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
  }

  const handleVerDetalhesVenda = (venda: VendaResponse) => {
    setVendasSelecionadaDetalhes(venda);
  };

  const handleFecharDetalhesVenda = () => {
    setVendasSelecionadaDetalhes(null);
  };

  const handleDateFilterSelect = (option: DateFilterOption) => {
    setDateFilter(option);
    const today = new Date();

    switch(option){
      case 'today':
        setFiltroDataInicio(formatDateForInput(today));
        setFiltroDataFim(formatDateForInput(today));
        break;
      case 'this_month':
        setFiltroDataInicio(formatDateForInput(startOfMonth(today)));
        setFiltroDataFim(formatDateForInput(endOfMonth(today)));
        break;
      case 'this_year':
        setFiltroDataInicio(formatDateForInput(startOfYear(today)));
        setFiltroDataFim(formatDateForInput(endOfYear(today)));
        break;
      case 'all':
        setFiltroDataInicio('');
        setFiltroDataFim('');
        break;
    }
  };

  const dateFilterOptions = [
    { key: 'today' as DateFilterOption, label: t('filter.today', t('filter.today')) },
    { key: 'this_month' as DateFilterOption, label: t('filter.month', t('filter.month')) },
    { key: 'this_year' as DateFilterOption, label: t('filter.year', t('filter.year')) },
    { key: 'all' as DateFilterOption, label: t('filter.all', t('filter.all')) },
  ];

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
    setDateFilter('this_month');
    setFiltroProduto(null);
    /**setOrdemVendas(opcoesOrdenacaoVendas[0].value); Reseta ordenação padrão */
    /**setCurrentPage(0) reseta a */
  };

  return (
    <>
      <div className="flex flex-wrap justify-between item-center fap-4 mb-8">
        <h1 className="text-2xl lg:text-3xl font-semibold text-text-primary dark:text-white">
          {t('vendas.title')}
        </h1>
        <Button onClick={handleOpenNovalVendaModal} iconLeft={<LuPlus className="mr-1" />}>
          {t('userActions.add') + t('vendas.objectName').replace(/^\w/, c => c.toLowerCase())}
        </Button>
      </div>

      <Card className="mb-6 p-card-padding">
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-[2fr_1fr_1fr_3fr_2fr_2fr_auto_auto] gap-4 items-end">
          <div className="flex flex-col h-full justify-end">
            <DateFilterDropdown
              selectedOption={dateFilter}
              onSelect={handleDateFilterSelect}
              options={dateFilterOptions}
            />

          </div>
          <Input
            label={t('filter.dateStart')}
            type="date"
            value={filtroDataInicio}
            onChange={(e) => {
              setFiltroDataInicio(e.target.value);
            }}
            disabled={loading}
          />
          <Input
            label={t('filter.dateEnd')}
            type="date"
            value={filtroDataFim}
            onChange={(e) => {
              setFiltroDataFim(e.target.value);
            }}
            disabled={loading}
          />
          <AutoCompleteInput
            label={`${t('filter.filterBy')} ${t('produtos.objectName')}`}
            placeholder={`${t('vendas.searchProductPlaceholder')}`}
            options={todosOsProdutos}
            value={filtroProduto}
            onChange={(selected) => {
              setFiltroProduto(selected as { value: number; label: string } | null);
              // fetchVendas() será chamado pelo useEffect que depende de filtroProduto
            }}
            // isLoading={loadingProdutosAutocomplete} // Se tiver loading separado
          />
          <Select
            label={t('clientes.objectName')}
            value={filtroClienteId}
            onChange={(e) => setFiltroClienteId(e.target.value)}
          >
            <option value="">{t('vendas.allClients')}</option>
            {clientes.map((c) => (
              <option key={c.id} value={c.id.toString()}>
                {c.nome}
              </option>
            ))}
          </Select>
          <Select
            label={t('common.paymentMethod')}
            value={filtroFormaPagamento}
            onChange={(e) => setFiltroFormaPagamento(e.target.value)}
          >
            <option value="">{t('vendas.allMethods')}</option>
            {Object.values(FormaPagamento).map((fp) => (
              <option key={fp} value={fp}>
                {t(`vendas.paymentMethods.${fp.toLowerCase()}`) || fp}
              </option>
            ))}
          </Select>
          <Button variant="ghost" onClick={clearFilters} className="lg:mt-auto" size="icon">
            <LuX />
          </Button>
          <Select
            label={t('filter.orderBy')}
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


      <div className='flex flex-wrap grid-cols-4 gap-4'>
        <ValueTotalCard color='blue' value={grossTotal} />
        <ValueTotalCard color={netProfit >= 0 ? 'green' : 'red'} value={netProfit} title={t("vendas.netTotal")} description={t("vendas.netTotalDescription")} />
        <TotalPorPagamentoCard totais={totaisPorPagamento} />
      </div>

      <div className="flex justify-between items-center mb-4">
        {/** TODO: card de produto mais vendido */}
      </div>

      {loading && <p className="p-6 text-center">Carregando vendas...</p>}
      {error && <p className="p-6 text-center text-red-500">{error}</p>}
      {!loading && !error && (
        <>
          <VendasTable
            vendas={processedVendas}
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

import React, { useCallback, useEffect, useState } from 'react';
import { ClienteResponse, ConfiguracaoGeralResponse } from '../types/domain';
import {
  deleteClienteFisicamente,
  getClientes,
  GetClientesParams,
  toggleAtividadeCliente,
} from '../services/clienteService';
import { getConfiguracaoGeral } from '../services/configuracaoService';
import Button from '../components/ui/Button';
import { LuPlus, LuSearch, LuX } from 'react-icons/lu';
import Card from '../components/ui/Card';
import Input from '../components/ui/Input';
import Select from '../components/ui/Select';
import ClienteCard from '../components/clientes/ClienteCard';
import ClienteFormModal from '../components/clientes/ClienteFormModal';
import { useTranslation } from 'react-i18next';

type FiltroAtivos = 'todos' | 'ativos' | 'inativos';
type OrdemCliente =
  | 'nomeAsc'
  | 'nomeDesc'
  | 'saldoDesc'
  | 'saldoAsc'
  | 'cadastroRecente'
  | 'cadastroAntigo';
type FiltroDevedores = 'todos' | 'devedores' | 'naoDevedores';
// type FiltroStatusFiado = 'todos' | 'emDia' | 'aVencer' | 'atrasado';
const ClientesPage: React.FC = () => {
  const { t } = useTranslation();

  const [clientes, setClientes] = useState<ClienteResponse[]>([]);
  const [configGeral, setConfigGeral] = useState<ConfiguracaoGeralResponse | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const [termoBuscaNome, setTermoBuscaNome] = useState('');
  const [ordem, setOrdem] = useState<OrdemCliente>('nomeAsc');
  const [filtroDevedor, setFiltroDevedor] = useState<FiltroDevedores>('todos');
  const [filtroAtividade, setFiltroAtividade] = useState<FiltroAtivos>('ativos');
  // const [filtroStatusFiado, setFiltroStatusFiado] = useState<FiltroStatusFiado>('todos');

  const [isModalOpen, setIsModalOpen] = useState<boolean>(false);
  const [clienteParaEditar, setClienteParaEditar] = useState<ClienteResponse | null>(null);

  const fetchClientes = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      //TODO: Ajustar backend para aceitar esses params
      const params: GetClientesParams = {};
      if (filtroAtividade === 'ativos') {
        params.apenasAtivos = true;
      } else if (filtroAtividade === 'inativos') {
        params.apenasAtivos = false;
      }
      if (ordem) params.orderBy = ordem;
      if (filtroDevedor === 'devedores') params.devedores = true;
      if (filtroDevedor === 'naoDevedores') params.devedores = false;
      if (termoBuscaNome.trim() !== '') params.nomeContains = termoBuscaNome.trim();

      const data = await getClientes(params);
      //TODO: Se o filtro de status fiado for no frontend, aplicar aqui sobre 'data'
      setClientes(data);
    } catch (err) {
      setError(t('siteFeedback.error'));
      console.error(err);
    } finally {
      setLoading(false);
    }
  }, [ordem, filtroDevedor, termoBuscaNome, filtroAtividade, t]);

  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const fetchConfig = useCallback(async () => {
    try {
      const data = await getConfiguracaoGeral();
      setConfigGeral(data);
    } catch (error) {
      console.error('Erro ao buscar configurações: ', error);
    }
  }, []);

  useEffect(() => {
    const handler = setTimeout(() => {
      fetchClientes();
    }, 500);

    return () => {
      clearTimeout(handler);
    };
  }, [fetchClientes]);

  useEffect(() => {
    fetchClientes();
  }, [fetchClientes]);

  const handleOpenAddModal = () => {
    setClienteParaEditar(null);
    setIsModalOpen(true);
  };
  const handleOpenEditModal = (cliente: ClienteResponse) => {
    setClienteParaEditar(cliente);
    setIsModalOpen(true);
  };
  const handleCloseModal = () => {
    setIsModalOpen(false);
    setClienteParaEditar(null);
  };
  const handleSaveSucess = () => {
    handleCloseModal();
    fetchClientes();
  };

  const handdleToggleAtivo = async (id: number, nomeCliente: string, estadoAtual: boolean) => {
    const acao = estadoAtual ? t('siteFeedback.inactive') : t('siteFeedback.active');
    //TODO: Modal de confirmação
    if (window.confirm(`${t('userActions.confirm')}: ${acao} ${t('clientes.objectName').toLowerCase()} "${nomeCliente}"?`)) {
      try {
        setLoading(true);
        await toggleAtividadeCliente(id, !estadoAtual);
        fetchClientes();
      } catch (err) {
        const axiosError = err as import('axios').AxiosError<{ message?: string }>;
        setError(axiosError.response?.data?.message || t('siteFeedback.error'));
        console.error(err);
      } finally {
        setLoading(false);
      }
    }
  };

  const handleDeletarFisicamente = async (id: number, nomeCliente: string) => {
    //TODO: Modal de confirmacao
    if (
      window.confirm(
        `${t('userActions.delete')} + '' + ${t('clientes.objectName')} ${nomeCliente}? ${t('userActions.no')} ${t('userActions.cancel')}.`
      )
    ) {
      try {
        setLoading(true);
        await deleteClienteFisicamente(id);
        fetchClientes();
      } catch (err) {
        const axiosError = err as import('axios').AxiosError<{ message?: string }>;
        setError(axiosError.response?.data?.message || t('siteFeedback.deleteError'));
        console.error(err);
      } finally {
        setLoading(false);
      }
    }
  };

  const clearNomeFilter = () => {
    setTermoBuscaNome('');
  };

  return (
    <div>
      <div className="flex justify-between items-center mb-8">
        <h1 className="text-2xl lg:text-3xl font-semibold text-text-primary dark:text-white">
          {t('clientes.title')}
        </h1>
        <Button onClick={handleOpenAddModal} iconLeft={<LuPlus className="mr-1" />}>
          {t('userActions.add')}{t('clientes.objectName')}
        </Button>
      </div>

      <Card className="mb-6 p-card-padding">
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 items-end">
          <div className="relative flex items-end">
            <div
              className={
                termoBuscaNome
                  ? 'flex-1 mr-2 transition-all duration-200'
                  : 'w-full transition-all duration-200'
              }
            >
              <Input
                label={t('userActions.searchBy') + t('common.name')}
                placeholder={t('userActions.searchBy') + t('common.name') + '...'}
                name={termoBuscaNome}
                value={termoBuscaNome}
                onChange={(e) => setTermoBuscaNome(e.target.value)}
                iconLeft={<LuSearch className="mr-1" />}
                className={termoBuscaNome ? '' : ''}
              />
            </div>
            {termoBuscaNome && (
              <Button variant="ghost" onClick={clearNomeFilter}>
                <LuX className="" />
              </Button>
            )}
          </div>

          <Select
            label={t('userActions.all')}
            name="filtroAtividade"
            value={filtroAtividade}
            onChange={(e) => setFiltroAtividade(e.target.value as FiltroAtivos)}
          >
            <option value="ativos">{t('siteFeedback.active')}</option>
            <option value="inativos">{t('siteFeedback.inactive')}</option>
            <option value="todos">{t('userActions.all')}</option>
          </Select>

          <Select
            label={t('filter.orderBy')}
            name="ordem"
            value={ordem}
            onChange={(e) => setOrdem(e.target.value as OrdemCliente)}
          >
            <option value="nomeAsc">{t('common.name')} (A-Z)</option>
            <option value="nomeDesc">{t('common.name')} (Z-A)</option>
            <option value="saldoDesc">{t('filter.highestValue')}</option>
            <option value="saldoAsc">{t('filter.lowestValue')}</option>
            <option value="cadastroRecente">{t('filter.mostRecent')}</option>
            <option value="cadastroAntigo">{t('filter.oldest')}</option>
          </Select>
          <Select
            label={t('clientes.onCredit')}
            name="filtroDevedor"
            value={filtroDevedor}
            onChange={(e) => setFiltroDevedor(e.target.value as FiltroDevedores)}
          >
            <option value="todos">{t('userActions.all')}</option>
            <option value="devedores">{t('clientes.onCredit')}</option>
            <option value="naoDevedores">{t('userActions.no')}</option>
          </Select>
        </div>
      </Card>

      {loading && <p className="p-6 text-center">{t('siteFeedback.loading')}</p>}
      {error && <p className="p-6 text-center text-red-500">{error}</p>}
      {!loading && !error && clientes.length === 0 && (
        <Card>
          <p className="p-6 text-center text-text-secondary">{t('siteFeedback.noData')}</p>
        </Card>
      )}
      {!loading && !error && clientes.length > 0 && (
        <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6">
          {clientes.map((cliente) => (
            <ClienteCard
              key={cliente.id}
              cliente={cliente}
              configFiado={configGeral}
              onEdit={() => handleOpenEditModal(cliente)}
              onToggleAtivo={() => handdleToggleAtivo(cliente.id, cliente.nome, cliente.ativo)}
              onDeletePermanente={() => handleDeletarFisicamente(cliente.id, cliente.nome)}
            />
          ))}
        </div>
      )}

      {isModalOpen && (
        <ClienteFormModal
          isOpen={isModalOpen}
          onClose={handleCloseModal}
          onSaveSuccess={handleSaveSucess}
          clienteInicial={clienteParaEditar}
        />
      )}
    </div>
  );
};
export default ClientesPage;

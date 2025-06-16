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
      setError('Falha ao carregar clientes');
      console.error(err);
    } finally {
      setLoading(false);
    }
  }, [ordem, filtroDevedor, termoBuscaNome, filtroAtividade]);

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
    const acao = estadoAtual ? 'INATIVAR' : 'ATIVAR';
    //TODO: Modal de confirmação
    if (window.confirm(`Tem certeza que deseja ${acao} o cliente "${nomeCliente}"`)) {
      try {
        setLoading(true);
        await toggleAtividadeCliente(id, !estadoAtual);
        fetchClientes();
      } catch (err) {
        const axiosError = err as import('axios').AxiosError<{ message?: string }>;
        setError(axiosError.response?.data?.message || `Falha ao ${acao.toLowerCase()} cliente`);
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
        `ATENÇÃO: Deletar PEMANENTEMENTE o cliente ${nomeCliente}? Esta ação é IRREVERSÍVEL`
      )
    ) {
      try {
        setLoading(true);
        await deleteClienteFisicamente(id);
        fetchClientes();
      } catch (err) {
        const axiosError = err as import('axios').AxiosError<{ message?: string }>;
        setError(axiosError.response?.data?.message || `Falha ao deletar cliente.`);
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
          Gerenciamento de Clientes
        </h1>
        <Button onClick={handleOpenAddModal} iconLeft={<LuPlus className="mr-1" />}>
          Novo Cliente
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
                label="Buscar por Nome"
                placeholder="Digite o nome..."
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
            label="Mostrar Clientes"
            name="filtroAtividade"
            value={filtroAtividade}
            onChange={(e) => setFiltroAtividade(e.target.value as FiltroAtivos)}
          >
            <option value="ativos">Apenas Ativos</option>
            <option value="inativos">Apenas Inativos</option>
            <option value="todos">Todos</option>
          </Select>

          <Select
            label="Ordernar por"
            name="ordem"
            value={ordem}
            onChange={(e) => setOrdem(e.target.value as OrdemCliente)}
          >
            <option value="nomeAsc">Nome (A-Z)</option>
            <option value="nomeDesc">Nome (Z-A)</option>
            <option value="saldoDesc">Maior Fiado</option>
            <option value="saldoAsc">Menor Fiado</option>
            <option value="cadastroRecente">Mais Recente</option>
            <option value="cadastroAntigo"> Mais Antigo</option>
          </Select>
          <Select
            label="Filtrar Devedores"
            name="filtroDevedor"
            value={filtroDevedor}
            onChange={(e) => setFiltroDevedor(e.target.value as FiltroDevedores)}
          >
            <option value="todos">Todos</option>
            <option value="devedores">Apenas Fiado</option>
            <option value="naoDevedores">Apenas Em Dia</option>
          </Select>
        </div>
      </Card>

      {loading && <p className="p-6 text-center">Carregando clientes...</p>}
      {error && <p className="p-6 text-center text-red-500">{error}</p>}
      {!loading && !error && clientes.length === 0 && (
        <Card>
          <p className="p-6 text-center text-text-secondary">Nenhum cliente cadastrado</p>
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

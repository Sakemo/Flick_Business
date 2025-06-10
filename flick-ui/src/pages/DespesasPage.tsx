import { useCallback, useEffect, useState } from 'react';
import { DespesaResponse, TipoDespesa } from '../types/domain';
import { deleteDespesa, getDespesas, GetDespesasParams } from '../services/despesaService';
import Button from '../components/ui/Button';
import { LuPlus } from 'react-icons/lu';
import Card from '../components/ui/Card';
import Input from '../components/ui/Input';
import Select from '../components/ui/Select';
import DespesasTable from '../components/despesas/DespesasTable';
import DespesaFormModal from '../components/despesas/DespesaFormModal';

const DespesaPage: React.FC = () => {
  const [despesas, setDespesas] = useState<DespesaResponse[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [isModalOpen, setIsModalOpen] = useState<boolean>(false);
  const [despesaParaEditar, setDespesaParaEditar] = useState<DespesaResponse | null>(null);

  const [filtroDataInicio, setFiltroDataInicio] = useState<string>('');
  const [filtroDataFim, setFiltroDataFim] = useState<string>('');
  const [filtroTipoDespesa, setFiltroTipoDespesa] = useState<string>('');

  const fetchDespesas = useCallback(async () => {
    setLoading(true);
    setError(null);

    try {
      const params: GetDespesasParams = {};
      if (filtroDataInicio) params.inicio = `${filtroDataInicio}T00:00:00`;
      if (filtroDataFim) params.fim = `${filtroDataFim}T23:59:59`;
      if (filtroTipoDespesa) params.tipoDespesa = filtroTipoDespesa;
      const data = await getDespesas(params);
      setDespesas(data);
    } catch (err) {
      setError('Falha ao carregar despesas');
      console.error(err);
    } finally {
      setLoading(false);
    }
  }, [filtroDataInicio, filtroDataFim, filtroTipoDespesa]);

  useEffect(() => {
    fetchDespesas();
  }, [fetchDespesas]);

  const handleOpenAddModal = () => {
    setDespesaParaEditar(null);
    setIsModalOpen(true);
  };

  const handleOpenEditModal = (despesa: DespesaResponse) => {
    setDespesaParaEditar(despesa);
    setIsModalOpen(true);
  };
  const handleCloseModal = () => {
    setIsModalOpen(false);
    setDespesaParaEditar(null);
  };
  const handleSaveSucess = () => {
    handleCloseModal();
    fetchDespesas(); //TODO: TOAST
  };

  const handleDelete = async (id: number) => {
    if (window.confirm('Tem certeza que deseja deletar essa despesa?')) {
      try {
        await deleteDespesa(id);
        fetchDespesas(); //TODO: + TOAST
      } catch (err) {
        setError('Falha ao deletar despesa');
        console.error(err);
      }
    } //TODO: modal de confirmação
  };

  const clearFilter = () => {
    setFiltroDataInicio('');
    setFiltroDataFim('');
    setFiltroTipoDespesa('');
  };

  return (
    <div>
      <div className="flex justify-between items-center mb-8">
        <h1 className="text-2xl lg:text-3xl font-semibold text-text-primary dark:text-white">
          Gerenciamento de Despesas
        </h1>
        <Button onClick={handleOpenAddModal} iconLeft={<LuPlus className="mr-1" />}>
          Nova Despesa
        </Button>
      </div>

      <Card className="mb-6 p-card-padding">
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 items-end">
          <Input
            label="Data Início"
            type="date"
            value={filtroDataInicio}
            onChange={(e) => setFiltroDataInicio(e.target.value)}
          />
          <Input
            label="Data Fim"
            type="date"
            value={filtroDataFim}
            onChange={(e) => setFiltroDataFim(e.target.value)}
          />
          <Select
            label="Tipo de Despesa"
            value={filtroTipoDespesa}
            onChange={(e) => setFiltroTipoDespesa(e.target.value)}
          >
            <option value="">Todos os tipos</option>
            {Object.values(TipoDespesa).map((tipo) => (
              <option key={tipo} value={tipo}>
                {tipo.charAt(0).toUpperCase() + tipo.slice(1).toLowerCase()}
              </option>
            ))}
          </Select>
          <Button variant="ghost" onClick={clearFilter}>
            Limpar Filtros
          </Button>
        </div>
      </Card>

      {loading && <p className="p-6 text-center">Carregando Despesas</p>}
      {error && <p className="p-6 text-center text-red-500">{error}</p>}
      {!loading && !error && (
        <DespesasTable despesas={despesas} onEdit={handleOpenEditModal} onDelete={handleDelete} />
      )}

      {isModalOpen && (
        <DespesaFormModal
          isOpen={isModalOpen}
          onClose={handleCloseModal}
          onSaveSucess={handleSaveSucess}
          despesaInicial={despesaParaEditar}
        />
      )}
    </div>
  );
};
export default DespesaPage;

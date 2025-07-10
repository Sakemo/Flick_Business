import { useCallback, useEffect, useState } from 'react';
import { DespesaResponse, TipoDespesa } from '../types/domain';
import { deleteDespesa, getDespesas, GetDespesasParams } from '../services/despesaService';
import Button from '../components/ui/Button';
import { LuPlus, LuSearch, LuX } from 'react-icons/lu';
import Card from '../components/ui/Card';
import Input from '../components/ui/Input';
import Select from '../components/ui/Select';
import DespesasTable from '../components/despesas/DespesasTable';
import DespesaFormModal from '../components/despesas/DespesaFormModal';
import { useTranslation } from 'react-i18next';
import DespesaDetalhesDrawer from '../components/despesas/DespesaDetalhesDrawer';
import clsx from 'clsx';

const DespesaPage: React.FC = () => {
  const [despesaSelecionada, setDespesaSelecionada] = useState<DespesaResponse | null>(null);
  const [despesas, setDespesas] = useState<DespesaResponse[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [isModalOpen, setIsModalOpen] = useState<boolean>(false);
  const [despesaParaEditar, setDespesaParaEditar] = useState<DespesaResponse | null>(null);

  const [filtroNome, setFiltroNome] = useState<string>('');
  const [filtroDataInicio, setFiltroDataInicio] = useState<string>('');
  const [filtroDataFim, setFiltroDataFim] = useState<string>('');
  const [filtroTipoDespesa, setFiltroTipoDespesa] = useState<string>('');
  const { t } = useTranslation();

    const fetchDespesas = useCallback(async () => {
      setLoading(true);
      setError(null);

      try {
        const params: GetDespesasParams = {};
        if (filtroDataInicio) params.inicio = `${filtroDataInicio}T00:00:00`;
        if (filtroDataFim) params.fim = `${filtroDataFim}T23:59:59`;
        if (filtroTipoDespesa) params.tipoDespesa = filtroTipoDespesa;
        if (filtroNome.trim()) params.nomeContains = filtroNome.trim();
        const data = await getDespesas(params);
        setDespesas(data);
      } catch (err) {
        setError(t('siteFeedback.error'));
        console.error(err);
      } finally {
        setLoading(false);
      }
    }, [filtroDataInicio, filtroDataFim, filtroTipoDespesa, filtroNome, t]);

    useEffect(() => {
      fetchDespesas();
    }, [fetchDespesas]);

    const handleRowClick = (despesa: DespesaResponse) => {
      if(despesaSelecionada && despesaSelecionada.id === despesa.id){
        setDespesaSelecionada(null);
      } else {
        setDespesaSelecionada(despesa);
      }
    }

    const handleCloseDrawer = () => {
      setDespesaSelecionada(null);
    }

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
      if (window.confirm(t('siteFeedback.confirmDelete'))) {
        try {
          await deleteDespesa(id);
          fetchDespesas(); //TODO: + TOAST
        } catch (err) {
          setError(t('siteFeedback.deleteError'));
          console.error(err);
        }
      } //TODO: modal de confirmação
    };

    const clearFilter = () => {
      setFiltroDataInicio('');
      setFiltroDataFim('');
      setFiltroTipoDespesa('');
      setFiltroNome('');
    };

    return (
      <div>
        <div className="flex justify-between items-center mb-8">
          <h1 className="text-2xl lg:text-3xl font-semibold text-text-primary dark:text-white">
            {t('despesas.title')}
          </h1>
          <Button onClick={handleOpenAddModal} iconLeft={<LuPlus className="mr-1" />}>
            {t('userActions.add') + t('despesas.objectName').replace(/^\w/, c => c.toLowerCase())}
          </Button>
        </div>

        <Card className="mb-6 p-card-padding">
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 items-end">
            <Input
              label={t('userActions.searchBy') + ' ' + t('common.name')}
              placeholder={t('userActions.searchBy') + '...'}
              value={filtroNome}
              onChange={(e) => setFiltroNome(e.target.value)}
              iconLeft={<LuSearch />}
            />

            <Input
              label={t('filter.dateStart')}
              type="date"
              value={filtroDataInicio}
              onChange={(e) => setFiltroDataInicio(e.target.value)}
            />
            <Input
              label={t('filter.dateEnd')}
              type="date"
              value={filtroDataFim}
              onChange={(e) => setFiltroDataFim(e.target.value)}
            />
            <Select
              label={t('common.category')}
              value={filtroTipoDespesa}
              onChange={(e) => setFiltroTipoDespesa(e.target.value)}
            >
              <option value="">{t('userActions.all')}</option>
              {Object.values(TipoDespesa).map((tipo) => (
                <option key={tipo} value={tipo}>
                  {t(`expenseCategories.${tipo}`)}
                </option>
              ))}
            </Select>
            <Button variant="ghost" onClick={clearFilter}>
              <LuX />
            </Button>
          </div>
        </Card>


      <div className="flex flex-col lg:flex-row gap-6">
        {/* Coluna da Tabela */}
        <div className={clsx("transition-all duration-300 ease-in-out", despesaSelecionada ? "lg:w-2/3" : "w-full")}>
          {loading && <p className="p-6 text-center">{t('siteFeedback.loading')}</p>}
          {error && <p className="p-6 text-center text-red-500">{error}</p>}
          {!loading && !error && (
            <DespesasTable
              despesas={despesas}
              onEdit={handleOpenEditModal}
              onDelete={handleDelete}
              onRowClick={handleRowClick}
              selectedRowId={despesaSelecionada?.id}
            />
          )}
        </div>

        {/* Coluna do Drawer (aparece e desaparece) */}
        <div className={clsx("transition-all duration-300 ease-in-out", despesaSelecionada ? "lg:w-1/3 opacity-100" : "w-0 opacity-0 overflow-hidden")}>
          {despesaSelecionada && (
            <DespesaDetalhesDrawer 
              despesa={despesaSelecionada}
              onClose={handleCloseDrawer}
              onEdit={handleOpenEditModal}
            />
          )}
        </div>
      </div>


      {isModalOpen && (
        <DespesaFormModal
          isOpen={isModalOpen}
          onClose={handleCloseModal}
          onSaveSuccess={handleSaveSucess}
          despesaInicial={despesaParaEditar}
        />
      )}
    </div>
    );
};
export default DespesaPage;

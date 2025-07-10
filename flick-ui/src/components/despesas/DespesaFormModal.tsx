import { useEffect, useState } from 'react';
import { DespesaRequest, DespesaResponse, TipoDespesa } from '../../types/domain';
import { format } from 'date-fns';
import { createDespesa, updateDespesa } from '../../services/despesaService';
import Modal from '../common/Modal';
import Input from '../ui/Input';
import Select from '../ui/Select';
import Textarea from '../ui/Textarea';
import Button from '../ui/Button';
import { useTranslation } from 'react-i18next';

interface DespesaFormModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSaveSuccess: () => void;
  despesaInicial?: DespesaResponse | null;
}

const DespesaFormModal: React.FC<DespesaFormModalProps> = ({
  isOpen,
  onClose,
  onSaveSuccess,
  despesaInicial,
}) => {
  const { t } = useTranslation();

  const isEditMode = !!despesaInicial;
  const [formData, setFormData] = useState<Partial<DespesaRequest>>({});
  const [isLoading, setIsLoading] = useState(false);
  const [errors, setErrors] = useState<Record<string, string>>({});

  useEffect(() => {
    if (isOpen) {
      if (isEditMode && despesaInicial) {
        setFormData({
          nome: despesaInicial.nome,
          valor: despesaInicial.valor,
          dataDespesa: despesaInicial.dataDespesa
            ? format(new Date(despesaInicial.dataDespesa), "yyyy-MM-dd'T'HH:mm")
            : '',
          tipoDespesa: despesaInicial.tipoDespesa,
          observacao: despesaInicial.observacao,
        });
      } else {
        setFormData({
          nome: '',
          valor: undefined,
          dataDespesa: format(new Date(), "yyyy-MM-dd'T'HH:mm"),
          tipoDespesa: TipoDespesa.OUTROS,
          observacao: '',
        });
      }
      setErrors({});
    }
  }, [isOpen, isEditMode, despesaInicial]);

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>
  ) => {
    const { name, value, type } = e.target;
    if (type === 'number') {
      setFormData((prev) => ({ ...prev, [name]: value === '' ? undefined : parseFloat(value) }));
    } else {
      setFormData((prev) => ({ ...prev, [name]: value }));
    }
    if (errors[name]) setErrors((prev) => ({ ...prev, [name]: '' }));
  };

  const validateForm = (): boolean => {
    const newErrors: Record<string, string> = {};
    if (!formData.nome?.trim()) newErrors.nome = 'Nome é obrigatório';
    if (formData.valor === undefined || formData.valor <= 0)
      newErrors.valor = 'Valor deve ser positivo';
    if (!formData.dataDespesa) newErrors.dataDespesa = 'Data da despesa é obrigatória';
    if (!formData.tipoDespesa) newErrors.tipoDespesa = 'Tipo de despesa é obrigatório';
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validateForm()) return;
    setIsLoading(true);

    const payload: DespesaRequest = {
      nome: formData.nome!,
      valor: formData.valor!,
      dataDespesa: formData.dataDespesa!,
      tipoDespesa: formData.tipoDespesa!,
      observacao: formData.observacao,
    };

    try {
      if (isEditMode && despesaInicial) {
        await updateDespesa(despesaInicial.id, payload);
      } else {
        await createDespesa(payload);
      }
      onSaveSuccess();
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
    } catch (error: any) {
      console.error('Erro ao salvar despesa: ', error);
      setErrors({ form: error.response?.data?.message || 'Erro ao salvar despesa' });
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={isEditMode ? 'Editar Despesa' : 'Registrar Nova Despesa'}
    >
      <form onSubmit={handleSubmit}>
        <div className="p-6 space-y-4">
          <Input
            label={t('common.name')}
            name="nome"
            value={formData.nome || ''}
            onChange={handleChange}
            error={errors.nome}
            required
          />
          <Input
            label={t('common.value')}
            name="valor"
            type="number"
            step="0.01"
            value={formData.valor ?? ''}
            onChange={handleChange}
            error={errors.valor}
            required
          />
          <Input
            label={t("despesas.form.expenseDate")}
            name="dataDespesa"
            type="datetime-local"
            value={formData.dataDespesa || ''}
            onChange={handleChange}
            error={errors.dataDespesa}
            required
          />
          <Select
            label={t("despesas.form.expenseType")}
            name="tipoDespesa"
            value={formData.tipoDespesa || ''}
            onChange={handleChange}
            error={errors.tipoDespesa}
            required
          >
            <option value="" disabled>
              {t('common.select')}
            </option>
            {Object.values(TipoDespesa).map((tipo) => (
              <option key={tipo} value={tipo}>
                {tipo.charAt(0) + tipo.slice(1).toLowerCase()}
              </option>
            ))}
          </Select>
          <Textarea
            label={t('vendas.form.observations')}
            name="observacao"
            value={formData.observacao || ''}
            onChange={handleChange}
            rows={3}
          />
          {errors.form && <p className="text-sm text-red-500 mt-2">{errors.form}</p>}
        </div>
        <div className="brand-muted dark:bg-gray-800 px-6 py-4 flex justify-end space-x-3">
          <Button type="submit" variant="secondary" onClick={onClose} disabled={isLoading}>
            Cancelar
          </Button>
          <Button type="submit" variant="primary" isLoading={isLoading}>
            {isEditMode ? 'Salvar' : 'Registrar'}
          </Button>
        </div>
      </form>
    </Modal>
  );
};
export default DespesaFormModal;

/**
 * Feat(Despesas): Implement CRUD functionality for expenses with filtering and modal support
 */

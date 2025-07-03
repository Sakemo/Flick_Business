/* eslint-disable @typescript-eslint/no-explicit-any */
import { useEffect, useState } from 'react';
import { ClienteRequest, ClienteResponse } from '../../types/domain';
import { createCliente, updateCliente } from '../../services/clienteService';
import Modal from '../common/Modal';
import Input from '../ui/Input';
import Textarea from '../ui/Textarea';
import Button from '../ui/Button';
import { useTranslation
  
 } from 'react-i18next';
interface ClienteFormModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSaveSuccess: () => void;
  clienteInicial?: ClienteResponse | null;
}

const ClienteFormModal: React.FC<ClienteFormModalProps> = ({
  isOpen,
  onClose,
  onSaveSuccess,
  clienteInicial,
}) => {
  const { t } = useTranslation();
  const isEditMode = !!clienteInicial;
  const [formData, setFormData] = useState<Partial<ClienteRequest>>({});
  const [isLoading, setIsLoading] = useState(false);
  const [errors, setErrors] = useState<Record<string, string>>({});

  useEffect(() => {
    if (isOpen) {
      if (isEditMode && clienteInicial) {
        setFormData({
          nome: clienteInicial.nome,
          cpf: clienteInicial.cpf,
          telefone: clienteInicial.telefone,
          endereco: clienteInicial.endereco,
          controleFiado: clienteInicial.controleFiado,
          limiteFiado: clienteInicial.limiteFiado ?? undefined,
          ativo: clienteInicial.ativo,
        });
      } else {
        setFormData({
          nome: '',
          cpf: '',
          telefone: '',
          endereco: '',
          controleFiado: false,
          limiteFiado: undefined,
          ativo: true,
        });
      }
      setErrors({});
    }
  }, [isOpen, isEditMode, clienteInicial]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value, type } = e.target;
    if (type === 'checkbox') {
      const { checked } = e.target as HTMLInputElement;
      setFormData((prev) => ({ ...prev, [name]: checked }));
    } else if (type === 'number') {
      setFormData((prev) => ({ ...prev, [name]: value === '' ? undefined : parseFloat(value) }));
    } else {
      setFormData((prev) => ({ ...prev, [name]: value }));
    }

    if (errors[name]) {
      setErrors((prev) => ({ ...prev, [name]: '' }));
    }
  };

  const validateForm = (): boolean => {
    const newErrors: Record<string, string> = {};
    if (!formData.nome || formData.nome.trim().length < 2) {
      newErrors.nome = t('clientes.form.validation.nameRequired', 'Nome é obrigatório (mínimo 2 caracteres)');
    }
    if (
      formData.cpf &&
      formData.cpf.length > 0 &&
      formData.limiteFiado !== undefined &&
      formData.limiteFiado < 0
    ) {
      newErrors.limiteFiado = t('clientes.form.validation.limitNonNegative','Limite de fiado não pode ser negativo');
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validateForm()) return;

    setIsLoading(true);
    setErrors({});

    const payload: ClienteRequest = {
      nome: formData.nome!,
      cpf: formData.cpf || null,
      telefone: formData.telefone || null,
      endereco: formData.endereco || null,
      controleFiado: formData.controleFiado ?? false,
      limiteFiado: formData.limiteFiado,
      ativo: formData.ativo ?? true,
    };

    console.log('Payload para Cliente: ', payload);

    try {
      if (isEditMode && clienteInicial) {
        await updateCliente(clienteInicial.id, payload);
      } else {
        await createCliente(payload);
      }
      onSaveSuccess();
    } catch (error: any) {
      console.error('Erro ao salvar cliente: ', error);
      const errorMsg =
        error.response?.data?.message ||
        (isEditMode ? t('clientes.form.error.updateFailed') : t('clientes.form.error.createFailed'));
      setErrors({ form: errorMsg });
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={isEditMode ? t('clientes.form.editTitle') : t('clientes.form.title')}
      className="sm:max-w-lg md:max-w-xl"
    >
      <form onSubmit={handleSubmit}>
        <div className="p-6 space-y-4">
          <Input
            label={t('clientes.form.fullName')}
            name="nome"
            value={formData.nome || ''}
            onChange={handleChange}
            error={errors.nome}
            required
          />
          <Input
            label={t('clientes.form.cpf')}
            name="cpf"
            value={formData.cpf || ''}
            onChange={handleChange}
            error={errors.cpf}
            maxLength={11}
          />
          <Input
            label={t('clientes.form.phone')}
            name="telefone"
            type="tel"
            value={formData.telefone || ''}
            onChange={handleChange}
            error={errors.telefone}
            maxLength={11}
          />
          <Textarea
            label={t('clientes.form.address')}
            name="endereco"
            value={formData.endereco || ''}
            onChange={handleChange}
            error={errors.endereco}
            rows={3}
          />

          <div className="pt-4 border-t border-border-light dark:border-border-dark">
            <h3 className="text-sm font-medium text-text-primary dark:text-white mb-2">
                {t('clientes.form.creditControlTitle')}
            </h3>
            <div className="flex items-center space-x-3">
              <input
                type="checkbox"
                id="controleFiado"
                name="controleFiado"
                checked={formData.controleFiado ?? false}
                onChange={handleChange}
                className="h-4 w-4 rounded border-gray-300 text-brand-primary focus:ring-brand-primary"
              />
              <label
                htmlFor="controleFiado"
                className="text-sm text-text-secondary dark:text-gray-300"
              >
                {t('clientes.form.allowCredit')}
              </label>
            </div>
            {formData.controleFiado && (
              <Input
                label={t('clientes.form.creditLimit')}
                name="limiteFiado"
                type="number"
                step="0.01"
                min="0"
                value={formData.limiteFiado ?? ''}
                onChange={handleChange}
                error={errors.limiteFiado}
                wrapperClassName="mt-3"
              />
            )}
          </div>

          <div className="flex items-center pt-4">
            <input
              type="checkbox"
              id="ativo"
              name="ativo"
              checked={formData.ativo ?? true}
              onChange={handleChange}
              className="h-4 w-4 rounded border-gray-300 text-brand-primary focus:ring-brand-primary"
            />
            <label htmlFor="ativo" className="ml-2 text-sm text-text-secondary dark:text-gray-300">
              {t('clientes.form.activeClient')}
            </label>
          </div>

          {errors.form && <p className="text-sm text-red-500 mt-2">{errors.form}</p>}
        </div>
        <div className="brand-muted dark:bg-gray-800 px-6 py-4 flex justify-end space-x-3">
          <Button type="button" variant="secondary" onClick={onClose} disabled={isLoading}>
            {t('userActions.cancel')}
          </Button>
          <Button type="submit" variant="primary" isLoading={isLoading}>
            {isEditMode ? t('userActions.saveChanges') : t('userActions.add')}
          </Button>
        </div>
      </form>
    </Modal>
  );
};
export default ClienteFormModal;

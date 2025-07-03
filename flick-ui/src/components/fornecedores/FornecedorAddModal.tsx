// src/components/fornecedores/FornecedorAddModal.tsx
import React, { useState, useEffect } from 'react'; // Adicionar useEffect
import Modal from '../common/Modal';
import Input from '../ui/Input';
import Button from '../ui/Button';
import { createFornecedor } from '../../services/fornecedorService';
import { FornecedorAddQuickRequest, FornecedorResponse, TipoPessoa } from '../../types/domain';
import Select from '../ui/Select';

interface FornecedorAddModalProps {
  isOpen: boolean;
  onClose: () => void;
  onFornecedorAdded: (novoFornecedor: FornecedorResponse) => void;
}

const FornecedorAddModal: React.FC<FornecedorAddModalProps> = ({
  isOpen,
  onClose,
  onFornecedorAdded,
}) => {
  const [formData, setFormData] = useState<FornecedorAddQuickRequest>({
    nome: '',
    tipoPessoa: '',
  });
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    setError(null);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!formData.nome.trim()) {
      setError('Nome do fornecedor é obrigatorio.');
      return;
    }
    setIsLoading(true);
    setError(null);
    try {
      const payload: FornecedorAddQuickRequest = {
        nome: formData.nome,
        tipoPessoa: formData.tipoPessoa === '' ? undefined : formData.tipoPessoa,
      };
      const novoFornecedor = await createFornecedor(payload);
      onFornecedorAdded(novoFornecedor);
      onClose();
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
    } catch (err: any) {
      console.error('Erro ao adicionar fornecedor:', err);
      setError(err.response?.data?.message || 'Falha ao adicionar fornecedor.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    if (isOpen) {
      setFormData({ nome: '', tipoPessoa: '' });
      setError(null);
    }
  }, [isOpen]);

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Adicionar Novo Fornecedor">
      <form onSubmit={handleSubmit}>
        <div className="p-2">
          <Input
            label="Nome do Fornecedor"
            name="nome"
            value={formData.nome}
            onChange={handleChange}
            error={error}
            required
            autoFocus
            className="mb-3"
          />
          <Select
            label="Tipo de Pessoa"
            name="tipoPessoa"
            value={formData.tipoPessoa || ''}
            onChange={handleChange}
          >
            <option value="">Selecione o tipo</option>
            {Object.values(TipoPessoa).map((tipo) => (
              <option key={tipo} value={tipo}>
                {tipo === TipoPessoa.FISICA ? 'Pessoa Física' : 'Pessoa Jurídica'}
              </option>
            ))}
          </Select>
          {error && <p className="text-xs text-red-500 mt-1">{error}</p>}
        </div>
        <div className="brand-muted dark:bg-gray-800 px-6 py-4 flex justify-end space-x-3">
          <Button
            className="p-4"
            type="button"
            variant="secondary"
            onClick={onClose}
            disabled={isLoading}
          >
            Cancelar
          </Button>
          <Button type="submit" isLoading={isLoading}>
            Salvar Fornecedor
          </Button>
        </div>
      </form>
    </Modal>
  );
};

export default FornecedorAddModal;

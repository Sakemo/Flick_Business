// src/components/fornecedores/FornecedorAddModal.tsx
import React, { useState, useEffect } from 'react'; // Adicionar useEffect
import Modal from '../common/Modal';
import Input from '../ui/Input';
import Button from '../ui/Button';
import { createFornecedor, FornecedorRequest } from '../../services/fornecedorService';
import { FornecedorResponse } from '../../types/domain';

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
  const [nomeFornecedor, setNomeFornecedor] = useState('');
  // Adicionar outros campos se FornecedorRequest for mais complexo
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    setError(null);
    try {
      const payload: FornecedorRequest = { nome: nomeFornecedor /* , outros campos */ };
      const novoFornecedor = await createFornecedor(payload);
      onFornecedorAdded(novoFornecedor);
      setNomeFornecedor('');
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
      setNomeFornecedor('');
      setError(null);
    }
  }, [isOpen]);

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Adicionar Novo Fornecedor">
      <form onSubmit={handleSubmit}>
        <div className="p-6">
          <Input
            label="Nome do Fornecedor"
            value={nomeFornecedor}
            onChange={(e) => setNomeFornecedor(e.target.value)}
            error={error}
            required
            autoFocus
          />
          {/* Adicionar outros inputs se FornecedorRequest for mais complexo */}
        </div>
        <div className="bg-gray-50 dark:bg-gray-800 px-6 py-4 flex justify-end space-x-3">
          <Button type="button" variant="secondary" onClick={onClose} disabled={isLoading}>
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

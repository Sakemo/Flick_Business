import React, { useEffect, useState } from 'react';
import { CategoriaResponse } from '../../types/domain';
import { createCategoria } from '../../services/categoriaService';
import { CategoriaRequest } from '../../types/domain';
import Modal from '../common/Modal';
import Input from '../ui/Input';
import Button from '../ui/Button';

interface CategoriaAddModalProps {
  isOpen: boolean;
  onClose: () => void;
  onCategoriaAdded: (novaCategoria: CategoriaResponse) => void;
}

const CategoriaAddModal: React.FC<CategoriaAddModalProps> = ({
  isOpen,
  onClose,
  onCategoriaAdded,
}) => {
  const [nomeCategoria, setNomeCategoria] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!nomeCategoria.trim()) {
      setError('Nome da categoria é obrigatório');
      return;
    }
    setIsLoading(true);
    setError(null);
    try {
      const payload: CategoriaRequest = { nome: nomeCategoria };
      const novaCategoria = await createCategoria(payload);
      onCategoriaAdded(novaCategoria);
      setNomeCategoria('');
      onClose();
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
    } catch (err: any) {
      console.error('Erro ao adicionar categoria: ', err);
      setError(err.response?.data?.message || 'Falha ao adicionar categoria');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    if (isOpen) {
      setNomeCategoria('');
      setError(null);
    }
  }, [isOpen]);

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Adicionar categoria">
      <form onSubmit={handleSubmit}>
        <div className="p-2">
          <Input
            label="Nome da Categoria"
            value={nomeCategoria}
            onChange={(e) => setNomeCategoria(e.target.value)}
            error={error}
            required
            autoFocus
          />
        </div>
        <div className="bg-gray-50 dark:bg-gray-800 px-6 py-4 flex justify-end space-x-3">
          <Button type="button" variant="secondary" onClick={onClose} disabled={isLoading}>
            Cancelar
          </Button>
          <Button type="submit" isLoading={isLoading}>
            Salvar
          </Button>
        </div>
      </form>
    </Modal>
  );
};

export default CategoriaAddModal;

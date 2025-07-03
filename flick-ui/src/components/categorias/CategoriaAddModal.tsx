// Arquivo: src/components/categorias/CategoriaAddModal.tsx

import React, { useEffect, useState } from 'react';
import { CategoriaResponse } from '../../types/domain';
import { createCategoria } from '../../services/categoriaService';
import { CategoriaRequest } from '../../types/domain';
import Modal from '../common/Modal';
import Input from '../ui/Input';
import Button from '../ui/Button';
import { useTranslation } from 'react-i18next'; // << PASSO 1: Importar o hook

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
  const { t } = useTranslation(); // << PASSO 2: Chamar o hook
  const [nomeCategoria, setNomeCategoria] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!nomeCategoria.trim()) {
      // Usar uma chave para a mensagem de erro de validação
      setError(t('categorias.form.validation.nameRequired')); 
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
    } catch (err: any) {
      console.error('Erro ao adicionar categoria: ', err);
      // Usar uma chave para a mensagem de erro da API
      setError(err.response?.data?.message || t('categorias.form.error.addFailed'));
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
    // << PASSO 3: Substituir os textos estáticos
    <Modal isOpen={isOpen} onClose={onClose} title={t('categorias.form.title')}>
      <form onSubmit={handleSubmit}>
        <div className="p-2">
          <Input
            label={t('categorias.form.name')}
            value={nomeCategoria}
            onChange={(e) => setNomeCategoria(e.target.value)}
            error={error}
            required
            autoFocus
          />
        </div>
        <div className="brand-muted dark:bg-gray-800 px-6 py-4 flex justify-end space-x-3">
          <Button type="button" variant="secondary" onClick={onClose} disabled={isLoading}>
            {t('userActions.cancel')}
          </Button>
          <Button type="submit" isLoading={isLoading}>
            {t('userActions.save')}
          </Button>
        </div>
      </form>
    </Modal>
  );
};

export default CategoriaAddModal;
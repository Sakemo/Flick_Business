import React, { useState, useEffect, useCallback } from 'react';
import Modal from '../common/Modal';
import Button from '../ui/Button';
import Input from '../ui/Input';
import Select from '../ui/Select';
import Textarea from '../ui/Textarea';
import {
  ProdutoRequest,
  ProdutoResponse,
  TipoUnidadeVenda,
  CategoriaResponse,
  FornecedorResponse,
} from '../../types/domain';
import { createProduto, updateProduto } from '../../services/produtoService';
import { getCategorias } from '../../services/categoriaService';
import { getFornecedores } from '../../services/fornecedorService';

interface ProdutoFormModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSaveSuccess: () => void;
  produtoInicial?: ProdutoResponse | null;
}

const ProdutosFormModal: React.FC<ProdutoFormModalProps> = ({
  isOpen,
  onClose,
  onSaveSuccess,
  produtoInicial,
}) => {
  const isEditMode = !!produtoInicial;
  const [formData, setFormData] = useState<Partial<ProdutoRequest>>({});
  const [categorias, setCategorias] = useState<CategoriaResponse[]>([]);
  const [fornecedores, setFornecedores] = useState<FornecedorResponse[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [errors, setErrors] = useState<Record<string, string>>({});

  const fetchDataForSelects = useCallback(async () => {
    try {
      const [cats, forns] = await Promise.all([getCategorias(), getFornecedores()]);
      setCategorias(cats);
      setFornecedores(forns);
    } catch (error) {
      console.error('Erro ao buscar dados para selects: ', error);
      //TODO: lidar com erro mostrando mensagem no modal
    }
  }, []);

  useEffect(() => {
    fetchDataForSelects();
  }, [fetchDataForSelects]);

  useEffect(() => {
    if (isEditMode && produtoInicial) {
      setFormData({
        nome: produtoInicial.nome,
        descricao: produtoInicial.descricao,
        quantidadeEstoque: produtoInicial.quantidadeEstoque,
        precoVenda: produtoInicial.precoVenda,
        precoCustoUnitario: produtoInicial.precoCustoUnitario,
        tipoUnidadeVenda: produtoInicial.tipoUnidadeVenda,
        ativo: produtoInicial.ativo,
        categoriaId: produtoInicial.categoria?.id,
        fornecedorId: produtoInicial.fornecedor?.id,
      });
    } else {
      setFormData({
        ativo: true,
        quantidadeEstoque: 0,
        tipoUnidadeVenda: TipoUnidadeVenda.UNIDADE,
      });
    }
  }, [isEditMode, produtoInicial]);

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>
  ) => {
    const { name, value, type } = e.target;

    if (type === 'checkbox') {
      const { checked } = e.target as HTMLInputElement;
      setFormData((prev) => ({ ...prev, [name]: checked }));
      return;
    }

    if (type === 'number') {
      setFormData((prev) => ({ ...prev, [name]: value === '' ? undefined : parseFloat(value) }));
      return;
    }

    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const validateForm = (): boolean => {
    const newErrors: Record<string, string> = {};
    if (!formData.nome || formData.nome.trim().length < 2) newErrors.nome = 'Nome é obrigatório';
    if (formData.precoVenda === undefined || formData.precoVenda <= 0)
      newErrors.precoVenda = 'Preço de venda é obrigatório';
    if (formData.quantidadeEstoque === undefined)
      newErrors.quantidadeEstoque = 'Estoque não pode ser negativo.';
    if (formData.categoriaId === undefined) newErrors.categoriaId = 'Categoria é obrigatória.';
    if (formData.fornecedorId === undefined) newErrors.fornecedorId = 'Fornecedor é obrigatório.';
    if (formData.tipoUnidadeVenda === undefined)
      newErrors.tipoUnidadeVenda = 'Tipo de Unidade é obrigatório.';

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validateForm()) return;

    setIsLoading(true);
    try {
      const payload: ProdutoRequest = {
        nome: formData.nome!,
        descricao: formData.descricao,
        codigoBarras: formData.codigoBarras,
        quantidadeEstoque: formData.quantidadeEstoque ?? 0,
        precoVenda: formData.precoVenda!,
        precoCustoUnitario: formData.precoCustoUnitario,
        tipoUnidadeVenda: formData.tipoUnidadeVenda!,
        ativo: formData.ativo ?? true,
        categoriaId: formData.categoriaId!,
        fornecedorId: formData.fornecedorId!,
      };

      if (isEditMode && produtoInicial) {
        await updateProduto(produtoInicial.id, payload);
      } else {
        await createProduto(payload);
      }
      onSaveSuccess();
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
    } catch (error: any) {
      console.error('Erro ao salvar o produto: ', error);
      const errorMsg =
        error.response?.data?.message ||
        (isEditMode ? 'Erro ao atualizar produto' : 'Erro ao criar produto');
      setErrors({ form: errorMsg });
      //TODO: Melhorar tratamento
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={isEditMode ? 'Editar Produto' : 'Adicionar Produto'}
    >
      <form onSubmit={handleSubmit}>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 p-6">
          <Input
            label="Nome do Produto *"
            name="nome"
            value={formData.nome || ''}
            onChange={handleChange}
            error={errors.nome}
            maxLength={100}
            required
          />
          <Select
            label="Categoria *"
            name="categoriaId"
            value={formData.categoriaId || ''}
            onChange={handleChange}
            error={errors.categoriaId}
            required
          >
            <option value="">Selecione...</option>
            {categorias.map((cat) => (
              <option key={cat.id} value={cat.id}>
                {cat.nome}
              </option>
            ))}
          </Select>
          <Input
            label="Preço de Venda *"
            name="precoVenda"
            type="number"
            step="0.01"
            min="0.01"
            value={formData.precoVenda || ''}
            onChange={handleChange}
            error={errors.precoVenda}
            required
          />
          <Input
            label="Estoque Atual"
            name="quantidadeEstoque"
            type="number"
            step={0.001}
            min={0}
            value={formData.quantidadeEstoque ?? ''}
            onChange={handleChange}
            error={errors.quantidadeEstoque}
          />
          <Select
            label="Unidade de Venda *"
            name="tipoUnidadeVenda"
            value={formData.tipoUnidadeVenda || ''}
            onChange={handleChange}
            error={errors.tipoUnidadeVenda}
            required
          >
            {Object.values(TipoUnidadeVenda).map((tipo) => (
              <option key={tipo} value={tipo}>
                {tipo}
              </option>
            ))}
          </Select>
          <Select
            label="Fornecedor"
            name="fornecedorId"
            value={formData.fornecedorId || ''}
            onChange={handleChange}
            error={errors.fornecedorId}
          >
            <option value="" disabled>
              Selecione...
            </option>
            {fornecedores.map((f) => (
              <option key={f.id} value={f.id}>
                {f.nome}
              </option>
            ))}
          </Select>
          <Input
            label="Preço de Custo"
            name="precoCustoUnitario"
            type="number"
            step="0.01"
            min="0"
            value={formData.precoCustoUnitario || ''}
            onChange={handleChange}
            error={errors.precoCustoUnitario}
          />
          <Input
            label="Código de Barras"
            name="codigoBarras"
            value={formData.codigoBarras || ''}
            onChange={handleChange}
            error={errors.codigoBarras}
            maxLength={50}
          />
          <Textarea
            label="Descrição"
            name="descricao"
            value={formData.descricao || ''}
            onChange={handleChange}
            error={errors.descricao}
            rows={3}
            wrapperClassName="md:col-span-2"
            maxLength={500}
          />
          <div className="flex items-center md:col-span-2">
            <input
              id="ativo"
              name="ativo"
              type="checkbox"
              checked={formData.ativo ?? true}
              onChange={handleChange}
              className="h-4 w-4 rounded border-gray-300 text-brand-primary focus:ring-brand-primary"
            />
            <label
              htmlFor="ativo"
              className="ml-2 block text-sm text-text-secondary dark:text-gray-300"
            >
              Produto Ativo
            </label>
          </div>

          {errors.form && <p className="text-sm text-red-500 md:col-span-2">{errors.form}</p>}
        </div>

        <div>
          <Button type="button" variant="secondary" onClick={onClose} disabled={isLoading}>
            Cancelar
          </Button>
          <Button type="submit" variant="primary" isLoading={isLoading} disabled={isLoading}>
            {isEditMode ? 'Salvar Alterações' : 'Criar Produto'}
          </Button>
        </div>
      </form>
    </Modal>
  );
};

export default ProdutosFormModal;

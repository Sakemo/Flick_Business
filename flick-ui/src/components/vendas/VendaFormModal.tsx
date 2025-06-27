import React, { useCallback, useEffect, useMemo, useState } from 'react';
import {
  FormaPagamento,
  ItemVendaRequest,
  ProdutoResponse,
  VendaRequest,
  TipoUnidadeVenda
} from '../../types/domain';
import { ClienteResponse } from '../../types/domain';
import { getClientes } from '../../services/clienteService';
import { getProdutos } from '../../services/produtoService';
import { registrarVenda } from '../../services/vendaService';
import { AxiosError } from 'axios';
import Modal from '../common/Modal';
import Select from '../ui/Select';
import Card from '../ui/Card';
import Input from '../ui/Input';
import { formatCurrency } from '../../utils/formatters';
import Button from '../ui/Button';
import { LuPlus, LuTrash2 } from 'react-icons/lu';
import Textarea from '../ui/Textarea';
interface VendaFormModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSaveSuccess: () => void;
}

interface ItemFormState extends ItemVendaRequest {
  //TODO: verificar outros campos para UI
  nomeProduto?: string;
  precoVendaProduto?: number;
}

const VendaFormModal: React.FC<VendaFormModalProps> = ({ isOpen, onClose, onSaveSuccess }) => {
  const [clientes, setClientes] = useState<ClienteResponse[]>([]);
  const [produtosDisponiveis, setProdutosDisponiveis] = useState<ProdutoResponse[]>([]);

  const [clienteId, setClienteId] = useState<string>('');
  const [formaPagamento, setFormaPagamento] = useState<FormaPagamento>(FormaPagamento.DINHEIRO);
  const [observacoes, setObservacoes] = useState<string>('');
  const [itensVenda, setItensVenda] = useState<ItemFormState[]>([]);

  const [produtoSelecionadoId, setProdutoSelecionadoId] = useState<string>('');
  const [quantidadeProduto, setQuantidadeProduto] = useState<string>('1');

  const [isLoading, setIsLoading] = useState(false);
  const [errors, setErrors] = useState<Record<string, string>>({});

  const produtoSelecionado = useMemo(() => {
    if (!produtoSelecionadoId) return null;
    return produtosDisponiveis.find(p => p.id === parseInt(produtoSelecionadoId));

  }, [produtoSelecionadoId, produtosDisponiveis]);

  const fetchData = useCallback(async () => {
    setIsLoading(true);
    try {
      const [clientesData, produtosData] = await Promise.all([
        getClientes({ apenasAtivos: true }),
        getProdutos({}),
        // TODO: getProdutos({ apenasAtivos: true }) [o filtro ainda não esta ativo]
      ]);
      setClientes(clientesData);
      setProdutosDisponiveis(produtosData);
    } catch (error) {
      console.error('Erro ao buscar dados para nova venda: ', error);
      setErrors((prev) => ({ ...prev, form: 'Erro ao carregar dados' }));
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    if (isOpen) {
      fetchData();
      setClienteId('');
      setFormaPagamento(FormaPagamento.DINHEIRO);
      setObservacoes('');
      setItensVenda([]);
      setProdutoSelecionadoId('');
      setQuantidadeProduto('1');
      setErrors({});
    }
  }, [isOpen, fetchData]);

  const handleCountChange = (value: string) => {
    if (produtoSelecionado != null && produtoSelecionado.tipoUnidadeVenda === TipoUnidadeVenda.UNIDADE){
      const totalValue = value.replace(/[^0-9]/g, '');
      setQuantidadeProduto(totalValue);
    } else {
      setQuantidadeProduto(value);
    }
  }

  const handleAddItem = () => {
    const quantidadeProdutoNumber = parseFloat(quantidadeProduto);

    if (produtoSelecionado == null ||!produtoSelecionadoId || isNaN(quantidadeProdutoNumber) || quantidadeProdutoNumber <= 0) {
      setErrors((prev) => ({ ...prev, item: 'Selecione um produto e quantidade válida.' }));
      return; 
    }

    if (produtoSelecionado?.tipoUnidadeVenda === TipoUnidadeVenda.UNIDADE && !Number.isInteger(quantidadeProdutoNumber)){
      setErrors((prev) => ({ ...prev, item: 'Este produto só pode ser vendido em quantidades inteiras.' }));
      return;
    }

    const itemExistenteIndex = itensVenda.findIndex((item) => item.idProduto === produtoSelecionado?.id);

    if (itemExistenteIndex > -1) {
      const novosItens = [...itensVenda];
      novosItens[itemExistenteIndex].quantidade += Number(quantidadeProduto);
      setItensVenda(novosItens);
    } else {
      setItensVenda((prev) => [
        ...prev,
        {
          idProduto: produtoSelecionado.id,
          quantidade: Number(quantidadeProduto),
          nomeProduto: produtoSelecionado.nome,
          precoVendaProduto: produtoSelecionado.precoVenda,
        },
      ]);
    }
    setQuantidadeProduto('1');
    setErrors((prev) => ({ ...prev, item: '' }));
  };

  const handleRemoveItem = (produtoId: number) => {
    setItensVenda((prev) => prev.filter((item) => item.idProduto !== produtoId));
  };

  const calcularSubtotalItem = (item: ItemFormState): number => {
    const produto = produtosDisponiveis.find((p) => p.id === item.idProduto);
    return produto ? produto.precoVenda * item.quantidade : 0;
  };

  const calcularValorTotalVenda = (): number => {
    return itensVenda.reduce((total, item) => total + calcularSubtotalItem(item), 0);
  };

  const validateForm = (): boolean => {
    const newErrors: Record<string, string> = {};
    if (formaPagamento === FormaPagamento.FIADO && !clienteId) {
      newErrors.clienteId = 'Cliente é obrigatório para venda fiado.';
    }
    if (itensVenda.length === 0) {
      newErrors.itens = 'É necessário adicionar pelo menos um item à venda.';
    }
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validateForm()) return;
    setIsLoading(true);

    const payload: VendaRequest = {
      clienteId: clienteId ? parseInt(clienteId) : null,
      formaPagamento,
      observacoes,
      itens: itensVenda.map((item) => ({ idProduto: item.idProduto, quantidade: item.quantidade })),
    };

    try {
      console.log('Venda registrada:', payload);
      await registrarVenda(payload);
      onSaveSuccess();
    } catch (error) {
      const axiosError = error as AxiosError<{ message?: string; errors?: Record<string, string> }>;
      console.error(
        'Resposta do servidor:',
        axiosError.response?.status,
        axiosError.response?.data
      );
      console.error('Erro ao registrar venda: ', error);
      const errorMsg = axiosError.response?.data.message || 'Erro desconhecido ao registrar venda';
      if (axiosError.response?.data?.errors) {
        setErrors(axiosError.response.data.errors);
      }
      setErrors((prev) => ({ ...prev, form: errorMsg }));
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Registrar Venda"
      className="sm:max-w-2xl md:max-w-4xl lg:max-w-5xl"
    >
      <form onSubmit={handleSubmit}>
        <div className="p-6 space-y-6">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <Select
              label="Cliente"
              name="clienteId"
              value={clienteId}
              onChange={(e) => setClienteId(e.target.value)}
              error={errors.clienteId}
            >
              <option value="" className="text-text-secondary dark:text-white">
                Anônimo
              </option>
              {clientes.map((c) => (
                <option key={c.id} value={c.id.toString()}>
                  {c.nome}
                </option>
              ))}
            </Select>
            <Select
              label="Forma de Pagamento"
              name="formaPagamento"
              value={formaPagamento}
              onChange={(e) => setFormaPagamento(e.target.value as FormaPagamento)}
              error={errors.formaPagamento}
              required
            >
              {Object.values(FormaPagamento).map((fp) => (
                <option key={fp} value={fp}>
                  {fp.replace('_', ' ')}
                </option>
              ))}
            </Select>
          </div>

          <Card padding="md">
            <h3 className="text-md font-semibold mb-3 text-text-primary dark:text-white">
              Adiconar item
            </h3>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4 items-end">
              <Select
                label="Produto"
                name="produtoSelecionadoId"
                value={produtoSelecionadoId}
                onChange={(e) => setProdutoSelecionadoId(e.target.value)}
                error={errors.item}
              >
                <option value="">Selecione um produto...</option>
                {produtosDisponiveis.map((produto) => (
                  <option key={produto.id} value={produto.id.toString()}>
                    {produto.nome} por ({formatCurrency(produto.precoVenda)})
                  </option>
                ))}
              </Select>
              <Input
                label="Quantidade"
                type="number"
                min="0"
                step={produtoSelecionado?.tipoUnidadeVenda === TipoUnidadeVenda.UNIDADE ? '1' : '0.001'}
                name="quantidadeProduto"
                value={quantidadeProduto}
                onChange={(e) => handleCountChange(e.target.value)}
                error={errors.item}
              />
              <Button
                type="button"
                onClick={handleAddItem}
                iconLeft={<LuPlus />}
                disabled={isLoading || !produtoSelecionadoId || parseFloat(quantidadeProduto) <= 0}
              >
                Adicionar
              </Button>
            </div>
            {errors.item && <p className="text-xs text-red-500 mt-1">{errors.item}</p>}
          </Card>

          {itensVenda.length > 0 && (
            <Card padding="md">
              <h3 className="text-md font-semibold mb-3 text-text-primary dark:text-white">
                Itens na Venda
              </h3>
              <ul className="divide-y divide-gray-200 dark:divide-gray-700 max-h-60 overflow-y-auto">
                {itensVenda.map((item, index) => {
                  const produtoDetalhe = produtosDisponiveis.find((p) => p.id === item.idProduto);
                  const subtotal = produtoDetalhe ? produtoDetalhe.precoVenda * item.quantidade : 0;
                  return (
                    <li key={index} className="py-3 flex justify-between items-center">
                      <div>
                        <p className="font-medium text-text-primary dark:text-white">
                          {item.nomeProduto || produtoDetalhe?.nome}
                        </p>
                        <p className="text-xs text-text-secondary">
                          {item.quantidade} x
                          {formatCurrency(
                            item.precoVendaProduto || produtoDetalhe?.precoVenda || 0
                          )}
                        </p>
                      </div>
                      <div className="flex items-center space-x-2">
                        <p className="font-medium text-text-primary dark:text-white">
                          {formatCurrency(subtotal)}
                        </p>
                        <Button
                          type="button"
                          onClick={() => handleRemoveItem(item.idProduto)}
                          size="icon"
                          variant="ghost"
                          title="Remover item"
                          className="text-red-500"
                        >
                          <LuTrash2 className="h-4 w-4" />
                        </Button>
                      </div>
                    </li>
                  );
                })}
              </ul>
              <div className="mt-4 pt-4 border-t border-border-light dark:border-border-dark text-right">
                <p className="text-lg font-bold text-text-primary dark:text-white">
                  Valor Total: {formatCurrency(calcularValorTotalVenda())}
                </p>
              </div>
            </Card>
          )}
          {errors.itens && <p className="text-xs text-red-500 mt-1">{errors.itens}</p>}

          <Textarea
            label="Observações"
            name="observacoes"
            value={observacoes}
            onChange={(e) => setObservacoes(e.target.value)}
            rows={2}
          />
          {errors.form && <p className="text-xs text-red-500 mt-1">{errors.form}</p>}
        </div>
        <div className="bg-gray-50 dark:bg-gray-800 px-6 py-4 flex justify-end space-x-2">
          <Button type="button" variant="secondary" onClick={onClose} disabled={isLoading}>
            Cancelar
          </Button>
          <Button
            type="submit"
            variant="primary"
            isLoading={isLoading}
            disabled={isLoading || itensVenda.length === 0}
          >
            Registrar Venda
          </Button>
        </div>
      </form>
    </Modal>
  );
};

export default VendaFormModal;

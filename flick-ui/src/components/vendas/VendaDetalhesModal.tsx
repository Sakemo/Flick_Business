import { format } from 'date-fns';
import { FormaPagamento, VendaResponse } from '../../types/domain';
import Modal from '../common/Modal';
import { ptBR } from 'date-fns/locale';
import Badge from '../ui/Badge';
import { formatCurrency } from '../../utils/formatters';
import Button from '../ui/Button';

interface VendaDetalhesModalProps {
  isOpen: boolean;
  onClose: () => void;
  venda: VendaResponse | null;
}

const VendaDetalhesModal: React.FC<VendaDetalhesModalProps> = ({ isOpen, onClose, venda }) => {
  if (!isOpen || !venda) return null;

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={`Detalhes da Venda #${venda.id}`}
      className="sm:max-w-xl md:max-w-2xl"
    >
      <div className="p-6 space-y-4">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <p className="text-sm font-medium text-text-secondary">Cliente:</p>
            <p className="text-text-primary dark:text-white">{venda.cliente?.nome || 'Anônimo'}</p>
          </div>
          <div>
            <p className="text-sm font-medium text-text-secondary">Data da Venda:</p>
            <p className="text-text-primary dark:text-white">
              {format(new Date(venda.dataVenda), 'dd/MM/yyyy HH:mm:ss', { locale: ptBR })}
            </p>
          </div>
          <div>
            <p className="text-sm font-medium text-text-secondary">Forma de Pagamento:</p>
            <Badge
              colorScheme={venda.formaPagamento == FormaPagamento.DINHEIRO ? 'green' : 'blue'}
              variant="subtle"
            >
              {venda.formaPagamento.replace('_', ' ')}
            </Badge>
          </div>
          <div>
            <p className="text-sm font-medium text-text-secondary">Valor Total:</p>
            <p className="text-lg font-semibold text-brand-primary">
              {formatCurrency(venda.valorTotal)}
            </p>
          </div>
        </div>

        {venda.observacoes && (
          <div>
            <p className="text-sm font-medium text-text-secondary">Observações:</p>
            <p className="text-text-primary p-2 bg-gray-50 dark:bg-gray-700 rounded-md">
              {venda.observacoes}
            </p>
          </div>
        )}

        <div>
          <h4 className="text-md font-semibold text-text-primary dark:text-white mb-2 mt-4">
            Itens da Venda:
          </h4>
          <ul className="divide-y divide-gray-200 dark:divide-gray-700 border rounded-md">
            {venda.itens.map((item) => (
              <li key={item.id} className="p-3 hover:bg-gray-50 dark:hover:bg-gray-700/50">
                <div className="flex justify-between items-center">
                  <div>
                    <p className="font-medium text-text-primary dark:text-white">
                      {item.produto.nome}
                    </p>
                    <p className="text-xs text-text-secondary">
                      {item.quantidade} x{formatCurrency(item.precoUnitarioVenda)}
                    </p>
                  </div>
                  <p className="font-medium text-text-primary dark:text-white">
                    {formatCurrency(item.valorTotalItem)}
                  </p>
                </div>
              </li>
            ))}
          </ul>
        </div>
      </div>
      <div className="bg-gray-50 dark:bg-gray-800 px-6 py-4 flex justify-end">
        <Button variant="secondary" onClick={onClose}>
          Fechar
        </Button>
      </div>
    </Modal>
  );
};
export default VendaDetalhesModal;

// TODO: ADICIONAR AÇÕES (ver recibo?)

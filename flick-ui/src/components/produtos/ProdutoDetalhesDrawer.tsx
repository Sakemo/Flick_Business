import { LuPen, LuX } from 'react-icons/lu';
import { ProdutoResponse } from '../../types/domain';
import Button from '../ui/Button';
import Card from '../ui/Card';
import { formatCurrency } from '../../utils/formatters';
import Badge from '../ui/Badge';
import { format } from 'date-fns';
import { ptBR } from 'date-fns/locale';

interface ProdutoDetalhesDrawerProps {
  produto: ProdutoResponse;
  onClose: () => void;
  onEdit?: (produto: ProdutoResponse) => void;
}

const DetailRow: React.FC<{ label: string; value?: React.ReactNode }> = ({ label, value }) => (
  <div className="py-2 sm:grid sm:grind-cols-4 sm:gap-1">
    <dt className="text-sm font-medium text-text-secondary dark:text-gray-400">{label}</dt>
    <dd className="mt-1 text-sm text-text-primary dark:text-white sm:mt-0 sm:col-span-2">
      {value ?? 'N/A'}
    </dd>
  </div>
);

const ProdutoDetalhesDrawer: React.FC<ProdutoDetalhesDrawerProps> = ({
  produto,
  onClose,
  onEdit,
}) => {
  if (!produto) return null;

  return (
    <Card className="h-full flex flex-col" padding="none">
      <div className="flex items-center justify-between p-4 border-b border-border-light dark:border-border-dark">
        <h2>Detalhes do Produto</h2>
        <div className="flex items-center space-x-2">
          {onEdit && (
            <Button
              variant="ghost"
              size="icon"
              onClick={() => onEdit(produto)}
              title="Editar Produto"
            >
              <LuPen className="h-5 w-5" />
            </Button>
          )}
          <Button variant="ghost" size="icon" onClick={onClose} title="Fechar Detalhes">
            <LuX className="h-5 w-5" />
          </Button>
        </div>
      </div>

      <div className="p-6 flex-grow overflow-y-auto space-y-4">
        <dl className="divide-y divide-gray-200 dark:divide-gray-700">
          <DetailRow label="Nome" value={produto.nome} />
          <DetailRow label="Categoria" value={produto.categoria?.nome} />
          <DetailRow label="Descrição" value={produto.descricao} />
          <DetailRow
            label="Código"
            value={produto.codigoBarras ? produto.codigoBarras : produto.id}
          />
          <DetailRow label="Preço de Venda" value={formatCurrency(produto.precoVenda)} />
          <DetailRow
            label="Custo"
            value={
              produto.precoCustoUnitario
                ? formatCurrency(produto.precoCustoUnitario)
                : 'Não Informado'
            }
          />
          <DetailRow label="Unidade de Venda" value={produto.tipoUnidadeVenda} />
          <DetailRow
            label="Status"
            value={
              <Badge colorScheme={produto.ativo ? 'green' : 'red'}>
                {produto.ativo ? 'Ativo' : 'Inativo'}
              </Badge>
            }
          />
          <DetailRow
            label="Estoque"
            value={`${produto.quantidadeEstoque ?? 0} ${produto.tipoUnidadeVenda}`}
          />
          <DetailRow
            label="Fornecedor"
            value={produto.fornecedor ? produto.fornecedor.nome : 'N/A'}
          />
          <DetailRow
            label="Criado Em"
            value={format(new Date(produto.criadoEm), 'dd/MM/yyyy HH:mm', { locale: ptBR })}
          />
          <DetailRow
            label="Última Atualização"
            value={format(new Date(produto.atualizadoEm), 'dd/MM/yyyy HH:mm', { locale: ptBR })}
          />
        </dl>
      </div>
    </Card>
  );
};

export default ProdutoDetalhesDrawer;

import { format } from 'date-fns';
import { FormaPagamento, VendaResponse } from '../../types/domain';
import Table, { TableColumn } from '../common/Table';
import { ptBR } from 'date-fns/locale';
import Badge, { BadgeColorScheme } from '../ui/Badge';
import { formatCurrency } from '../../utils/formatters';
import Button from '../ui/Button';
import { LuEye } from 'react-icons/lu';

interface VendasTableProps {
  vendas: VendaResponse[];
  onViewDetails: (venda: VendaResponse) => void;
  onDelete?: (id: number) => void; //TODO: Implementar deleção de venda
  selectedRowId?: number | undefined; //TODO: Implementar seleção de linha
}

const BadgeColorByFormaPagamento: Record<FormaPagamento, BadgeColorScheme> = {
  [FormaPagamento.DINHEIRO]: 'green',
  [FormaPagamento.PIX]: 'pix',
  [FormaPagamento.CREDITO]: 'purple',
  [FormaPagamento.DEBITO]: 'blue',
  [FormaPagamento.FIADO]: 'yellow',
};

const VendasTable: React.FC<VendasTableProps> = ({ vendas, onViewDetails, selectedRowId }) => {
  const columns: TableColumn<VendaResponse>[] = [
    { header: 'ID', accessor: 'id', width: 'w-16' },
    {
      header: 'Data',
      accessor: (row) => format(new Date(row.dataVenda), 'dd/MM/yyyy HH:mm', { locale: ptBR }),
    },
    { header: 'Cliente', accessor: (row) => row.cliente?.nome || 'N/A' },
    {
      header: 'Itens',
      accessor: (row) => row.itens.length,
      className: 'text-center',
      headerClassName: 'text-center',
    },
    {
      header: 'Pagamento',
      accessor: (row) => (
        <Badge colorScheme={BadgeColorByFormaPagamento[row.formaPagamento]} variant="subtle">
          {row.formaPagamento.replace('_', ' ')}
        </Badge>
      ),
    },
    {
      header: 'Valor Total',
      accessor: (row) => formatCurrency(row.valorTotal),
      className: 'text-right font-semibold',
      headerClassName: 'text-right',
    },
    {
      header: 'Ações',
      accessor: (row) => (
        <div className="flex justify-end">
          <Button
            variant="ghost"
            size="icon"
            onClick={() => onViewDetails(row)}
            title="Ver detalhes"
          >
            <LuEye className="h-4 w-4" />
          </Button>
        </div>
      ),
      className: 'text-right',
    },
  ];
  return (
    <Table<VendaResponse>
      columns={columns}
      data={vendas}
      emptyMessage="Nenhuma venda encontrada"
      onRowClick={onViewDetails}
      selectedRowId={selectedRowId}
    />
  );
};
export default VendasTable;

//TODO: Implementar seleção de linha
//TODO: ADICONAR AÇÕES DE DELEÇÃO E ATUALIZAÇÃO, E OUTROS

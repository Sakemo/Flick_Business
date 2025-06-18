import { FormaPagamento, VendaResponse } from '../../types/domain';
import Table, { TableColumn } from '../common/Table';
import Badge, { BadgeColorScheme } from '../ui/Badge';
import { formatCurrency, formatVendaDate } from '../../utils/formatters';
import Button from '../ui/Button';
import { LuEye, LuTrash2 } from 'react-icons/lu';

interface VendasTableProps {
  vendas: VendaResponse[];
  onViewDetails: (venda: VendaResponse) => void;
  onDelete: (id: number, vendaDisplayInfo: string) => void;
  selectedRowId?: number | undefined; //TODO: Implementar seleção de linha
}

const BadgeColorByFormaPagamento: Record<FormaPagamento, BadgeColorScheme> = {
  [FormaPagamento.DINHEIRO]: 'green',
  [FormaPagamento.PIX]: 'pix',
  [FormaPagamento.CREDITO]: 'purple',
  [FormaPagamento.DEBITO]: 'blue',
  [FormaPagamento.FIADO]: 'yellow',
};

const VendasTable: React.FC<VendasTableProps> = ({
  vendas,
  onViewDetails,
  selectedRowId,
  onDelete,
}) => {
  const columns: TableColumn<VendaResponse>[] = [
    { header: 'ID', accessor: 'id', width: 'w-16' },
    {
      header: 'Data',
      accessor: (row) => formatVendaDate(row.dataVenda),
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
          <Button
            variant="ghost"
            size="icon"
            onClick={() => onDelete(row.id, `#${row.id} de ${formatCurrency(row.valorTotal)}`)}
            className="text-red-500 hover:text-red-700 hover:bg-red-100 dark:hover-bg-red-700/50"
            title="Deletar Venda"
          >
            <LuTrash2 className="h-4 w-4" />
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

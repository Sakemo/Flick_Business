import { FormaPagamento, VendaResponse } from '../../types/domain';
import Table, { TableColumn } from '../common/Table';
import Badge, { BadgeColorScheme } from '../ui/Badge';
import { formatCurrency, formatVendaDate } from '../../utils/formatters';
import Button from '../ui/Button';
import { LuEye, LuTrash2 } from 'react-icons/lu';
import { TableRow } from '../../hooks/GroupHeader';
import clsx from 'clsx';
import { useTranslation } from 'react-i18next';

interface VendasTableProps {
  vendas: TableRow[];
  onViewDetails: (venda: VendaResponse) => void;
  onDelete: (id: number, vendaDisplayInfo: string) => void;
  selectedRowId?: number | undefined;
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

  const getTimeRow = (row:any): string[] => {
    return formatVendaDate(row.dataVenda, true);
  }

  const { t } = useTranslation();
  const columns: TableColumn<VendaResponse>[] = [
    { header: 'ID', accessor: 'id', width: 'w-16' },
    {
      header: t('common.date'),
      accessor: (row) => {
        const [day, time] = getTimeRow(row);

        if (day === '1') return t('filter.today') + time;
        if (day === '0') return t('filter.yesterday') + time;
        return [day, time];
      },
    },
    { header: t('common.client'), accessor: (row) => row.cliente?.nome || 'N/A' },
    {
      header: t('common.items'),
      accessor: (row) => row.itens.length,
      className: 'text-center',
      headerClassName: 'text-center',
    },
    {
      header: t('common.payment'),
      accessor: (row) => (
        <Badge colorScheme={BadgeColorByFormaPagamento[row.formaPagamento]} variant="subtle">
          {row.formaPagamento.replace('_', ' ')}
        </Badge>
      ),
    },
    {
      header: t('vendas.form.totalValue'),
      accessor: (row) => formatCurrency(row.valorTotal),
      className: 'text-right font-semibold',
      headerClassName: 'text-right',
    },
    {
      header: t('common.actions'),
      accessor: (row) => (
        <div className="flex justify-end">
          <Button
            variant="ghost"
            size="icon"
            onClick={() => onViewDetails(row)}
            title={t('common.details')}
          >
            <LuEye className="h-4 w-4" />
          </Button>
          <Button
            variant="ghost"
            size="icon"
            onClick={() => onDelete(row.id, `#${row.id} de ${formatCurrency(row.valorTotal)}`)}
            className="text-red-500 hover:text-red-700 hover:bg-red-100 dark:hover-bg-red-700/50"
            title={t('userActions.delete')}
          >
            <LuTrash2 className="h-4 w-4" />
          </Button>
        </div>
      ),
      className: 'text-right',
    },
  ];
  return (
    <Table<TableRow>
      columns={columns}
      data={vendas}
      emptyMessage={t('siteFeedback.noData')}
      selectedRowId={selectedRowId}
      renderRow={(item, cols) => {
        if ('isGroupHeader' in item){
          return (
            <tr key={item.groupKey} className='bg-gray-100 dark:bg-gray-800 border-y border-gray-300 dark:border-gray-600 sticky top-0 z-10'>
              <td colSpan={cols.length} className="px-4 py-2 font-semibold text-text-secondary dark:text-gray-200">
                <div className='flex justify-between'>
                <span className='text-sm'>{item.title}</span>
                <span className='text-base text-brand-primary dark:text-brand-accent'>{formatCurrency(item.value)}</span>
                </div>
              </td>
            </tr>
          );
        }

        const venda = item;
        return (
          <tr key={venda.id} onClick={() => onViewDetails(venda)} className={clsx('hover:brand-muted dark:hover:bg-gray-700/40 transition-colors cursor-pointer', selectedRowId === venda.id && 'bg-brand-muted/50 dark:bg-gray-700')}>
            {cols.map((col, index) => (
              <td key={index} className={clsx(
                'px-4 py-3 whitespace-nowrap text-sm text-text-primary dark:text-gray-200', col.className)}>
                {typeof col.accessor === 'function' ? col.accessor(venda) : String(venda[col.accessor as keyof VendaResponse] ?? '')}
              </td>
            ))}
          </tr>
        )
      }}
    />
  );
};
export default VendasTable;

//TODO: Implementar seleção de linha
//TODO: ADICONAR AÇÕES DE DELEÇÃO E ATUALIZAÇÃO, E OUTROS

import { format } from 'date-fns';
import { DespesaResponse } from '../../types/domain';
import { formatCurrency } from '../../utils/formatters';
import Table, { TableColumn } from '../common/Table';
import Badge from '../ui/Badge';
import { enUS, ptBR } from 'date-fns/locale';
import Button from '../ui/Button';
import { LuPencil, LuTrash2 } from 'react-icons/lu';
import { useTranslation } from 'react-i18next';
import i18n from '../../i18n';

interface DespesasTableProps {
  despesas: DespesaResponse[];
  onEdit: (despesa: DespesaResponse) => void;
  onDelete: (id: number) => void;
  onRowClick: (despesa: DespesaResponse) => void;
  selectedRowId: number | undefined;
}

const DespesasTable: React.FC<DespesasTableProps> = ({ despesas, onEdit, onDelete, onRowClick, selectedRowId }) => {
  const { t } = useTranslation();
    const currentLocale = i18n.language.startsWith('pt') ? ptBR : enUS;

  const columns: TableColumn<DespesaResponse>[] = [
    { header: t('common.name'), accessor: 'nome', headerClassName: 'w-2/5' },
    {
      header: t('common.value'),
      accessor: (row) => formatCurrency(row.valor),
      className: 'text-right',
      headerClassName: 'text-right',
    },
    {
      header: t('common.category'),
      accessor: (row) => (
        <Badge
          colorScheme={
            row.tipoDespesa === 'EMPRESARIAL'
              ? 'blue'
              : row.tipoDespesa === 'INVESTIMENTO'
                ? 'purple'
                : row.tipoDespesa === 'PESSOAL'
                  ? 'yellow'
                  : 'red'
          }
        >
          {row.tipoDespesa.charAt(0).toUpperCase() + row.tipoDespesa.slice(1).toLowerCase()}
        </Badge>
      ),
    },
    {
      header: t('common.date'),
      accessor: (row) => format(new Date(row.dataDespesa), `${currentLocale === enUS ? 'MM/dd/yyyy' : 'dd/MM/yyyy'}`, { locale: currentLocale }),
    },
    {
      header: t('common.actions'),
      accessor: (row) => (
        <div className="flex justify-end space-x-2">
          <Button variant="ghost" size="icon" onClick={(e) => { e.stopPropagation(); onEdit(row)}} title={t('userActions.edit')}>
            <LuPencil className="h-4 w-4" />
          </Button>
          <Button
            variant="ghost"
            size="icon"
            onClick={(e) => {e.stopPropagation();  onDelete(row.id)}}
            title= {t('userActions.delete')}
            className="hover:bg-red-100 dark:hover:bg-red-700/50"
          >
            <LuTrash2 className="h-4 w-4" />
          </Button>
        </div>
      ),
      className: 'text-right',
    },
  ];

  return (
    <Table<DespesaResponse>
      columns={columns}
      data={despesas}
      emptyMessage= {t('siteFeedback.noData')}
      selectedRowId={selectedRowId}
      onRowClick={onRowClick}
    />
  );
};

export default DespesasTable;

import { format } from 'date-fns';
import { DespesaResponse } from '../../types/domain';
import { formatCurrency } from '../../utils/formatters';
import Table, { TableColumn } from '../common/Table';
import Badge from '../ui/Badge';
import { ptBR } from 'date-fns/locale';
import Button from '../ui/Button';
import { LuPencil, LuTrash2 } from 'react-icons/lu';

interface DespesasTableProps {
  despesas: DespesaResponse[];
  onEdit: (despesa: DespesaResponse) => void;
  onDelete: (id: number) => void;
}

const DespesasTable: React.FC<DespesasTableProps> = ({ despesas, onEdit, onDelete }) => {
  const columns: TableColumn<DespesaResponse>[] = [
    { header: 'Nome', accessor: 'nome', headerClassName: 'w-2/5' },
    {
      header: 'Valor',
      accessor: (row) => formatCurrency(row.valor),
      className: 'text-right',
      headerClassName: 'text-right',
    },
    {
      header: 'Tipo',
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
      header: 'Data Despesa',
      accessor: (row) => format(new Date(row.dataDespesa), 'dd/MM/yyyy', { locale: ptBR }),
    },
    {
      header: 'Ações',
      accessor: (row) => (
        <div className="flex justify-end space-x-2">
          <Button variant="ghost" size="icon" onClick={() => onEdit(row)} title="Editar">
            <LuPencil className="h-4 w-4" />
          </Button>
          <Button
            variant="ghost"
            size="icon"
            onClick={() => onDelete(row.id)}
            title="Deletar"
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
      emptyMessage="Nenhuma despesa registrada"
      selectedRowId={undefined}
    />
  );
};

export default DespesasTable;

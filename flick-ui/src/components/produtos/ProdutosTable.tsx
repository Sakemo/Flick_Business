import React from 'react';
import { ProdutoResponse } from '../../types/domain';
import Table, { TableColumn } from '../common/Table';
import Button from '../ui/Button';
import Badge from '../ui/Badge';
import { LuCheck, LuCopy, LuDelete, LuPencil, LuTrash2 } from 'react-icons/lu';
import { format } from 'date-fns';
import { formatCurrency } from '../../utils/formatters';
import { useTranslation } from 'react-i18next';


interface ProdutosTableProps {
  produtos: ProdutoResponse[];
  onEdit: (produto: ProdutoResponse) => void;
  onDelete: (id: number, nomeProduto: string, status: boolean) => void;
  onDeletePerm: (id: number, nomeProduto: string) => void;
  onRowClick: (produto: ProdutoResponse) => void;
  onCopy: (id: number) => void;
  isLoading?: boolean;
  selectedRowId: number | undefined;
}

const ProdutosTable: React.FC<ProdutosTableProps> = ({
  produtos,
  onEdit,
  onDelete,
  onDeletePerm,
  onRowClick,
  onCopy,
  isLoading,
  selectedRowId,
}) => {
  const { t } = useTranslation();
  const columns: TableColumn<ProdutoResponse>[] = [
    {
      header: t('common.name'),
      accessor: 'nome',
      headerClassName: 'w-1/4',
    },
    {
      header: t('common.category'),
      accessor: (row) => row.categoria?.nome || 'N/A',
    },
    {
      header: t('produtos.form.salesPrice'),
      accessor: (row) => formatCurrency(row.precoVenda),
      className: 'text-right',
      headerClassName: 'text-right',
    },
    {
      header: t('common.status'),
      accessor: (row) => (
        <Badge colorScheme={row.ativo ? 'green' : 'red'}>{row.ativo ? 'Ativo' : 'Inativo'}</Badge>
      ),
      className: 'text-center',
      headerClassName: 'text-center',
    },
    {
      header: t('common.stock'),
      accessor: (row) => `${row.quantidadeEstoque} ${row.tipoUnidadeVenda}`,
      className: 'text-center',
      headerClassName: 'text-center',
    },
    {
      header: t('produtos.lastUpdate'),
      accessor: (row) => format(new Date(row.atualizadoEm), 'dd/MM/yyyy HH:mm'),
    },
    {
      header: t('common.actions'),
      accessor: (row) => (
        <div className="flex justify-end space-x-1">
          <Button
            variant="ghost"
            size="icon"
            onClick={(e) => {
              e.stopPropagation();
              onEdit(row);
            }}
            title={t('userActions.edit')}
          >
            <LuPencil className="h-4 w-4" />
          </Button>
          <Button
            variant='ghost'
            size='icon'
            onClick={(e) => {
              e.stopPropagation();
              onCopy(row.id);
            }}
            title={t('userActions.copy')}
          >
            <LuCopy className='h-4 w-4' />
          </Button>
          <Button
            variant="ghost"
            size="icon"
            onClick={(e) => {
              e.stopPropagation();
              onDelete(row.id, row.nome, row.ativo);
            }}
            className="text-red-500 hover:bg-red-100 dark:hover:gb-red-700/50"
            title={row.ativo ? t('userActions.inactivate') : t('userActions.activate')}
          >
            {row.ativo ? <LuDelete className="h-4 w-4" /> : <LuCheck className="h-4 w-4" />}
          </Button>
          <Button
            variant="danger"
            size="icon"
            onClick={(e) => {
              e.stopPropagation();
              onDeletePerm(row.id, row.nome);
            }}
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
    <Table<ProdutoResponse>
      columns={columns}
      data={produtos}
      isLoading={isLoading}
      emptyMessage={t('siteFeedback.noData')}
      onRowClick={onRowClick}
      selectedRowId={selectedRowId}
    />
  );
};

export default ProdutosTable;

import React from 'react';
import { ProdutoResponse } from '../../types/domain';
import Table, { TableColumn } from '../common/Table';
import Button from '../ui/Button';
import Badge from '../ui/Badge';
import { LuCheck, LuDelete, LuPencil, LuTrash2 } from 'react-icons/lu';
import { format } from 'date-fns';
import { formatCurrency } from '../../utils/formatters';

interface ProdutosTableProps {
  produtos: ProdutoResponse[];
  onEdit: (produto: ProdutoResponse) => void;
  onDelete: (id: number, nomeProduto: string, status: boolean) => void;
  onDeletePerm: (id: number, nomeProduto: string) => void;
  isLoading?: boolean;
  onRowClick: (produto: ProdutoResponse) => void;
  selectedRowId: number | undefined;
}

const ProdutosTable: React.FC<ProdutosTableProps> = ({
  produtos,
  onEdit,
  onDelete,
  onDeletePerm,
  isLoading,
  onRowClick,
  selectedRowId,
}) => {
  const columns: TableColumn<ProdutoResponse>[] = [
    {
      header: 'Nome',
      accessor: 'nome',
      headerClassName: 'w-1/4',
    },
    {
      header: 'Categoria',
      accessor: (row) => row.categoria?.nome || 'N/A',
    },
    {
      header: 'Preço de Venda',
      accessor: (row) => formatCurrency(row.precoVenda),
      className: 'text-right',
      headerClassName: 'text-right',
    },
    {
      header: 'Status',
      accessor: (row) => (
        <Badge colorScheme={row.ativo ? 'green' : 'red'}>{row.ativo ? 'Ativo' : 'Inativo'}</Badge>
      ),
      className: 'text-center',
      headerClassName: 'text-center',
    },
    {
      header: 'Estoque',
      accessor: (row) => `${row.quantidadeEstoque} ${row.tipoUnidadeVenda}`,
      className: 'text-center',
      headerClassName: 'text-center',
    },
    {
      header: 'Últ. Atualização',
      accessor: (row) => format(new Date(row.atualizadoEm), 'dd/MM/yyyy HH:mm'),
    },
    {
      header: 'Ações',
      accessor: (row) => (
        <div className="flex justify-end space-x-2">
          <Button
            variant="ghost"
            size="icon"
            onClick={(e) => {
              e.stopPropagation();
              onEdit(row);
            }}
            title="Editar"
          >
            <LuPencil className="h-4 w-4" />
          </Button>
          <Button
            variant="ghost"
            size="icon"
            onClick={(e) => {
              e.stopPropagation();
              onDelete(row.id, row.nome, row.ativo);
            }}
            className="text-red-500 hover:bg-red-100 dark:hover:gb-red-700/50"
            title={row.ativo ? 'Desativar' : 'Ativar'}
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
            title="Deletar"
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
      emptyMessage="Nenhum produto registrado"
      onRowClick={onRowClick}
      selectedRowId={selectedRowId}
    />
  );
};

export default ProdutosTable;

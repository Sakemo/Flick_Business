import React from 'react';
import clsx from 'clsx';

export interface TableColumn<T> {
  header: string;
  accessor: keyof T | ((row: T) => React.ReactNode);
  className?: string;
  headerClassName?: string;
  width?: string;
}

interface TableProps<T> {
  columns: TableColumn<T>[];
  data: T[];
  isLoading?: boolean;
  emptyMessage?: string;
  className?: string;
  onRowClick?: (row: T) => void;
}
function Table<T extends { id: number | string }>({
  columns,
  data,
  isLoading = false,
  emptyMessage = 'Nenhum dado encontrado',
  className,
  onRowClick,
}: TableProps<T>) {
  const renderCellContent = (row: T, column: TableColumn<T>) => {
    if (typeof column.accessor === 'function') {
      return column.accessor(row);
    }
    return String(row[column.accessor] ?? '');
  };

  return (
    <div className="overflow-x-auto rounded-lg border border-gray-200 dark:border-gray-700 shadow-soft">
      <table
        className={clsx(
          'min-w-full divide-y divide-gray-200 dark:divide-gray-700 bg-card-light dark:bg-card-dark',
          className
        )}
      >
        <thead className="bg-gray-50 dark:bg-gray-700/50">
          <tr>
            {columns.map((col, index) => (
              <th
                key={index}
                scope="col"
                className={clsx(
                  'px-4 py-3 text-left text-xs font-semibold text-text-secondary dark:text-gray-400 uppercase tracking-wider',
                  col.headerClassName,
                  col.width
                )}
              >
                {col.header}
              </th>
            ))}
          </tr>
        </thead>
        <tbody className="divide-y divide-gray-200 dark:divide-gray-700/50">
          {isLoading ? (
            <tr>
              <td
                colSpan={columns.length}
                className="p-8 text-center text-text-secondary dark:text-gray-400"
              >
                Carregando dados {/*TODO: adicionar um spinner ou algo assim*/}
              </td>
            </tr>
          ) : data.length === 0 ? (
            <tr>
              <td
                colSpan={columns.length}
                className="p-8 text-center text-text-secondary dark:text-gray-400"
              >
                {emptyMessage}
              </td>
            </tr>
          ) : (
            data.map((row) => (
              <tr
                key={row.id}
                className={clsx(
                  'hover:=bg-gray-50 dark:hover:bg-gray-700/40 transition-colors',
                  onRowClick && 'cursor-pointer'
                )}
              >
                {columns.map((col, index) => (
                  <td
                    key={index}
                    className={clsx(
                      'px-4 py-3 whitespace-nowrap text-sm text-text-primary dark:text-gray-200',
                      col.className
                    )}
                  >
                    {renderCellContent(row, col)}
                  </td>
                ))}
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  );
}

export default Table;

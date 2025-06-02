import { format } from 'date-fns';
import { ClienteResponse, ConfiguracaoGeralResponse } from '../../types/domain';
import { calcularStatusFiado, CalculoFiadoResult } from '../../utils/fiadoUtils';
import { formatCurrency } from '../../utils/formatters';
import Badge from '../ui/Badge';
import Card from '../ui/Card';
import { ptBR } from 'date-fns/locale';
import Button from '../ui/Button';
import { LuCheck, LuDelete, LuPen, LuTrash2 } from 'react-icons/lu';

interface ClienteCardProps {
  cliente: ClienteResponse;
  configFiado: ConfiguracaoGeralResponse | null;
  onEdit: () => void;
  onToggleAtivo: () => void;
  onDeletePermanente: () => void;
}

const ClienteCard: React.FC<ClienteCardProps> = ({
  cliente,
  configFiado,
  onEdit,
  onToggleAtivo,
  onDeletePermanente,
}) => {
  const statusFiadoInfo = calcularStatusFiado(cliente, configFiado);

  const getStatusFiadoBadge = (statusInfo: CalculoFiadoResult) => {
    switch (statusInfo.status) {
      case 'ATRASADO':
        return (
          <Badge
            colorScheme="red"
            variant="solid"
          >{`Atrasado ${statusInfo.diasParaVencer ? `(${Math.abs(statusInfo.diasParaVencer)} dias)` : ''}`}</Badge>
        );
      case 'A_VENCER': {
        let textoVencer = 'A Vencer';
        if (statusInfo.diasParaVencer === 0) textoVencer = 'Vence Hoje';
        return (
          <Badge colorScheme="yellow" variant="subtle">
            {textoVencer}
          </Badge>
        );
      }
      case 'EM_DIA': {
        let textoVencer = '';
        if (statusInfo.diasParaVencer && statusInfo.diasParaVencer > 0)
          textoVencer = `Vence em ${statusInfo.diasParaVencer} dias`;
        else if (statusInfo.valorComJuros === 0) textoVencer = `EM DIA`;
        return (
          <Badge colorScheme="green" variant="subtle">
            {textoVencer}
          </Badge>
        );
      }
      default:
        return null;
    }
  };

  return (
    <Card className="flex flex-col justify-between h-full" padding="md">
      <div>
        <div className="flex justify-between items-start mb-2">
          <h3
            className="text-lg font-semibold text-text-primary dark:text-white mr-2 truncate"
            title={cliente.nome}
          >
            {cliente.nome}
          </h3>
          <Badge colorScheme={cliente.ativo ? 'green' : 'gray'} variant="outline">
            {cliente.ativo ? 'Ativo' : 'Inativo'}
          </Badge>
        </div>

        {statusFiadoInfo.status !== 'NÃO_APLICAVEL' && cliente.saldoDevedor > 0 && (
          <div className="mb-2">{getStatusFiadoBadge(statusFiadoInfo)}</div>
        )}

        <p
          className="text-sm text-text-secondary dark:text-gray-400 truncate"
          title={cliente.telefone}
        >
          Tel: {cliente.telefone || 'N/A'}
        </p>
        <p
          className="text-sm text-text-secondary dark:text-gray-400 truncate"
          title={cliente.endereco}
        >
          End.: {cliente.endereco || 'N/A'}
        </p>
        <p className="text-sm text-text-secondary dark:text-gray-400">
          CPF: {cliente.cpf || 'N/A'}
        </p>

        {cliente.controleFiado && (
          <div className="mt-3 pt-3 border-t border-border-light dark:border-border-dark">
            <p className="text-xs text-text-secondary dark:text-gray-500">
              Controle de Fiado Ativo
            </p>
            <p className="text-sm font-medium text-text-primary dark:text-white">
              Saldo Devedor:{' '}
              <span className={cliente.saldoDevedor > 0 ? 'text-red-500' : ''}>
                {formatCurrency(cliente.saldoDevedor)}
              </span>
              {statusFiadoInfo.status === 'ATRASADO' &&
                statusFiadoInfo.valorComJuros &&
                statusFiadoInfo.valorComJuros > cliente.saldoDevedor && (
                  <span className="text-xs text-red-400 ml-1">
                    {formatCurrency(statusFiadoInfo.valorComJuros)}
                  </span>
                )}
            </p>
            {statusFiadoInfo.dataVencimento && (
              <p className="text-xs text-text-secondary dark:text-gray-400">
                Vencimento: {format(statusFiadoInfo.dataVencimento, 'dd/MM/yyyy', { locale: ptBR })}
              </p>
            )}
            <p className="text-xs text-text-secondary dark:text-gray-400">
              Limite Fiado:{' '}
              {cliente.limiteFiado ? formatCurrency(cliente.limiteFiado) : 'Não definido'}
            </p>
          </div>
        )}
      </div>

      <div className="mt-4 pt-4 border-t border-border-light dark:border-border-dark flex items-center justify-end space-x-2">
        <Button variant="ghost" size="icon" onClick={onEdit} title="Editar Cliente">
          <LuPen className="h-4 w-4" />
        </Button>
        <Button
          variant="ghost"
          size="icon"
          onClick={onToggleAtivo}
          title={cliente.ativo ? 'Inativar Cliente' : 'Ativar Cliente'}
          className={
            cliente.ativo
              ? 'text-yellow-500 hover:text-yellow-600'
              : 'text-green-500 hover:text-green-600'
          }
        >
          {cliente.ativo ? <LuDelete className="h-4 w-4" /> : <LuCheck className="h-4 w-4" />}
        </Button>
        <Button
          variant="danger"
          size="icon"
          onClick={onDeletePermanente}
          title="Deletar Permanentemente"
        >
          <LuTrash2 className="h-4 w-4" />
        </Button>
      </div>
    </Card>
  );
};
export default ClienteCard;

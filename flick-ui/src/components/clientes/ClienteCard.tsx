import { format } from 'date-fns';
import { ClienteResponse, ConfiguracaoGeralResponse } from '../../types/domain';
import { calcularStatusFiado, CalculoFiadoResult } from '../../utils/fiadoUtils';
import { formatCurrency } from '../../utils/formatters';
import Badge from '../ui/Badge';
import Card from '../ui/Card';
import { enUS, ptBR } from 'date-fns/locale';
import Button from '../ui/Button';
import { LuCheck, LuDelete, LuPen, LuTrash2 } from 'react-icons/lu';

import { useTranslation } from 'react-i18next';

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
  const { t, i18n } = useTranslation(); 

  const statusFiadoInfo = calcularStatusFiado(cliente, configFiado);
  const currentLocale = i18n.language.startsWith('pt') ? ptBR : enUS;

  const getStatusFiadoBadge = (statusInfo: CalculoFiadoResult) => {
    switch (statusInfo.status) {
      case 'ATRASADO':
        return (
          <Badge
            colorScheme="red"
            variant="solid"
          >{`Atrasado ${t('clientes.status.overdue', { count: Math.abs(statusInfo.diasParaVencer || 0) })}`}</Badge>
        );
      case 'A_VENCER': {
        let textoVencer = t('clientes.status.dueSoon');
        if (statusInfo.diasParaVencer === 0) textoVencer = t('clientes.status.dueToday');
        return (
          <Badge colorScheme="yellow" variant="subtle">
            {textoVencer}
          </Badge>
        );
      }
      case 'EM_DIA': {
        let textoVencer = '';
        if (statusInfo.diasParaVencer && statusInfo.diasParaVencer > 0)
          textoVencer = `${t('clientes.status.dueIn', { count: statusInfo.diasParaVencer })}`;
        else if (statusInfo.valorComJuros === 0) textoVencer = t('clientes.status.onTime');
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
            {cliente.ativo ? t('siteFeedback.active') : t('siteFeedback.inactive')}
          </Badge>
        </div>

        {statusFiadoInfo.status !== 'NÃO_APLICAVEL' && cliente.saldoDevedor > 0 && (
          <div className="mb-2">{getStatusFiadoBadge(statusFiadoInfo)}</div>
        )}

        <p
          className="text-sm text-text-secondary dark:text-gray-400 truncate"
          title={cliente.telefone}
        >
          {t('clientes.form.phone')}: {cliente.telefone || 'N/A'}
        </p>
        <p
          className="text-sm text-text-secondary dark:text-gray-400 truncate"
          title={cliente.endereco}
        >
          {t('clientes.form.address')}: {cliente.endereco || 'N/A'}
        </p>
        <p className="text-sm text-text-secondary dark:text-gray-400">
          {t('clientes.form.cpf')}: {cliente.cpf || 'N/A'}
        </p>

        {cliente.controleFiado && (
          <div className="mt-3 pt-3 border-t border-border-light dark:border-border-dark">
            <p className="text-xs text-text-secondary dark:text-gray-500">
              {t('clientes.form.creditControlTitle')}
            </p>
            <p className="text-sm font-medium text-text-primary dark:text-white">
              {t('clientes.form.debtBalance')}: {' '}
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
                {t('clientes.form.dueDate')}: {format(statusFiadoInfo.dataVencimento, `${currentLocale === enUS ? 'MM/dd/yyyy' : 'dd/MM/yyyy'}`, { locale: currentLocale })}
              </p>
            )}
            <p className="text-xs text-text-secondary dark:text-gray-400">
              {t('clientes.form.creditLimit')}: {' '}
              {cliente.limiteFiado ? formatCurrency(cliente.limiteFiado) : t('clientes.notSet')}
            </p>
          </div>
        )}
      </div>

      <div className="mt-4 pt-4 border-t border-border-light dark:border-border-dark flex items-center justify-end space-x-2">
        <Button variant="ghost" size="icon" onClick={onEdit} title={t('userActions.edit') + ' ' + t('clientes.objectName')}>
          <LuPen className="h-4 w-4" />
        </Button>
        <Button
          variant="ghost"
          size="icon"
          onClick={onToggleAtivo}
          title={cliente.ativo ? t('userActions.deactivate') : t('userActions.activate')}
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
          title={t('userActions.deletePermanent')}
        >
          <LuTrash2 className="h-4 w-4" />
        </Button>
      </div>
    </Card>
  );
};
export default ClienteCard;

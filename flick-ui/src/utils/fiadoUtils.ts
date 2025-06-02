import { addMonths, differenceInCalendarMonths, isPast, isToday } from "date-fns";
import { ClienteResponse, ConfiguracaoGeralResponse } from "../types/domain";

// Define os possíveis status do fiado
export type StatusFiado = 'EM_DIA' | 'A_VENCER' | 'ATRASADO' | 'NÃO_APLICAVEL';

// Interface do resultado do cálculo do fiado
export interface CalculoFiadoResult {
  status: StatusFiado;
  dataVencimento?: Date | null;
  diasParaVencer?: number | null;
  valorComJuros?: number | null;
}

// Função principal para calcular o status do fiado
export const calcularStatusFiado = (
  cliente: ClienteResponse,
  config: ConfiguracaoGeralResponse | null
): CalculoFiadoResult => {
  // Se não controla fiado, não tem data de última compra ou não deve nada, não se aplica
  if(!cliente.controleFiado || !cliente.dataUltimaCompraFiado || cliente.saldoDevedor <= 0){
    return { status: 'NÃO_APLICAVEL' };
  }

  // Obtém configurações de prazo e taxa de juros
  const prazoPagamentoMeses = config?.prazoPagamentoFiado ?? null;
  const taxasJurosMensal = config?.taxaJurosAtraso ?? null;

  // Se não há prazo definido, considera em dia
  if(!prazoPagamentoMeses){
    return { status: 'EM_DIA', dataVencimento: null, valorComJuros: cliente.saldoDevedor  };
  }

  // Calcula data de vencimento
  const dataUltimaCompra = new Date(cliente.dataUltimaCompraFiado);
  const dataVencimento = addMonths(dataUltimaCompra, prazoPagamentoMeses);
  const hoje = new Date();

  // Ajusta horários para evitar problemas de comparação
  dataVencimento.setHours(23, 59, 59, 999);
  hoje.setHours(0,0,0,0);
  
  // Calcula diferença em dias para o vencimento
  const diffDias = Math.ceil((dataVencimento.getTime() - hoje.getTime())/(1000*60*60*24));

  // Se já passou do vencimento e não é hoje, está atrasado
  if(isPast(dataVencimento) && !isToday(dataVencimento)){
    let valorComJuros = cliente.saldoDevedor;
    if(taxasJurosMensal && taxasJurosMensal > 0){
      const mesesAtraso = Math.max(0, differenceInCalendarMonths(new Date(), dataVencimento));
      if(mesesAtraso > 0){
        const juros = (cliente.saldoDevedor * (taxasJurosMensal / 100)) * mesesAtraso;
        valorComJuros = cliente.saldoDevedor + juros;
      }
    }
    return { status: 'ATRASADO', dataVencimento, diasParaVencer: diffDias, valorComJuros };
  } else if (isToday(dataVencimento)){
    // Se vence hoje
    return { status: 'A_VENCER', dataVencimento, diasParaVencer: diffDias, valorComJuros: cliente.saldoDevedor };
  } else {
    // Ainda está em dia
    return { status: 'EM_DIA', dataVencimento, diasParaVencer:diffDias, valorComJuros: cliente.saldoDevedor };
  }
};
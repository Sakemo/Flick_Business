import { format, isToday, isYesterday, parseISO } from "date-fns";
import { ptBR } from "date-fns/locale";

export const formatCurrency = (value: number | null | undefined): string => {
  if(value === null || value === undefined){
    return 'R$0,00';
  }
  return new Intl.NumberFormat('pt-BR', {
    style: 'currency',
    currency: 'BRL',
  }).format(value);
};

export const formatVendaDate = (dateString: string | null | undefined): string => {
  if (!dateString) {
    return 'Data inválida';
  }
  let dateObj: Date;
  try {
    dateObj = parseISO(dateString);
    if (isNaN(dateObj.getTime())) {
      dateObj = new Date(dateString);
      if (isNaN(dateObj.getTime())){
        return 'Data inválida'
      }
    }

  } catch (error){
    console.error("Erro ao parsear data: ", dateString, error);
    return `${error}`  
  }

  if (isToday(dateObj)){
    return `Hoje ás ${format(dateObj, 'HH:mm', { locale: ptBR })}`;
  } else if (isYesterday(dateObj)){
    return `Ontem ás ${format(dateObj, 'HH:mm', { locale: ptBR })}`;
  } else {
    return format(dateObj, 'dd/MM/yyyy ás HH:mm', { locale:ptBR })
  }
} 
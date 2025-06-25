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

export const formatVendaDate = (dateString: string | null | undefined, hours:boolean | null | undefined): string => {
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

  const today = isToday(dateObj);
  const yesterday = isYesterday(dateObj);

  if (today){
    return hours ? `Hoje às ${format(dateObj, 'HH:mm', { locale: ptBR })}`
    : `Hoje`;
  }

  if (yesterday){
    return hours ? `Ontem às ${format(dateObj, 'HH:mm', { locale: ptBR })}` 
    : `Ontem`;
  }

  return format (dateObj, hours ? 'dd/MM/yyyy às HH:mm' : 'dd/MM/yyyy', {locale: ptBR})

} 
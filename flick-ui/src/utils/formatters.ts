import { format, isToday, isYesterday, parseISO } from "date-fns";
import { ptBR } from "date-fns/locale";
import i18n from "../i18n";

export const formatCurrency = (value: number | null | undefined): string => {
  let cashSymbol;
  if (i18n.language.startsWith('pt')) {
    cashSymbol = 'R$';
  } else {
    cashSymbol = '$';
  }
  
  if(value === null || value === undefined){
    return `${cashSymbol}0,00`;
  }

  switch (cashSymbol){
    case 'R$':
      return new Intl.NumberFormat('pt-BR', {
        style: 'currency',
        currency: 'BRL',
      }).format(value);
    case '$':
      return new Intl.NumberFormat('en-US', {
        style: 'currency',
        currency: 'USD',
      }).format(value);
    default:
      console.warn(`Formato de moeda não suportado: ${cashSymbol}`);
      return `${cashSymbol}${value.toFixed(2).replace('.', ',')}`;
  }
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
    : '1';
  }

  if (yesterday){
    return hours ? `Ontem às ${format(dateObj, 'HH:mm', { locale: ptBR })}` 
    : '0';
  }

  return format (dateObj, hours ? 'dd/MM/yyyy às HH:mm' : 'dd/MM/yyyy', {locale: ptBR})

} 
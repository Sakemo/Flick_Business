import apiClient from "../lib/axios";
import { VendaRequest, VendaResponse } from "../types/domain";


export interface PageResponse<T> {
  content: T[];
  number: number;
  totalPages: number;
  totalElements: number;
}
export interface GetVendasParams {
  inicio?: string | null;
  fim?: string | null;
  clienteId?: number | null;
  formaPagamento?: string | null;
  produtoId?: number | null;
  orderBy?: string | null | 'dataVenda,desc' | 'dataVenda,Asc' | 'valorTotal,asc' | 'valorTotal,desc';
  page?: number;
  size?: number;
}
export interface GroupSummary {
  groupKey: string;
  groupTitle: string;
  totalValue: number;
}

export const getVendas = async (params?: GetVendasParams):Promise<PageResponse<VendaResponse>> => {
  try {
    const queryParams = { ...params, size: params?.size || 8 };
    console.log("Frontend: Enviando para /api/vendas (GET) com params: ", queryParams);
    const response = await apiClient.get<PageResponse<VendaResponse>>('/api/vendas', { params: queryParams });
    console.log("Frontend: Recebendo resposta de /api/vendas (GET): ", response.data);
    return response.data;
  } catch (error) {
    console.error("Frontend: Erro ao buscar vendas: ", error);
    throw error;
  }
};

export const getVendasSummary = async (params:GetVendasParams):Promise<GroupSummary[]> => {
  const groupBy = params.orderBy?.split(',')[0];
  if (!groupBy) return [];

  const queryParams = { ...params, groupBy };
  delete (queryParams as any).orderBy;
  delete (queryParams as any).page;
  delete (queryParams as any).size;

  try {
    const response = await apiClient.get<GroupSummary[]>('/api/vendas/summary-by-group', { params: queryParams });
    return response.data;
  } catch (error){
    console.error("Erro ao buscar resumo de vendas: ", error);
    throw error;
  }
};

export const getVendaById = async (id: number): Promise<VendaResponse> => {
  try{
    const response = await apiClient.get<VendaResponse>(`/api/vendas/${id}`);
    console.log("Frontend: Recebendo resposta de /api/vendas/:id (GET): ", response.data);
    return response.data;
  } catch (error) {
    console.error("Frontend: Erro ao buscar venda por ID: ", error);
    throw error;
  }
};

export const registrarVenda = async (data: VendaRequest): Promise<VendaResponse> => {
  try{
    console.log("Frontend: Enviando para /api/vendas (POST) com dados: ", data);
    const response = await apiClient.post<VendaResponse>('/api/vendas', data);
    console.log("Frontend: Recebendo resposta de /api/vendas (POST): ", response.data);
    return response.data;
  } catch (error) {
    console.error(`Erro ao registrar venda: `, error);
    throw error;
  }
};

export const deleteVendaFisicamente = async (id: number): Promise<void> => {
  try {
    await apiClient.delete(`/api/vendas/${id}/permanente`);
  } catch (error){
    console.error(`Erro ao deletar venda ${id} : `, error);
    throw error;
  }
};
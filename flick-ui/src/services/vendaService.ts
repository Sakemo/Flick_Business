import apiClient from "../lib/axios";
import { VendaRequest, VendaResponse } from "../types/domain";

export interface GetVendasParams {
  inicio?: string | null;
  fim?: string | null;
  clienteId?: number | null;
  formaPagamento?: string | null;
  produtoId?: number | null;
  //TODO:  orderBy: 'dataDesc' | 'dataAsc' | 'valorDesc' | 'valorDesc';
}

export const getVendas = async (params?: GetVendasParams):Promise<VendaResponse[]> => {
  try {
    console.log("Frontend: Enviando para /api/vendas (GET) com params: ", params);
    const response = await apiClient.get<VendaResponse[]>('/api/vendas', { params });
    console.log("Frontend: Recebendo resposta de /api/vendas (GET): ", response.data);
    return response.data;
  } catch (error) {
    console.error("Frontend: Erro ao buscar vendas: ", error);
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

//TODO: Funções para cancelar, atualizar e deletar

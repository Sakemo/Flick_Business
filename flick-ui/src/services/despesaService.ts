import apiClient from "../lib/axios";
import { DespesaRequest, DespesaResponse } from "../types/domain";

export interface GetDespesasParams {
  start?: string | null;
  end?: string | null;
  tipoExpense?: string | null;
  nameContains?: string | null; //TODO: BACKEND PARA nomeContains
}

export const getDespesas = async (params?: GetDespesasParams):Promise<DespesaResponse[]> => {
  try {
    const response = await apiClient.get<DespesaResponse[]>('/api/expenses', { params });
    return response.data;
  } catch (error){
    console.error("Erro ao buscar despesas: ", error);
    throw error;
  }
};

export const getTotalExpenses = async (params: { begin?: string | null; end?: string | null }): Promise<number> => {
  try {
    const backendParams = {
      start: params.begin,
      end: params.end,
    };
    const response = await apiClient.get<number>('/api/expenses/total', { params: backendParams });
    return response.data ?? 0;
  } catch (error) {
    console.error("Erro ao buscar total de despesas:", error);
    throw error;
  }
};

export const getDespesaById = async (id: number): Promise<DespesaResponse> => {
  try{
    const response = await apiClient.get<DespesaResponse>(`/api/expenses/${id}`);
    return response.data
  } catch(error){
    console.error(`Erro ao buscar despesa ${id}:`, error);
    throw error
  }
};

export const createDespesa = async (data: DespesaRequest):Promise<DespesaResponse> => {
  try{
      const response = await apiClient.post<DespesaResponse>('/api/expenses', data);
      return response.data;
  } catch (error){
    console.error("Erro ao criar despesa:", error)
    throw error;
  }
};

export const updateDespesa = async (id: number, data: DespesaRequest):Promise<DespesaResponse> => {
  try{
    const response = await apiClient.put<DespesaResponse>(`/api/expenses/${id}`, data);
    return response.data;
  } catch (error){
    console.error(`Erro ao atualizar despesa ${id}:`, error);
    throw error;
  }
};

export const deleteDespesa = async (id: number): Promise<void> => {
  try{
    await apiClient.delete(`/api/expenses/${id}`);
  } catch(error){
    console.error(`Erro ao deletar despesa ${id}`, error);
    throw error;
  }
};


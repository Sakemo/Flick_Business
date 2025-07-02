import apiClient from "../lib/axios";
import { DespesaRequest, DespesaResponse } from "../types/domain";

export interface GetDespesasParams {
  inicio?: string | null;
  fim?: string | null;
  tipoDespesa?: string | null;
  nomeContains?: string | null; //TODO: BACKEND PARA nomeContains
}

export const getDespesas = async (params?: GetDespesasParams):Promise<DespesaResponse[]> => {
  try {
    const response = await apiClient.get<DespesaResponse[]>('/api/despesas', { params });
    return response.data;
  } catch (error){
    console.error("Erro ao buscar despesas: ", error);
    throw error;
  }
};

export const getTotalExpenses = async (params: { begin?: string | null; end?: string | null }): Promise<number> => {
  try {
    const response = await apiClient.get<number>('/api/despesas/total', { params });
    return response.data;
  } catch (error) {
    console.error("Erro ao buscar total de despesas:", error);
    throw error;
  }
};

export const getDespesaById = async (id: number): Promise<DespesaResponse> => {
  try{
    const response = await apiClient.get<DespesaResponse>(`/api/despesas/${id}`);
    return response.data
  } catch(error){
    console.error(`Erro ao buscar despesa ${id}:`, error);
    throw error
  }
};

export const createDespesa = async (data: DespesaRequest):Promise<DespesaResponse> => {
  try{
      const response = await apiClient.post<DespesaResponse>('/api/despesas', data);
      return response.data;
  } catch (error){
    console.error("Erro ao criar despesa:", error)
    throw error;
  }
};

export const updateDespesa = async (id: number, data: DespesaRequest):Promise<DespesaResponse> => {
  try{
    const response = await apiClient.put<DespesaResponse>(`/api/despesas/${id}`, data);
    return response.data;
  } catch (error){
    console.error(`Erro ao atualizar despesa ${id}:`, error);
    throw error;
  }
};

export const deleteDespesa = async (id: number): Promise<void> => {
  try{
    await apiClient.delete(`/api/despesas/${id}`);
  } catch(error){
    console.error(`Erro ao deletar despesa ${id}`, error);
    throw error;
  }
};


import apiClient from "../lib/axios";
import { FornecedorResponse } from "../types/domain";

export const getFornecedores = async (): Promise<FornecedorResponse[]> => {
  try {
    const response = await apiClient.get<FornecedorResponse[]>('/api/fornecedores');
    return response.data;
  }catch(error){
    console.error("Erro ao buscar fornecedores: ", error);
    throw error;
  }
};
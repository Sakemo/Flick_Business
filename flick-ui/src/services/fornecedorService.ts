import apiClient from "../lib/axios";
import { FornecedorResponse } from "../types/domain";

export interface FornecedorRequest {
  nome: string;
  /* TODO:  
     * tipoPessoa
     * cnpjCpf
     * telefone
     * email
     * notas
  */
}

export const getFornecedores = async (): Promise<FornecedorResponse[]> => {
  try {
    const response = await apiClient.get<FornecedorResponse[]>('/api/fornecedores');
    return response.data;
  }catch(error){
    console.error("Erro ao buscar fornecedores: ", error);
    throw error;
  }
};

export const createFornecedor = async (data:FornecedorRequest):
Promise<FornecedorResponse> => {
  try{
    const response = await apiClient.post<FornecedorResponse>('api/fornecedores', data);
    return response.data;
  }catch(error){
    console.error("Erro ao criar fornecedor: ", error);
    throw error;
  }
}
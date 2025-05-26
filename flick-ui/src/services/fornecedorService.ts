import apiClient from "../lib/axios";
import { FornecedorResponse, TipoPessoa } from "../types/domain";

interface BackendFornecedorDTO {
  id?: number | null;
  nome: string;
  tipoPessoa?: TipoPessoa | null;
  cnpjCpf?: string | null;
  telefone?: string | null;
  email?: string | null;
  notas?: string | null;
}

export interface FornecedorAddQuickRequest {
  nome: string;
  tipoPessoa?: TipoPessoa | '' | null;
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

export const createFornecedor = async (data:FornecedorAddQuickRequest):
Promise<FornecedorResponse> => {
  try{
    const payload: BackendFornecedorDTO = {
      nome: data.nome,
      tipoPessoa: data.tipoPessoa === '' ? null : data.tipoPessoa || null,
      cnpjCpf: null,
      telefone: null,
      email: null,
      notas: null,
    };
    console.log("Payload para criar fornecedor: ", payload);
    const response = await apiClient.post<FornecedorResponse>('api/fornecedores', payload);
    return response.data;
  }catch(error){
    console.error("Erro ao criar fornecedor: ", error);
    throw error;
  }
}
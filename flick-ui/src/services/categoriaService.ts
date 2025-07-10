import apiClient from "../lib/axios";
import { CategoriaResponse } from "../types/domain";

export interface CategoriaRequest {
  nome: string;
}

export const getCategorias = async (): Promise<CategoriaResponse[]> => {
  try{
    const response = await apiClient.get<CategoriaResponse[]>('/api/categorys');
    return response.data;
  }catch(error){
    console.error("Erro ao buscar categorias: ", error);
    // TODO: tratar erro de forma mais robusta
    throw error;
  }
};

export const createCategoria = async (data:CategoriaRequest):
Promise<CategoriaResponse> => {
  try{
    const response = await apiClient.post<CategoriaResponse>('/api/categorys', data);
    return response.data;
  }catch(error) {
    console.error("Erro ao criar categoria: ", error)
    throw error;
  }
}
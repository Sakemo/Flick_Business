import apiClient from "../lib/axios";
import { CategoriaResponse } from "../types/domain";

export const getCategorias = async (): Promise<CategoriaResponse[]> => {
  try{
    const response = await apiClient.get<CategoriaResponse[]>('/api/categorias');
    return response.data;
  }catch(error){
    console.error("Erro ao buscar categorias: ", error);
    // TODO: tratar erro de forma mais robusta
    throw error;
  }
}

// TODO: Adicionar createCategoria, etc
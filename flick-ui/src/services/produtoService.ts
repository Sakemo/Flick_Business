import apiClient from '../lib/axios';
import { ProdutoResponse, ProdutoRequest } from '../types/domain';

interface GetProdutosParams {
  categoriaId?:number | null;
  nome?: string;
  orderBy?: string;
}

export const getProdutos = async (params?: GetProdutosParams) :
Promise<ProdutoResponse[]> => {
  try {
    const response = await apiClient.get<ProdutoResponse[]>('/api/produtos', { params });
    return response.data;
  } catch(error){
    console.error("Erro ao buscar produtos: ", error);
    throw error;
  }
};

export const getProdutoById = async (id:number): Promise<ProdutoResponse> =>
{
  try{
    const response = await apiClient.get<ProdutoResponse>(`/api/produtos/${id}`)
    return response.data;
  } catch (error){
    console.error(`Erro ao buscar produto ${id}: `, error)
    throw error;
  }
};

export const createProduto = async (data:ProdutoRequest):
Promise<ProdutoResponse> => {
  try{
    const response = await apiClient.post<ProdutoResponse>('/api/produtos', data);
    return response.data;
  } catch(error){
    console.error('Erro ao criar o produto: ', error)
    throw error;
  }
};

export const updateProduto = async(id:number, data:ProdutoRequest):
Promise<ProdutoResponse> => {
  try{
    const response = await apiClient.put<ProdutoResponse>(`/api/produtos/${id}`, data);
    return response.data;
  } catch(error){
    console.error(`Erro ao atualizar produto ${id}: `, error);
    throw error;
  }
};

export const deleteProduto = async (id:number) : Promise<void> => {
  try{
    await apiClient.delete(`/api/produtos/${id}`)
  }catch(error){
    console.error(`Erro ao deletar produto ${id}: `, error);
    throw error;
  }
}

export const deleteProdutoFisicamente = async (id:number): Promise<void> =>{
  try{
    await apiClient.delete(`/api/produtos/${id}/permanente`);
  }catch(error){
    console.error(`Erro ao deletear fisicamente produto ${id}: `, error);
    throw error;
  }
}

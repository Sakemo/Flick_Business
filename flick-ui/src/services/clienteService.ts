import apiClient from "../lib/axios";
import { ClienteResponse, ClienteRequest } from "../types/domain";

export interface GetClientesParams {
  apenasAtivos?: boolean | null;
  devedores?: boolean | null;
  orderBy?: 'nomeAsc' | 'nomeDesc' | 'saldoDesc' | 'saldoAsc' | 'cadastroRecente' | 'cadastroAntigo' | null;
  nomeContains?: string | null;
}

export const getClientes = async (params?: GetClientesParams):Promise<ClienteResponse[]> => {
  try{
    console.log("Frontend: Enviado para /api/clientes com params: ", params);
    const response = await apiClient.get<ClienteResponse[]>('/api/clientes', { params });
    return response.data;
  }catch(error){
    console.error("Erro ao buscar clientes: ", error);
    throw error;
  }
};

export const getCLienteById = async (id:number): Promise<ClienteResponse> => {
  try{
    const response = await apiClient.get<ClienteResponse>(`/api/clientes/${id}`);
    return response.data;
  }catch(error){
    console.error(`Erro ao buscar cliente ${id}: `, error)
    throw error;
  }
};

export const createCliente = async (data: ClienteRequest): Promise<ClienteResponse> => {
  try{
    const response = await apiClient.post<ClienteResponse>('/api/clientes', data);
    return response.data;
  } catch(error){
    console.error("Erro ao criar cliente: ", error);
    throw error;
  }
};

export const updateCliente = async (id: number, data: ClienteRequest):Promise<ClienteResponse> => {
  try{
    const response = await apiClient.put<ClienteResponse>(`/api/clientes/${id}`, data);
    return response.data;
  } catch (error) {
    console.error(`Erro ao atualizar cliente ${id}`, error);
    throw error;
  }
}

export const toggleAtividadeCliente = async (id:number, ativo:boolean):Promise<ClienteResponse> => {
  try{
    const response = await apiClient.patch<ClienteResponse>(`/api/clientes/${id}/ativo`, ativo, {
      headers: { 'Content-Type' : 'application/json' }
    });
    return response.data;
  } catch (error){
    console.error(`Erro ao ${ativo ? 'ativar' : 'inativar'} cliente ${id}: `, error);
    throw error;
  }
};

export const deleteClienteFisicamente = async (id:number): Promise<void> => {
  try{
    await apiClient.delete(`/api/clientes/${id}/permanente`);
  }catch (error){
    console.error(`Erro ao deletar permanentemente o cliente ${id}:`, error);
    throw error;
  }
};
import apiClient from "../lib/axios";
import { ConfiguracaoGeralResponse } from "../types/domain";

export const getConfiguracaoGeral = async () : Promise<ConfiguracaoGeralResponse> => {
  try{
    const response = await apiClient.get<ConfiguracaoGeralResponse>('/api/configuracoes');
    return response.data;
  } catch (error){
    console.error("Erro ao buscar configuracoes gerais: ", error);
    throw error;
  }
};

//TODO: Adicionar updateConfiguracaoGeral


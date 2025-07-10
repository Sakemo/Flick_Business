import axios, { AxiosError } from "axios";
import { API_BASE_URL } from "../constants";

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  },
  timeout: 10000,
})

apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('authToken');
    if(token){
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error)
  }
);

apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    console.error('Erro na chamada API: ', {
      message: error.message,
      url: error.config?.url,
      method: error.config?.method,
      status: error.response?.status,
      responseData: error.response?.data,
      requestData:error.config?.data,
    });

    if (error.response?.status === 401 || error.response?.status === 403){
      console.log('Acesso Negado')
    }

    if (error.response?.status === 500){
      console.log('Error ao acessar o servidor')
    }

    return Promise.reject(error);
  }
);

export default apiClient;
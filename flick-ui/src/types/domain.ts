export enum TipoUnidadeVenda{
  UNIDADE = 'UNIDADE',
  PESO = 'PESO',
  VOLUME = 'VOLUME'
}

export interface CategoriaResponse {
  id: number;
  nome: string;
}

export interface CategoriaRequest {
  nome: string;
}

export interface FornecedorResponse {
  id: number;
  nome: string;
}

export interface ProdutoResponse {
  id:number;
  nome:string;
  descricao?:string | null;
  codigoBarras?:string | null;
  quantidadeEstoque?: number | null;
  precoVenda:number;
  precoCustoUnitario?:number|null;
  tipoUnidadeVenda: TipoUnidadeVenda;
  ativo:boolean;
  categoria?: { id: number; nome: string } | null;
  fornecedor?: { id: number; nome: string } | null;
  criadoEm:string;
  atualizadoEm:string;
}

export interface ProdutoRequest{
  nome: string;
  descricao?: string | null;
  codigoBarras?: string | null;
  quantidadeEstoque?: number | null;
  precoVenda: number;
  precoCustoUnitario?: number | null;
  tipoUnidadeVenda: TipoUnidadeVenda;
  ativo: boolean;
  categoriaId: number | undefined;
  fornecedorId:number | null;
}
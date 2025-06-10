export enum TipoUnidadeVenda{
  UNIDADE = 'UNIDADE',
  PESO = 'PESO',
  VOLUME = 'VOLUME'
}

export enum TipoPessoa {
  FISICA = 'FISICA',
  JURIDICA = 'JURIDICA',
} 

export interface FornecedorResponse {
  id: number;
  nome: string;
  tipoPessoa?: TipoPessoa | null;
  cnpjCpf?: string | null;
  telefone?: string | null;
  email?: string | null;
  notas?: string | null;
}

export interface FornecedorAddQuickRequest {
  nome: string;
  tipoPessoa?: TipoPessoa | '';
}

export enum FormaPagamento {
  DINHEIRO = 'DINHEIRO',
  DEBITO = 'DEBITO',
  CREDITO = 'CREDITO',
  FIADO = 'FIADO',
  PIX = 'PIX',
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

export interface ClienteResponse {
  id:number;
  nome: string;
  cpf?: string | null;
  telefone?: string | null;
  endereco?: string | null;
  controleFiado: boolean;
  limiteFiado?: number | null;
  saldoDevedor: number;
  dataUltimaCompraFiado?: string | null;
  dataCadastro: string;
  dataAtualizacao: string;
  ativo: boolean;
}

export interface ClienteRequest {
  nome: string;
  cpf?: string | null;
  telefone?: string | null;
  endereco?: string | null;
  controleFiado: boolean;
  limiteFiado?: number;
  ativo?: boolean | null;
}

export interface ConfiguracaoGeralResponse {
  taxaJurosAtraso?: number | null;
  prazoPagamentoFiado?: number | null;
  nomeNegocio?: string | null;
  dataAtualizacao: string | null;
}

export enum TipoDespesa {
  PESSOAL = 'PESSOAL',
  EMPRESARIAL = 'EMPRESARIAL',
  INVESTIMENTO = 'INVESTIMENTO',
  OUTROS = 'OUTROS',
}

export interface DespesaResponse {
  id: number;
  nome: string;
  valor: number;
  dataDespesa: string;
  tipoDespesa: TipoDespesa;
  observacao?: string | null;
  dataCriacao: string;
  dataAtualizacao: string;
}

export interface DespesaRequest {
  nome: string;
  valor: number;
  dataDespesa: string;
  tipoDespesa: TipoDespesa;
  observacao?: string | null;
}


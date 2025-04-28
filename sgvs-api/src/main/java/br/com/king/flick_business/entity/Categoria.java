package br.com.king.flick_business.entity;

// JAKARTA SETUP
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// LOMBOK SETUP
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity // Define a entidade
@Table(name = "categorias") // Cria a tabela
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Categoria {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank(message = "Nome da categoria não pode estar em branco")
  @Size(min = 3, max = 45, message = "Deve ter entre 2 e 45 caracteres")
  @Column(nullable = false, length = 45, unique = true)
  private String nome;

  public void setNome(String nome) {
    this.nome = nome;
  }
}

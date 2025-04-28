package br.com.king.flick_business.entity;
import br.com.king.flick_business.enums.TipoPessoa;

// JAKARTA SETUP
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

// LOMBOK SETUP
import lombok.Data; import lombok.NoArgsConstructor; import lombok.AllArgsConstructor; import lombok.Builder;

@Entity
@Table(name = "fornecedores")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Fornecedor {
    /*
     * id
     * nome
     * tipoPessoa
     * cnpjCpf
     * telefone
     * email
     * notas
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome é obrigatório")    @Size(min = 2, max = 60, message = "Nome deve ter entre 2 e 60 caracteres")
    @Column(nullable = false, length = 60)
    private String nome;

    @NotNull(message = "Tipo de pessoa é obrigatório")    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoPessoa tipoPessoa;

    @Pattern(
        regexp = "^(\\d{11}|\\d{14})$",
        message = "Informe um CPF ou CNPJ válido, apenas números"
    )
    @Column(nullable = true, length = 14, unique = true) // Opcional
    private String cnpjCpf;

    @Pattern(
        regexp = "^\\d{10,11}$"
    )
    @Column(length = 11)
    private String telefone;

    @Email(message = "E-mail inválido")
    @Size(max = 100)
    @Column(length = 100)
    private String email;

    @Size(max = 300)
    private String notas;
}

package br.com.king.flick_business.entity;

import br.com.king.flick_business.enums.TipoPessoa;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "providers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Provider {
    /*
     * id
     * name
     * tipoPessoa
     * cnpjCpf
     * telefone
     * email
     * notas
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O name é obrigatório")
    @Size(min = 2, max = 60, message = "Name deve ter entre 2 e 60 caracteres")
    @Column(nullable = false, length = 60)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true, length = 10)
    private TipoPessoa tipoPessoa;

    @Pattern(regexp = "^(\\d{11}|\\d{14})$", message = "Informe um CPF ou CNPJ válido, apenas números")
    @Column(nullable = true, length = 14, unique = true) // Opcional
    private String cnpjCpf;

    @Pattern(regexp = "^\\d{10,11}$")
    @Column(length = 11, nullable = true)
    private String telefone;

    @Email(message = "E-mail inválido")
    @Size(max = 100)
    @Column(length = 100, unique = true, nullable = true)
    private String email;

    @Size(max = 300)
    @Column(length = 500, nullable = true)
    private String notas;
}

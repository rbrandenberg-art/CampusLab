package CampusLab.ms_campuslab_rabbitmq.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateQueueRequest(
        @NotBlank(message = "El nombre de la cola es obligatorio y no puede estar vacío")
        @Pattern(regexp = "^[a-zA-Z0-9_.-]+$", message = "El nombre contiene caracteres inválidos. Solo letras, números, guiones y puntos.")
        String name,
        
        boolean durable
) {
}
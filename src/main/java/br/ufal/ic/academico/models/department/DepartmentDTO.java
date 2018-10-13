package br.ufal.ic.academico.models.department;

import br.ufal.ic.academico.models.secretary.SecretaryDTO;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
@ToString
public class DepartmentDTO {
    public Long id;
    public String name;
    public List<SecretaryDTO> secretaries = new ArrayList<>();

    public DepartmentDTO(Department entity) {
        this.id = entity.getId();
        this.name = entity.name;
        if (entity.graduation != null) {
            this.secretaries.add(new SecretaryDTO(entity.graduation));
        }
        if (entity.postGraduation != null) {
            this.secretaries.add(new SecretaryDTO(entity.postGraduation));
        }
    }
}

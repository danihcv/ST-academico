package br.ufal.ic.academico.models.course;

import br.ufal.ic.academico.models.discipline.DisciplineDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.List;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class CourseDTO {
    public Long id;
    public String name;
    private List<DisciplineDTO> disciplines;

    public CourseDTO(Course entity) {
        this.id = entity.getId();
        this.name = entity.name;

        LinkedList<DisciplineDTO> disciplines = new LinkedList<>();
        assert entity.disciplines != null;
        entity.disciplines.forEach(d -> disciplines.addLast(new DisciplineDTO(d)));
        this.disciplines = disciplines;
    }
}

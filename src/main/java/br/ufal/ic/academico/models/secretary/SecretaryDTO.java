package br.ufal.ic.academico.models.secretary;

import br.ufal.ic.academico.models.discipline.Discipline;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class SecretaryDTO {
    public Long id;
    public String type;
    public List<DisciplineDTO> disciplines = new LinkedList<>();

    public SecretaryDTO(Secretary entity) {
        this.id = entity.getId();
        this.type = entity.type;
        ArrayList<DisciplineDTO> dtoList = new ArrayList<>();
        entity.courses.forEach(c -> {
            assert c.getDisciplines() != null;
            dtoList.addAll(c.getDisciplines().stream().map(DisciplineDTO::new).collect(Collectors.toList()));
        });
        this.disciplines = dtoList;
    }

    @Getter
    @RequiredArgsConstructor
    public static class DisciplineDTO {
        Long id;
        String code, name;
        Integer credits, requiredCredits;
        List<String> requiredDisciplines;

        DisciplineDTO(Discipline entity) {
            this.id = entity.getId();
            this.code = entity.getCode();
            this.code = entity.getCode();
            this.name = entity.getName();
            this.credits = entity.getCredits();
            this.requiredCredits = entity.getRequiredCredits();
            this.requiredDisciplines = entity.getRequiredDisciplines();
        }
    }
}

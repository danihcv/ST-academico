package br.ufal.ic.academico.models.discipline;

import br.ufal.ic.academico.models.person.student.Student;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class DisciplineDTO {
    public Long id;
    public String code;
    public String name;
    public Integer credits, requiredCredits;
    public List<String> requiredDisciplines;
    public String teacher;
    public List<StudentDTO> students;

    public DisciplineDTO(Discipline entity) {
        this.id = entity.getId();
        this.code = entity.code;
        this.name = entity.name;
        this.credits = entity.credits;
        this.requiredCredits = entity.requiredCredits;
        this.requiredDisciplines = entity.requiredDisciplines;
        if (entity.teacher != null) {
            this.teacher = entity.teacher.getFirstName() + (entity.teacher.getLastName() != null ? " " + entity.teacher.getLastName() : "");
        }
        ArrayList<StudentDTO> dtoList = new ArrayList<>();
        entity.students.forEach(s -> dtoList.add(new StudentDTO(s)));
        this.students = dtoList;
    }

    @Getter
    @RequiredArgsConstructor
    public static class StudentDTO {
        public Long id;
        public String name;

        StudentDTO(Student entity) {
            this.id = entity.getId();
            this.name = entity.getFirstName() + (entity.getLastName() != null ? " " + entity.getLastName() : "");
        }
    }
}

package br.ufal.ic.academico.models.person.student;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@RequiredArgsConstructor
@ToString
public class StudentDTO {
    public Long id;
    public String firstName, lastName, role;
    public Integer credits;
    public String course;
    public List<String> completedDisciplines = new ArrayList<>();

    public StudentDTO(Student entity) {
        this.id = entity.getId();
        this.firstName = entity.getFirstname();
        this.lastName = entity.getLastName();
        this.role = entity.getRole();
        this.credits = entity.getCredits();
        if (entity.course != null) {
            this.course = entity.course.getName();
        }
        this.completedDisciplines = entity.completedDisciplines;
    }
}

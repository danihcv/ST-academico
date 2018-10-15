package br.ufal.ic.academico.models.person.teacher;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@RequiredArgsConstructor
@ToString
public class TeacherDTO {
    public Long id;
    public String firstName, lastName, role;

    public TeacherDTO(Teacher entity) {
        this.id = entity.getId();
        this.firstName = entity.getFirstName();
        this.lastName = entity.getLastName();
        this.role = entity.getRole();
    }
}

package br.ufal.ic.academico.resources;

import br.ufal.ic.academico.ConfigApp;
import br.ufal.ic.academico.models.course.CourseDTO;
import br.ufal.ic.academico.models.department.DepartmentDTO;
import br.ufal.ic.academico.models.discipline.DisciplineDTO;
import br.ufal.ic.academico.models.person.student.StudentDTO;
import br.ufal.ic.academico.models.person.teacher.TeacherDTO;
import br.ufal.ic.academico.models.secretary.SecretaryDTO;
import io.dropwizard.testing.junit5.DropwizardAppExtension;

import javax.ws.rs.client.Entity;

class BasicBackground {
    private String url;

    BasicBackground(String url) {
        this.url = url;
    }

    DepartmentDTO createDepartment(DropwizardAppExtension<ConfigApp> RULE, String departmentName) {
        return RULE.client().target(url + "department").request()
                .post(Entity.json(new DepartmentDTO(null, departmentName, null)), DepartmentDTO.class);
    }

    SecretaryDTO createSecretary(DropwizardAppExtension<ConfigApp> RULE, DepartmentDTO department, String type) {
        return RULE.client().target(url + "department/" + department.getId() + "/secretary").request()
                .post(Entity.json(new SecretaryDTO(null, type, null)), SecretaryDTO.class);
    }

    CourseDTO createCourse(DropwizardAppExtension<ConfigApp> RULE, SecretaryDTO secretary, String courseName) {
        return RULE.client().target(url + "secretary/" + secretary.getId() + "/course").request()
                .post(Entity.json(new CourseDTO(null, courseName, null)), CourseDTO.class);
    }

    DisciplineDTO createDiscipline(DropwizardAppExtension<ConfigApp> RULE, CourseDTO course, String disciplineCode,
                                   String disciplineName, Integer disciplineCredits) {
        return RULE.client().target(url + "course/" + course.getId() + "/discipline").request()
                .post(Entity.json(new DisciplineDTO(null, disciplineCode, disciplineName, disciplineCredits, null, null, null, null)), DisciplineDTO.class);
    }

    TeacherDTO createTeacher(DropwizardAppExtension<ConfigApp> RULE, String firstName, String lastName) {
        return RULE.client().target(url + "enrollment/teacher/").request()
                .post(Entity.json(new TeacherDTO(null, firstName, lastName, "TEACHER")), TeacherDTO.class);
    }

    StudentDTO createStudent(DropwizardAppExtension<ConfigApp> RULE, String firstName, String lastName, Integer credits) {
        return RULE.client().target(url + "enrollment/student/").request()
                .post(Entity.json(new StudentDTO(null, firstName, lastName, "STUDENT", credits, null, null)), StudentDTO.class);
    }

    void enrollInCourse(DropwizardAppExtension<ConfigApp> RULE, StudentDTO student, CourseDTO course) {
        RULE.client().target(url + "enrollment/student/" + student.getId() + "/course/" + course.getId()).request().post(null);
    }

    void enrollInDiscipline(DropwizardAppExtension<ConfigApp> RULE, StudentDTO student, DisciplineDTO discipline) {
        RULE.client().target(url + "enrollment/student/" + student.getId() + "/discipline/" + discipline.getId()).request().post(null);
    }
}

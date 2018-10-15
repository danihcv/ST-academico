package br.ufal.ic.academico.resources;

import br.ufal.ic.academico.models.course.CourseDTO;
import br.ufal.ic.academico.models.department.DepartmentDTO;
import br.ufal.ic.academico.models.discipline.DisciplineDTO;
import br.ufal.ic.academico.models.person.student.StudentDTO;
import br.ufal.ic.academico.models.person.teacher.TeacherDTO;
import br.ufal.ic.academico.models.secretary.SecretaryDTO;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(DropwizardExtensionsSupport.class)
class TeacherIntegrationTest extends IntegrationTestBase {
    private List<TeacherDTO> teachers = new ArrayList<>();

    @Test
    void teacherResources() {
        assertEquals(new ArrayList<>(), RULE.client().target(url + "enrollment/teacher").request()
                        .get(new GenericType<ArrayList<StudentDTO>>(){}),
                "Banco de dados inicializou com Teachers cadastros");

        createTeachers();
        getTeacherByID();
        updateTeacher();

        assertEquals(teachers.size(), RULE.client().target(url + "enrollment/teacher").request()
                        .get(new GenericType<ArrayList<StudentDTO>>(){}).size(),
                "Quantidade de Teachers listados, a partir do banco de dados, é diferente da quantidade cadastrada");

        deleteTeacher();

        assertEquals(teachers.size(), RULE.client().target(url + "enrollment/teacher").request()
                        .get(new GenericType<ArrayList<StudentDTO>>(){}).size(),
                "Quantidade de Teachers listados está diferente da quantidade esperada após remoção de Teacher");

        alocateTeacher();
    }

    private void createTeachers() {
        assertThrows(BadRequestException.class, () -> RULE.client().target(url + "enrollment/teacher").request().post(Entity.json(new TeacherDTO()), TeacherDTO.class),
                "API não retornou status 400 ao tentar criar um Teacher sem First Name");

        for (int i = 1; i <= 5; i++) {
            TeacherDTO entity = new TeacherDTO();
            entity.firstName = faker.name().firstName();
            TeacherDTO response = RULE.client().target(url + "enrollment/teacher").request()
                    .post(Entity.json(entity), TeacherDTO.class);

            assertNotNull(response.id, "Teacher criado sem ID");
            assertEquals(entity.firstName, response.firstName, "First Name incorreto associado ao Teacher criado");
            assertNull(response.lastName);
            assertEquals("TEACHER", response.role, "Role do Teacher está diferente do esperado");
            teachers.add(response);
        }
    }

    private void getTeacherByID() {
        assertThrows(NotFoundException.class, () -> RULE.client().target(url + "enrollment/teacher/0").request().get(StudentDTO.class),
                "API não retornou status 404 ao tentar retornar um Teacher com ID inválido");

        TeacherDTO response = RULE.client().target(url + "enrollment/teacher/" + teachers.get(0).id).request()
                .get(TeacherDTO.class);
        assertEquals(teachers.get(0).id, response.id, "Teacher retornado tem ID diferente do informado");
    }

    private void updateTeacher() {
        assertThrows(NotFoundException.class, () -> RULE.client().target(url + "enrollment/teacher/0").request()
                        .put(Entity.json(new TeacherDTO()), TeacherDTO.class),
                "API não retornou status 404 ao tentar atualizar um Teacher com ID inválido");

        final TeacherDTO original = teachers.get(0);
        TeacherDTO entity = original;
        entity.lastName = faker.name().lastName();
        StudentDTO response = RULE.client().target(url + "enrollment/teacher/" + original.id).request()
                .put(Entity.json(entity), StudentDTO.class);
        assertEquals(original.id, response.id, "Teacher atualizado possui ID diferente do informado");
        assertEquals(entity.lastName, response.lastName, "Last Name do Teacher não foi atualizado corretamente");
        assertEquals(original.firstName, response.firstName,
                "First Name do Teacher foi modificado durante a atualização do Last Name");

        entity = new TeacherDTO();
        entity.firstName = faker.name().firstName();
        response = RULE.client().target(url + "enrollment/teacher/" + original.id).request()
                .put(Entity.json(entity), StudentDTO.class);
        assertEquals(original.id, response.id, "Teacher atualizado possui ID diferente do informado");
        assertEquals(entity.firstName, response.firstName,
                "First Name do Teacher não foi atualizado corretamente");
        assertEquals(original.lastName, response.lastName,
                "Last Name do Teacher foi modificado durante a atualização do First Name");
    }

    private void deleteTeacher() {
        assertThrows(NotFoundException.class, () -> RULE.client().target(url + "enrollment/teacher/0").request()
                        .delete(TeacherDTO.class),
                "API não retornou status 404 ao tentar deletar um Teacher com ID inválido");

        final TeacherDTO original = teachers.get(0);
        teachers.remove(original);
        assertDoesNotThrow(() -> RULE.client().target(url + "enrollment/teacher/" + original.id).request()
                .delete(), "Falhou ao tentar deletar um Teacher com ID válido");

        assertThrows(NotFoundException.class, () -> RULE.client().target(url + "enrollment/teacher/" + original.id)
                .request().get(StudentDTO.class), "API não retornou status 404 ao tentar acessar um Teacher removido");
    }

    private void alocateTeacher() {
        // setup para os testes
        final DepartmentDTO department = background.createDepartment(RULE, "IC");
        final SecretaryDTO secretary = background.createSecretary(RULE, department, "GRADUATION");
        final CourseDTO course = background.createCourse(RULE, secretary, "Ciência da Computação");
        final DisciplineDTO discipline = background.createDiscipline(RULE, course, "CC001", "Programação 1", null);

        // testes da alocação do Teacher na Discipline
        final TeacherDTO teacher = teachers.get(0);

        assertThrows(NotFoundException.class, () -> RULE.client().target(url + "enrollment/teacher/0/discipline/" + discipline.getId())
                .request().post(null, DisciplineDTO.class), "API não retornou status 404 ao tentar alocar um Teacher com ID inválido");

        assertThrows(NotFoundException.class, () -> RULE.client().target(url + "enrollment/teacher/" + teacher.id + "/discipline/0").request()
                .post(null, DisciplineDTO.class), "API não retornou status 404 ao tentar alocar um Teacher válido numa Discipline inválida");


        DisciplineDTO response = RULE.client().target(url + "enrollment/teacher/" + teacher.id + "/discipline/" + discipline.getId()).request()
                .post(null, DisciplineDTO.class);

        assertNotNull(response.getTeacher(), "Teacher não foi alocado corretamente à Discipline informada");
        assertEquals(discipline.getCode(), response.getCode(), "Teacher foi alocado numa Discipline diferente da informada");
        assertEquals(discipline.getId(), response.getId(), "Discipline retornada na alocação tem um ID diferente do informado");
        assertEquals(discipline.getName(), response.getName(), "Discipline teve seu Name alterado durante a alocação do Teacher");
        assertEquals(discipline.getCredits(), response.getCredits(), "Discipline teve seus Credits alterado durante a alocação do Teacher");
        assertEquals(discipline.getRequiredCredits(), response.getRequiredCredits(),
                "Discipline teve seus Required Credits alterados durante a alocação do Teacher");
        assertEquals(discipline.getRequiredDisciplines(), response.getRequiredDisciplines(),
                "Discipline teve suas Required Discipline alteradas durante a alocação do Teacher");

        deleteAllocatedTeacher(teacher, response);
    }

    private void deleteAllocatedTeacher(TeacherDTO teacher, DisciplineDTO discipline) {
        assertDoesNotThrow(() -> RULE.client().target(url + "enrollment/teacher/" + teacher.getId()).request().delete(),
                "Falhou ao remover um Teacher que está alocado numa Discipline");

        DisciplineDTO response = RULE.client().target(url + "discipline/" + discipline.getId()).request()
                .get(DisciplineDTO.class);
        assertEquals(discipline.getId(), response.getId(),"Após remover Teacher, sua Discipline retornou um ID diferente do esperado");
        assertNull(response.getTeacher());
    }
}

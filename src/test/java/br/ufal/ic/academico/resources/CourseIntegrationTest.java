package br.ufal.ic.academico.resources;

import br.ufal.ic.academico.models.course.CourseDTO;
import br.ufal.ic.academico.models.department.DepartmentDTO;
import br.ufal.ic.academico.models.discipline.DisciplineDTO;
import br.ufal.ic.academico.models.secretary.SecretaryDTO;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(DropwizardExtensionsSupport.class)
class CourseIntegrationTest extends IntegrationTestBase {
    @Test
    void courseResources() {
        assertEquals(0, RULE.client().target(url + "course").request().get(new GenericType<List<CourseDTO>>(){}).size(),
                "Banco de dados iniciou com Courses cadastrados");

        DepartmentDTO department = background.createDepartment(RULE, "IC");
        SecretaryDTO secretary = background.createSecretary(RULE, department, "GRADUATION");
        department.secretaries.add(secretary);
        CourseDTO course = background.createCourse(RULE, secretary, "Engenharia da Computação");

        getCourseByID(course);
        course = updateCourse(course, "Ciência da Computação");
        deleteCourse(course, secretary);

        course = background.createCourse(RULE, secretary, "Sistemas de Informação");
        getAllDisciplinesFromCourse(course, 0);
        DisciplineDTO discipline = createDiscipline(course, "CC001", "Programação 1", 25, 36, new ArrayList<>());
        deleteCourseWithAssociatedDiscipline(course, secretary, discipline);
    }

    private void getCourseByID(CourseDTO course) {
        assertThrows(NotFoundException.class, () -> RULE.client().target(url + "course/0").request().get(CourseDTO.class),
                "API não retornou status 404 ao recuperar Course com ID inválido");

        CourseDTO response = RULE.client().target(url + "course/" + course.getId()).request().get(CourseDTO.class);
        assertEquals(course.id, response.id, "ID do Course retornado está diferente do informado");
        assertEquals(course.name, response.name, "Name do Course retornado está diferente do informado");
        assertEquals(course.getDisciplines().size(), response.getDisciplines().size(),
                "Quantidade de Disciplines do Course retornado está diferente da informada");
    }

    private CourseDTO updateCourse(CourseDTO course, String newName) {
        assertThrows(NotFoundException.class, () -> RULE.client().target(url + "course/0").request()
                .put(Entity.json(new CourseDTO()), CourseDTO.class),
                "API não retornou status 404 ao atualizar Course com ID inválido");

        CourseDTO response = RULE.client().target(url + "course/" + course.getId()).request()
                .put(Entity.json(new CourseDTO()), CourseDTO.class);
        assertEquals(course.id, response.id, "ID do Course foi alterado ao mandar um Course vazio para atualização");
        assertEquals(course.name, response.name, "Name do Course foi alterado ao mandar um Course vazio para atualização");
        assertEquals(course.getDisciplines().size(), response.getDisciplines().size(),
                "Quantidade de Disciplines do Course foi alterada ao mandar um Course vazio para atualização");

        CourseDTO entity = new CourseDTO(0L, newName, null);
        response = RULE.client().target(url + "course/" + course.getId()).request().put(Entity.json(entity), CourseDTO.class);
        assertEquals(newName, response.name, "Name do Course não foi alterado corretamente");
        assertEquals(course.id, response.id, "Após atualizar Course, seu ID foi alterado");
        assertEquals(course.getDisciplines(), response.getDisciplines(), "Após atualizar Course, seu ID foi alterado");

        return response;
    }

    private void deleteCourse(CourseDTO course, SecretaryDTO secretary) {
        assertThrows(NotFoundException.class, () -> RULE.client().target(url + "course/0").request().delete(CourseDTO.class),
                "API não retornou status 404 ao deletar Course com ID inválido");

        assertDoesNotThrow(() -> RULE.client().target(url + "course/" + course.getId()).request().delete(CourseDTO.class),
                "Falhou ao deletar Course válido");

        SecretaryDTO response = RULE.client().target(url + "secretary/" + secretary.getId()).request().get(SecretaryDTO.class);
        assertEquals(secretary.id, response.id, "ID da Secretary foi alterado ao deletar um Course associado");
        assertEquals(secretary.type, response.type, "Type da Secretary foi alterado ao deletar um Course associado");
    }

    private void getAllDisciplinesFromCourse(CourseDTO course, int expectedDisciplinesQuantity) {
        assertThrows(NotFoundException.class, () -> RULE.client().target(url + "course/0/discipline").request()
                .get(new GenericType<List<DisciplineDTO>>(){}),
                "API não retornou status 404 ao recuperar as Disciplines de um Course com ID inválido");

        List<DisciplineDTO> response = RULE.client().target(url + "course/" + course.getId() + "/discipline").request()
                .get(new GenericType<List<DisciplineDTO>>(){});
        assertEquals(expectedDisciplinesQuantity, response.size(), "Quantidade de Disciplines associdadas ao Course está diferente da esperada");
    }

    private DisciplineDTO createDiscipline(CourseDTO course, String code, String name, int credits, int requiredCredits,
                                  List<String> requiredDisciplines) {
        DisciplineDTO entity = new DisciplineDTO();
        entity.code = code;
        entity.name = name;
        assertThrows(NotFoundException.class, () -> RULE.client().target(url + "course/0/discipline").request().post(Entity.json(entity),
                DisciplineDTO.class), "API não retornou status 404 ao criar Discipline num Course inválido");

        assertThrows(BadRequestException.class, () -> RULE.client().target(url + "course/" + course.getId() + "/discipline").request()
                .post(Entity.json(new DisciplineDTO()), DisciplineDTO.class),
                "API não retornou status 400 ao criar uma Discipline sem Code num Course válido");
        entity.code = "";
        assertThrows(BadRequestException.class, () -> RULE.client().target(url + "course/" + course.getId() + "/discipline").request()
                .post(Entity.json(entity), DisciplineDTO.class),
                "API não retornou status 400 ao criar uma Discipline com Code vazio num Course válido");

        entity.code = code;
        entity.name = null;
        assertThrows(BadRequestException.class, () -> RULE.client().target(url + "course/" + course.getId() + "/discipline").request()
                        .post(Entity.json(entity), DisciplineDTO.class),
                "API não retornou status 400 ao criar uma Discipline sem Name num Course válido");
        entity.name = "";
        assertThrows(BadRequestException.class, () -> RULE.client().target(url + "course/" + course.getId() + "/discipline").request()
                        .post(Entity.json(entity), DisciplineDTO.class),
                "API não retornou status 400 ao criar uma Discipline com Name vazio num Course válido");

        entity.name = name;
        entity.credits = credits;
        entity.requiredCredits = requiredCredits;
        entity.requiredDisciplines = requiredDisciplines;
        DisciplineDTO response = RULE.client().target(url + "course/" + course.getId() + "/discipline").request()
                .post(Entity.json(entity), DisciplineDTO.class);
        assertNotNull(response.id, "Discipline criada não possui ID");
        assertEquals(code, response.code, "Code da Discipline criada não corresponde com o informado");
        assertEquals(name, response.name, "Name da Discipline criada não corresponde com o informado");
        assertEquals(credits, (int) response.credits, "Credits da Discipline criada não corresponde com o informado");
        assertEquals(requiredCredits, (int) response.requiredCredits, "Required Credits da Discipline criada não corresponde com o informado");
        assertEquals(0, response.students.size(), "Lista de Students da Discipline criada não está vazia");
        assertNull(response.teacher, "Discipline criada possui um Teacher associado");
        assertEquals(requiredDisciplines.size(), response.requiredDisciplines.size(),
                "Quantidade de Required Disciplines não está igual à quantidade informada");

        return response;
    }

    private void deleteCourseWithAssociatedDiscipline(CourseDTO course, SecretaryDTO secretary, DisciplineDTO discipline) {
        assertDoesNotThrow(() -> RULE.client().target(url + "course/" + course.getId()).request()
                .delete(DisciplineDTO.class), "Falhou ao deletar Course válido");

        assertDoesNotThrow(() -> RULE.client().target(url + "secretary/" + secretary.getId()).request()
                .delete(DisciplineDTO.class), "Falhou ao recuperar Secretary associada ao Course deletado");

        assertThrows(NotFoundException.class, () -> RULE.client().target(url + "discipline/" + discipline.getId()).request()
                .get(DisciplineDTO.class), "API não retornou status 404 ao recuperar Discipline associada ao Course deletado");
    }
}

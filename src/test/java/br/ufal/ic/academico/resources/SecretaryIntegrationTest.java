package br.ufal.ic.academico.resources;

import br.ufal.ic.academico.models.course.CourseDTO;
import br.ufal.ic.academico.models.department.DepartmentDTO;
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
class SecretaryIntegrationTest extends IntegrationTestBase {
    @Test
    void secretaryResources() {
        assertEquals(0, RULE.client().target(url + "secretary").request().get(new GenericType<List<SecretaryDTO>>(){}).size(),
                "Banco de dados iniciou com Secretaries cadastradas");

        DepartmentDTO department = background.createDepartment(RULE, "IC");
        SecretaryDTO secretary = background.createSecretary(RULE, department, "GRADUATION");
        department.secretaries.add(secretary);
        getSecretaryByID(secretary);

        assertEquals(1, RULE.client().target(url + "secretary").request().get(new GenericType<List<SecretaryDTO>>(){}).size(),
                "Listagem de Secretaries não foi atualizada corretamente após criação de Secretary");

        deleteSecretary(secretary, department, null);

        assertEquals(0, RULE.client().target(url + "secretary").request().get(new GenericType<List<SecretaryDTO>>(){}).size(),
                "Listagem de Secretaries não foi atualizada corretamente após remoção de Secretary");

        secretary = background.createSecretary(RULE, department, "POST-GRADUATION");
        department.secretaries.add(secretary);
        getSecretaryByID(secretary);
        getCoursesFromSecretary(secretary, 0);

        List<CourseDTO> courses = new ArrayList<>();
        courses.add(createCourse(secretary, "Engenharia da Computação"));
        getCoursesFromSecretary(secretary, 1);

        deleteSecretary(secretary, department, courses);
    }

    private void getSecretaryByID(SecretaryDTO secretary) {
        assertThrows(NotFoundException.class, () -> RULE.client().target(url + "secretary/0").request().get(SecretaryDTO.class),
                "API não retornou status 404 ao tentar recuperar uma Secretary inválida");

        SecretaryDTO response = RULE.client().target(url + "secretary/" + secretary.getId()).request().get(SecretaryDTO.class);
        assertEquals(secretary.id, response.id, "Secretary retornada possui ID diferente do informado");
        assertEquals(secretary.type, response.type, "Secretary retornada possui Type diferente da informada");
        assertEquals(secretary.disciplines, response.disciplines, "Secretary retornada possui uma lista de Disciplines diferente da informada");
    }

    private void deleteSecretary(SecretaryDTO secretary, DepartmentDTO department, List<CourseDTO> courses) {
        assertThrows(NotFoundException.class, () -> RULE.client().target(url + "secretary/0").request()
                .delete(SecretaryDTO.class), "API não retornou status 404 ao deletar uma Secretary inválida");

        assertDoesNotThrow(() -> RULE.client().target(url + "secretary/" + secretary.getId()).request()
                .delete(SecretaryDTO.class), "Falhou ao deletar uma Secretary válida");

        DepartmentDTO response = RULE.client().target(url + "department/" + department.getId()).request().get(DepartmentDTO.class);
        assertEquals(department.id, response.id, "Department teve seu ID alterado ao remover uma Secretary associada");
        assertEquals(department.secretaries.size() - 1, response.secretaries.size(),
                "Quantidade de Secretaries associadas ao Department não decresceu ao deletar uma Secretary associada");

        if (courses != null) {
            for (CourseDTO course : courses) {
                assertThrows(NotFoundException.class, () -> RULE.client().target(url + "course/" + course.id).request().get(CourseDTO.class));
            }
        }

        department.secretaries.remove(secretary);
    }

    private void getCoursesFromSecretary(SecretaryDTO secretary, int expectedCoursesQuantity) {
        assertThrows(NotFoundException.class, () -> RULE.client().target(url + "secretary/0/course").request()
                .get(new GenericType<List<String>>(){}), "API não retornou status 404 ao recuperar Coruses de uma Secretary inválida");

        List<String> response = RULE.client().target(url + "secretary/" + secretary.getId() + "/course").request()
                .get(new GenericType<List<String>>(){});
        assertEquals(expectedCoursesQuantity, response.size(), "Quantidade de Courses associados à Secretary não está conforme o esperado");
    }

    private CourseDTO createCourse(SecretaryDTO secretary, String courseName) {
        assertThrows(NotFoundException.class, () -> RULE.client().target(url + "secretary/0/course").request()
                .post(Entity.json(new CourseDTO()), CourseDTO.class),
                "API não retornou status 404 ao criar um Course numa Secretary inválida");

        CourseDTO entity = new CourseDTO();
        assertThrows(BadRequestException.class, () -> RULE.client().target(url + "secretary/" + secretary.getId() + "/course").request()
                        .post(Entity.json(entity), CourseDTO.class),
                "API não retornou status 400 ao criar um Course sem Name");
        entity.name = "";
        assertThrows(BadRequestException.class, () -> RULE.client().target(url + "secretary/" + secretary.getId() + "/course").request()
                        .post(Entity.json(entity), CourseDTO.class),
                "API não retornou status 400 ao criar um Course com Name vazio");


        entity.name = courseName;
        CourseDTO response = RULE.client().target(url + "secretary/" + secretary.getId() + "/course").request()
                .post(Entity.json(entity), CourseDTO.class);
        assertNotNull(response.getId(), "Course criado não possui ID");
        assertEquals(courseName, response.name, "Name do Course criado não corresponde com o informado");
        assertEquals(0, response.getDisciplines().size(), "Course criado possui Disciplines associadas");

        return response;
    }
}

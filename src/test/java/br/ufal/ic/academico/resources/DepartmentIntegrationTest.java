package br.ufal.ic.academico.resources;

import br.ufal.ic.academico.models.department.DepartmentDTO;
import br.ufal.ic.academico.models.secretary.SecretaryDTO;
import org.junit.jupiter.api.Test;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DepartmentIntegrationTest extends IntegrationTestBase {
    @Test
    void departmentResources() {
        assertEquals(0, RULE.client().target(url + "department").request().get(new GenericType<List<DepartmentDTO>>(){}).size(),
                "Banco de dados iniciou com Departments cadastrados");

        DepartmentDTO department = createDepartment();
        getDepartmentByID(department);
        department = updateDepartment(department);
        deleteDepartment(department);

        department = createDepartment();
        getDepartmentSecretaries(department);
        createSecretary(department, "POST-GRADUATION");
        getDepartmentByID(department);
        getDepartmentSecretaries(department);
        SecretaryDTO secretary = createSecretary(department, "GRADUATION");
        getDepartmentByID(department);
        getDepartmentSecretaries(department);
    }

    private DepartmentDTO createDepartment() {
        assertThrows(BadRequestException.class, () ->   RULE.client().target(url + "department").request()
                .post(Entity.json(new DepartmentDTO()), DepartmentDTO.class),
                "API não retornou status 400 ao criar um Department sem Name");
        assertThrows(BadRequestException.class, () ->   RULE.client().target(url + "department").request()
                .post(Entity.json(new DepartmentDTO(null, "", null)), DepartmentDTO.class),
                "API não retornou status 400 ao criar um Department com Name vazio");

        DepartmentDTO entity = new DepartmentDTO();
        entity.name = "IC";
        DepartmentDTO response = RULE.client().target(url + "department").request()
                .post(Entity.json(entity), DepartmentDTO.class);
        assertNotNull(response.id, "Department criado não possui ID");
        assertEquals(entity.name, response.name, "Name do Department criado está diferente do informado");
        assertEquals(0, response.secretaries.size(), "Lista de Secretaries do Department criado não está vazia");

        return response;
    }

    private DepartmentDTO getDepartmentByID(DepartmentDTO department) {
        assertThrows(NotFoundException.class, () -> RULE.client().target(url + "department/0").request()
                .get(DepartmentDTO.class), "API não retornou status 404 ao recuperar um Department com ID inválido");

        DepartmentDTO response =  RULE.client().target(url + "department/" + department.getId()).request().get(DepartmentDTO.class);
        assertEquals(department.id, response.id, "ID do Department retornado está diferente do esperado");
        assertEquals(department.name, response.name, "Name do Department retornado está diferente do esperado");
        assertEquals(department.secretaries.size(), response.secretaries.size(),
                "Lista de Secretaries do Department retornada está diferente da esperada");

        return response;
    }

    private DepartmentDTO updateDepartment(DepartmentDTO department) {
        assertThrows(NotFoundException.class, () -> RULE.client().target(url + "department/0").request()
                        .put(Entity.json(new DepartmentDTO()), DepartmentDTO.class), "API não retornou status 404 ao atualizar um Department inválido");

        DepartmentDTO response = RULE.client().target(url + "department/" + department.getId()).request()
                .put(Entity.json(new DepartmentDTO()), DepartmentDTO.class);
        assertEquals(department.getId(), response.getId(), "Ao enviar um Department vazio na atualização de Department seu ID foi alterado");
        assertEquals(department.name, response.name, "Ao enviar um Department vazio na atualização de Department seu Name foi alterado");
        assertEquals(department.secretaries, response.secretaries,
                "Ao enviar um Department vazio na atualização de Department suas Secretaries foram alteradas");

        DepartmentDTO entity = new DepartmentDTO();
        entity.name = "FDA";
        response = RULE.client().target(url + "department/" + department.getId()).request()
                .put(Entity.json(entity), DepartmentDTO.class);
        assertEquals(entity.name, response.name, "Name do Department não foi atualizado corretamente");
        assertEquals(department.id, response.id, "ID do Department retornado na atualização está diferente do informado");
        assertEquals(department.secretaries, response.secretaries,
                "Secretaries do Department foram alteradas durante a atualização de Name do Department");

        return response;
    }

    private void deleteDepartment(DepartmentDTO department) {
        assertThrows(NotFoundException.class, () -> RULE.client().target(url + "department/0").request()
                .delete(DepartmentDTO.class), "API não retornou status 404 ao deletar Department inválido");
        assertDoesNotThrow(() -> RULE.client().target(url + "department/" + department.getId()).request()
                .delete(DepartmentDTO.class), "Falhou ao deletar Department válido");
    }

    private void getDepartmentSecretaries(DepartmentDTO department) {
        assertThrows(NotFoundException.class, () -> RULE.client().target(url + "department/0/secretary").request()
                .get(new GenericType<List<SecretaryDTO>>(){}),
                "API não retornou status 404 ao recuperar as Secretaries de um Department inválido");

        List<SecretaryDTO> response = RULE.client().target(url + "department/" + department.getId() + "/secretary").request()
                .get(new GenericType<List<SecretaryDTO>>(){});
        assertEquals(department.secretaries.size(), response.size(), "Listagem de Secretaries do Department está diferente da esperada");
    }

    private SecretaryDTO createSecretary(DepartmentDTO department, String type) {
        assertThrows(NotFoundException.class, () -> RULE.client().target(url + "department/0/secretary").request()
                .post(Entity.json(new SecretaryDTO()), SecretaryDTO.class),
                "API não retornou status 404 ao criar uma Secretary num Department inválido");
        SecretaryDTO entity = new SecretaryDTO();
        entity.type = "A";
        assertThrows(BadRequestException.class, () -> RULE.client().target(url + "department/" + department.getId() + "/secretary").request()
                .post(Entity.json(entity), SecretaryDTO.class),
                "API não retornou status 400 ao criar uma Secretary com Type inválido");

        entity.type = type;
        SecretaryDTO response = RULE.client().target(url + "department/" + department.getId() + "/secretary").request()
                .post(Entity.json(entity), SecretaryDTO.class);
        assertNotNull(response.id, "Secretary criada não possui ID");
        assertEquals(entity.type, response.type, "Secretary criada possui um Type diferente do informado");
        assertEquals(0, response.disciplines.size(), "Secretary criada não possui uma lista vazia de Disciplines");
        assertThrows(BadRequestException.class, () -> RULE.client().target(url + "department/" + department.getId() + "/secretary").request()
                .post(Entity.json(entity), DepartmentDTO.class),
                "API não retornou status 400 ao criar uma nova Secretary com Type igual a outra já existente");

        department.secretaries.add(response);
        return response;
    }
}

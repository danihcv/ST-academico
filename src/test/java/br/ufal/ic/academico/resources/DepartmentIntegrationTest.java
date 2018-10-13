package br.ufal.ic.academico.resources;

import br.ufal.ic.academico.models.department.DepartmentDTO;
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

    private void getDepartmentByID(DepartmentDTO department) {
        assertThrows(NotFoundException.class, () -> RULE.client().target(url + "department/0").request()
                .get(DepartmentDTO.class), "API não retornou status 404 ao recuperar um Department com ID inválido");

        DepartmentDTO response =  RULE.client().target(url + "department/" + department.getId()).request().get(DepartmentDTO.class);
        assertEquals(department.id, response.id, "ID do Department retornado está diferente do esperado");
        assertEquals(department.name, response.name, "Name do Department retornado está diferente do esperado");
        assertEquals(department.secretaries, response.secretaries, "Secretaries do Department retornado está diferente do esperado");
    }
}

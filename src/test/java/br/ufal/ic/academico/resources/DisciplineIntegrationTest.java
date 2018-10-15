package br.ufal.ic.academico.resources;

import br.ufal.ic.academico.models.course.CourseDTO;
import br.ufal.ic.academico.models.department.DepartmentDTO;
import br.ufal.ic.academico.models.discipline.Discipline;
import br.ufal.ic.academico.models.discipline.DisciplineDTO;
import br.ufal.ic.academico.models.secretary.SecretaryDTO;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(DropwizardExtensionsSupport.class)
class DisciplineIntegrationTest extends IntegrationTestBase {
    @Test
    void disciplineResources() {
        assertEquals(0, RULE.client().target(url + "discipline").request().get(new GenericType<List<DisciplineDTO>>(){}).size(),
                "Banco de dados iniciou com Disciplines cadastradas");

        DepartmentDTO department = background.createDepartment(RULE, "IC");
        SecretaryDTO secretary = background.createSecretary(RULE, department, "GRADUATION");
        CourseDTO course = background.createCourse(RULE, secretary, "Ciência da Computação");
        DisciplineDTO discipline = background.createDiscipline(RULE, course, "CC100", "Teste de Software", 80);
        course.getDisciplines().add(discipline);

        getDisciplineByID(discipline);

        List<String> requiredDisciplines = new ArrayList<>();
        requiredDisciplines.add("CC001");
        discipline = updateDiscipline(discipline, "Testes", 40, 20, requiredDisciplines);

        assertEquals(1, RULE.client().target(url + "discipline").request().get(new GenericType<List<DisciplineDTO>>(){}).size(),
                "Listagem de Disciplines não está refletindo a Discipline criada");

        deleteDiscipline(discipline, course);

        assertEquals(0, RULE.client().target(url + "discipline").request().get(new GenericType<List<DisciplineDTO>>(){}).size(),
                "Listagem de Disciplines não está refletindo a Discipline deletada");
    }

    private void getDisciplineByID(DisciplineDTO discipline) {
        assertThrows(NotFoundException.class, () -> RULE.client().target(url + "discipline/0").request().get(DisciplineDTO.class),
                "API não retornou status 404 ao recuperar uma Discipline com ID inválida");

        DisciplineDTO response = RULE.client().target(url + "discipline/" + discipline.getId()).request().get(DisciplineDTO.class);
        assertEquals(discipline.id, response.id, "Discipline recuperada possui ID diferente do informado");
        assertEquals(discipline.code, response.code, "Discipline recuperada possui Code diferente do informado");
        assertEquals(discipline.teacher, response.teacher, "Discipline recuperada possui Teacher diferente do informado");
        assertEquals(discipline.credits, response.credits, "Discipline recuperada possui Credits diferente do informado");
        assertEquals(discipline.requiredCredits, response.requiredCredits, "Discipline recuperada possui Required Credits diferente do informado");
        assertEquals(discipline.requiredDisciplines.size(), response.requiredDisciplines.size(),
                "Discipline recuperada possui Quantidade de Required Disciplines diferente do informado");
    }

    private DisciplineDTO updateDiscipline(DisciplineDTO discipline, String newName, int newCredits, int newRequiredCredits,
                                           List<String> newRequiredDisciplines) {
        assertThrows(NotFoundException.class, () -> RULE.client().target(url + "discipline/0").request()
                .put(Entity.json(new DisciplineDTO()), DisciplineDTO.class),
                "API não retornou status 404 ao atualizar Discipline com ID inválido");

        DisciplineDTO entity = new DisciplineDTO();
        DisciplineDTO response = RULE.client().target(url + "discipline/" + discipline.getId()).request()
                .put(Entity.json(entity), DisciplineDTO.class);
        assertEquals(discipline.id, response.id, "Discipline teve seu ID alterado ao enviar uma Discipline vazia na atualização");
        assertEquals(discipline.code, response.code, "Discipline teve seu Code alterado ao enviar uma Discipline vazia na atualização");
        assertEquals(discipline.name, response.name, "Discipline teve seu Name alterado ao enviar uma Discipline vazia na atualização");
        assertEquals(discipline.credits, response.credits, "Discipline teve seus Credits alterados ao enviar uma Discipline vazia na atualização");
        assertEquals(discipline.requiredCredits, response.requiredCredits,
                "Discipline teve seus Required Credits alterados ao enviar uma Discipline vazia na atualização");
        assertEquals(discipline.teacher, response.teacher,
                "Discipline teve seu Teacher alterado ao enviar uma Discipline vazia na atualização");
        assertEquals(discipline.students.size(), response.students.size(),
                "Discipline teve a quantidade de Students alterada ao enviar uma Discipline vazia na atualização");
        assertEquals(discipline.requiredDisciplines.size(), response.requiredDisciplines.size(),
                "Discipline teve a quantidade de Required Disciplines alterada ao enviar uma Discipline vazia na atualização");

        entity.name = newName;
        entity.credits = newCredits;
        entity.requiredCredits = newRequiredCredits;
        entity.requiredDisciplines = newRequiredDisciplines;
        response = RULE.client().target(url + "discipline/" + discipline.getId()).request().put(Entity.json(entity), DisciplineDTO.class);
        assertEquals(discipline.id, response.id, "Discipline teve seu ID alterado ao ser atualizada");
        assertEquals(discipline.code, response.code, "Discipline teve seu Code alterado ao ser atualizada");
        assertEquals(discipline.teacher, response.teacher, "Discipline teve seu Teacher alterado ao ser atualizada");
        assertEquals(discipline.students.size(), response.students.size(),
                "Discipline teve a quantidade de Students alterada ao ser atualizada");
        assertEquals(newName, response.name, "Name da Discipline não foi atualizado corretamente");
        assertEquals(newCredits, (int) response.credits, "Credits da Discipline não foi atualizado corretamente");
        assertEquals(newRequiredCredits, (int) response.requiredCredits, "Required Credits da Discipline não foi atualizado corretamente");
        assertEquals(newRequiredDisciplines.size(), response.requiredDisciplines.size(),
                "Quantidade de Required Disciplines da Discipline não foi atualizado corretamente");

        return response;
    }

    private void deleteDiscipline(DisciplineDTO discipline, CourseDTO course) {
        assertThrows(NotFoundException.class, () -> RULE.client().target(url + "discipline/0").request().delete(Discipline.class),
                "API não retornou status 404 ao deletar Discipline com ID inválido");

        assertDoesNotThrow(() -> RULE.client().target(url + "discipline/" + discipline.getId()).request().delete(Discipline.class),
                "Falhou ao deletar Discipline válida");

        CourseDTO response = RULE.client().target(url + "course/" + course.getId()).request().get(CourseDTO.class);
        assertEquals(course.id, response.id, "Ao deletar Discipline, o Course associado teve seu ID alterado");
        assertEquals(course.name, response.name, "Ao deletar Discipline, o Course associado teve seu Name alterado");
        assertEquals(course.getDisciplines().size() - 1, response.getDisciplines().size(),
                "Ao deletar Discipline, o Course associado não teve a quantidade de Disciplines decrescida");

        course.getDisciplines().remove(discipline);
    }
}

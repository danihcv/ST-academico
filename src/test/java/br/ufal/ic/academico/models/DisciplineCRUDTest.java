package br.ufal.ic.academico.models;

import br.ufal.ic.academico.models.course.Course;
import br.ufal.ic.academico.models.discipline.Discipline;
import br.ufal.ic.academico.models.discipline.DisciplineDAO;
import br.ufal.ic.academico.models.person.student.Student;
import br.ufal.ic.academico.models.person.teacher.Teacher;
import br.ufal.ic.academico.models.secretary.Secretary;
import io.dropwizard.testing.junit5.DAOTestExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(DropwizardExtensionsSupport.class)
class DisciplineCRUDTest {

    private DAOTestExtension dbTesting = DAOTestExtension.newBuilder()
            .addEntityClass(Student.class)
            .addEntityClass(Teacher.class)
            .addEntityClass(Secretary.class)
            .addEntityClass(Course.class)
            .addEntityClass(Discipline.class)
            .build();

    private DisciplineDAO dao = new DisciplineDAO(dbTesting.getSessionFactory());

    @Test
    void disciplineCRUD() {
        final Discipline d1 = create("Programação 1", "CC001", 80, 0, new ArrayList<>());
        get(d1);

        d1.setTeacher(new Teacher("Rodrigo", "Paes"));
        d1.setCredits(60);
        d1.setRequiredCredits(100);
        List<String> preRequisites = new ArrayList<>();
        preRequisites.add("CC002");
        preRequisites.add("CC003");
        d1.setRequiredDisciplines(preRequisites);
        update(d1);

        delete(d1);

        assertEquals(0, dbTesting.inTransaction(dao::getAll).size(),
                "Discipline1 não foi removido da listagem total de Disciplines");

        final Discipline d2 = create("Programação 2", "CC002", 0, 0, new ArrayList<>());
        get(d2);
        final Discipline d3 = create("Teste de Software", "CC003", 0, 0, new ArrayList<>());
        get(d3);

        assertEquals(2, dbTesting.inTransaction(dao::getAll).size(),
                "Nem todas as novas Disciplines estão aparecendo na listagem total de Disciplines");

        delete(d2);

        assertEquals(1, dbTesting.inTransaction(dao::getAll).size(),
                "Discipline2 não foi removido da listagem total de Disciplines");
        assertEquals(d3.getId(), dbTesting.inTransaction(dao::getAll).get(0).getId(),
                "Discipline3 não está na listagem total de Disciplines");
    }

    private Discipline create(String name, String code, Integer credits, Integer requiredCredits, List<String> requiredDisciplines) {
        final Discipline discipline = new Discipline(name, code, credits, requiredCredits, requiredDisciplines);

        final Discipline saved = dbTesting.inTransaction(() -> dao.persist(discipline));
        assertNull(dbTesting.inTransaction(() -> dao.getCourse(discipline)), "Discipline foi associada a um Course ao ser criada");
        assertNull(dbTesting.inTransaction(() -> dao.getSecretary(discipline)), "Discipline foi associada a uma Secretary ao ser criada");
        assertNotNull(saved, "Falhou ao salvar uma nova Discipline");
        assertNotNull(saved.getId(), "Discipline não recebeu um id ao ser criada");
        assertEquals(code, saved.getCode(), "Code da Discipline não corresponde com o informado");
        assertEquals(name, saved.getName(), "Name da Discipline não corresponde com o informado");
        assertEquals(credits, saved.getCredits(), "Credits não corresponde com o informado");
        assertEquals(requiredCredits, saved.getRequiredCredits(), "Required Credits não corresponde com o informado");
        assertEquals(requiredDisciplines.size(), saved.getRequiredDisciplines().size(), "Pré-requisitos foram associados incorretamente");
        assertNull(saved.getTeacher(), "Um professor foi associado à nova Discipline");
        assertEquals(new ArrayList<>(), saved.getStudents(), "Aluno(s) foi(ram) associado(s) à nova Discipline");

        return discipline;
    }

    private void get(Discipline discipline) {
        Discipline recovered = dbTesting.inTransaction(() -> dao.get(discipline.getId()));

        assertEquals(discipline.getId(), recovered.getId(), "ID da Discipline recuperada não confere com o informado");
        assertEquals(discipline.getName(), recovered.getName(), "Name da Discipline recuperada não confere com o informada");
        assertEquals(discipline.getCode(), recovered.getCode(), "Code da Discipline recuperada não confere com o informado");
        assertEquals(discipline.getCredits(), recovered.getCredits(), "Credits da Discipline recuperada não confere com o informado");
        assertEquals(discipline.getRequiredCredits(), recovered.getRequiredCredits(),
                "Required Credits da Discipline recuperada não confere com o informado");
        assertEquals(discipline.getRequiredDisciplines().size(), recovered.getRequiredDisciplines().size(),
                "Quantidade de Required Disciplines da Discipline recuperada não confere com a informada");
    }

    private void update(Discipline discipline) {
        final Discipline updated = dbTesting.inTransaction(() -> dao.persist(discipline));

        assertEquals(discipline.getId(), updated.getId(), "Ao ser atualizada, Discipline teve seu ID alterado");
        assertEquals(discipline.getName(), updated.getName(), "Name da Discipline não foi alterado corretamente");
        assertEquals(discipline.getCode(), updated.getCode(), "Code da Discipline não foi alterado corretamente");
        if (discipline.getTeacher() != null) {
            assertNotNull(updated.getTeacher(), "Nenhum Teacher foi associado à Discipline");
            assertEquals(discipline.getTeacher().getId(), updated.getTeacher().getId(), "Teacher correto não foi associado à Discipline");
        } else {
            assertNull(updated.getTeacher(), "Teacher foi associado à Discipline ao atualizá-la");
        }
        assertEquals(discipline.getStudents().size(), updated.getStudents().size(), "Lista de Students foi alterada incorretamente");
        assertEquals(discipline.getCredits(), updated.getCredits(), "O valor de credits da Discipline não foi atualizado corretamente");
        assertEquals(discipline.getRequiredCredits(), updated.getRequiredCredits(), "Required credits não foi atualizado corretamente");
        assertEquals(discipline.getRequiredDisciplines().size(), updated.getRequiredDisciplines().size(),
                "Pré-requisitos não foram atualizados corretamente");
    }

    private void delete(Discipline discipline) {
        dbTesting.inTransaction(() -> dao.delete(discipline));
        assertNull(dbTesting.inTransaction(() -> dao.get(discipline.getId())), "Discipline não foi removida");
    }
}

package br.ufal.ic.academico.models;

import br.ufal.ic.academico.models.course.Course;
import br.ufal.ic.academico.models.course.CourseDAO;
import br.ufal.ic.academico.models.discipline.Discipline;
import br.ufal.ic.academico.models.person.student.Student;
import br.ufal.ic.academico.models.person.teacher.Teacher;
import br.ufal.ic.academico.models.secretary.Secretary;
import io.dropwizard.testing.junit5.DAOTestExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(DropwizardExtensionsSupport.class)
class CourseCRUDTest {

    private DAOTestExtension dbTesting = DAOTestExtension.newBuilder()
            .addEntityClass(Student.class)
            .addEntityClass(Teacher.class)
            .addEntityClass(Secretary.class)
            .addEntityClass(Course.class)
            .addEntityClass(Discipline.class)
            .build();

    private CourseDAO dao = new CourseDAO(dbTesting.getSessionFactory());

    @Test
    void courseCRUD() {
        final Course c1 = create("Ciência da Computação");
        get(c1);

        c1.setName("Engenharia da Computação");
        c1.addDiscipline(new Discipline());
        update(c1);

        delete(c1);

        assertEquals(0, dbTesting.inTransaction(dao::getAll).size(),
                "Course1 não foi removido da listagem total de Courses");

        final Course c2 = create("Jornalismo");
        final Course c3 = create("Direito");

        assertEquals(2, dbTesting.inTransaction(dao::getAll).size(),
                "Nem todos os novos Courses estão aparecendo na listagem total de Courses");

        delete(c2);

        assertEquals(1, dbTesting.inTransaction(dao::getAll).size(),
                "Course2 não foi removido da listagem total de Courses");
        assertEquals(c3.getId(), dbTesting.inTransaction(dao::getAll).get(0).getId(),
                "Course3 não está na listagem total de Courses");
    }

    private Course create(String name) {
        final Course course = new Course(name);
        final Course saved = dbTesting.inTransaction(() -> dao.persist(course));

        assertNotNull(saved, "Falhou ao salvar um novo Course");
        assertNotNull(saved.getId(), "Course não recebeu um id ao ser criado");
        assertEquals(name, saved.getName(),
                "Name do Course não corresponde com o informado");
        assertNotNull(saved.getDisciplines(), "Course não recebeu uma lista vazia de Disciplines");
        assertEquals(0, saved.getDisciplines().size(), "Course foi criado com Discipline(s) associada(s)");
        assertNull(dbTesting.inTransaction(() -> dao.getSecretary(saved)), "Course foi associado à uma Secretary");

        return course;
    }

    private void get(Course course) {
        Course recovered = dbTesting.inTransaction(() -> dao.get(course.getId()));

        assertEquals(course.getId(), recovered.getId(), "ID do Course recuperado não confere com o informado");
        assertEquals(course.getName(), recovered.getName(), "Name do Course recuperado não confere com o informado");
        assertEquals(course.getDisciplines().size(), recovered.getDisciplines().size(),
                "Quantidade de Disciplines do Course recuperado não confere com a informada");
    }

    private void update(Course course) {
        final Course updated = dbTesting.inTransaction(() -> dao.persist(course));
        assertEquals(course.getId(), updated.getId(), "Ao ser atualizado, Course teve seu ID alterado");
        assertEquals(course.getName(), updated.getName(), "Name do Course não foi atualizado corretamente");
        assertNotNull(updated.getDisciplines(), "Ao ser atualizado, Course teve sua lista de Disciplines deletada");
        assertEquals(course.getDisciplines().size(), updated.getDisciplines().size(), "Discpline não foi associada corretamente");
    }

    private void delete(Course course) {
        dbTesting.inTransaction(() -> dao.delete(course));
        assertNull(dbTesting.inTransaction(() -> dao.get(course.getId())), "Course não foi removido");
    }
}

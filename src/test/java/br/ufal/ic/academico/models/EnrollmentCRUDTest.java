package br.ufal.ic.academico.models;

import br.ufal.ic.academico.models.course.Course;
import br.ufal.ic.academico.models.course.CourseDAO;
import br.ufal.ic.academico.models.department.Department;
import br.ufal.ic.academico.models.department.DepartmentDAO;
import br.ufal.ic.academico.models.discipline.Discipline;
import br.ufal.ic.academico.models.discipline.DisciplineDAO;
import br.ufal.ic.academico.models.person.student.Student;
import br.ufal.ic.academico.models.person.student.StudentDAO;
import br.ufal.ic.academico.models.person.teacher.Teacher;
import br.ufal.ic.academico.models.person.teacher.TeacherDAO;
import br.ufal.ic.academico.models.secretary.Secretary;
import br.ufal.ic.academico.models.secretary.SecretaryDAO;
import io.dropwizard.testing.junit5.DAOTestExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;

@ExtendWith(DropwizardExtensionsSupport.class)
class EnrollmentCRUDTest {
    
    private DAOTestExtension dbTesting = DAOTestExtension.newBuilder()
            .addEntityClass(Student.class)
            .addEntityClass(Teacher.class)
            .addEntityClass(Department.class)
            .addEntityClass(Secretary.class)
            .addEntityClass(Course.class)
            .addEntityClass(Discipline.class)
            .build();

    private StudentDAO studentDAO = new StudentDAO(dbTesting.getSessionFactory());
    private TeacherDAO teacherDAO = new TeacherDAO(dbTesting.getSessionFactory());
    private DepartmentDAO departmentDAO = new DepartmentDAO(dbTesting.getSessionFactory());
    private SecretaryDAO secretaryDAO = new SecretaryDAO(dbTesting.getSessionFactory());
    private CourseDAO courseDAO = new CourseDAO(dbTesting.getSessionFactory());
    private DisciplineDAO disciplineDAO = new DisciplineDAO(dbTesting.getSessionFactory());

    @Test
    void enrollmentCRUD() {
        // Students
        Student stdntNewGrad = new Student("Daniel", "Vassalo");
        Student savedStdntNewGrad = dbTesting.inTransaction(() -> studentDAO.persist(stdntNewGrad));

        Student stdntOldGrad = new Student("Gabriel", "Barbosa");
        stdntOldGrad.setCredits(240);
        Student savedStdntOldGrad = dbTesting.inTransaction(() -> studentDAO.persist(stdntOldGrad));

        Student stdntNewPostGrad = new Student("Romero", "Malaquias");
        Student savedStdntNewPostGrad = dbTesting.inTransaction(() -> studentDAO.persist(stdntNewPostGrad));

        Student stdntOldPostGrad = new Student("Marcos", "Paulo");
        stdntOldPostGrad.setCredits(300);
        Student savedStdntOldPostGrad = dbTesting.inTransaction(() -> studentDAO.persist(stdntOldPostGrad));

        // Teachers
        Teacher teacher1 = new Teacher("Willy", "Tiengo");
        dbTesting.inTransaction(() -> teacherDAO.persist(teacher1));

        Teacher teacher2 = new Teacher("Rodrigo", "Paes");
        dbTesting.inTransaction(() -> teacherDAO.persist(teacher2));

        // Disciplines
        List<String> prerequisites = new ArrayList<>();
        Discipline discipGrad1 = new Discipline("Programação 1", "EC001", 80, 0, new ArrayList<>());
        discipGrad1.setTeacher(teacher1);
        Discipline savedDiscipGrad1 = dbTesting.inTransaction(() -> disciplineDAO.persist(discipGrad1));

        prerequisites.add("EC001");
        Discipline discipGrad2 = new Discipline("Programação 2", "EC002", 80, 80, prerequisites);
        discipGrad2.setTeacher(teacher2);
        Discipline savedDiscipGrad2 = dbTesting.inTransaction(() -> disciplineDAO.persist(discipGrad2));

        Discipline discipPostGrad1 = new Discipline("Projeto e Análise de Algoritmos", "CC101", 80, 0, new ArrayList<>());
        discipPostGrad1.setTeacher(teacher2);
        Discipline savedDiscipPostGrad1 = dbTesting.inTransaction(() -> disciplineDAO.persist(discipPostGrad1));

        Discipline discipGrad3 = new Discipline("Direito Constitucional", "DD001", 80, 0, new ArrayList<>());
        discipGrad3.setTeacher(teacher1);
        Discipline savedDiscipGrad3 = dbTesting.inTransaction(() -> disciplineDAO.persist(discipGrad3));

        Discipline discipPostGrad2 = new Discipline("Direito Penal", "DD101", 80, 0, new ArrayList<>());
        discipPostGrad2.setTeacher(teacher2);
        Discipline savedDiscipPostGrad2 = dbTesting.inTransaction(() -> disciplineDAO.persist(discipPostGrad2));

        // Courses
        Course compEngineeringGrad = new Course("Engenharia da Computação");
        compEngineeringGrad.addDiscipline(discipGrad1);
        compEngineeringGrad.addDiscipline(discipGrad2);
        Course savedCompEngineeringGrad = dbTesting.inTransaction(() -> courseDAO.persist(compEngineeringGrad));

        Course compSciencePostGrad = new Course("Ciência da Computação");
        compSciencePostGrad.addDiscipline(discipPostGrad1);
        Course savedCompSciencePostGrad = dbTesting.inTransaction(() -> courseDAO.persist(compSciencePostGrad));

        Course lawGrad = new Course("Direito");
        lawGrad.addDiscipline(discipGrad3);
        Course savedLawGrad = dbTesting.inTransaction(() -> courseDAO.persist(lawGrad));

        Course lawPostGrad = new Course("Direito");
        lawPostGrad.addDiscipline(discipPostGrad2);
        Course savedLawPostGrad = dbTesting.inTransaction(() -> courseDAO.persist(lawPostGrad));

        // Secretaries
        Secretary secICgrad = new Secretary("GRADUATION");
        secICgrad.addCourse(compEngineeringGrad);
        Secretary savedSecICGrad = dbTesting.inTransaction(() -> secretaryDAO.persist(secICgrad));

        Secretary secICPostGrad = new Secretary("POST-GRADUATION");
        secICPostGrad.addCourse(compSciencePostGrad);
        Secretary savedSecICPostGrad = dbTesting.inTransaction(() -> secretaryDAO.persist(secICPostGrad));

        Secretary secFDAGrad = new Secretary("GRADUATION");
        secFDAGrad.addCourse(lawGrad);
        Secretary savedSecFDAGrad = dbTesting.inTransaction(() -> secretaryDAO.persist(secFDAGrad));

        Secretary secFDAPostGrad = new Secretary("POST-GRADUATION");
        secFDAPostGrad.addCourse(lawPostGrad);
        Secretary savedSecFDAPostGrad = dbTesting.inTransaction(() -> secretaryDAO.persist(secFDAPostGrad));

        // Departments
        Department IC = new Department("IC");
        IC.setGraduation(secICgrad);
        IC.setPostGraduation(secICPostGrad);
        Department savedIC = dbTesting.inTransaction(() -> departmentDAO.persist(IC));

        Department FDA = new Department("FDA");
        FDA.setGraduation(secFDAGrad);
        FDA.setPostGraduation(secFDAPostGrad);
        Department savedFDA = dbTesting.inTransaction(() -> departmentDAO.persist(FDA));

        // Tests
        assertNotNull(discipGrad1.enroll(stdntNewGrad, null, IC, null, secICgrad));

        assertNull(savedStdntNewGrad.getCourse(), "New Student foi associado à um curso na criação");
        stdntNewGrad.setCourse(compEngineeringGrad);
        savedStdntNewGrad = dbTesting.inTransaction(() -> studentDAO.persist(stdntNewGrad));
        assertEquals(compEngineeringGrad.getId(), savedStdntNewGrad.getCourse().getId(),
                "New Student não foi associado corretamente ao Course");
        assertEquals(secICgrad.getId(), dbTesting.inTransaction(() -> studentDAO.getSecretary(stdntNewGrad)).getId(),
                "New Student não foi associado à Secretary correta");
        assertEquals(IC.getId(), dbTesting.inTransaction(() -> studentDAO.getDepartment(stdntNewGrad)).getId(),
                "New Student não foi associado ao Department correto");

        assertNull(savedStdntOldGrad.getCourse(), "Old Student foi associado à um curso na criação");
        stdntOldGrad.setCourse(compEngineeringGrad);
        savedStdntOldGrad = dbTesting.inTransaction(() -> studentDAO.persist(stdntOldGrad));
        assertEquals(compEngineeringGrad.getId(), savedStdntOldGrad.getCourse().getId(),
                "Old Student não foi associado corretamente ao Course");
        assertEquals(secICgrad.getId(), dbTesting.inTransaction(() -> studentDAO.getSecretary(stdntOldGrad)).getId(),
                "Old Student não foi associado à Secretary correta");
        assertEquals(IC.getId(), dbTesting.inTransaction(() -> studentDAO.getDepartment(stdntOldGrad)).getId(),
                "Old Student não foi associado ao Department correto");

        assertNull(savedStdntNewPostGrad.getCourse(), "New Post Gradute Student foi associado à um curso na criação");
        stdntNewPostGrad.setCourse(compSciencePostGrad);
        savedStdntNewPostGrad = dbTesting.inTransaction(() -> studentDAO.persist(stdntNewPostGrad));
        assertEquals(compSciencePostGrad.getId(), savedStdntNewPostGrad.getCourse().getId(),
                "New Post Graduate Student não foi associado corretamente ao Course");
        assertEquals(secICPostGrad.getId(), dbTesting.inTransaction(() -> studentDAO.getSecretary(stdntNewPostGrad)).getId(),
                "New Post Graduate Student não foi associado à Secretary correta");
        assertEquals(IC.getId(), dbTesting.inTransaction(() -> studentDAO.getDepartment(stdntNewPostGrad)).getId(),
                "New Post Graduate Student não foi associado ao Department correto");

        assertNull(savedStdntOldPostGrad.getCourse(), "Old Post Graduate Student foi associado à um curso na criação");
        stdntOldPostGrad.setCourse(compSciencePostGrad);
        savedStdntOldPostGrad = dbTesting.inTransaction(() -> studentDAO.persist(stdntOldPostGrad));
        assertEquals(compSciencePostGrad.getId(), savedStdntOldPostGrad.getCourse().getId(),
                "Old Post Graduate Student não foi associado corretamente ao Course");
        assertEquals(secICPostGrad.getId(), dbTesting.inTransaction(() -> studentDAO.getSecretary(stdntOldPostGrad)).getId(),
                "Old Post Graduate Student não foi associado à Secretary correta");
        assertEquals(IC.getId(), dbTesting.inTransaction(() -> studentDAO.getDepartment(stdntOldPostGrad)).getId(),
                "Old Post Graduate Student não foi associado ao Department correto");

        assertEquals(compEngineeringGrad.getId(), dbTesting.inTransaction(() -> disciplineDAO.getCourse(discipGrad1)).getId(),
                "Graduation Discipline não foi associada ao Course correto");
        assertEquals(compEngineeringGrad.getId(), dbTesting.inTransaction(() -> disciplineDAO.getCourse(discipGrad2)).getId(),
                "Graduation Discipline não foi associada ao Course correto");
        assertEquals(compSciencePostGrad.getId(), dbTesting.inTransaction(() -> disciplineDAO.getCourse(discipPostGrad1)).getId(),
                "Post graduation Discipline não foi associada ao Course correto");
        assertEquals(lawGrad.getId(), dbTesting.inTransaction(() -> disciplineDAO.getCourse(discipGrad3)).getId(),
                "Graduation Discipline não foi associada ao Course correto");
        assertEquals(lawPostGrad.getId(), dbTesting.inTransaction(() -> disciplineDAO.getCourse(discipPostGrad2)).getId(),
                "Post graduation Discipline não foi associada ao Course correto");

        assertEquals(secICgrad.getId(), dbTesting.inTransaction(() -> disciplineDAO.getSecretary(discipGrad1)).getId(),
                "Graduation Discipline não foi associada à Secretary correta");
        assertEquals(secICgrad.getId(), dbTesting.inTransaction(() -> disciplineDAO.getSecretary(discipGrad2)).getId(),
                "Graduation Discipline não foi associada à Secretary correta");
        assertEquals(secICPostGrad.getId(), dbTesting.inTransaction(() -> disciplineDAO.getSecretary(discipPostGrad1)).getId(),
                "Post graduation Discipline não foi associada à Secretary correta");
        assertEquals(secFDAGrad.getId(), dbTesting.inTransaction(() -> disciplineDAO.getSecretary(discipGrad3)).getId(),
                "Graduation Discipline não foi associada à Secretary correta");
        assertEquals(secFDAPostGrad.getId(), dbTesting.inTransaction(() -> disciplineDAO.getSecretary(discipPostGrad2)).getId(),
                "Post graduation Discipline não foi associada à Secretary correta");

        assertEquals(IC.getId(), dbTesting.inTransaction(() -> disciplineDAO.getDepartment(discipGrad1)).getId(),
                "Graduation Discipline não foi associada ao Department correto");
        assertEquals(IC.getId(), dbTesting.inTransaction(() -> disciplineDAO.getDepartment(discipGrad2)).getId(),
                "Graduation Discipline não foi associada ao Department correto");
        assertEquals(IC.getId(), dbTesting.inTransaction(() -> disciplineDAO.getDepartment(discipPostGrad1)).getId(),
                "Post graduation Discipline não foi associada ao Department correto");
        assertEquals(FDA.getId(), dbTesting.inTransaction(() -> disciplineDAO.getDepartment(discipGrad3)).getId(),
                "Graduation Discipline não foi associada ao Department correto");
        assertEquals(FDA.getId(), dbTesting.inTransaction(() -> disciplineDAO.getDepartment(discipPostGrad2)).getId(),
                "Post graduation Discipline não foi associada ao Department correto");

        assertEquals(teacher1.getId(), savedDiscipGrad1.getTeacher().getId(),
                "Teacher não foi associado à Graduation Discipline correta");
        assertEquals(teacher2.getId(), savedDiscipGrad2.getTeacher().getId(),
                "Teacher não foi associado à Graduation Discipline correta");
        assertEquals(teacher2.getId(), savedDiscipPostGrad1.getTeacher().getId(),
                "Teacher não foi associado à Post Graduation Discipline correta");
        assertEquals(teacher1.getId(), savedDiscipGrad3.getTeacher().getId(),
                "Teacher não foi associado à Graduation Discipline correta");
        assertEquals(teacher2.getId(), savedDiscipPostGrad2.getTeacher().getId(),
                "Teacher não foi associado à Post Graduation Discipline correta");

        assertEquals(2, savedCompEngineeringGrad.getDisciplines().size(),
                "Graduation Disciplines não foram associadas ao Course corretamente");
        assertEquals(1, savedCompSciencePostGrad.getDisciplines().size(),
                "Post Graduation Disciplines não foram associadas ao Course corretamente");
        assertEquals(1, savedLawGrad.getDisciplines().size(),
                "Graduation Disciplines não foram associadas ao Course corretamente");
        assertEquals(1, savedLawPostGrad.getDisciplines().size(),
                "Post Graduation Disciplines não foram associadas ao Course corretamente");

        assertEquals(secICgrad.getId(), dbTesting.inTransaction(() -> courseDAO.getSecretary(compEngineeringGrad)).getId(),
                "Graduation Course não foi associado à Secretary correta");
        assertEquals(secICPostGrad.getId(), dbTesting.inTransaction(() -> courseDAO.getSecretary(compSciencePostGrad)).getId(),
                "Post Graduation Course não foi associado à Secretary correta");
        assertEquals(secFDAGrad.getId(), dbTesting.inTransaction(() -> courseDAO.getSecretary(lawGrad)).getId(),
                "Graduation Course não foi associado à Secretary correta");
        assertEquals(secFDAPostGrad.getId(), dbTesting.inTransaction(() -> courseDAO.getSecretary(lawPostGrad)).getId(),
                "Post Graduation Course não foi associado à Secretary correta");

        assertEquals(1, savedSecICGrad.getCourses().size(),
                "Course não foi associado à Graduation Secretary corretamente");
        assertEquals(1, savedSecICPostGrad.getCourses().size(),
                "Course não foi associado à Post Graduation Secretary corretamente");
        assertEquals(1, savedSecFDAGrad.getCourses().size(),
                "Course não foi associado à Graduation Secretary corretamente");
        assertEquals(1, savedSecFDAPostGrad.getCourses().size(),
                "Course não foi associado à Post Graduation Secretary corretamente");

        assertEquals(secICgrad.getId(), savedIC.getGraduation().getId(),
                "Graduation Secretary não foi associada ao Department correto");
        assertEquals(secICPostGrad.getId(), savedIC.getPostGraduation().getId(),
                "Post Graduation Secretary não foi associada ao Department correto");
        assertEquals(secFDAGrad.getId(), savedFDA.getGraduation().getId(),
                "Graduation Secretary não foi associada ao Department correto");
        assertEquals(secFDAPostGrad.getId(), savedFDA.getPostGraduation().getId(),
                "Post Graduation Secretary não foi associada ao Department correto");

        assertEquals(0, discipGrad1.getStudents().size(),
                "Graduation Discipline possui Students matriculados");
        assertNull(discipGrad1.enroll(stdntNewGrad, IC, IC, secICgrad, secICgrad));
        assertNull(discipGrad1.enroll(stdntOldGrad, IC, IC, secICgrad, secICgrad));
        assertNotNull(discipGrad1.enroll(stdntNewPostGrad, IC, IC, secICPostGrad, secICgrad));
        assertNotNull(discipGrad1.enroll(stdntOldPostGrad, IC, IC, secICPostGrad, secICgrad));
        assertEquals(2, discipGrad1.getStudents().size(),
                "Graduation Discipline possui número de Students matriculados diferente do esperado");
        assertTrue(discipGrad1.getStudents().contains(stdntNewGrad),
                "Newbie Graduation Student não foi matriculado em " + discipGrad1.getName());
        assertTrue(discipGrad1.getStudents().contains(stdntOldGrad),
                "Veteran Graduation Student não foi matriculado em " + discipGrad1.getName());
        assertFalse(discipGrad1.getStudents().contains(stdntNewPostGrad),
                "Newbie Post Graduation Student foi matriculado em " + discipGrad1.getName());
        assertFalse(discipGrad1.getStudents().contains(stdntOldPostGrad),
                "Veteran Post Graduation Student foi matriculado em " + discipGrad1.getName());

        assertNotNull(discipGrad3.enroll(stdntNewGrad, IC, FDA, secICgrad, secFDAGrad),
                "Newbie Graduation Student foi matriculado numa Discipline de um Department diferente do Student");
        assertNotNull(discipGrad2.enroll(stdntNewPostGrad, IC, IC, secICPostGrad, secICgrad),
                "Newbie Post Graduation Student foi matriculado numa Graduation Discipline");
        assertNull(discipPostGrad1.enroll(stdntNewPostGrad, IC, IC, secICPostGrad, secICPostGrad),
                "Newbie Post Graduation Student não foi matriculado numa Post Graduation Discipline");
        assertNotNull(discipPostGrad1.enroll(stdntNewGrad, IC, IC, secICgrad, secICPostGrad),
                "Newbie Graduation Student foi matriculado numa Post Graduation Discipline ");
        assertNull(discipPostGrad1.enroll(stdntOldGrad, IC, IC, secICgrad, secICPostGrad),
                "Veteran Graduation Student não foi matriculado numa Discipline de Post Graduation");
        assertNotNull(discipGrad2.enroll(stdntNewGrad, IC, IC, secICgrad, secICgrad),
                "Newbie Graduation Student foi matriculado numa Graduation Discipline que ele não possui créditos suficientes");
        assertNotNull(discipGrad2.enroll(stdntOldGrad, IC, IC, secICgrad, secICgrad),
                "Veteran Graduation Student foi matriculado numa Graduation Discipline que ele não atende aos pré-requisitos (Required Discipline)");
        assertNotNull(discipGrad1.enroll(stdntOldGrad, IC, IC, secICgrad, secICgrad),
                "Veteran Graduation Student foi matriculado numa Graduation Discipline em que ele já está matriculado");
        assertTrue(stdntOldGrad.completeDiscipline(discipGrad1),
                "Veteran Graduation Student falhou ao concluir uma Graduation Discipline sem requisitos");
        assertNotNull(discipGrad1.enroll(stdntOldGrad, IC, IC, secICgrad, secICgrad),
                "Veteran Graduation Student foi matriculado numa Graduation Discipline que ele já concluiu");
        assertNull(discipGrad2.enroll(stdntOldGrad, IC, IC, secICgrad, secICgrad),
                "Veteran Graduation Student não foi matriculado numa Graduation Discipline que ele atende todos os requisitos");

        assertEquals(1, dbTesting.inTransaction(() -> disciplineDAO.getAllByStudent(stdntNewGrad)).size(),
                "Quantidade de Disciplines em que o Newbie Graduation Student se encontra matriculado está diferente do esperado");
        assertEquals(2, dbTesting.inTransaction(() -> disciplineDAO.getAllByStudent(stdntOldGrad)).size(),
                "Quantidade de Disciplines em que o Veteran Graduation Student se encontra matriculado está diferente do esperado");
        assertEquals(1, dbTesting.inTransaction(() -> disciplineDAO.getAllByStudent(stdntNewPostGrad)).size(),
                "Quantidade de Disciplines em que o Newbie Post Graduation Student se encontra matriculado está diferente do esperado");
        assertEquals(0, dbTesting.inTransaction(() -> disciplineDAO.getAllByStudent(stdntOldPostGrad)).size(),
                "Quantidade de Disciplines em que o Veteran Post Graduation Student se encontra matriculado está diferente do esperado");

        savedStdntNewGrad = dbTesting.inTransaction(() -> studentDAO.persist(stdntNewGrad));
        assertEquals(0, (int) savedStdntNewGrad.getCredits(),
                "Newbie Graduation Student Credits diferente do esperado");
        assertEquals(0, savedStdntNewGrad.getCompletedDisciplines().size(),
                "Quantidade de Disciplines concluídas pelo Newbie Graduation Student está diferente do esperado");
        assertTrue(stdntNewGrad.completeDiscipline(discipGrad1),
                "Newbie Graduation Student falhou ao concluir uma Discipline sem requisitos");
        assertFalse(stdntNewGrad.completeDiscipline(discipGrad2),
                "Newbie Graduation Student concluiu uma Graduation Discipline que não estava matriculado");
        assertFalse(stdntNewGrad.completeDiscipline(discipGrad3),
                "Newbie Graduation Student concluiu uma Graduation Discipline de outro Department");
        assertFalse(stdntNewGrad.completeDiscipline(discipPostGrad1),
                "Newbie Graduation Student concluiu uma Post Graduation Discipline");
        assertEquals(discipGrad1.getCredits(), stdntNewGrad.getCredits(),
                "Credits obtido pelo Newbiew Graduation Student está diferente do esperado");
        assertEquals(1, stdntNewGrad.getCompletedDisciplines().size(),
                "Quantidade de Disciplines concluídas pelo Newbiew Graduation Student está em diferente do esperado");

        savedStdntOldGrad = dbTesting.inTransaction(() -> studentDAO.persist(stdntOldGrad));
        assertEquals(discipGrad1.getCredits() + 240, (int) savedStdntOldGrad.getCredits(),
                "Veteran Graduation Student Credits diferente do esperado");
        assertEquals(1, savedStdntNewGrad.getCompletedDisciplines().size(),
                "Quantidade de Disciplines concluídas pelo Veteran Graduation Student está diferente do esperado");
        assertFalse(stdntOldGrad.completeDiscipline(discipGrad1),
                "Veteran Graduation Student concluiu uma Discipline que ele já havia completado antes");
        assertTrue(stdntOldGrad.completeDiscipline(discipGrad2),
                "Veteran Graduation Student falhou ao concluir uma Graduation Discipline, com requisitos, que ele está matriculado");
        assertFalse(stdntOldGrad.completeDiscipline(discipGrad3),
                "Veteran Graduation Student concluiu uma Graduation Discipline de outro Department");
        assertTrue(stdntOldGrad.completeDiscipline(discipPostGrad1),
                "Veteran Graduation Student falhou ao concluir uma Post Graduation Discipline que ele está matriculado");
        assertEquals(discipGrad1.getCredits() + discipGrad2.getCredits() + discipPostGrad1.getCredits() + 240, (int) stdntOldGrad.getCredits(),
                "Credits obtido pelo Veteran Graduation Student está diferente do esperado");
        assertEquals(3, stdntOldGrad.getCompletedDisciplines().size(),
                "Quantidade de Disciplines concluídas pelo Veteran Graduation Student está em diferente do esperado");

        savedStdntNewPostGrad = dbTesting.inTransaction(() -> studentDAO.persist(stdntNewPostGrad));
        assertEquals(0, (int) savedStdntNewPostGrad.getCredits(),
                "Newbie Post Graduation Student Credits diferente do esperado");
        assertEquals(0, savedStdntNewPostGrad.getCompletedDisciplines().size(),
                "Quantidade de Disciplines concluídas pelo Newbie Post Graduation Student está diferente do esperado");
        assertFalse(stdntNewPostGrad.completeDiscipline(discipGrad1),
                "Newbie Post Graduation Student concluiu uma Graduation Discipline sem requisitos");
        assertFalse(stdntNewPostGrad.completeDiscipline(discipGrad2),
                "Newbie Post Graduation Student concluiu uma Graduation Discipline com requisitos");
        assertFalse(stdntNewPostGrad.completeDiscipline(discipGrad3),
                "Newbie Post Graduation Student concluiu uma Graduation Discipline de outro Department");
        assertTrue(stdntNewPostGrad.completeDiscipline(discipPostGrad1),
                "Newbie Post Graduation Student falhou ao concluir uma Post Graduation Discipline em que está matriculado");
        assertEquals(discipPostGrad1.getCredits(), stdntNewPostGrad.getCredits(),
                "Credits obtido pelo Newbiew Post Graduation Student está diferente do esperado");
        assertEquals(1, stdntNewPostGrad.getCompletedDisciplines().size(),
                "Quantidade de Disciplines concluídas pelo Newbiew Post Graduation Student está em diferente do esperado");

        savedStdntOldPostGrad = dbTesting.inTransaction(() -> studentDAO.persist(stdntOldPostGrad));
        assertEquals(300, (int) savedStdntOldPostGrad.getCredits(),
                "Veteran Post Graduation Student Credits diferente do esperado");
        assertEquals(0, savedStdntOldPostGrad.getCompletedDisciplines().size(),
                "Quantidade de Disciplines concluídas pelo Veteran Post Graduation Student está diferente do esperado");
        assertFalse(stdntOldPostGrad.completeDiscipline(discipGrad1),
                "Veteran Post Graduation Student concluiu uma Graduation Discipline sem requisitos");
        assertFalse(stdntOldPostGrad.completeDiscipline(discipGrad2),
                "VEteran Post Graduation Student concluiu uma Graduation Discipline com requisitos");
        assertFalse(stdntOldPostGrad.completeDiscipline(discipGrad3),
                "VEteran Post Graduation Student concluiu uma Graduation Discipline de outro Department");
        assertFalse(stdntOldPostGrad.completeDiscipline(discipPostGrad1),
                "Veteran Post Graduation Student concluiu uma Post Graduation Discipline em que não está matriculado");
        assertEquals(300, (int) stdntOldPostGrad.getCredits(),
                "Credits obtido pelo Veteran Post Graduation Student está diferente do esperado");
        assertEquals(0, stdntOldPostGrad.getCompletedDisciplines().size(),
                "Quantidade de Disciplines concluídas pelo Veteran Post Graduation Student está em diferente do esperado");

        assertTrue(compEngineeringGrad.deleteDiscipline(discipGrad1),
                "Falhou ao remover uma Graduation Discipline que pertence ao Course");
        assertFalse(compEngineeringGrad.deleteDiscipline(discipPostGrad1),
                "Removeu uma Post Graduation Discipline de um Graduation Course");
        assertFalse(lawGrad.deleteDiscipline(discipGrad2),
                "Removeu uma Discipline que não pertence ao Department do Course");
    }
}

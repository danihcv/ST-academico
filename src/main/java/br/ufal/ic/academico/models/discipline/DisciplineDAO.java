package br.ufal.ic.academico.models.discipline;

import br.ufal.ic.academico.models.GeneralDAO;
import br.ufal.ic.academico.models.course.Course;
import br.ufal.ic.academico.models.department.Department;
import br.ufal.ic.academico.models.person.student.Student;
import br.ufal.ic.academico.models.person.teacher.Teacher;
import br.ufal.ic.academico.models.secretary.Secretary;
import br.ufal.ic.academico.models.secretary.SecretaryDAO;
import org.hibernate.SessionFactory;

import java.util.ArrayList;
import java.util.List;

public class DisciplineDAO extends GeneralDAO<Discipline> {
    public DisciplineDAO(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    @Override
    public ArrayList<Discipline> getAll() {
        return (ArrayList<Discipline>) currentSession().createQuery("from Discipline").list();
    }

    public Course getCourse(Discipline discipline) {
        ArrayList<Course> courses = (ArrayList<Course>) currentSession().createQuery("from Course").list();
        for (Course c : courses) {
            assert c.getDisciplines() != null;
            for (Discipline d : c.getDisciplines()) {
                if (d.getId().equals(discipline.getId())) {
                    return c;
                }
            }
        }
        return null;
    }

    public List<Discipline> getAllByStudent(Student s) {
        List<Discipline> disciplines = new ArrayList<>();
        List<Discipline> allDisciplines = this.getAll();
        for (Discipline d : allDisciplines) {
            if (d.students.contains(s)) {
                disciplines.add(d);
            }
        }
        return disciplines;
    }

    public Secretary getSecretary(Discipline discipline) {
        Course course = this.getCourse(discipline);

        SecretaryDAO secretaryDAO = new SecretaryDAO(currentSession().getSessionFactory());
        Secretary secretary = null;
        List<Secretary> secretaries = secretaryDAO.getAll();
        for (Secretary s : secretaries) {
            if (s.getCourses().contains(course)) {
                secretary = s;
                break;
            }
        }
        return secretary;
    }

    public Department getDepartment(Discipline discipline) {
        Secretary secretary = this.getSecretary(discipline);

        SecretaryDAO secretaryDAO = new SecretaryDAO(currentSession().getSessionFactory());
        return secretaryDAO.getDepartment(secretary);
    }

    public void deallocateTeacherFromAllDisciplines(Teacher t) {
        List<Discipline> allDisciplines = this.getAll();
        for (Discipline d : allDisciplines) {
            if (d.teacher != null && d.teacher.getId().equals(t.getId())) {
                d.teacher = null;
                this.persist(d);
            }
        }
    }

    public void disenrollStudentFromAll(Student student) {
        List<Discipline> allDisciplines = this.getAll();
        for (Discipline d : allDisciplines) {
            if (d.students.contains(student)) {
                d.removeStudent(student);
                this.persist(d);
            }
        }
    }

    public void disenrollStudents(Discipline discipline) {
        discipline.getStudents().clear();
        this.persist(discipline);
    }
}

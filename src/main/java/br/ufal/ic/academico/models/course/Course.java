package br.ufal.ic.academico.models.course;

import br.ufal.ic.academico.models.discipline.Discipline;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.annotation.Nullable;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@RequiredArgsConstructor
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Setter
    String name;

    @ManyToMany(cascade = CascadeType.ALL)
    @Nullable
    List<Discipline> disciplines;

    public Course(CourseDTO entity) {
        this(entity.name);
    }

    public Course(String name) {
        this.name = name;
        this.disciplines = new ArrayList<>();
    }

    public void update(CourseDTO entity) {
        if (entity.name != null) {
            this.name = entity.name;
        }
    }

    public void addDiscipline(Discipline discipline) {
        this.disciplines.add(discipline);
    }

    public boolean deleteDiscipline(Discipline d) {
        return this.disciplines.remove(d);
    }
}

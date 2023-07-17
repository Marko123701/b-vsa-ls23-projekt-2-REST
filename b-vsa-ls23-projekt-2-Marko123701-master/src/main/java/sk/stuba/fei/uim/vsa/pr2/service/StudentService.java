package sk.stuba.fei.uim.vsa.pr2.service;

import sk.stuba.fei.uim.vsa.pr2.domain.FinalThesis;
import sk.stuba.fei.uim.vsa.pr2.domain.Status;
import sk.stuba.fei.uim.vsa.pr2.domain.Student;

import javax.persistence.*;
import java.util.Collections;
import java.util.List;

public class StudentService {

    EntityManagerFactory emf = Persistence.createEntityManagerFactory("vsa-project-2");

    public Student createStudent(Long aisId, String name, String email, String password, int year, int term, String programme) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            TypedQuery<Long> countQuery1 = em.createQuery("SELECT COUNT(s) FROM Student s WHERE s.email = :email", Long.class);
            countQuery1.setParameter("email", email);
            Long count1 = countQuery1.getSingleResult();
            if (count1 > 0) {
                return null;
                //throw new IllegalArgumentException("email already exists");
            }
            TypedQuery<Long>countQuery2 = em.createQuery("SELECT COUNT(s) FROM Student s WHERE s.aisId = :aisId", Long.class);
            countQuery2.setParameter("aisId", aisId);
            Long count2 = countQuery2.getSingleResult();
            if (count2 > 0) {
                return null;
                //throw new IllegalArgumentException("aisId already exists");
            }

            Student student = new Student();
            student.setAisId(aisId);
            student.setName(name);
            student.setEmail(email);
            student.setPassword(password);
            student.setYear(year);
            student.setTerm(term);
            student.setProgramme(programme);
            em.persist(student);
            tx.commit();
            return student;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            e.printStackTrace();
            return null;
        } finally {
            em.close();
        }
    }

    public Student getStudent(Long id) {

        if (id == null) {
            throw new IllegalArgumentException("id parameter cannot be null");
        }
        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            return em.find(Student.class, id);
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public Student updateStudent(Student student) {
        if (student == null || student.getId() == null) {
            throw new IllegalArgumentException("Invalid student entity or entity identifier.");
        }
        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();
            Student updatedStudent = em.merge(student);
            em.getTransaction().commit();
            return updatedStudent;
        } catch (Exception e) {
            em.getTransaction().rollback();
            return null;
        } finally {
            em.close();
        }
    }

    public List<Student> getStudents() {
        EntityManager em = emf.createEntityManager();
        List<Student> students;

        TypedQuery<Student> query = em.createNamedQuery(Student.FIND_ALL, Student.class);
        students = query.getResultList();
        em.close();

        return students != null ? students : Collections.emptyList();
    }


    public Student deleteStudent(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Identifier cannot be null.");
        }
        EntityManager em = emf.createEntityManager();
        Student student = em.find(Student.class, id);

        if (student == null) {
            return null;
        }

        FinalThesis finalThesis = this.getThesisByStudent(id);
        if (finalThesis != null) {
            finalThesis.setStatus(Status.FREE_TO_TAKE);
            finalThesis.setStudent(null);
            em.merge(finalThesis);
        }
        try {
            em.getTransaction().begin();
            em.remove(student);
            em.getTransaction().commit();
            return student;
        } catch (Exception e) {
            em.getTransaction().rollback();
            return null;
        } finally {
            em.close();
        }
    }

    public FinalThesis getThesisByStudent(Long studentId) {
        if (studentId == null) {
            return null;
        }
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<FinalThesis> query = em.createQuery(
                    "SELECT ft FROM FinalThesis ft WHERE ft.student.id = :studentId", FinalThesis.class);
            query.setParameter("studentId", studentId);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    public Student getStudentByEmail(String email) {
        if (email == null) {
            throw new IllegalArgumentException("Email parameter cannot be null");
        }
        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            TypedQuery<Student> query = em.createQuery("SELECT s FROM Student s WHERE s.email = :email", Student.class);
            query.setParameter("email", email);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }
}

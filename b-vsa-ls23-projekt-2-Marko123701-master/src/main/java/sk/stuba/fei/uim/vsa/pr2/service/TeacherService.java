package sk.stuba.fei.uim.vsa.pr2.service;

import sk.stuba.fei.uim.vsa.pr2.domain.Educator;
import sk.stuba.fei.uim.vsa.pr2.domain.FinalThesis;

import javax.persistence.*;
import java.util.Collections;
import java.util.List;

public class TeacherService {

    EntityManagerFactory emf = Persistence.createEntityManagerFactory("vsa-project-2");

    public Educator createTeacher(Long aisId, String name, String email,String password, String institute, String department) {

        EntityManager em = emf.createEntityManager();
        TypedQuery<Educator> query = em.createQuery("SELECT e FROM Educator e WHERE e.email = :email", Educator.class);
        query.setParameter("email", email);
        List<Educator> existingEducators = query.getResultList();
        if (!existingEducators.isEmpty()) {
            return null;
        }
        query = em.createQuery("SELECT e FROM Educator e WHERE e.aisId = :aisId", Educator.class);
        query.setParameter("aisId", aisId);
        existingEducators = query.getResultList();
        if (!existingEducators.isEmpty()) {
            return null;
        }

        Educator educator = new Educator();
        educator.setAisId(aisId);
        educator.setName(name);
        educator.setEmail(email);
        educator.setPassword(password);
        educator.setInstitute(institute);
        educator.setDepartment(department);

        try {
            em.getTransaction().begin();
            em.persist(educator);
            em.getTransaction().commit();
            return educator;
        } catch (Exception e) {
            em.getTransaction().rollback();
            return null;
        }
    }

    public Educator getTeacher(Long id) {

        if (id == null) {
            throw new IllegalArgumentException("Identifier cannot be null.");
        }
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(Educator.class, id);
        } finally {
            em.close();
        }
    }

    public Educator updateTeacher(Educator teacher) {
        if (teacher == null) {
            throw new IllegalArgumentException("Teacher instance cannot be null.");
        }
        if (teacher.getId() == null) {
            throw new IllegalArgumentException("Teacher identifier cannot be null.");
        }
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("vsa-project-1");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Educator updatedTeacher = em.merge(teacher);
            tx.commit();
            return updatedTeacher;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            return null;
        } finally {
            em.close();
        }
    }

    public List<Educator> getTeachers() {
        EntityManager em = emf.createEntityManager();
        List<Educator> educators;

        try {
            TypedQuery<Educator> query = em.createNamedQuery(Educator.FIND_ALL, Educator.class);
            educators = query.getResultList();
        } finally {
            em.close();
        }

        return educators != null ? educators : Collections.emptyList();
    }

    public Educator deleteTeacher(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Identifier cannot be null");
        }

        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();
            Educator educator = em.find(Educator.class, id);
            if (educator != null) {
                for (FinalThesis finalThesis : this.getThesesByTeacher(id)) {
                    this.deleteThesis(finalThesis.getId());
                }
                em.remove(educator);
            }
            em.getTransaction().commit();
            return educator;
        } catch (Exception e) {
            em.getTransaction().rollback();
            e.printStackTrace();
            return null;
        } finally {
            em.close();
        }
    }

    public FinalThesis deleteThesis(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Final work identifier cannot be null");
        }

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("vsa-project-1");
        EntityManager em = emf.createEntityManager();
        FinalThesis thesis = this.getThesis(id);

        if (thesis == null) {
            return null;
        }

        em.getTransaction().begin();
        FinalThesis mergedThesis = em.merge(thesis);
        em.remove(mergedThesis);
        em.getTransaction().commit();

        return thesis;
    }

    public FinalThesis getThesis(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Identifier cannot be null.");
        }

        EntityManager em = emf.createEntityManager();

        try {
            return em.find(FinalThesis.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            em.close();
        }
    }

    public List<FinalThesis> getThesesByTeacher(Long teacherId) {
        EntityManager em = emf.createEntityManager();
        TypedQuery<FinalThesis> query = em.createNamedQuery(
                FinalThesis.FIND_BY_TEACHER_ID, FinalThesis.class);
        query.setParameter("id", teacherId);
        List<FinalThesis> finalTheses = query.getResultList();
        if (finalTheses == null) {
            return Collections.emptyList();
        }
        return finalTheses;
    }
}

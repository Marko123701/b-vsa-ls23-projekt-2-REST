package sk.stuba.fei.uim.vsa.pr2.User;

import sk.stuba.fei.uim.vsa.pr2.auth.Role;
import sk.stuba.fei.uim.vsa.pr2.web.StudentResource;

import javax.persistence.*;
import java.util.Optional;

public class UserSevice {
    private static final org.slf4j.Logger log=org.slf4j.LoggerFactory.getLogger(StudentResource.class);

    private static UserSevice instance;

    private final EntityManagerFactory emf;

    public static UserSevice getInstance(){
        if(instance == null){
            instance = new UserSevice();
        }
        return instance;
    }

    public UserSevice(){
        emf = Persistence.createEntityManagerFactory("vsa-project-2");
    }

    public User createUser(String email, String password, Role role) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            User user = new User();
            user.setEmail(email);
            user.setPassword(password);
            user.setRole(role);
            em.persist(user);
            tx.commit();
            return user;
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

    public Optional<User> getUserByEmail(String username){
        EntityManager em = emf.createEntityManager();
        TypedQuery<User> q = em.createQuery("select u from User u where u.email = :email", User.class);
        q.setParameter("email", username);
        Optional<User> uop = q.getResultStream().findFirst();
        em.close();
        return uop;
    }

    public User save(User user){
        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();
            em.persist(user);
            em.getTransaction().commit();
            return user;
        } catch (Exception e) {
            log.info(e.getMessage(), e);
            if(em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            return null;
        }
        finally {
            em.close();
        }
    }

    public void deleteUserByEmail(String email) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            // Find the user by email
            TypedQuery<User> query = em.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class);
            query.setParameter("email", email);
            Optional<User> userOptional = query.getResultStream().findFirst();

            if (userOptional.isPresent()) {
                User user = userOptional.get();
                em.remove(user);
                tx.commit();
                log.info("User deleted: " + user.getEmail());
            } else {
                log.info("User not found for email: " + email);
            }
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            log.error("Error deleting user: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public void close(){
        emf.close();
    }
}

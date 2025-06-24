package milou.DataAccessObject;

import milou.Entity.User;
import org.hibernate.Session;

public class UserDao {

    public void save(Session session, User user) {
        session.persist(user);
    }

    public User findByEmail(Session session, String email) {
        return session.createQuery("FROM User WHERE email = :email", User.class)
                .setParameter("email", email)
                .uniqueResult();
    }

    public User findByEmailAndPassword(Session session, String email, String password) {
        return session.createQuery("FROM User WHERE email = :email AND password = :password", User.class)
                .setParameter("email", email)
                .setParameter("password", password)
                .uniqueResult();
    }

    public void delete(Session session, User user) {
        session.remove(user);
    }

    public void update(Session session, User user) {
        session.merge(user);
    }
}

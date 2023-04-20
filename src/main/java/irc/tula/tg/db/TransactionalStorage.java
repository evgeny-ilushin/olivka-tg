package irc.tula.tg.db;

import lombok.extern.slf4j.Slf4j;

/*
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.SessionFactoryBuilder;
import org.hibernate.boot.model.naming.ImplicitNamingStrategyJpaCompliantImpl;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import javax.persistence.Query;
*/

@Slf4j
public class TransactionalStorage {
/*
    private static final SessionFactory sessionFactory;

    static {
        try {
            StandardServiceRegistry standardRegistry = new StandardServiceRegistryBuilder()
                    .configure()
                    .build();
            Metadata metadata = new MetadataSources(standardRegistry)
                    .addAnnotatedClass(User.class)
                    .getMetadataBuilder()
                    .applyImplicitNamingStrategy(ImplicitNamingStrategyJpaCompliantImpl.INSTANCE)
                    .build();

            SessionFactoryBuilder sessionFactoryBuilder = metadata.getSessionFactoryBuilder();
            sessionFactory = sessionFactoryBuilder.build();
        } catch (Throwable ex) {
            log.error("TransactionalStorage Initialize error:", ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    private TransactionalStorage() {
    }

    private static volatile TransactionalStorage instance;

    public static TransactionalStorage getInstance() {
        TransactionalStorage localInstance = instance;

        if (localInstance == null) {
            synchronized (TransactionalStorage.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new TransactionalStorage();
                }
            }
        }
        return localInstance;
    }

    public static Session getSession() throws HibernateException {
        return sessionFactory.openSession();
    }


    public User getUser(Integer userId) {
        Session session = null;
        try {
            session = getSession();
            User u = new User();
            u.setNick("говно");
            session.save(u);
            //return session.get(User.class, userId);
            return null;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return null;
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    public User findUser(String name) {
        Session session = null;
        try {
            session = getSession();
            val query = session.createQuery("FROM irc.tula.tg.db.entity.User U WHERE U.nick=:u_n", User.class);
            query.setParameter("u_n", name);
            query.setMaxResults(1);
            return query.getSingleResult();
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return null;
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }
*/
}

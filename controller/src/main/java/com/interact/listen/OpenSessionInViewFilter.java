package com.interact.listen;

import java.io.IOException;

import javax.servlet.*;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.StaleObjectStateException;

/**
 * Provides automatic Hibernate transaction handling for {@code Servlet} requests. If a {@code Servlet} throws an
 * {@code Exception}, this {@code Filter} will assume the transaction needs to be rolled back. Otherwise it will assume
 * everything was okay and will commit the transaction. This {@code Filter} will not eat {@code Throwables}; any {@code
 * Throwable} that is caught will be rethrown after any necessary transaction processing is performed.
 */
public class OpenSessionInViewFilter implements Filter
{
    private static final Logger LOG = Logger.getLogger(OpenSessionInViewFilter.class);

    private SessionFactory sessionFactory;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
        throws ServletException, IOException
    {
        try
        {
            sessionFactory.getCurrentSession().beginTransaction();
            filterChain.doFilter(request, response);

            LOG.debug("OpenSessionInView: committing transaction");
            sessionFactory.getCurrentSession().getTransaction().commit();
        }
        catch(StaleObjectStateException e)
        {
            // TODO perhaps implement smarter logic here
            LOG.warn("OpenSessionInView: caught StaleObjectStateException, rethrowing");
            throw e;
        }
        catch(Throwable e) // SUPPRESS CHECKSTYLE IllegalCatchCheck
        {
            try
            {
                if(sessionFactory.getCurrentSession().getTransaction().isActive())
                {
                    LOG.debug("OpenSessionInView: rolling back transaction");
                    sessionFactory.getCurrentSession().getTransaction().rollback();
                }
                else
                {
                    LOG.debug("OpenSessionInView: transaction not active, no rollback will be performed");
                }
            }
            catch(Throwable t) // SUPPRESS CHECKSTYLE IllegalCatchCheck
            {
                LOG.error("Could not rollback transaction", t);
            }

            throw new ServletException(e);
        }
    }

    @Override
    public void init(FilterConfig filterConfig)
    {
        sessionFactory = HibernateUtil.getSessionFactory();
    }

    @Override
    public void destroy()
    {
    // no default implementation
    }
}

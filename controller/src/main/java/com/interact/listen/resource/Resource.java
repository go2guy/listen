package com.interact.listen.resource;

import com.interact.listen.PersistenceService;
import com.interact.listen.history.HistoryService;

import java.util.ArrayList;

import javax.persistence.Transient;

public abstract class Resource
{
    public static final int DEFAULT_PAGE_SIZE = 15;

    @Transient
    private ArrayList<String> errors = new ArrayList<String>();

    public abstract void setId(Long id);

    public abstract Long getId();

    public abstract boolean validate();

    /**
     * Returns a copy of this {@code Resource}.
     * 
     * @param withIdAndVersion whether or not to copy the id and version of the original onto the copy (typically
     * {@code false})
     * @return copy of this {@code Resource}
     */
    public abstract Resource copy(boolean withIdAndVersion);

    public boolean hasErrors()
    {
        return errors.size() > 0;
    }

    public void addToErrors(String errorMessage)
    {
        errors.add(errorMessage);
    }

    public ArrayList<String> errors()
    {
        return errors;
    }

    /**
     * Callback to be executed before this {@code Resource} is saved.
     * 
     * @param persistenceService PersistenceService triggering callback
     * @param historyService HistoryService to use when writing histories
     */
    public void beforeSave(PersistenceService persistenceService, HistoryService historyService)
    {
        // default implementation is no-op
    }

    /**
     * Callback to be executed before this {@code Resource} is updated.
     * 
     * @param persistenceService PersistenceService triggering callback
     * @param historyService HistoryService to use when writing histories
     * @param original {@code Resource} before properties were updated
     */
    public void beforeUpdate(PersistenceService persistenceService, HistoryService historyService, Resource original)
    {
        // default implementation is no-op
    }

    /**
     * Callback to be executed before this {@code Resource} is deleted.
     * 
     * @param persistenceService PersistenceService triggering callback
     * @param historyService HistoryService to use when writing histories
     */
    public void beforeDelete(PersistenceService persistenceService, HistoryService historyService)
    {
        // default implementation is no-op
    }

    /**
     * Callback to be executed after this {@code Resource} is saved.
     * 
     * @param persistenceService PersistenceService triggering callback
     * @param historyService HistoryService to use when writing histories
     */
    public void afterSave(PersistenceService persistenceService, HistoryService historyService)
    {
        // default implementation is no-op
    }

    /**
     * Callback to be executed after this {@code Resource} is updated.
     * 
     * @param persistenceService PersistenceService triggering callback
     * @param historyService HistoryService to use when writing histories
     * @param original {@code Resource} before properties were updated
     */
    public void afterUpdate(PersistenceService persistenceService, HistoryService historyService, Resource original)
    {
        // default implementation is no-op
    }

    /**
     * Callback to be executed after this {@code Resource} is deleted.
     * 
     * @param persistenceService PersistenceService triggering callback
     * @param historyService HistoryService to use when writing histories
     */
    public void afterDelete(PersistenceService persistenceService, HistoryService historyService)
    {
        // default implementation is no-op
    }
}

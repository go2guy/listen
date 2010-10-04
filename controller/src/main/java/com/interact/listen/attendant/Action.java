package com.interact.listen.attendant;

import com.interact.listen.command.IvrCommand;

import java.util.List;

import javax.persistence.*;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.json.simple.JSONObject;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "ATTENDANT_ACTION", uniqueConstraints = { @UniqueConstraint(columnNames = { "ATTENDANT_MENU_ID", "KEY_PRESSED" }) })
public abstract class Action implements IvrCommand
{
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private Long id;

    @Column(name = "VERSION")
    @Version
    private Integer version = Integer.valueOf(0);

    @JoinColumn(name = "ATTENDANT_MENU_ID", nullable = true)
    @ManyToOne
    private Menu menu;

    @Column(name = "KEY_PRESSED", nullable = true)
    private String keyPressed;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Integer getVersion()
    {
        return version;
    }

    public void setVersion(Integer version)
    {
        this.version = version;
    }

    public Menu getMenu()
    {
        return menu;
    }

    public void setMenu(Menu menu)
    {
        this.menu = menu;
    }

    public String getKeyPressed()
    {
        return keyPressed;
    }

    public void setKeyPressed(String keyPressed)
    {
        this.keyPressed = keyPressed;
    }

    public abstract JSONObject toJson();

    protected JSONObject createJsonObject(String action, JSONObject arguments)
    {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("keyPressed", keyPressed);
        json.put("action", action);
        json.put("arguments", arguments);
        return json;
    }

    public static List<Action> queryByMenu(Session session, Menu menu)
    {
        Criteria criteria = session.createCriteria(Action.class);
        criteria.createAlias("menu", "menu_alias");
        criteria.add(Restrictions.eq("menu_alias.id", menu.getId()));

        criteria.setFetchMode("menu", FetchMode.SELECT);

        return (List<Action>)criteria.list();
    }
    
    public static List<Action> queryByMenuWithoutDefaultAndTimeout(Session session, Menu menu)
    {
        Criteria criteria = session.createCriteria(Action.class);
        criteria.add(Restrictions.ne("id", menu.getDefaultAction().getId()));
        criteria.add(Restrictions.ne("id", menu.getTimeoutAction().getId()));
        criteria.add(Restrictions.eq("menu.id", menu.getId()));

        criteria.setFetchMode("menu", FetchMode.SELECT);

        return (List<Action>)criteria.list();
    }
}

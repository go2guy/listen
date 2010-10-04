package com.interact.listen.attendant;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.Session;
import org.json.simple.JSONObject;

@Entity
public class GoToMenuAction extends Action
{
    @JoinColumn(name = "GO_TO_ATTENDANT_MENU_ID")
    @ManyToOne
    private Menu goToMenu;

    public Menu getGoToMenu()
    {
        return goToMenu;
    }

    public void setGoToMenu(Menu goToMenu)
    {
        this.goToMenu = goToMenu;
    }

    @Override
    public JSONObject toJson()
    {
        JSONObject args = new JSONObject();
        args.put("menuId", goToMenu == null ? null : goToMenu.getId());
        return createJsonObject("GoToMenu", args);
    }
    
    @Override
    public String toIvrCommandJson(Session session)
    {
        //not used
        return "";
    }
}

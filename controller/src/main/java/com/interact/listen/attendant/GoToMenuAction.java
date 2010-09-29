package com.interact.listen.attendant;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

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
        if(goToMenu == null)
        {
            args.put("goToMenu", null);
        }
        else
        {
            JSONObject destination = new JSONObject();
            destination.put("id", goToMenu.getId());
            args.put("goToMenu", destination);
        }
        return createJsonObject("GoToMenu", args);
    }
}

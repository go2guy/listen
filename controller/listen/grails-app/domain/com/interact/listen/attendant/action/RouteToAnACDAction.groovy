package com.interact.listen.attendant.action

import com.interact.listen.attendant.Menu
import com.interact.listen.acd.Skill

class RouteToAnACDAction extends Action {
    Skill skill
    
    static belongsTo = [ skill: Skill ]
    
    static constraints = {
        skill nullable: true
    }
}

package com.interact.listen.acd

import com.interact.listen.User
import com.interact.listen.acd.Skill

class UserSkill {
    User user
    Skill skill
    
    static belongsTo = [ user: User, skill: Skill ]
    
    static constraints = {
        user nullable: false
        skill nullable: false
        user(unique: ['skill'])
    }
    
}

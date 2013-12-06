package com.interact.listen.acd

import com.interact.listen.Organization
import com.interact.listen.User

class Skill {
    String skillname
    Organization organization
    String description
    Integer userCount = 0
    
    static transients = ['userCount']
    
    static hasMany = [ userSkill : UserSkill ]
     
    static constraints = {
        skillname blank: false
        organization nullable: false
        skillname(unique: ['organization'])
    }
    
    String toString() {
        skillname
    }
}

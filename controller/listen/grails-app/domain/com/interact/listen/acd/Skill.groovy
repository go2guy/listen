package com.interact.listen.acd

import com.interact.listen.Organization
import com.interact.listen.User

class Skill {
    String skillname
    Organization organization
    String description
    Integer userCount = 0
    Integer menuCount = 0
    
    static transients = ['userCount', 'menuCount']
    
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

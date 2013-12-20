package com.interact.listen.acd

import com.interact.listen.User
import com.interact.listen.PhoneNumber
import org.joda.time.DateTime

class AcdUserStatus
{
    User owner
    AcdQueueStatus AcdQueueStatus
    DateTime statusModified
    boolean onACall
    PhoneNumber contactNumber

    static constraints =
        {
            statusModified nullable: true
            contactNumber nullable: true
        }

    /**
     * Executed before an update.
     */
    def beforeUpdate()
    {
        //Update modified time if changing whether on a call or not
        if (this.isDirty("onACall"))
        {
            this.statusModified = DateTime.now();
        }
    }
  
  def toggleStatus() {
      if (this.acdQueueStatus == AcdQueueStatus.Unavailable)
          this.acdQueueStatus = AcdQueueStatus.Available
      else
          this.acdQueueStatus = AcdQueueStatus.Unavailable
  }
}

enum AcdQueueStatus
{
    Available("Available"),
    Unavailable("Unavailable")

    final String value

    AcdQueueStatus(String value)
    {
        this.value = value
    }

    static AcdQueueStatus fromString(String value)
    {
        return this.valueOf(value.toUpperCase())
    }

    String toString()
    {
        value
    }

    String getKey()
    {
        name()
    }

}

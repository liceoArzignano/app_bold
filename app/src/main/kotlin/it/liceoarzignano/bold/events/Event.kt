package it.liceoarzignano.bold.events

import it.liceoarzignano.bold.database.DBItem

class Event : DBItem {
    var title = ""
    var date = 0L
    var description = ""
    var category = 0

    /*
     * Category values
     * 0 = test
     * 1 = school
     * 2 = bday
     * 3 = homework
     * 4 = reminder
     * 5 = hangout
     * 6 = other
     */

    constructor()

    constructor(id: Long, title: String, date: Long, description: String, category: Int) {
        this.id = id
        this.title = title
        this.date = date
        this.description = description
        this.category = category
    }
}

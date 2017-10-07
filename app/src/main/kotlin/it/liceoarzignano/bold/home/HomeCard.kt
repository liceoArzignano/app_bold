package it.liceoarzignano.bold.home

class HomeCard(val type: CardType) {

    var title = ""
    var content = ""
    var action = ""
    var listener: Listener? = null

    fun onCardClick() = listener?.onCardClick()
    fun onActionClick() = listener?.onActionClick()

    interface Listener {
        fun onCardClick()
        fun onActionClick()
    }

    enum class CardType {
        EVENTS,
        NEWS,
        MARKS,
        SUGGESTIONS
    }
}

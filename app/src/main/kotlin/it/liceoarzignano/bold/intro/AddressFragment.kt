package it.liceoarzignano.bold.intro

import android.annotation.TargetApi
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.AppCompatButton
import android.support.v7.widget.CardView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import it.liceoarzignano.bold.MainActivity
import it.liceoarzignano.bold.R
import it.liceoarzignano.bold.settings.AppPrefs
import it.liceoarzignano.bold.utils.SystemUtils
import it.liceoarzignano.bold.utils.UiUtils

class AddressFragment : Fragment() {
    lateinit private var mCard: CardView
    lateinit private var mIcon: ImageView
    lateinit private var mTitle: TextView
    lateinit private var mButton: AppCompatButton
    var mPosition: Int = 0

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.item_benefits_address, container, false)
        if (view == null) {
            return view
        }

        mCard = view.findViewById(R.id.row_benefits_address_card)
        mIcon = view.findViewById(R.id.row_benefits_address_icon)
        mTitle = view.findViewById(R.id.row_benefits_address_title)
        mButton = view.findViewById(R.id.row_benefits_address_button)

        mIcon.setImageResource(when (mPosition) {
            0 -> R.drawable.ic_address_1
            1 -> R.drawable.ic_address_2
            2 -> R.drawable.ic_address_3
            3 -> R.drawable.ic_address_4
            4 -> R.drawable.ic_address_5
            else -> R.drawable.ic_address_6
        })

        mTitle.setText(when (mPosition) {
            0 -> R.string.pref_address_1
            1 -> R.string.pref_address_2
            2 -> R.string.pref_address_3
            3 -> R.string.pref_address_4
            4 -> R.string.pref_address_5
            else -> R.string.pref_address_teacher
        })

        mButton.setOnClickListener({ _ -> setAddress() })

        // Elevate the first card to match on-swipe behaivour
        if (SystemUtils.isNotLegacy && mPosition == 0) {
            mCard.translationZ = UiUtils.dpToPx(resources, 8F)
        }
        return view
    }

    private fun setAddress() {
        val prefs = AppPrefs(context)
        val isTeacher = mPosition == 5
        prefs.set(AppPrefs.KEY_IS_TEACHER, isTeacher)
        prefs.set(AppPrefs.KEY_ADDRESS, if (isTeacher) "0" else "${mPosition + 1}")
        prefs.set(AppPrefs.KEY_INTRO_SCREEN, true)
        prefs.set(AppPrefs.KEY_INTRO_VERSION, "0")
        context.startActivity(Intent(activity, MainActivity::class.java))
        activity.finish()
    }

    @TargetApi(21)
    fun setSelected(isSelected: Boolean) =
            mCard.animate()
                    .translationZ(UiUtils.dpToPx(resources, if (isSelected) 8F else 1F))
                    .start()
}